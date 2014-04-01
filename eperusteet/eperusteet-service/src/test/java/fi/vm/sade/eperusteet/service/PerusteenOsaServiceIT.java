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
package fi.vm.sade.eperusteet.service;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import fi.vm.sade.eperusteet.domain.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.repository.ArviointiRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;

/**
 *
 * @author teele1
 */
@Transactional
public class PerusteenOsaServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaService perusteenOsaService;
    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;
    @Autowired
    private ArviointiRepository arviointiRepository;
    @PersistenceContext
    private EntityManager em;

    private ArviointiAsteikko arviointiasteikko;

    @Before
    public void setUp() {
        TekstiPalanen osaamistasoOtsikko = new TekstiPalanen(Collections.singletonMap(Kieli.FI, "otsikko"));
        em.persist(osaamistasoOtsikko);

        Osaamistaso osaamistaso = new Osaamistaso();
        osaamistaso.setId(1L);
        osaamistaso.setOtsikko(osaamistasoOtsikko);

        em.persist(osaamistaso);

        ArviointiAsteikko arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setId(1L);
        arviointiasteikko.setOsaamistasot(Lists.newArrayList(osaamistaso));

        em.persist(arviointiasteikko);
        this.arviointiasteikko = arviointiasteikko;
        em.flush();
    }

    @Test
    @Rollback(true)
    public void testSaveWithArviointi() {
        TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
        tutkinnonOsa.setNimi(TestUtils.tekstiPalanenOf(Kieli.FI, "Nimi"));
        tutkinnonOsa.setArviointi(TestUtils.createArviointi(arviointiasteikko));
        tutkinnonOsa = (TutkinnonOsa) perusteenOsaRepository.saveAndFlush(tutkinnonOsa);
        List<PerusteenOsaDto> perusteenOsat = perusteenOsaService.getAll();

        Assert.assertNotNull(perusteenOsat);
        Assert.assertEquals(1, perusteenOsat.size());

        Assert.assertTrue(TutkinnonOsaDto.class.isInstance(perusteenOsat.get(0)));
        TutkinnonOsaDto to = (TutkinnonOsaDto) perusteenOsat.get(0);
        to.getArviointi();
        Assert.assertNotNull(tutkinnonOsa.getArviointi());
    }

    @Test(expected = ConstraintViolationException.class)
    @Rollback(true)
    public void testWithInvalidHtml() {
    	TekstiKappaleDto dto = new TekstiKappaleDto();
    	dto.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "<i>otsikko</i>")));
    	
    	perusteenOsaService.save(dto, TekstiKappaleDto.class, TekstiKappale.class);
    }
}
