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
/* global _, moment, alert */

angular.module('eperusteApp', [
  'ngRoute',
  'ngSanitize',
  'ui.router',
  'ngResource',
  'ngAnimate',
  'pascalprecht.translate',
  'ui.bootstrap',
  'ui.utils',
  'ui.sortable',
  'monospaced.elastic'
])
  .constant('SERVICE_LOC', '/eperusteet-service/api')
  // .constant('ORGANISATION_SERVICE_LOC', '/organisaatio-service/rest')
  .constant('ORGANISATION_SERVICE_LOC', '')
  .constant('AUTHENTICATION_SERVICE_LOC', '/authentication-service/resources')
  .constant('REQUEST_TIMEOUT', 5000)
  .constant('SPINNER_WAIT', 100)
  .constant('NOTIFICATION_DELAY_SUCCESS', 2000)
  .constant('NOTIFICATION_DELAY_WARNING', 8000)
  .config(function($urlRouterProvider, $sceProvider) {
    $sceProvider.enabled(true);
    $urlRouterProvider.when('','/');
    $urlRouterProvider.otherwise(function($injector, $location) {
      $injector.get('virheService').setData({path: $location.path()});
      $injector.get('$state').go('virhe');
    });
  })
  .config(function($translateProvider) {
    $translateProvider.useStaticFilesLoader({
      prefix: 'localisation/locale-',
      suffix: '.json'
    });
    $translateProvider.preferredLanguage('fi');
    moment.lang('fi');
  })
  .config(function($httpProvider) {
    $httpProvider.interceptors.push(['$rootScope', '$q', 'Kaanna', function($rootScope, $q, Kaanna) {
        return {
          request: function(request) {
            request.timeout = 5000;
            return request;
          },
          responseError: function(error) {
            if (error.status === 0) {
              alert(Kaanna.kaanna('yhteys-palvelimeen-timeout'));
            }
            return $q.reject(error);
          }
        };
      }]);
  })
  .config(function($httpProvider) {
    $httpProvider.interceptors.push(['$rootScope', '$q', 'SpinnerService', function($rootScope, $q, Spinner) {
        return {
          request: function(request) {
            Spinner.enable();
            return request;
          },
          response: function(response) {
            Spinner.disable();
            return response || $q.when(response);
          },
          responseError: function(error) {
            Spinner.disable();
            return $q.reject(error);
          }
        };
      }]);
  })
  // Uudelleenohjaus autentikointiin ja palvelinvirheiden ilmoitukset
  .config(function($httpProvider) {
    // Asetetaan oma interceptor kuuntelemaan palvelinkutsuja
    $httpProvider.interceptors.push(['$rootScope', '$q', function($rootScope, $q) {
        return {
          'response': function(response) {
            // var uudelleenohjausStatuskoodit = [401, 403, 412, 500];
            var uudelleenohjausStatuskoodit = [412, 500];
            if (_.indexOf(uudelleenohjausStatuskoodit, response.status) !== -1) {
              // TODO: ota käyttöön poistamalla kommentista
              $rootScope.$emit('event:uudelleenohjattava', response.status);
            }
            return response || $q.when(response);
          },
          'responseError': function(err) {
            return $q.reject(err);
          }
        };
      }]);
  })
  .run(function($rootScope, $modal, $location, $window, $state, paginationConfig, Editointikontrollit,
                Varmistusdialogi, Kaanna, virheService) {
    paginationConfig.firstText = '';
    paginationConfig.previousText = '';
    paginationConfig.nextText = '';
    paginationConfig.lastText = '';
    paginationConfig.maxSize = 5;
    paginationConfig.rotate = false;

    var onAvattuna = false;

    $rootScope.$on('event:uudelleenohjattava', function(event, status) {
      if (onAvattuna) {
        return;
      }
      onAvattuna = true;

      function getCasURL() {
        var host = $location.host();
        var port = $location.port();
        var protocol = $location.protocol();
        var cas = '/cas/login';
        var redirectURL = encodeURIComponent($location.absUrl());
        var url = protocol + '://' + host;

        if (port !== 443 && port !== 80) {
          url += ':' + port;
        }

        url += cas + '?service=' + redirectURL;
        return url;
      }

      var casurl = getCasURL();

      var uudelleenohjausModaali = $modal.open({
        templateUrl: 'views/modals/uudelleenohjaus.html',
        controller: 'UudelleenohjausModalCtrl',
        resolve: {
          status: function() {
            return status;
          },
          redirect: function() {
            return casurl;
          }
        }
      });

      uudelleenohjausModaali.result.then(function() {
      }, function() {
      }).finally(function() {
        onAvattuna = false;
        switch (status) {
          case 500:
            $location.path('/');
            break;
          case 412:
            $window.location.href = casurl;
            break;
        }
      });
    });

    $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState) {
      if (Editointikontrollit.getEditMode() && fromState.name !== 'perusteprojekti.suoritustapa.tutkinnonosat') {
        event.preventDefault();

        var data = {toState: toState, toParams: toParams};
        Varmistusdialogi.dialogi({
          successCb: function(data) {
            Editointikontrollit.cancelEditing();
            $state.go(data.toState, data.toParams);
          }, data: data, otsikko: 'vahvista-liikkuminen', teksti: 'tallentamattomia-muutoksia',
          lisaTeksti: 'haluatko-jatkaa',
          primaryBtn: 'poistu-sivulta'
        })();
      }
    });

    $rootScope.$on('$stateChangeError', function(event, toState/*, toParams, fromState*/) {
      console.error(event);
      virheService.setData({state: toState.name});
      $state.go('virhe');
    });

    $rootScope.$on('$stateNotFound', function(event, toState/*, toParams, fromState*/) {
      console.error(event);
      virheService.setData({state: toState.to});
      $state.go('virhe');
    });

    // Jos käyttäjä editoi dokumenttia ja koittaa poistua palvelusta (reload, iltalehti...), niin varoitetaan, että hän menettää muutoksensa jos jatkaa.
    $window.addEventListener('beforeunload', function(event) {
      if (Editointikontrollit.getEditMode()) {
        var confirmationMessage = Kaanna.kaanna('tallentamattomia-muutoksia');
        (event || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
      }
    });

  });
