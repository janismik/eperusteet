'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('PerusteprojektiTutkinnonOsatCtrl', function($scope, $rootScope, $state, $stateParams,
    Navigaatiopolku, PerusteProjektiService, PerusteRakenteet, PerusteenRakenne, TreeCache, Notifikaatiot,
    Editointikontrollit, Kaanna, PerusteTutkinnonosa, TutkinnonOsanTuonti, TutkinnonOsaEditMode, Lukitus) {

    $scope.editoi = false;
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.tosarajaus = '';
    $scope.rakenne = {
      $resolved: false,
      rakenne: {osat: []},
      tutkinnonOsat: {}
    };

    $scope.paivitaRajaus = function(rajaus) { $scope.tosarajaus = rajaus; };

    function haeRakenne() {
      PerusteenRakenne.hae($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
        res.$suoritustapa = $scope.suoritustapa;
        res.$resolved = true;
        $scope.rakenne = res;
      });
    }
    $scope.haeRakenne = haeRakenne;
    haeRakenne();

    $scope.rajaaTutkinnonOsia = function(haku) {
      return Kaanna.kaanna(haku.nimi).toLowerCase().indexOf($scope.tosarajaus.toLowerCase()) !== -1;
    };

    $scope.tuoTutkinnonosa = TutkinnonOsanTuonti.modaali($scope.suoritustapa, function(osat) {
      _.forEach(osat, function(osa) { $scope.lisaaTutkinnonOsa(osa); });
    });

    $scope.navigoiTutkinnonosaan = function (osa) {
      $state.go('perusteprojekti.suoritustapa.perusteenosa', {
        perusteenOsaId: osa._tutkinnonOsa,
        perusteenOsanTyyppi: 'tutkinnonosa'
      });
    };

    $scope.lisaaTutkinnonOsa = function(osa, cb) {
      osa = osa ? {_tutkinnonOsa: osa._tutkinnonOsa} : {};
      cb = cb || angular.noop;

      PerusteTutkinnonosa.save({
        perusteenId: $scope.rakenne.$peruste.id,
        suoritustapa: $scope.rakenne.$suoritustapa
      }, osa,
      function(res) {
        $scope.rakenne.tutkinnonOsat[res._tutkinnonOsa] = res;
        cb();
        TutkinnonOsaEditMode.setMode(true);
        $scope.navigoiTutkinnonosaan(res);
      },
      function(err) {
        Notifikaatiot.fataali('tallennus-epäonnistui', err);
        cb();
      });
    };
  });
