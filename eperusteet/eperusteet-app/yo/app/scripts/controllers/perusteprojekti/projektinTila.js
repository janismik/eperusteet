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
  .service('PerusteprojektinTilanvaihto', function ($modal) {
    var that = this;
    this.start = function(parametrit, setFn, successCb) {
      successCb = successCb || angular.noop;
      if (_.isFunction(setFn)) {
        that.setFn = setFn;
      }
      $modal.open({
        templateUrl: 'views/modals/perusteprojektinTila.html',
        controller: 'PerusteprojektinTilaModal',
        resolve: {
          data: function () {
            return {
              oldStatus: parametrit.currentStatus,
              mahdollisetTilat: parametrit.mahdollisetTilat,
              korvattavatDiaarinumerot: parametrit.korvattavatDiaarinumerot,
              statuses: _.map(parametrit.mahdollisetTilat, function (item) {
                return {key: item, description: 'tilakuvaus-' + item};
              })
            };
          }
        }
      })
      .result.then(successCb);
    };
    this.set = function(status, siirtymaPaattyy, successCb) {
      that.setFn(status, siirtymaPaattyy, successCb);
    };
  })
  .controller('PerusteprojektinTilaModal', function ($scope, $modal, $modalInstance, $state, data) {
    $scope.data = data;
    $scope.data.selected = null;
    $scope.data.editable = false;

    $scope.valitse = function () {
      $modalInstance.close();
      $modal.open({
        templateUrl: 'views/modals/perusteprojektinTilaVarmistus.html',
        controller: 'PerusteprojektinTilaVarmistusModal',
        resolve: {
          data: function () { return $scope.data; }
        }
      });
    };

    $scope.peruuta = function () {
      $modalInstance.dismiss();
    };
  })
  .controller('PerusteprojektinTilaVarmistusModal', function ($scope,
      $modalInstance, data, PerusteprojektinTilanvaihto, Perusteet, YleinenData) {
    $scope.data = data;

    $scope.datePicker = {
          options: YleinenData.dateOptions,
          format: YleinenData.dateFormatDatepicker,
          state: false,
          open: function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.datePicker.state = !$scope.datePicker.state;
          }
        };

    $scope.korvattavatNimiMap = {};
    $scope.ladataanDiaareja = false;
    if (data.korvattavatDiaarinumerot !== null && data.korvattavatDiaarinumerot !== undefined && data.korvattavatDiaarinumerot.length > 0) {
      $scope.ladataanDiaareja = true;
      angular.forEach(data.korvattavatDiaarinumerot, function(diaari) {
        Perusteet.diaari({diaarinumero: diaari}, function (vastaus) {
          $scope.korvattavatNimiMap[diaari] = vastaus.nimi;
        }, function() {
        $scope.korvattavatNimiMap[diaari] = 'korvattavaa-ei-loydy-jarjestelmasta';
      });
      });
      $scope.ladataanDiaareja = false;
    }

    $scope.edellinen = function () {
      $modalInstance.dismiss();
      PerusteprojektinTilanvaihto.start({currentStatus: data.oldStatus, mahdollisetTilat: data.mahdollisetTilat, korvattavatDiaarinumerot: data.korvattavatDiaarinumerot});
    };

    $scope.ok = function () {
      if ($scope.data.siirtymaPaattyy !== null && $scope.data.siirtymaPaattyy !== undefined && typeof $scope.data.siirtymaPaattyy === 'object') {
        $scope.data.siirtymaPaattyy = $scope.data.siirtymaPaattyy.valueOf();
      }
      PerusteprojektinTilanvaihto.set(data.selected, $scope.data.siirtymaPaattyy);
      $modalInstance.close();
    };

    $scope.peruuta = function () {
      $modalInstance.dismiss();
    };
  });
