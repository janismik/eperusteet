/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
*
* This program is free software: Licensed under the EUPL, Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* European Union Public Licence for more details.
*/

'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('tabHelper', function () {
    // Helper to deal with ui-router weirdness
    this.changeInited = false;
  })

  .service('OppimaaraHelper', function (PerusopetusService) {
    var instance = null;
    var params = {};
    function OppimaaraHelperImpl(oppiaine) {
      this.oppiaine = oppiaine;
    }

    this.init = function (oppiaine, stateParams) {
      instance = new OppimaaraHelperImpl(oppiaine);
      params = _.clone(stateParams);
      return instance;
    };

    this.reset = function () {
      console.log("reset");
      instance = null;
      params = {};
    };

    this.instance = function() {
      return instance;
    };

    this.getBackState = function () {
      return instance ? ['root.perusteprojekti.osaalue', params] :
      ['root.perusteprojekti.osalistaus', {osanTyyppi: PerusopetusService.OPPIAINEET}];
    };

    this.save = function (model) {
      if (!instance) {
        return false;
      }
      model.$isNew = false;
      var oppimaara = _.cloneDeep(model);
      if (!_.isArray(instance.oppiaine.oppimaarat)) {
        instance.oppiaine.oppimaarat = [];
      }
      instance.oppiaine.oppimaarat.push(oppimaara);
      return instance.oppiaine;
    };
  })

  .directive('muokkausOppiaine', function() {
    return {
      templateUrl: 'views/directives/perusopetus/oppiaine.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'OppiaineController',
      link: function (scope, element) {
        scope.$watch('editEnabled', function (value) {
          if (!value) {
            element.find('.info-placeholder').hide();
          }
        });
      }
    };
  })

  .controller('OppiaineController', function ($scope, PerusopetusService, Kaanna,
      PerusteProjektiSivunavi, Oppiaineet, $timeout, $state, $stateParams, $q, YleinenData, tabHelper,
      CloneHelper, OppimaaraHelper) {
    $scope.editableModel = {};
    $scope.editEnabled = false;
    $scope.mappedVuosiluokat = [];
    $scope.yleisetosat = ['tehtava', 'osaalue'];
    $scope.activeTab =  parseInt($stateParams.tabId, 10);
    $scope.isOppimaara = !!OppimaaraHelper.instance();
    $scope.oppimaaraRequested = false;
    if (_.isNumber(tabHelper.changeInited)) {
      $scope.activeTab = tabHelper.changeInited;
      tabHelper.changeInited = false;
    }

    var cloner = CloneHelper.init(['koosteinen', 'nimi', 'tehtava', 'vuosiluokkakokonaisuudet']);

    var callbacks = {
      edit: function () {
        cloner.clone($scope.editableModel);
      },
      save: function () {
        var koosteinenOppiaine = OppimaaraHelper.save($scope.editableModel);
        if (koosteinenOppiaine) {
          $scope.editableModel = koosteinenOppiaine;
        }
        if (!$scope.editableModel.id) {
          Oppiaineet.save({
            perusteId: PerusopetusService.getPerusteId()
          }, $scope.editableModel, function (res) {
            $scope.editableModel = res;
            $state.transitionTo($state.current, _.extend(_.clone($stateParams), {osanId: res.id}), {
              reload: true, inherit: false, notify: false
            });
          });
        } else {
          $scope.editableModel.$save({
            perusteId: PerusopetusService.getPerusteId()
          }, function (res) {
            $scope.editableModel = res;
          });
        }
      },
      cancel: function () {
        cloner.restore($scope.editableModel);
        if ($scope.editableModel.$isNew) {
          $scope.editableModel.$isNew = false;
          $timeout(function () {
            $state.go.apply($state, $scope.data.options.backState);
          });
        }
      },
      notify: function (value) {
        $scope.editEnabled = value;
        PerusteProjektiSivunavi.setVisible(!value);
      },
      validate: function () { return true; }
    };

    $scope.$on('$destroy', function () {
      if (!$scope.oppimaaraRequested) {
        OppimaaraHelper.reset();
      }
    });

    $scope.data = {
      options: {
        title: function () { return $scope.editableModel.nimi; },
        editTitle: $scope.isOppimaara ? 'muokkaa-oppimaaraa' : 'muokkaa-oppiainetta',
        newTitle: $scope.isOppimaara ? 'uusi-oppimaara' : 'uusi-oppiaine',
        backLabel: 'oppiaineet',
        backState: OppimaaraHelper.getBackState(),
        removeWholeLabel: 'poista-oppiaine',
        removeWholeConfirmationText: 'poistetaanko-oppiaine',
        removeWholeFn: function () {
          PerusopetusService.deleteOsa($scope.editableModel);
        },
        actionButtons: [
          {
            label: 'lisaa-oppimaara',
            role: 'add',
            callback: function () {
              OppimaaraHelper.init($scope.editableModel, $stateParams);
              $scope.oppimaaraRequested = true;
              $state.go('root.perusteprojekti.osaalue', {
                osanTyyppi: PerusopetusService.OPPIAINEET,
                osanId: 'uusi',
                tabId: 0
              });
            },
            hide: '!editableModel.koosteinen'
          }
        ],
        addFieldCb: function (field) {
          if (field.path === 'tehtava') {
            $scope.editableModel.tehtava = {
              otsikko: {fi: 'Oppiaineen tehtävä'},
              teksti: {fi: ''}
            };
          } else if (field.path === 'vuosiluokkakokonaisuudet') {
            if (!$scope.editableModel.vuosiluokkakokonaisuudet) {
              $scope.editableModel.vuosiluokkakokonaisuudet = [];
            }
            $scope.editableModel.vuosiluokkakokonaisuudet.push(field.empty());
          }
        },
        fieldRenderer: '<kenttalistaus mode="sortable" edit-enabled="editEnabled" ' +
          'object-promise="modelPromise" fields="config.fields" hide-empty-placeholder="true"></kenttalistaus>',
        fields: [
          {
            path: 'tekstikappaleet[].teksti',
            menuLabel: 'tekstikappale',
            localeKey: 'nimi',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            titleplaceholder: 'muokkaus-teksikappaleen-nimi-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 300
          },
          {
            path: 'tehtava',
            localeKey: 'oppiaine-osio-tehtava',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 100
          },
          {
            path: 'osaalue',
            localeKey: 'oppiaine-osio-osaalue',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 200
          },

        ],
        editingCallbacks: callbacks
      }
    };

    $scope.chooseTab = function (chosenIndex, noStateChange) {
      _.each($scope.mappedVuosiluokat, function (item, index) {
        item.$tabActive = chosenIndex === index;
      });
      $scope.vuosiluokka = $scope.mappedVuosiluokat[chosenIndex];
      if (!noStateChange) {
        tabHelper.changeInited = chosenIndex;
        var params = _.extend(_.clone($stateParams), {tabId: chosenIndex});
        $state.transitionTo($state.$current.name, params, {
          location: true,
          reload: false,
          notify: false
        });
      }
    };

    $scope.getVuosiluokkakokonaisuus = function (oppiaineenVuosiluokkakokonaisuus) {
      return _.find($scope.vuosiluokkakokonaisuudet, function (item) {
        return item.id === parseInt(oppiaineenVuosiluokkakokonaisuus.vuosiluokkaKokonaisuus, 10);
      });
    };

    var modelPromise = $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
    });
    var vuosiluokatPromise = PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT).$promise;

    function getTitle(key) {
      var obj = {};
      _.each(YleinenData.kielet, function (kieli) {
        obj[kieli] = Kaanna.kaanna('oppiaine-osio-' + key);
      });
      return obj;
    }

    function updateChosen() {
      $scope.chosenVuosiluokat = _.map($scope.editableModel.vuosiluokkakokonaisuudet, function (item) {
        return parseInt(item.vuosiluokkaKokonaisuus, 10);
      });
    }

    $q.all([modelPromise, vuosiluokatPromise]).then(function (data) {
      // Add addable items to menu
      $scope.vuosiluokkakokonaisuudet = data[1];
      if (_.size($scope.vuosiluokkakokonaisuudet) > 0) {
        $scope.data.options.fields.push({divider: true, order: 99});
      }
      var menuItems = [];
      _.each($scope.vuosiluokkakokonaisuudet, function (item) {
        menuItems.push({
          path: 'vuosiluokkakokonaisuudet',
          localeKey: item.nimi,
          empty: function () {
            var vlk = {
              vuosiluokkaKokonaisuus: item.id,
              sisaltoAlueet: [],
              tavoitteet: []
            };
            _.each(['tehtava', 'tyotavat', 'ohjaus', 'arviointi'], function (osio) {
              vlk[osio] = {otsikko: getTitle(osio), teksti: {}};
            });
            return vlk;
          },
          order: 10,
          visibleFn: function () {
            updateChosen();
            return _.indexOf($scope.chosenVuosiluokat, item.id) > -1;
          },
          remove: function () {
            var index = _.findIndex($scope.editableModel.vuosiluokkakokonaisuudet, function (vlk) {
              return parseInt(vlk.vuosiluokkaKokonaisuus, 10) === item.id;
            });
            $scope.editableModel.vuosiluokkakokonaisuudet.splice(index, 1);
          }
        });
      });
      _(menuItems).sortBy(function (item) {
        return Kaanna.kaanna(item.localeKey);
      }).each(function (item, index) {
        item.order += index;
      });
      $scope.data.options.fields = menuItems.concat($scope.data.options.fields);

      $scope.$watch('editableModel.vuosiluokkakokonaisuudet', function () {
        $scope.mappedVuosiluokat = _($scope.editableModel.vuosiluokkakokonaisuudet).map(function (item) {
          var thisItem = $scope.getVuosiluokkakokonaisuus(item);
          thisItem.$sisalto = item;
          return thisItem;
        }).sortBy(function (item) {
          return Kaanna.kaanna(item.nimi);
        }).value();
        $scope.chooseTab($scope.activeTab, true);
      }, true);
    });
  });
