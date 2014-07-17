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
/*global _, moment*/

angular.module('eperusteApp')
  .service('YleinenData', function YleinenData($rootScope, $translate, Arviointiasteikot, Notifikaatiot) {
    this.dateOptions = {
      'year-format': 'yy',
      //'month-format': 'M',
      //'day-format': 'd',
      'starting-day': 1
    };

    this.naviOmit = ['root', 'editoi', 'suoritustapa', 'sisalto', 'aloitussivu', 'selaus', 'esitys'];

    // TODO: poista joskus
    this.loremIpsum = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.';

    this.kontekstit = ['ammatillinenperuskoulutus',
      'ammatillinenaikuiskoulutus'];

    this.rakenneRyhmaRoolit = [
      'määritelty',
      'määrittelemätön'
    ];

    this.yksikot = [
      'OSAAMISPISTE',
      'OPINTOVIIKKO',
    ];
    this.yksikotMap = {
      osp: 'OSAAMISPISTE',
      ov: 'OPINTOVIIKKO',
    };

    $rootScope.
      this.kontekstit = [
        'ammatillinenperuskoulutus',
        'ammatillinenaikuiskoulutus'
      ];

      this.tilakuvaukset = [
        'laadinta',
        'kommentointi',
        'viimeistely',
        'kaannos',
        'hyvaksytty'
      ];

    this.suoritustavat = [
      'ops',
      'naytto'
    ];

    this.koulutustyypit = [
      'koulutustyyppi_1',
      'koulutustyyppi_11',
      'koulutustyyppi_12'
    ];

    this.kielet = {
      'suomi': 'fi',
      'ruotsi': 'sv'
    };

    this.kieli = 'fi';

    this.arviointiasteikot = undefined;

    this.defaultItemsInModal = 10;

    this.dateFormatDatepicker = 'd.M.yyyy';
    this.dateFormatMomentJS = 'D.M.YYYY';

    this.haeArviointiasteikot = function() {
      if (this.arviointiasteikot === undefined) {
        var self = this;
        Arviointiasteikot.query({}, function(tulos) {

          self.arviointiasteikot = _.indexBy(tulos, 'id');
          $rootScope.$broadcast('arviointiasteikot');

        }, Notifikaatiot.serverCb);

      } else {
        $rootScope.$broadcast('arviointiasteikot');
      }
    };

    this.lisääKontekstitPerusteisiin = function(perusteet) {
      if (perusteet) {
        for (var i = 0; i < perusteet.length; i++) {
          switch (perusteet[i].tutkintokoodi)
          {
            case '1':
              perusteet[i].konteksti = this.kontekstit[0];
              break;
            case '2':
              perusteet[i].konteksti = this.kontekstit[1];
              break;
            case '3':
              perusteet[i].konteksti = this.kontekstit[1];
              break;
          }
        }
      }
    };

    this.vaihdaKieli = function(kielikoodi) {
      var löytyi = false;
      for (var avain in this.kielet) {
        if (this.kielet.hasOwnProperty(avain) && this.kielet[avain] === kielikoodi) {
          löytyi = true;
          break;
        }
      }
      // Jos kielikoodi ei löydy listalta niin käytetään suomea.
      if (!löytyi) {
        kielikoodi = 'fi';
      }
      if (this.kielikoodi !== kielikoodi) {
        moment.lang(kielikoodi);
        $translate.use(kielikoodi);
        this.kieli = kielikoodi;
        $rootScope.$broadcast('notifyCKEditor');
      }
    };

    this.valitseKieli = function(teksti) {

      if (teksti && teksti.hasOwnProperty(this.kieli)) {
        return teksti[this.kieli];
      } else {
        return '';
      }

    };

  });
