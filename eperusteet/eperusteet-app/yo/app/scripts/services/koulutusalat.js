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
/*global _*/

angular.module('eperusteApp')
  .service('Koulutusalat', function Koulutusalat($resource, SERVICE_LOC) {

      var koulutusalatResource = $resource(SERVICE_LOC + '/koulutusalat/:koulutusalaId',
        {koulutusalaId: '@id'}, {'query': {method: 'GET', isArray: true, cache: true}});
      this.koulutusalatMap = {};
      this.koulutusalat = [];
      var self = this;

      var koulutusalaPromise = koulutusalatResource.query().$promise;

      this.haeKoulutusalat = function() {
        return self.koulutusalat;
      };

      this.haeKoulutusalaNimi = function(koodi) {
        return self.koulutusalatMap[koodi];
      };

      return koulutusalaPromise.then(function(vastaus) {

        self.koulutusalatMap = _.zipObject(_.pluck(vastaus, 'koodi'), _.map(vastaus, function(e) {
          return {
            nimi: e.nimi
          };
        }));
        self.koulutusalat = vastaus;
        return self;
      });

    });
