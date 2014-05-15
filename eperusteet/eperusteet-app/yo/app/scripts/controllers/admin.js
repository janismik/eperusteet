'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('admin', {
        url: '/admin',
        naviBase: ['admin'],
        templateUrl: 'views/admin.html',
        controller: 'AdminCtrl'
      });
  })
  .controller('AdminCtrl', function() {
  });