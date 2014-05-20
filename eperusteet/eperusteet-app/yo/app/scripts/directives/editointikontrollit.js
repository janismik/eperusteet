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
  .directive('editointikontrollit', function() {
    return {
      templateUrl: 'views/partials/editointikontrollit.html',
      restrict: 'E'
    };
  })
  .controller('EditointiCtrl', function($scope, $rootScope, Editointikontrollit) {

    $scope.kommentti = '';
    $scope.buttonClass = 'btn-default';
    $scope.hideControls = true;
    function setEditControls() {
      if(Editointikontrollit.editingEnabled()) {
        $scope.hideControls = false;
      } else {
        $scope.hideControls = true;
        $scope.editStarted = false;
      }
    }

    setEditControls();

    $rootScope.$on('$locationChangeSuccess', function() {
      Editointikontrollit.unregisterCallback();
      setEditControls();
    });

    Editointikontrollit.registerCallbackListener(setEditControls);

    $rootScope.$on('enableEditing', function () {
      $scope.editClass = 'editing';
      $scope.buttonClass = 'btn-info';
      $scope.editStarted = true;
      $scope.kommentti = '';
    });
    $rootScope.$on('disableEditing', function () {
      $scope.editClass = '';
      $scope.buttonClass = 'btn-default';
      $scope.editStarted = false;
    });

    $scope.start = function() {
      Editointikontrollit.startEditing();
    };
    $scope.save = function() {
      Editointikontrollit.saveEditing();
    };
    $scope.cancel = function() {
      Editointikontrollit.cancelEditing();
    };
  });
