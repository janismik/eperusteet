'use strict';
/*global _*/

angular.module('eperusteApp')
  .factory('PerusteenOsat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId',
      {
        osanId: '@id'
      }, {
        byKoodiUri: { method: 'GET', isArray: true, params: { koodi: true } },
        saveTekstikappale: {method:'POST', params:{tyyppi:'perusteen-osat-tekstikappale'}},
        saveTutkinnonOsa: {method:'POST', params:{tyyppi:'perusteen-osat-tutkinnon-osa'}},
        revisions: {method: 'GET', isArray: true, url: SERVICE_LOC + '/perusteenosat/:osanId/revisions'},
        getRevision: {method: 'GET', url: SERVICE_LOC + '/perusteenosat/:osanId/revisions/:revisionId'}
      });
  })
  .factory('PerusteenOsaViitteet', function($resource, SERVICE_LOC) {
      return $resource(SERVICE_LOC + '/perusteenosaviitteet/sisalto/:viiteId');
  })
  .service('TutkinnonOsanValidointi', function($q, PerusteenOsat) {
    function validoi(tutkinnonOsa) {
      var virheet = [];
      var kentat = ['nimi'];
      _.forEach(kentat, function(f) {
        if (!tutkinnonOsa[f] || tutkinnonOsa[f] === '') {
          virheet.push(f);
        }
      });
      if (!_.isEmpty(virheet)) {
        virheet.unshift('koodi-virhe-3');
      }
      return virheet;
    }

    return {
      validoi: function(tutkinnonOsa) {
        var deferred = $q.defer();

        PerusteenOsat.byKoodiUri({
          osanId: tutkinnonOsa.koodiUri
        }, function(re) {
          if (re.length === 0) {
            deferred.resolve();
          } else {
            deferred.reject(['koodi-virhe-2']);
          }
        }, function() {
          var virheet = validoi(tutkinnonOsa);
          if (_.isEmpty(virheet)) {
            deferred.resolve();
          } else {
            deferred.reject(virheet);
          }
        });
        return deferred.promise;
      }
    };
  });
