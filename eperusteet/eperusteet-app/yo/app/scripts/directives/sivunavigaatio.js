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

/**
 * Perusteprojektin sivunavigaatioelementti
 */
angular.module('eperusteApp')
  .directive('sivunavigaatio', function() {
    return {
      templateUrl: 'views/partials/sivunavigaatio.html',
      restrict: 'E',
      controller: 'sivunavigaatioCtrl'
    };
  })

  .controller('sivunavigaatioCtrl', function($rootScope, $scope, $state, SivunavigaatioService, PerusteProjektiService) {
    $scope.menuCollapsed = true;
    $rootScope.$on('$stateChangeStart', function () {
      $scope.menuCollapsed = true;
    });
    $scope.goBackToMain = function () {
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $scope.projekti.id, suoritustapa: PerusteProjektiService.getSuoritustapa()});
    };
    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };
    $scope.isHidden = function () {
      if ($scope.data.piilota) {
        // TODO: parempi/tehokkaampi ratkaisu. Sisältö-div on tämän direktiivin
        // ulkopuolella, mutta sen tyyli riippuu 'piilota'-attribuutista.
        angular.element('.sivunavi-sisalto').addClass('disable');
      } else {
        angular.element('.sivunavi-sisalto').removeClass('disable');
      }
      return $scope.data.piilota;
    };
    SivunavigaatioService.bind($scope);
  })

  .service('SivunavigaatioService', function (Suoritustapa) {
    this.data = {
      osiot: false,
      piilota: false,
      projekti: {id: 0}
    };
    this.bind = function (scope) {
      scope.data = this.data;
    };

    /**
     * Asettaa sivunavigaation tiettyyn tilaan.
     * Suositeltu käyttöpaikka: $stateProvider.state -> onEnter
     * @param data {Object} Mahdolliset asetukset:
     *     osiot: true näyttää projektin kaikki osiot,
     *            false näyttää vain "takaisin"-linkin
     *     piilota: true piilottaa koko navigaatioelementin
     */
    this.aseta = function (data) {
      if (data.projekti) {
        this.data.projekti = data.projekti;
        // TODO: kevyempi API jolla haetaan pelkät otsikot/linkkeihin
        // tarvittavat tiedot, ei koko sisältöä
        var that = this;
        Suoritustapa.get({perusteenId: this.data.projekti._peruste, suoritustapa: 'ops'}, function(vastaus) {
          that.data.projekti.peruste = {};
          that.data.projekti.peruste.sisalto = vastaus;
        });
        return;
      }
      if (!_.isUndefined(data.osiot)) {
        this.data.osiot = data.osiot;
      }
      this.data.piilota = !!data.piilota;
    };
    /**
     * Asettaa projektiobjektin, täytyy sisältää ainakin 'id'
     */
    this.asetaProjekti = function (projekti) {
      this.aseta({projekti: projekti});
    };
  });
