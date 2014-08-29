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
package fi.vm.sade.eperusteet.resource;

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.util.BooleanDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author harrik
 */
@Controller
@RequestMapping("/perusteprojektit")
@Api(value = "perusteprojektit", description = "Perusteprojektien hallinta")
public class PerusteprojektiController {

    @Autowired
    private PerusteprojektiService service;

    @RequestMapping(value = "/info", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteprojektiInfoDto>> getAll() {
        return new ResponseEntity<>(service.getBasicInfo(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> get(@PathVariable("id") final long id) {
        PerusteprojektiDto t = service.get(id);
        if (t == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/jasenet", method = GET)
    @ResponseBody
    public ResponseEntity<List<KayttajanTietoDto>> getJasenet(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getJasenet(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/jasenet/tiedot", method = GET)
    @ResponseBody
    public ResponseEntity<List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>>> getJasenetTiedot(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getJasenetTiedot(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tilat", method = GET)
    @ResponseBody
    public ResponseEntity<Set<ProjektiTila>> getTilat(@PathVariable("id") final long id) {
        return new ResponseEntity<>(service.getTilat(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PerusteprojektiDto update(@PathVariable("id") final long id, @RequestBody PerusteprojektiDto perusteprojektiDto) {
        perusteprojektiDto = service.update(id, perusteprojektiDto);
        return perusteprojektiDto;
    }

    @RequestMapping(value = "/{id}/tila/{tila}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public TilaUpdateStatus updateTila(@PathVariable("id") final long id, @PathVariable("tila") final String tila) {
        return service.updateTila(id, ProjektiTila.of(tila));
    }

    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<PerusteprojektiDto> add(@RequestBody PerusteprojektiLuontiDto perusteprojektiLuontiDto, UriComponentsBuilder ucb) {
        service.onkoDiaarinumeroKaytossa(perusteprojektiLuontiDto.getDiaarinumero());

        PerusteprojektiDto perusteprojektiDto = service.save(perusteprojektiLuontiDto);
        return new ResponseEntity<>(perusteprojektiDto, buildHeadersFor(perusteprojektiDto.getId(), ucb), HttpStatus.CREATED);
    }

    private HttpHeaders buildHeadersFor(Long id, UriComponentsBuilder ucb) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucb.path("/perusteprojektit/{id}").buildAndExpand(id).toUri());
        return headers;
    }

    @RequestMapping(value = "/diaarinumero/uniikki/{diaarinumero}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<BooleanDto> get(@PathVariable("diaarinumero") final String diaarinumero) {
        try {
            service.onkoDiaarinumeroKaytossa(diaarinumero);
        } catch (BusinessRuleViolationException ex) {
            return new ResponseEntity<>(new BooleanDto(false), HttpStatus.OK);
        }

        return new ResponseEntity<>(new BooleanDto(true), HttpStatus.OK);
    }
}
