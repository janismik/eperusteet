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
  .service('OsanMuokkausHelper', function ($stateParams, PerusopetusService, $state) {
    this.model = null;
    this.backState = null;
    this.vuosiluokka = null;
    this.path = null;
    this.oppiaine = null;
    this.vuosiluokat = [];

    this.getModel = function () {
      return this.path ? this.model[this.path] : this.model;
    };

    this.setup = function (model, path, oppiaine) {
      this.oppiaine = $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET ? model : null;
      if (oppiaine) {
        this.oppiaine = oppiaine;
      }
      this.model = model;
      this.path = path;
      this.backState = [$state.current.name, _.clone($stateParams)];
      this.vuosiluokat = PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT, true);
      if (model.vuosiluokkaKokonaisuus) {
        this.vuosiluokka = _.find(this.vuosiluokat, function (vl) {
          return vl.id === parseInt(model.vuosiluokkaKokonaisuus, 10);
        });
      } else {
        this.vuosiluokka = null;
      }
    };

    this.save = function () {
      if (this.isVuosiluokkakokonaisuudenOsa()) {
        PerusopetusService.saveVuosiluokkakokonaisuudenOsa(this.model, this.oppiaine);
      } else if (this.path) {
        var payload = _.pick(this.model, ['id', this.path]);
        PerusopetusService.saveOsa(payload, this.backState[1]);
      }
    };

    this.goBack = function () {
      if (!this.backState) {
        return;
      }
      var params = _.clone(this.backState);
      this.backState = null;
      $state.go.apply($state, params, {reload: true});
    };

    this.isVuosiluokkakokonaisuudenOsa = function () {
      return !!this.vuosiluokka;
    };

    this.getOppiaine = function () {
      return this.oppiaine;
    };

    this.getVuosiluokkakokonaisuus = function () {
      return this.vuosiluokka;
    };
  })

  .controller('OsanMuokkausController', function($scope, $stateParams, $compile, OsanMuokkausHelper,
      Editointikontrollit, $rootScope) {
    $scope.objekti = OsanMuokkausHelper.getModel();
    if (!$scope.objekti) {
      return;
    }

    var MAPPING = {
      tekstikappale: {
        directive: 'osanmuokkaus-tekstikappale',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            $rootScope.$broadcast('notifyCKEditor');
            OsanMuokkausHelper.save();
            OsanMuokkausHelper.goBack();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      sisaltoalueet: {
        directive: 'osanmuokkaus-sisaltoalueet',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            $rootScope.$broadcast('notifyCKEditor');
            OsanMuokkausHelper.save();
            OsanMuokkausHelper.goBack();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      tavoitteet: {
        directive: 'osanmuokkaus-tavoitteet',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            var idFn = function (item) { return item.id; };
            var filterFn = function (item) { return !item.$hidden; };
            _.each(OsanMuokkausHelper.model.tavoitteet, function (tavoite) {
              tavoite.sisaltoalueet = _(tavoite.$sisaltoalueet).filter(filterFn).map(idFn).value();
              tavoite.laajattavoitteet = _(tavoite.$osaaminen).filter(filterFn).map(idFn).value();
            });
            OsanMuokkausHelper.save();
            OsanMuokkausHelper.goBack();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      }
    };
    var config = MAPPING[$stateParams.osanTyyppi];
    $scope.config = config;
    var muokkausDirective = angular.element('<' + config.directive + '>').attr('config', 'config');
    _.each(config.attrs, function (value, key) {
      muokkausDirective.attr(key, value);
    });
    var el = $compile(muokkausDirective)($scope);

    angular.element('#muokkaus-elementti-placeholder').replaceWith(el);

    Editointikontrollit.registerCallback(config.callbacks);
    Editointikontrollit.startEditing();
    // TODO muokkauksesta poistumisvaroitus
  })

  .directive('osanmuokkausTekstikappale', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaustekstikappale.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '=',
      },
      controller: 'OsanmuokkausTekstikappaleController'
    };
  })

  .controller('OsanmuokkausTekstikappaleController', function ($scope, OsanMuokkausHelper, $rootScope) {
    $scope.getTitle = function () {
      return OsanMuokkausHelper.isVuosiluokkakokonaisuudenOsa() ?
        'muokkaus-vuosiluokkakokonaisuuden-osa' : 'muokkaus-tekstikappale';
    };
    // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
    var received = 0;
    $scope.$on('ckEditorInstanceReady', function() {
      if (++received === 2) {
        $rootScope.$broadcast('editointikontrollitRefresh');
      }
    });
  })

  .directive('osanmuokkausTavoitteet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaustavoitteet.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
      }
    };
  })

  .directive('perusopetusMuokkausInfo', function (OsanMuokkausHelper) {
    return {
      templateUrl: 'views/directives/perusopetus/muokkausinfo.html',
      restrict: 'AE',
      link: function (scope, element, attrs) {
        scope.muokkausinfoOsa = attrs.osa || '';
      },
      controller: function ($scope) {
        $scope.getOppiaine = function () {
          var oppiaine = OsanMuokkausHelper.getOppiaine();
          return oppiaine ? oppiaine.nimi : '';
        };
        $scope.getVuosiluokkakokonaisuus = function () {
          var vlk = OsanMuokkausHelper.getVuosiluokkakokonaisuus();
          return vlk ? vlk.nimi : '';
        };
      }
    };
  });
