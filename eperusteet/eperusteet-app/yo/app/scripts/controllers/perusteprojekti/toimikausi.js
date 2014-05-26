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
  .controller('PerusteprojektiToimikausiCtrl', function ($scope, YleinenData) {

  $scope.kalenteriTilat = {
    'toimikausiAlkuButton': false,
    'toimikausiLoppuButton': false
  };

  $scope.showWeeks = true;

  $scope.open = function($event) {
    $event.preventDefault();
    $event.stopPropagation();

    for (var key in $scope.kalenteriTilat) {
      if ($scope.kalenteriTilat.hasOwnProperty(key) && key !== $event.target.id) {
        $scope.kalenteriTilat[key] = false;
      }
    }
    $scope.kalenteriTilat[$event.target.id] = !$scope.kalenteriTilat[$event.target.id];
  };

  $scope.dateOptions = {
    'year-format': 'yy',
    //'month-format': 'M',
    //'day-format': 'd',
    'starting-day': 1
  };

  $scope.format = YleinenData.dateFormatDatepicker;
  });
