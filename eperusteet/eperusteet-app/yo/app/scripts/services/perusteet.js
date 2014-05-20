'use strict';
/* global _ */

angular.module('eperusteApp')
  .factory('PerusteTutkinnonosa', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa/tutkinnonosat/:osanId',
      {
        perusteenId: '@id',
        suoritustapa: '@suoritustapa',
        osanId: '@id'
      });
  })
  .factory('PerusteTutkinnonosat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa/tutkinnonosat',
      {
        perusteenId: '@id',
        suoritustapa: '@suoritustapa'
      }, {
        update: { method: 'PUT' }
      });
  })
  .factory('PerusteRakenteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa/rakenne',
      {
        perusteenId: '@id',
        suoritustapa: '@suoritustapa'
      });
  })
  .factory('Perusteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId',
      {
        perusteenId: '@id'
      });
  })
  .factory('PerusteenOsaviitteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosaviitteet/:viiteId');
  })
  .factory('Suoritustapa', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa');
  })
  .factory('SuoritustapaSisalto', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto',
    {
      perusteId: '@id',
      suoritustapa: '@suoritustapa'
    }, {
        add: {method: 'PUT'}
    });
  })
  .service('PerusteenRakenne', function(PerusteProjektiService, PerusteprojektiResource, PerusteRakenteet, TreeCache, PerusteTutkinnonosat, Perusteet, PerusteTutkinnonosa, Notifikaatiot) {
    function haeRakenne(perusteProjektiId, suoritustapa, success) {
      var response = {};

      PerusteprojektiResource.get({ id: perusteProjektiId }, function(vastaus) {
        PerusteProjektiService.save(vastaus);
        Perusteet.get({
          perusteenId: vastaus._peruste
        }, function(peruste) {
          suoritustapa = suoritustapa || peruste.suoritustavat[0].suoritustapakoodi;
          PerusteRakenteet.get({
            perusteenId: peruste.id,
            suoritustapa: suoritustapa
          }, function(rakenne) {
            PerusteTutkinnonosat.query({
              perusteenId: peruste.id,
              suoritustapa: suoritustapa
            }, function(tosat) {
              response.rakenne = rakenne;
              response.$peruste = peruste;
              response.tutkinnonOsat = _(tosat)
                                        .pluck('_tutkinnonOsa')
                                        .zipObject(tosat)
                                        .value();
              success(response);
            });
          });
        });
      });
    }

    function kaikilleRakenteille(rakenne, f) {
      if (!rakenne || !f) { return; }
      _.forEach(rakenne, function(r) {
        kaikilleRakenteille(r.osat, f);
        f(r);
      });
    }

    function tallennaRakenne(rakenne, id, suoritustapa, success) {
      var after = _.after(_.size(rakenne.tutkinnonOsat) + 1, success);

      success = success || function() {};

      PerusteRakenteet.save({
        perusteenId: id,
        suoritustapa: suoritustapa
      }, rakenne.rakenne, function() {
        after();
        _.forEach(_.values(rakenne.tutkinnonOsat), function(osa) {
          PerusteTutkinnonosa.save({
            perusteenId: id,
            suoritustapa: suoritustapa,
            osanId: osa.id
          },
          osa,
          after(),
          Notifikaatiot.serverCb);
        });
      }, Notifikaatiot.serverCb);

    }

    function poistaTutkinnonOsaViite(osaId, _peruste, suoritustapa, success) {
      PerusteTutkinnonosa.remove({
          perusteenId: _peruste,
          suoritustapa: suoritustapa,
          osanId: osaId
      }, function(res) {
        success(res);
      }, Notifikaatiot.serverCb);
    }

    return {
      hae: haeRakenne,
      tallenna: tallennaRakenne,
      poistaTutkinnonOsaViite: poistaTutkinnonOsaViite,
      kaikilleRakenteille: kaikilleRakenteille
    };
  });
