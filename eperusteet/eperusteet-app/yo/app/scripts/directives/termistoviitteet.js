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
  .directive('termistoTekstikentta', function() {
    return {
      restrict: 'E',
      scope: { model: '=tekstikentta' },
      template: '<p ng-bind-html="model | kaanna | unsafe" termisto-viitteet="model"></p>'
    };
  })
  .directive('termistoViitteet', function (Kaanna, TermistoService, $document, $timeout) {
    var TERMI_MATCHER = 'abbr[data-viite]';
    return {
      restrict: 'A',
      scope: {
        model: '=termistoViitteet'
      },
      link: function (scope, element) {
        scope.popovers = [];
        function destroy() {
          element.find(TERMI_MATCHER).each(function () {
            var jqEl = angular.element(this);
            if (jqEl.popover) {
              jqEl.popover('destroy');
            }
          });
          scope.popovers = [];
        }
        function setup() {
          element.find(TERMI_MATCHER).each(function () {
            var jqEl = angular.element(this);
            var viiteId = jqEl.attr('data-viite');
            if (viiteId) {
              TermistoService.preload();
            }
            var popover = jqEl.popover({
              placement: 'auto',
              html: true,
              title: Kaanna.kaanna('termin-selitys'),
              trigger: 'click'
            }).on('show.bs.popover', function () {
              var res = TermistoService.getWithAvain(viiteId, true);
              var content = res ? '<strong>' + Kaanna.kaanna(res.termi) + '</strong>: ' + Kaanna.kaanna(res.selitys) :
                Kaanna.kaanna('termia-ei-loytynyt');
              popover.attr('data-content', content);
              $timeout(function () {
                var title = element.find('.popover-title');
                var closer = angular.element(
                  '<span class="closer pull-right">&#x2715;</span>');
                closer.on('click', function () {
                  popover.popover('hide');
                });
                title.append(closer);
              }, 100);
            });
            scope.popovers.push(popover);
          });
        }

        // Click anywhere to close
        $document.on('click', function (event) {
          if (element.find(event.target).length > 0) {
            return;
          }
          _.each(scope.popovers, function (popover) {
            popover.popover('hide');
          });
        });

        function refresh() {
          $timeout(function () {
            destroy();
            setup();
          }, 500);
        }

        scope.$watch('model', refresh);
        scope.$on('termisto:update', refresh);
        scope.$on('$destroy', function () {
          $document.off('click');
          destroy();
        });
      },
    };
  });
