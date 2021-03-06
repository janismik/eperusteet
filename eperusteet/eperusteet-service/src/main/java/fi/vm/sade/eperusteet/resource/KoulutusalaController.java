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

import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author jussini
 */
@Controller
@RequestMapping("/koulutusalat")
@InternalApi
public class KoulutusalaController {

    @Autowired
    private KoulutusalaService service;

    @RequestMapping(method = GET)
    @ResponseBody
    public List<KoulutusalaDto> getAll() {
        List<KoulutusalaDto> klist = service.getAll();
        return klist;
    }

    /*@RequestMapping(value = "/{id}", method = GET)
    @ResponseBody
    public ResponseEntity<KoulutusalaDto> get(@PathVariable("id") final Long id) {
        KoulutusalaDto k = service.get(id);
        if (k == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(k, HttpStatus.OK);
    }*/

}

