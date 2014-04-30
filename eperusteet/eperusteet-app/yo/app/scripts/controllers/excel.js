'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('excel', {
        url: '/excel',
        templateUrl: 'views/excel.html',
        controller: 'ExcelCtrl',
        naviBase: ['tuo-excel'],
      });
  })
  .controller('ExcelCtrl', function($scope, ExcelService, PerusteenOsat, TutkinnonOsanValidointi, Koodisto, PerusteprojektiResource, PerusteTutkinnonosat, SuoritustapaSisalto) {
    $scope.osatutkinnot = [];
    $scope.vaihe = [];
    $scope.errors = [];
    $scope.warnings = [];
    $scope.filename = '';
    $scope.lukeeTiedostoa = true;
    $scope.naytaVirheet = false;
    $scope.uploadErrors = [];
    $scope.uploadSuccess = false;
    $scope.tutkinnonTyyppi = 'ammattitutkinto';
    $scope.parsinnanTyyppi = 'peruste';
    $scope.projekti = {};
    $scope.haettuProjekti = {};
    $scope.projekti.peruste = {};
    $scope.perusteTallennettu = false;

    $scope.clearSelect = function() {
      $scope.osatutkinnot = [];
      $scope.vaihe = [];
      $scope.errors = [];
      $scope.warnings = [];
      $scope.lukeeTiedostoa = true;
      $scope.lukeeTiedostoa = true;
      $scope.uploadErrors = [];
      $scope.uploadSuccess = false;
    };

    $scope.editoiOsatutkintoa = function() {
    };

    $scope.poistaOsatutkinto = function(ot) { _.remove($scope.osatutkinnot, ot); };

    $scope.liitaKoodiOT = function(ot) {
      Koodisto.modaali(function(koodi) {
        ot.koodiUri = koodi.koodi;
      }, { tyyppi: function() { return 'tutkinnonosat'; } })();
    };

    $scope.rajaaKoodit = function(koodi) { return koodi.koodi.indexOf('_3') !== -1; };

    $scope.tallennaPerusteprojekti = function(perusteprojekti) {
      PerusteprojektiResource.update(perusteprojekti, function(vastaus) {
        $scope.perusteTallennettu = true;
        $scope.haettuProjekti = vastaus;
      }, function (virhe) {
        console.log('virhe:', virhe);
        // TODO: Virhe notifikaatio
      });
    };

    $scope.tallennaTekstikappaleet = function(tekstikentat) {
      var doneSuccess = _.after(_.size(tekstikentat), function() { $scope.uploadSuccess = true; });

      _(tekstikentat).filter(function(tk) {
        return tk.$ladattu !== 0;
      }).forEach(function(tk) {
        PerusteenOsat.saveTekstikappale(tk, function(re) {
          SuoritustapaSisalto.add({
            perusteId: $scope.haettuProjekti.peruste.id,
            suoritustapa: $scope.haettuProjekti.peruste.suoritustavat[0].suoritustapakoodi
          }, { _perusteenOsa: re.id }, function() {
            tk.$ladattu = true;
            tk.id = re.id;
            doneSuccess();
          }, function(err) {
            tk.$syy = err.data.syy;
            // TODO: Lisää notifikaatio
          });
        }, function(err) {
          tk.$syy = err.data.syy;
        });
      });
    };

    $scope.perusteHaku = function(koodisto) {
      $scope.hakemassa = true;

      $scope.projekti.peruste.nimi = koodisto.nimi;
      $scope.projekti.peruste.koodi = koodisto.koodi;
      $scope.projekti.peruste.koulutukset = [{}];
      $scope.projekti.peruste.koulutukset[0].koulutuskoodi = koodisto.koodi;
      $scope.projekti.peruste.suoritustavat = [{suoritustapakoodi: 'ops'}];

      Koodisto.haeAlarelaatiot($scope.projekti.peruste.koodi, function (relaatiot) {
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              $scope.projekti.peruste.koulutukset[0].koulutusalakoodi = rel.koodi;
              break;
            case 'opintoalaoph2002':
              $scope.projekti.peruste.koulutukset[0].opintoalakoodi = rel.koodi;
              break;
            case 'koulutustyyppi':
              if (rel.koodi === 'koulutustyyppi_1' || rel.koodi === 'koulutustyyppi_11' || rel.koodi === 'koulutustyyppi_12') {
                $scope.projekti.peruste.tutkintokoodi = rel.koodi;
              }
              break;
          }
        });
        $scope.hakemassa = false;
      }, function (virhe) {
        console.log('koodisto alarelaatio virhe', virhe);
        $scope.hakemassa = false;
      });
    };

    $scope.poistaTekstikentta = function(tekstikentta) { _.remove($scope.peruste.tekstikentat, tekstikentta); };

    $scope.tallennaOsatutkinnot = function() {
      var doneSuccess = _.after(_.size($scope.osatutkinnot), function() { $scope.uploadSuccess = true; });

      _($scope.osatutkinnot).filter(function(ot) {
        return ot.$ladattu !== 0;
      }).forEach(function(ot) {
        var koodiUriKaytossa = _.any($scope.osatutkinnot, function(toinen) {
          return (ot !== toinen && ot.koodiUri !== '' && toinen.koodiUri === ot.koodiUri);
        });
        if (koodiUriKaytossa) {
          ot.$syy = ['excel-ei-kahta-samaa'];
        } else {
          var cop = _.clone(ot);
          // TutkinnonOsanValidointi.validoi(cop).then(function() {
            cop.tavoitteet = {};
            PerusteenOsat.saveTutkinnonOsa(cop, function(re) {
              console.log($scope.haettuProjekti.peruste.id, $scope.haettuProjekti.peruste.suoritustavat[0].suoritustapakoodi, re.id);
              PerusteTutkinnonosat.save({
                perusteenId: $scope.haettuProjekti.peruste.id,
                suoritustapa: $scope.haettuProjekti.peruste.suoritustavat[0].suoritustapakoodi
              }, {
                _tutkinnonOsa: re.id
              }, function() {
                ot.$ladattu = 0;
                ot.id = re.id;
                ot.koodiUri = re.koodiUri;
                doneSuccess();
              }, function(err) {
                if (err) {
                  ot.$syy = [err.data.syy];
                  ot.$ladattu = 1;
                }
              });
            }, function(err) {
              if (err) {
                ot.$syy = [err.data.syy];
                ot.$ladattu = 1;
              }
            });
          // }, function(virhe) {
            // ot.$syy = virhe;
            // ot.$ladattu = 1;
          // });
        }
      });
    };

    $scope.onFileSelect = function(err, file) {
      $scope.lukeeTiedostoa = true;
      $scope.alussa = false;
      $scope.osatutkinnot = [];

      if (err || !file) {
        // TODO: Hoida virhetilanteet
      } else {
        var promise = ExcelService.parseXLSXToOsaperuste(file, $scope.tutkinnonTyyppi, $scope.parsinnanTyyppi);
        promise.then(function(resolve) {
          $scope.warnings = resolve.osatutkinnot.varoitukset;
          $scope.projekti.peruste = _.omit(resolve.peruste, 'tekstikentat');
          $scope.tekstikentat = _.map(resolve.peruste.tekstikentat, function(tk) {
            return _.merge(tk, {
                $ladattu: -1,
                $syy: []
            });
          });
          $scope.osatutkinnot = _.map(resolve.osatutkinnot.osaperusteet, function(ot) {
            return _.merge(ot, {
                $ladattu: -1,
                koodiUri: '',
                $syy: []
            });
          });
          $scope.lukeeTiedostoa = false;
        }, function(errors) {
          $scope.errors = errors;
          $scope.lukeeTiedostoa = false;
        }, function() {
          // TODO: Ota tilannepäivitykset vastaan ja rendaa tilapalkki
        });
      }
    };
    $scope.onProgress = function() {
    };
  });
