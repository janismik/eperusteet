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
package fi.vm.sade.eperusteet.resource.peruste;

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenSisaltoViiteDto;
import fi.vm.sade.eperusteet.resource.util.PerusteenOsaMappings;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 *
 * @author jhyoty
 */
@RestController
@RequestMapping("/perusteet/{perusteId}/suoritustavat/{suoritustapa}")
@Api(value = "Perusteet", description = "Perusteiden suoritustavan hallintaan liittyvät operaatiot")
public class PerusteenSisaltoController {

    @Autowired
    private PerusteService service;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    /**
     * Luo perusteeseen suoritustavan alle uuden perusteenosan
     *
     * @param perusteId
     * @param suoritustapa
     * @param dto perusteenosaviite (valinnainen, luodaan tyhjänä jos puuttuu)
     * @return Luodun perusteenOsaViite entityReferencen
     */
    @RequestMapping(value = "/sisalto", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public PerusteenSisaltoViiteDto addSisalto(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody(required = false) final PerusteenSisaltoViiteDto dto
    ) {
        PerusteenSisaltoViiteDto viite = service.addSisalto(perusteId, Suoritustapakoodi.of(suoritustapa), null);
        if (dto != null && dto.getPerusteenOsaDto() != null) {
            Long id = Long.valueOf(viite.getPerusteenOsa().getId());
            perusteenOsaService.lock(id);
            try {
                dto.getPerusteenOsaDto().setId(id);
                perusteenOsaService.update(dto.getPerusteenOsaDto());
            } finally {
                perusteenOsaService.unlock(id);
            }
        }
        return viite;
    }

    @RequestMapping(value = "/sisalto", method = PUT)
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisaltoViite(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @RequestBody PerusteenSisaltoViiteDto sisaltoViite) {
        return new ResponseEntity<>(service.addSisalto(perusteId, Suoritustapakoodi.of(suoritustapa), sisaltoViite), HttpStatus.CREATED);
    }

//    @RequestMapping(value = "/{perusteId}/suoritustavat/{suoritustapa}/sisalto/{perusteenosaViiteId}/kloonaa", method = POST)
//    @ResponseBody
//    public PerusteenOsaViiteDto kloonaa(
//            @PathVariable("perusteId") final Long perusteId,
//            @PathVariable("suoritustapa") final String suoritustapa,
//            @PathVariable("perusteenosaViiteId") final Long id) {
//        PerusteenOsaViiteDto re = PerusteenOsaViiteService.kloonaa(perusteId, suoritustapa, id);
//        return re;
//    }
    @RequestMapping(value = "/sisalto/{perusteenosaViiteId}/lapsi", method = POST)
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisaltoLapsi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @PathVariable("perusteenosaViiteId") final Long perusteenosaViiteId) {
        return new ResponseEntity<>(service.addSisaltoLapsi(perusteId, perusteenosaViiteId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/sisalto/{parentId}/lapsi/{childId}", method = POST)
    public ResponseEntity<PerusteenSisaltoViiteDto> addSisaltoLapsi(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapa,
        @PathVariable("parentId") final Long parentId,
        @PathVariable("childId") final Long childId) {
        return new ResponseEntity<>(service.attachSisaltoLapsi(perusteId, parentId, childId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/sisalto", method = GET)
    public ResponseEntity<PerusteenOsaViiteDto> getSuoritustapaSisalto(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("suoritustapa") final String suoritustapakoodi) {

        PerusteenOsaViiteDto dto = service.getSuoritustapaSisalto(perusteId, Suoritustapakoodi.of(suoritustapakoodi));
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/sisalto/{id}", method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSisaltoViite(@PathVariable("id") final Long id) {
        perusteenOsaViiteService.removeSisalto(id);
    }

    @RequestMapping(value = "/sisalto/{id}", method = POST)
    public void updateSisaltoViite(
        @PathVariable("id") final Long id,
        @RequestBody final PerusteenOsaViiteDto pov) {
        perusteenOsaViiteService.update(id, pov);
    }

    @RequestMapping(value = "/sisalto/{id}/muokattavakopio", method = POST, params = PerusteenOsaMappings.IS_TEKSTIKAPPALE_PARAM)
    public PerusteenOsaViiteDto kloonaaTekstiKappale(
        @PathVariable("perusteId") final Long perusteId,
        @PathVariable("id") final Long id) {
        return perusteenOsaViiteService.kloonaaTekstiKappale(id);
    }

}
