'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Koodisto', function($http, $modal, SERVICE_LOC, $resource, Kaanna) {
    var taydennykset = [];
    var koodistoVaihtoehdot = ['tutkinnonosat', 'koulutus'];
    var nykyinenKoodisto = _.first(koodistoVaihtoehdot);
    var lisaFiltteri = function() {
      return true;
    };

    function hae(koodisto, cb) {
      if (!_.isEmpty(taydennykset) && koodisto === nykyinenKoodisto) {
        cb();
        return;
      }
      $http.get(SERVICE_LOC + '/koodisto/' + koodisto).then(function(re) {
        taydennykset = koodistoMapping(re.data);
        taydennykset = _.sortBy(taydennykset, function(t) {
          return Kaanna.kaanna(t.nimi);
        });
        cb();
      });
    }

    function haeAlarelaatiot(koodi, cb) {
      var resource = $resource(SERVICE_LOC + '/koodisto/relaatio/sisaltyy-alakoodit/:koodi');
      resource.query({koodi: koodi}, function(vastaus) {
        var relaatiot = koodistoMapping(vastaus);
        cb(relaatiot);
      });
    }

    function haeYlarelaatiot(koodi, tyyppi, cb) {
      var resource = $resource(SERVICE_LOC + '/koodisto/relaatio/sisaltyy-ylakoodit/:koodi');
      resource.query({koodi: koodi}, function(re) {
        taydennykset = suodataTyypinMukaan(re, tyyppi);
        taydennykset = koodistoMapping(taydennykset);
        taydennykset = _.sortBy(taydennykset, function(t) {
          return Kaanna.kaanna(t.nimi);
        });
        cb();
      });
    }
    
    function suodataTyypinMukaan(koodistodata, tyyppi) {
      var tulos = [];
      angular.forEach(koodistodata, function(data){
        if (data.koodiUri.substr(0, tyyppi.length) === tyyppi) {
          this.push(data);
        }
      }, tulos);
      return tulos;
    }

    function koodistoMapping(koodistoData) {
      return _(koodistoData).map(function(kd) {
        var nimi = {
          fi: '',
          sv: '',
          en: ''
        };
        _.forEach(kd.metadata, function(obj) {
          nimi[obj.kieli.toLowerCase()] = obj.nimi;
        });

        var haku = _.reduce(_.values(nimi), function(result, v) {
          return result + v;
        }).toLowerCase();
        return {
          koodi: kd.koodiUri,
          nimi: nimi,
          koodisto: kd.koodisto,
          haku: haku
        };
      }).value();
    }

    function filtteri(haku) {
      haku = haku.toLowerCase();
      return _.filter(taydennykset, function(t) {
        return (t.koodi.indexOf(haku) !== -1 || t.haku.indexOf(haku) !== -1);
      });
    }

    function modaali(successCb, resolve, failureCb, lisaf) {
      if (filtteri) {
        lisaFiltteri = lisaf;
      }
      return function() {
        resolve = resolve || {};
        failureCb = failureCb || function() {
        };
        $modal.open({
          templateUrl: 'views/modals/koodistoModal.html',
          controller: 'KoodistoModalCtrl',
          resolve: resolve
        }).result.then(successCb, failureCb);
      };
    }

    return {
      hae: hae,
      filtteri: filtteri,
      vaihtoehdot: _.clone(koodistoVaihtoehdot),
      modaali: modaali,
      haeAlarelaatiot: haeAlarelaatiot,
      haeYlarelaatiot: haeYlarelaatiot
    };
  })
  .controller('KoodistoModalCtrl', function($scope, $modalInstance, $translate, $timeout, Koodisto, tyyppi, ylarelaatioTyyppi) {
    $scope.koodistoVaihtoehdot = Koodisto.vaihtoehdot;
    $scope.tyyppi = tyyppi;
    $scope.ylarelaatioTyyppi = ylarelaatioTyyppi;
    $scope.loydetyt = [];
    $scope.nakyvilla = [];
    $scope.lataa = true;
    $scope.syote = '';

    $scope.nykyinen = 0;

    var persivu = 10;

    $scope.valitseSivu = function(sivu) {
      if (sivu >= 0 && sivu < _.size($scope.loydetyt) / persivu) {
        $scope.nykyinen = sivu;
        $scope.nakyvilla = _.last(_.first($scope.loydetyt, persivu * sivu + persivu), persivu);
      }
    };

    $scope.haku = function(rajaus) {
      $scope.loydetyt = Koodisto.filtteri(rajaus);
      $scope.valitseSivu(0);
    };

    if ($scope.ylarelaatioTyyppi === '') {
      Koodisto.hae($scope.tyyppi, function() {
        $scope.lataa = false;
        $scope.haku('');
      });
    } else {
      Koodisto.haeYlarelaatiot($scope.ylarelaatioTyyppi, $scope.tyyppi, function() {
        $scope.lataa = false;
        $scope.haku('');
      });
    }

    $scope.ok = function(koodi) {
      $modalInstance.close(koodi);
    };
    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };
  })
  .directive('koodistoSelect', function(Koodisto) {
    return {
      template: '<button class="btn btn-default" type="text" ng-click="activate()">{{ "hae-koodi-koodistosta" | translate }}</button>',
      restrict: 'E',
      link: function($scope, el, attrs) {
        var valmis = $scope.$eval(attrs.valmis);
        var filtteri = $scope.$eval(attrs.filtteri);
        var tyyppi = attrs.tyyppi || 'tutkinnonosat';
        var ylarelaatioTyyppi = attrs.ylarelaatiotyyppi || '';
        
        attrs.$observe('ylarelaatiotyyppi', function() {
                ylarelaatioTyyppi = attrs.ylarelaatiotyyppi || '';
        });

        if (!valmis) {
          console.log('koodisto-select: valmis-callback puuttuu');
          return;
        } else if (_.indexOf(Koodisto.vaihtoehdot, tyyppi) === -1) {
          console.log('koodisto-select:', tyyppi, 'ei vastaa mitään mitään vaihtoehtoa:', Koodisto.vaihtoehdot);
          return;
        }
        $scope.activate = Koodisto.modaali(function(koodi) {valmis(koodi);}, {tyyppi: function() {return tyyppi;}, ylarelaatioTyyppi: function() {return ylarelaatioTyyppi;}}, function() {}, filtteri);
      }
    };
  });