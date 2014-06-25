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
// /*global _*/

angular.module('eperusteApp')
  .factory('Dokumentti', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/dokumentti/:tapa/:id', {
      tapa: '@tapa', // create | get
      id: '@id'
    });
  })
  .service('Pdf', function(Dokumentti) {
    function generoiPdf(perusteId, success, failure) {

    }

    function lataaPdf(perusteId, success, failure) {
      Dokumentti.get({
        tapa: 'get',
        id: perusteId
      }, function(res) {
        console.log(res);
      });
    }

    return {
      generoiPdf: generoiPdf,
      lataaPdf: lataaPdf
    };
  });
