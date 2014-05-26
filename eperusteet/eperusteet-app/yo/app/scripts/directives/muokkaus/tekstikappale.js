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

angular.module('eperusteApp')
  .directive('muokkausTekstikappale', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tekstikappale.html',
      restrict: 'E',
      scope: {
        tekstikappale: '='
      },
      controller: function($scope, $state, $q, $modal, Editointikontrollit, PerusteenOsat, Notifikaatiot, Lukitus) {

        $scope.fields =
          new Array({
             path: 'nimi',
             hideHeader: true,
             localeKey: 'teksikappaleen-nimi',
             type: 'editor-header',
             localized: true,
             mandatory: true,
             order: 1
           },{
             path: 'teksti',
             localeKey: 'tekstikappaleen-teksti',
             type: 'editor-area',
             localized: true,
             mandatory: true,
             order: 2
           });

        function setupTekstikappale(kappale) {
          $scope.editableTekstikappale = angular.copy(kappale);
          $scope.tekstikappaleenMuokkausOtsikko = $scope.editableTekstikappale.id ? 'muokkaus-tekstikappale' : 'luonti-tekstikappale';

          function successCb(res) {
            Lukitus.vapauta(res.id);
            Notifikaatiot.onnistui('muokkaus-tutkinnon-osa-tallennettu');
          }

          Editointikontrollit.registerCallback({
            edit: function() {
            },
            save: function() {
              //TODO: Validate tutkinnon osa
              if ($scope.editableTekstikappale.id) {
                $scope.editableTekstikappale.$saveTekstikappale(successCb, Notifikaatiot.serverCb);
              } else {
                PerusteenOsat.saveTekstikappale($scope.editableTekstikappale, successCb, Notifikaatiot.serverCb);
              }
              $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
            },
            cancel: function() {
              $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
              var tekstikappaleDefer = $q.defer();
              $scope.tekstikappalePromise = tekstikappaleDefer.promise;
              tekstikappaleDefer.resolve($scope.editableTekstikappale);
              Lukitus.vapauta($scope.tekstikappale.id);
            }
          });
        }

        if ($scope.tekstikappale) {
          $scope.tekstikappalePromise = $scope.tekstikappale.$promise.then(function(response) {
            setupTekstikappale(response);
            return $scope.editableTekstikappale;
          });
        } else {
          var objectReadyDefer = $q.defer();
          $scope.tekstikappalePromise = objectReadyDefer.promise;
          $scope.tekstikappale = {};
          setupTekstikappale($scope.tekstikappale);
          objectReadyDefer.resolve($scope.editableTekstikappale);
        }

        $scope.muokkaa = function () {
          Lukitus.lukitse($scope.tekstikappale.id, function() {
            Editointikontrollit.startEditing();
          });
        };
      }
    };
  });

