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
  .directive('muokkausVuosiluokka', function(PerusopetusService) {
    return {
      templateUrl: 'views/directives/vuosiluokkakokonaisuus.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: function($scope) {
        $scope.editEnabled = false;
        $scope.data = {
          options: {
            title: $scope.model.nimi,
            backLabel: 'vuosiluokkakokonaisuudet',
            backState: ['root.perusteprojekti.osalistaus', {osanTyyppi: PerusopetusService.VUOSILUOKAT}],
            fieldRenderer: '<kenttalistaus edit-enabled="editEnabled" object-promise="modelPromise" fields="config.fields"></kenttalistaus>',
            fields: [
              {
                path: 'osaamisenkuvaukset',
                localeKey: 'laaja-alainen-osaaminen',
                type: 'osaaminen',
                localized: true,
                collapsible: true,
              },
              {
                path: 'tekstikappaleet',
                type: 'editor-area',
                localized: true,
                collapsible: true,
                multiple: true,
              }
            ]
          }
        };
      }
    };
  })

  .directive('osallinenOsa', function ($compile) {
    return {
      templateUrl: 'views/directives/osallinenosa.html',
      restrict: 'AE',
      transclude: true,
      scope: {
        editEnabled: '=',
        model: '=',
        config: '=',
        versiot: '='
      },
      controller: 'OsallinenOsaController',
      link: function (scope, element) {
        var el = $compile(angular.element(scope.config.fieldRenderer))(scope);
        element.find('.tutkinnonosa-sisalto').empty().append(el);
      }
    };
  })

  .controller('OsallinenOsaController', function ($scope, $state, VersionHelper, $q) {
    var deferred = $q.defer();
    $scope.modelPromise = deferred.promise;
    deferred.resolve($scope.model);
    $scope.isLocked = false;
    $scope.isNew = false;

    $scope.isPublished = function () {
      return $scope.model.tila === 'julkaistu';
    };

    $scope.canAdd = function () {
      return true;
    };

    $scope.generateBackHref = function () {
      if ($scope.config && $scope.config.backState) {
        return $state.href.apply($state, $scope.config.backState);
      }
    };

    $scope.vaihdaVersio = function () {
      $scope.versiot.hasChanged = true;
      VersionHelper.setUrl($scope.versiot);
      //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tutkinnonOsa.id}, responseFn);
    };

    $scope.revertCb = function (response) {
      // TODO
      //responseFn(response);
      //saveCb(response);
    };
  });
