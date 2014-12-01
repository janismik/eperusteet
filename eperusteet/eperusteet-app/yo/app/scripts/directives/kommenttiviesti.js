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
  .directive('kommenttiViesti', function ($timeout, $compile, kommenttiViestiTemplate, Algoritmit) {
    return {
      restrict: 'AE',
      template: '',
      scope: {
        sisalto: '=',
        depth: '=',
        parent: '='
      },
      link: function (scope, element) {
        scope.$watch('sisalto.$resolved', function () {
          scope.processMessages();
          $timeout(function () {
            element.html('');
            $compile(kommenttiViestiTemplate)(scope, function (clone) {
              element.append(clone);
            });
          });
        });
      },
      controller: function ($scope, Profiili) {
        $scope.editoi = false;
        $scope.model = {
          editoitava: ''
        };
        $scope.indent = ($scope.depth * 60) + 'px';
        $scope.oidResolved = false;
        $scope.selfOid = Profiili.oid();
        if (!$scope.selfOid) {
          Profiili.casTiedot().then(function (res) {
            if (res.oid) {
              $scope.selfOid = res.oid;
            }
            $scope.oidResolved = true;
          });
        } else {
          $scope.oidResolved = true;
        }

        $scope.processMessage = function (item) {
          item.$nimikirjaimet = $scope.nimikirjaimet(item.nimi || item.muokkaaja);
        };

        $scope.processMessages = function () {
          Algoritmit.kaikilleLapsisolmuille($scope.sisalto, 'viestit', $scope.processMessage);
        };

        $scope.poistaKommentti = $scope.$parent.poistaKommentti;
        $scope.muokkaaKommenttia = $scope.$parent.muokkaaKommenttia;
        $scope.lisaaKommentti = $scope.$parent.lisaaKommentti;
        $scope.nimikirjaimet = $scope.$parent.nimikirjaimet;
        $scope.startEditing = function (viesti) {
          $scope.editoi = viesti.id;
          $scope.model.editoitava = angular.copy(viesti.sisalto);
        };
        $scope.cancelEditing = function () {
          $scope.editoi = false;
          $scope.model.editoitava = '';
        };
        $scope.saveEditing = function (viesti) {
          $scope.editoi = false;
          $scope.muokkaaKommenttia(viesti, angular.copy($scope.model.editoitava), function () {
            $scope.processMessage(viesti);
          });
          $scope.model.editoitava = '';
        };
        $scope.saveNew = function (viesti, editoitava) {
          $scope.lisaaKommentti(viesti, editoitava, function () {
            $scope.processMessages();
          });
          editoitava = '';
          viesti.$lisaa = false;
        };
      }
    };
  })
  .factory('kommenttiViestiTemplate', function () {
    return '<div ng-repeat="viesti in sisalto.viestit" ng-if="oidResolved">' +
      '<div ng-style="{\'margin-left\': indent }" class="kommentti" ng-class="{\'kommentti-oma\': selfOid === viesti.muokkaaja}">' +
      '<div class="kommentti-poistettu" ng-if="viesti.poistettu">' +
      '  <h3>{{\'viesti-poistettu\' | kaanna }} {{ viesti.muokattu | aikaleima }}</h3>' +
      '</div>' +
      '<div ng-if="!viesti.poistettu">' +
      '  <div class="pull-left ryhma-jasen-avatar kommentti-avatar">' +
      '    <div class="nimikirjain-avatar">{{viesti.$nimikirjaimet}}</div>' +
      '  </div>' +
      '  <div class="kommentti-sisalto">' +
      '    <h3>' +
      '      {{ viesti.nimi || viesti.muokkaaja }}' +
      '      <span class="pull-right" ng-if="selfOid === viesti.muokkaaja">' +
      '        <a class="action-link" ng-click="startEditing(viesti)" icon-role="edit" oikeustarkastelu="{ target: \'peruste\', permission: \'muokkaus\' }"></a>' +
      '        <a class="action-link" ng-click="poistaKommentti(viesti)" icon-role="remove" oikeustarkastelu="{ target: \'peruste\', permission: \'poisto\' }"></a>' +
      '      </span>' +
      '    </h3>' +
      '    <div ng-hide="editoi === viesti.id"><p ng-repeat="p in viesti.sisalto|paragraphsplit track by $index">{{p}}</p></div>' +
      '    <div ng-show="editoi === viesti.id" class="kommentti-muokkaus">' +
      '      <textarea class="form-control msd-elastic" ng-model="model.editoitava"></textarea>' +
      '      <div>' +
      '        <button class="btn" ng-click="cancelEditing()" kaanna>peruuta</button>' +
      '        <button class="btn btn-primary" ng-click="saveEditing(viesti)" kaanna>tallenna</button>' +
      '      </div>' +
      '    </div>' +
      '    <div class="kommentti-footer">' +
      '      <span ng-if="viesti.muokattu" class="aikaleima"><span class="muokattu" kaanna="\'muokattu\'"></span>{{ viesti.muokattu | aikaleima }}</span>' +
      '      <span ng-if="!viesti.muokattu" class="aikaleima">{{ viesti.luotu | aikaleima }}</span>' +
      '      <a class="action-link" ng-click="viesti.$lisaa = true" oikeustarkastelu="{ target: \'peruste\', permission: \'muokkaus\' }" kaanna>vastaa</a>' +
      '      <span ng-show="viesti.viestit.length > 0">' +
      '        <a class="action-link" ng-show="viesti.$piilotaAliviestit" ng-click="viesti.$piilotaAliviestit = false" kaanna>nayta-aliviestit</a>' +
      '        <a class="action-link" ng-hide="viesti.$piilotaAliviestit" ng-click="viesti.$piilotaAliviestit = true" kaanna>piilota-aliviestit</a>' +
      '      </span>' +
      '    </div>' +
      '  </div>' +
      '</div>' +
      '<hr>' +
      '<div ng-show="viesti.$lisaa">' +
      '  <div class="kommentti-uusi">' +
      '    <textarea class="form-control msd-elastic" ng-model="editoitava"></textarea>' +
      '    <div class="kommentti-painikkeet">' +
      '      <button class="btn" ng-click="viesti.$lisaa = false" kaanna>peruuta</button>' +
      '      <button class="btn btn-primary" ng-click="saveNew(viesti, editoitava)" kaanna>tallenna</button>' +
      '    </div>' +
      '    <div class="clearfix"></div>' +
      '  </div>' +
      '</div>' +
      '</div>' +
      '<kommentti-viesti ng-if="!viesti.$piilotaAliviestit" parent="sisalto" depth="depth + 1" sisalto="viesti"></kommentti-viesti>' +
      '</div>';
  });
