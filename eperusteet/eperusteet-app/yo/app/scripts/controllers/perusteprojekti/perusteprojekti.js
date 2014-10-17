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
  .config(function($stateProvider) {
    $stateProvider
      .state('root.perusteprojekti', {
        url: '/perusteprojekti/:perusteProjektiId',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        resolve: {
          'koulutusalaService': 'Koulutusalat',
          'opintoalaService': 'Opintoalat',
          'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'perusteprojektiAlustus': ['perusteprojektiTiedot', '$stateParams', function(perusteprojektiTiedot, $stateParams) {
              return perusteprojektiTiedot.alustaProjektinTiedot($stateParams);
            }],
          'perusteprojektiOikeudet': 'PerusteprojektiOikeudetService',
          'perusteprojektiOikeudetNouto': ['perusteprojektiOikeudet', '$stateParams', function(perusteprojektiOikeudet, $stateParams) {
              perusteprojektiOikeudet.noudaOikeudet($stateParams);
          }]
        },
        abstract: true
      })
      .state('root.perusteprojekti.perusopetus', {
        url: '/perusopetus',
        templateUrl: 'views/partials/perusteprojekti/perusopetus.html',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'projektinTiedotAlustettu': ['perusteprojektiTiedot', function(perusteprojektiTiedot) {
            return perusteprojektiTiedot.projektinTiedotAlustettu();
          }],
          'perusteenSisaltoAlustus': ['perusteprojektiTiedot', 'projektinTiedotAlustettu', '$stateParams',
            function(perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) {
              return perusteprojektiTiedot.alustaPerusteenSisalto($stateParams);
            }]
        },
        controller: 'PerusopetusSisaltoController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.osalistaus', {
        url: '/osat/:osanTyyppi',
        templateUrl: 'views/partials/perusteprojekti/osalistaus.html',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'projektinTiedotAlustettu': ['perusteprojektiTiedot', function(perusteprojektiTiedot) {
            return perusteprojektiTiedot.projektinTiedotAlustettu();
          }],
          'perusteenSisaltoAlustus': ['perusteprojektiTiedot', 'projektinTiedotAlustettu', '$stateParams',
            function(perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) {
              return perusteprojektiTiedot.alustaPerusteenSisalto($stateParams, true);
            }]
        },
        controller: 'OsalistausController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.osaalue', {
        url: '/osat/:osanTyyppi/:osanId',
        templateUrl: 'views/partials/perusteprojekti/osaalue.html',
        controller: 'OsaAlueController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.muokkaus', {
        url: '/muokkaus/:osanTyyppi/:osanId',
        templateUrl: 'views/muokkaus.html',
        controller: 'OsanMuokkausController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.suoritustapa', {
        url: '/:suoritustapa',
        template: '<div ui-view></div>',
        navigaationimi: 'navi-perusteprojekti',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'projektinTiedotAlustettu': ['perusteprojektiTiedot', function(perusteprojektiTiedot) {
            return perusteprojektiTiedot.projektinTiedotAlustettu();
          }],
          'perusteenSisaltoAlustus': ['perusteprojektiTiedot', 'projektinTiedotAlustettu', '$stateParams',
            function(perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) {
              return perusteprojektiTiedot.alustaPerusteenSisalto($stateParams);
            }]
        },
        abstract: true
      })
      .state('root.perusteprojekti.suoritustapa.muodostumissaannot', {
        url: '/rakenne{versio:(?:/[^/]+)?}',
        templateUrl: 'views/partials/perusteprojekti/muodostumissaannot.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/partials/perusteprojekti/tutkinnonosat.html',
        controller: 'PerusteprojektiTutkinnonOsatCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.perusteenosa', {
        url: '/perusteenosa/{perusteenOsanTyyppi}/{perusteenOsaId}{versio:(?:/[^/]+)?}',
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        resolve: {'tutkinnonosanTiedot': 'TutkinnonosanTiedotService',
          'perusteprojektiOikeudetNouto': ['tutkinnonosanTiedot', '$stateParams', function (tutkinnonosanTiedot, $stateParams) {
              tutkinnonosanTiedot.noudaTutkinnonOsa($stateParams);
            }]},
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.perusteenosa.osaalue', {
        url: '/osaalue/{osaAlueId}',
        templateUrl: 'views/partials/muokkaus/tutkinnonOsaOsaAlue.html',
        controller: 'TutkinnonOsaOsaAlueCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.suoritustapa.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/partials/perusteprojekti/sisalto.html',
        controller: 'PerusteprojektisisaltoCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.peruste', {
        url: '/peruste',
        templateUrl: 'views/partials/perusteprojekti/peruste.html',
        controller: 'PerusteenTiedotCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.projektiryhma', {
        url: '/projektiryhma',
        templateUrl: 'views/partials/perusteprojekti/projektiryhma.html',
        controller: 'ProjektiryhmaCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojektiwizard', {
        url: '/perusteprojekti',
        template: '<div ui-view></div>',
        abstract: true
      })
      .state('root.perusteprojektiwizard.pohja', {
        url: '/perustepohja',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService'}
      })
      .state('root.perusteprojektiwizard.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService'}
      });
  })
  .controller('PerusteprojektiCtrl', function($scope, $state, $stateParams,
    Navigaatiopolku, koulutusalaService, opintoalaService,
    PerusteProjektiService, perusteprojektiTiedot, PerusteProjektiSivunavi, PdfCreation,
    SuoritustapaSisalto, Notifikaatiot, TutkinnonOsaEditMode, perusteprojektiOikeudet) {

    $scope.muokkausEnabled = false;



    $scope.luoPdf = function () {
      PdfCreation.setPerusteId($scope.projekti._peruste);
      PdfCreation.openModal();
    };

    function init() {
      $scope.projekti = perusteprojektiTiedot.getProjekti();
      $scope.peruste = perusteprojektiTiedot.getPeruste();
    }
    init();

    var amFooter = '<button class="btn btn-default" kaanna="lisaa-tutkintokohtainen-osa" icon-role="add" ng-click="$parent.lisaaTekstikappale()" oikeustarkastelu="{ target: \'peruste\', permission: \'muokkaus\' }"></button>';
    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.sivunavi = {
      suoritustapa: PerusteProjektiService.getSuoritustapa(),
      items: [],
      footer: amFooter,
      type: 'AM'
    };
    var sivunaviItemsChanged = function (items) {
      console.log("items changed", items.length);
      $scope.sivunavi.items = items;
    };
    var sivunaviTypeChanged = function (type) {
      $scope.sivunavi.type = type;
      switch (type) {
        case 'YL':
          $scope.sivunavi.suoritustapa = '';
          $scope.sivunavi.footer = '';
          break;
        default:
          $scope.sivunavi.footer = amFooter;
          $scope.sivunavi.suoritustapa = PerusteProjektiService.getSuoritustapa();
          break;
      }
    };
    PerusteProjektiSivunavi.register('itemsChanged', sivunaviItemsChanged);
    PerusteProjektiSivunavi.register('typeChanged', sivunaviTypeChanged);
    PerusteProjektiSivunavi.refresh(true);

    $scope.$on('$stateChangeSuccess', function() {
      var newSuoritustapa = PerusteProjektiService.getSuoritustapa();
      if (newSuoritustapa !== $scope.sivunavi.suoritustapa) {
        PerusteProjektiSivunavi.refresh(true);
      }
      $scope.sivunavi.suoritustapa = $scope.sivunavi.type === 'AM' ? newSuoritustapa : '';
    });

    Navigaatiopolku.asetaElementit({
      perusteprojekti: {
        nimi: $scope.projekti.nimi,
        url: 'root.perusteprojekti.suoritustapa.sisalto'
      }
    });

    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };

    $scope.canChangePerusteprojektiStatus = function() {
      return perusteprojektiOikeudet.onkoOikeudet('perusteprojekti', 'tilanvaihto');
    };

    $scope.showBackLink = function () {
      return !($state.is('root.perusteprojekti.suoritustapa.sisalto') ||
               $state.is('root.perusteprojekti.perusopetus'));
    };

    $scope.getBackLink = function () {
      return PerusteProjektiService.getUrl($scope.projekti, $scope.peruste);
    };

    $scope.isNaviVisible = function () {
      return PerusteProjektiSivunavi.isVisible();
    };

    $scope.$on('update:perusteprojekti', function () {
      perusteprojektiTiedot.alustaProjektinTiedot($stateParams).then(function () {
        init();
        PerusteProjektiSivunavi.refresh(true);
      });
    });

    $scope.lisaaTekstikappale = function () {
      function lisaaSisalto(method, sisalto, cb) {
        cb = cb || angular.noop;
        SuoritustapaSisalto[method]({
          perusteId: $scope.projekti._peruste,
          suoritustapa: PerusteProjektiService.getSuoritustapa()
        }, sisalto, cb, Notifikaatiot.serverCb);
      }
      lisaaSisalto('save', {}, function(response) {
        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
        $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsanTyyppi: 'tekstikappale',
          perusteenOsaId: response._perusteenOsa,
          versio: ''
        });
      });
    };

    $scope.$on('enableEditing', function() {
      $scope.muokkausEnabled = true;
    });
    $scope.$on('disableEditing', function() {
      $scope.muokkausEnabled = false;
    });
  })
  .service('PerusteProjektiSivunavi', function (PerusteprojektiTiedotService, $stateParams, $q,
                                                $state, $location, YleinenData, PerusopetusService) {
    var STATE_OSAT = 'root.perusteprojekti.suoritustapa.tutkinnonosat';
    var STATE_OSA = 'root.perusteprojekti.suoritustapa.perusteenosa';
    var STATE_OSALISTAUS = 'root.perusteprojekti.osalistaus';
    var STATE_OSAALUE = 'root.perusteprojekti.osaalue';
    var isTutkinnonosatActive = function () {
      return $state.is(STATE_OSAT) || ($state.is(STATE_OSA) &&
        $stateParams.perusteenOsanTyyppi === 'tutkinnonosa');
    };
    var AM_ITEMS = [
      {
        label: 'tutkinnonosat',
        link: [STATE_OSAT, {}],
        isActive: isTutkinnonosatActive
      }
    ];
    var service = null;
    var _isVisible = false;
    var items = [];
    var nameMap = {};
    var perusteenTyyppi = 'AM';
    var data = {
      projekti: {
        peruste: {
          sisalto: {
          }
        }
      }
    };
    var callbacks = {
      itemsChanged: angular.noop,
      typeChanged: angular.noop
    };

    function getLink(lapsi) {
      return lapsi.perusteenOsa.tunniste && lapsi.perusteenOsa.tunniste === 'rakenne' ?
        ['root.perusteprojekti.suoritustapa.muodostumissaannot', {versio: ''}] :
        [
          STATE_OSA,
          {
            perusteenOsanTyyppi: 'tekstikappale',
            perusteenOsaId: lapsi.perusteenOsa.id,
            versio: null
          }
        ];
    }

    var processNode = function (node, level) {
      level = level || 0;
      _.each(node.lapset, function (lapsi) {
        items.push({
          label: lapsi.perusteenOsa.nimi,
          id: lapsi.perusteenOsa.id,
          depth: level,
          link: getLink(lapsi),
          isActive: isRouteActive,
        });
        nameMap[lapsi.perusteenOsa.id] = lapsi.perusteenOsa.nimi;
        processNode(lapsi, level + 1);
      });
    };

    var isRouteActive = function (item) {
      // ui-sref-active doesn't work directly in ui-router 0.2.*
      // with optional parameters.
      // Versionless url should be considered same as specific version url.
      var url = item.href && item.href.indexOf('/rakenne') > -1 ?
        item.href.substr(1) : $state.href(STATE_OSA, {
        perusteenOsaId: item.id,
        versio: null
      }, {inherit:true}).replace(/#/g, '');
      return $location.url().indexOf(url) > -1;
    };

    function mapYL(osat, key) {
      _.each(osat, function (osa) {
        items.push({
          depth: 1,
          label: osa.nimi ? osa.nimi : osa.perusteenOsa.nimi,
          link: [STATE_OSAALUE, {osanTyyppi: key, osanId: osa.id}]
        });
      });
    }

    var buildTree = function () {
      items = [];
      if (perusteenTyyppi === 'YL') {
        var tiedot = service.getYlTiedot();
        _.each(PerusopetusService.LABELS, function (key, label) {
          items.push({
            label: label,
            link: [STATE_OSALISTAUS, {osanTyyppi: key}]
          });
          mapYL(tiedot[key], key);
        });
      } else {
        items = _.clone(AM_ITEMS);
        processNode(data.projekti.peruste.sisalto);
      }
      callbacks.itemsChanged(items);
    };

    var load = function () {
      data.projekti = service.getProjekti();
      data.projekti.peruste = service.getPeruste();
      data.projekti.peruste.sisalto = service.getSisalto();
      var oldTyyppi = perusteenTyyppi;
      perusteenTyyppi = YleinenData.isPerusopetus(data.projekti.peruste) ? 'YL' : 'AM';
      if (oldTyyppi !== perusteenTyyppi) {
        callbacks.typeChanged(perusteenTyyppi);
      }
      buildTree();
    };

    this.register = function (key, cb) {
      callbacks[key] = cb;
    };

    this.refresh = function (light) {
      if (!service) {
        PerusteprojektiTiedotService.then(function(res) {
          service = res;
          load();
        });
      } else {
        if (light) {
          load();
        } else {
          service.alustaPerusteenSisalto($stateParams, true).then(function () {
            load();
          });
        }
      }
    };

    this.setVisible = function (visible) {
      _isVisible = _.isUndefined(visible) ? true : visible;
    };

    this.isVisible = function () {
      return _isVisible;
    };

    this.setCrumb = function (ids) {
      var crumbEl = angular.element('#tekstikappale-crumbs');
      ids.splice(0, 1);
      ids.reverse();
      var crumbs = _.map(ids, function (id) {
        return {name: nameMap[id], id: id};
      });
      var scope = crumbEl.scope();
      if (scope) {
        scope.setCrumbs(crumbs);
      }
    };
  });
