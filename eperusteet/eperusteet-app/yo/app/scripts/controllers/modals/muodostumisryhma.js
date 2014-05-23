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

/* global _ */
'use strict';

angular.module('eperusteApp')
  .controller('MuodostumisryhmaModalCtrl', function($scope, $modalInstance, ryhma, vanhempi, suoritustapa, Varmistusdialogi) {
    $scope.vanhempi = vanhempi;
    $scope.suoritustapa = suoritustapa;

    var msl = ryhma && ryhma.muodostumisSaanto && ryhma.muodostumisSaanto.laajuus ? ryhma.muodostumisSaanto.laajuus : null;
    var msk = ryhma && ryhma.muodostumisSaanto && ryhma.muodostumisSaanto.koko ? ryhma.muodostumisSaanto.koko : null;

    $scope.ms = {
      laajuus: msl ? true : false,
      koko: msk ? true : false
    };

    $scope.luonti = !_.isObject(ryhma);
    $scope.ryhma = ryhma ? angular.copy(ryhma) : {};
    if (!$scope.ryhma.muodostumisSaanto) {
      $scope.ryhma.muodostumisSaanto = {};
    }
    if (!$scope.ryhma.nimi) {
      $scope.ryhma.nimi = {};
    }
    if (!$scope.ryhma.kuvaus) {
      $scope.ryhma.kuvaus = {};
    }

    $scope.ok = function(uusiryhma) {
      if (uusiryhma) {
        if (uusiryhma.osat === undefined) {
          uusiryhma.osat = [];
        }
        if (!$scope.ms.laajuus) {
          delete uusiryhma.muodostumisSaanto.laajuus;
        }
        if (!$scope.ms.koko) {
          delete uusiryhma.muodostumisSaanto.koko;
        }
      }
      $modalInstance.close(uusiryhma);
    };

    $scope.poista = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'poistetaanko-ryhma',
        successCb: function () {
          $scope.ok(null);
        }
      })();
    };

    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };
  });