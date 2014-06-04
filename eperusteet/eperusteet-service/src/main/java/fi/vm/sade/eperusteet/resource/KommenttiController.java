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

import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.service.KommenttiService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author nkala
 */
@Controller
@RequestMapping("/kommentit")
public class KommenttiController {

    @Autowired
    KommenttiService service;

    @RequestMapping(value = "/perusteprojekti/{id}/suoritustapa/{suoritustapa}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KommenttiDto>> getAll(@PathVariable("id") final long id, @PathVariable("suoritustapa") final String suoritustapa) {
        List<KommenttiDto> t = service.getAllBySuoritustapa(id, suoritustapa);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteprojekti/{id}/perusteenosa/{perusteenOsaId}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KommenttiDto>> getAll(@PathVariable("id") final long id, @PathVariable("perusteenOsaId") final long perusteenOsaId) {
        List<KommenttiDto> t = service.getAllByPerusteenOsa(id, perusteenOsaId);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/perusteprojekti/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KommenttiDto>> getAll(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByPerusteprojekti(id);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/ylin/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KommenttiDto>> getAllByYlin(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByYlin(id);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/parent/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KommenttiDto>> getAllByParent(@PathVariable("id") final long id) {
        List<KommenttiDto> t = service.getAllByParent(id);
        return new ResponseEntity<>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<KommenttiDto> get(@PathVariable("id") final long id) {
        KommenttiDto t = service.get(id);
        return new ResponseEntity<>(t, t == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(method = {POST, PUT})
    @ResponseBody
    public ResponseEntity<KommenttiDto> add(@RequestBody KommenttiDto body) {
        return new ResponseEntity<>(service.add(body), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = {POST, PUT})
    @ResponseBody
    public ResponseEntity<KommenttiDto> update(@PathVariable("id") final long id, @RequestBody KommenttiDto body) {
        return new ResponseEntity<>(service.update(id, body), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    @ResponseBody
    public ResponseEntity delete(@PathVariable("id") final long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}