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
/*global _*/

angular.module('eperusteApp')
  .directive('revertNote', function() {
    return {
      templateUrl: 'views/partials/muokkaus/revertnote.html',
      restrict: 'AE',
      replace: true,
      scope: {
        'versiot': '=versions',
        'object': '=',
        'revertCb': '&',
        'changeVersion': '&'
      },
      controller: function ($scope, Varmistusdialogi, Lukitus, VersionHelper, $translate) {
        $scope.version = {
          revert: function () {
            var isRakenne = _.has($scope.object, 'rakenne') && _.has($scope.object, '$peruste');
            var suoritustapa = $scope.$parent.suoritustapa;
            var revCb = function (res) {
              $scope.revertCb({response: res});
            };
            var cb = isRakenne ? function () {
              Lukitus.lukitseSisalto($scope.object.$peruste.id, suoritustapa, function() {
                VersionHelper.revertRakenne($scope.versiot, {id: $scope.object.$peruste.id, suoritustapa: suoritustapa}, revCb);
              });
            } : function () {
              Lukitus.lukitsePerusteenosa($scope.object.id, function() {
                VersionHelper.revertPerusteenosa($scope.versiot, $scope.object, revCb);
              });
            };
            Varmistusdialogi.dialogi({
              successCb: cb,
              otsikko: 'vahvista-version-palauttaminen',
              teksti: $translate('vahvista-version-palauttaminen-teksti', {versio: $scope.versiot.chosen.index}),
              primaryBtn: 'vahvista',
              comment: {
                enabled: true,
                placeholder: 'kommentoi-muutosta'
              }
            })();

          },
          goToLatest: function () {
            VersionHelper.chooseLatest($scope.versiot);
            $scope.changeVersion();
          }
        };
      }
    };
  });