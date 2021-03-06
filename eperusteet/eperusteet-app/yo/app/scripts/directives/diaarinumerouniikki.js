'use strict';
/*global _*/

angular.module('eperusteApp')
  .directive('diaarinumerouniikki', function(DiaarinumeroUniqueResource) {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ngModel) {

        ngModel.$parsers.push(function(viewValue) {
          if (!_.isEmpty(viewValue)) {
            validate(viewValue);
          } else {
            ngModel.$setValidity('diaarinumerouniikki', true);
          }
          return viewValue;
        });

        function doValidate(viewValue) {
          DiaarinumeroUniqueResource.get({
            diaarinumero: viewValue,
          }, function(vastaus) {
            ngModel.$setValidity('diaarinumerouniikki', vastaus.vastaus === true);
          });
        }

        var validate = _.debounce(function(viewValue) {
          scope.$apply(function () {
            doValidate(viewValue);
          });
        }, 300);
      }
    };
  });
