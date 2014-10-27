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

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.ArrayList;
import java.util.Collections;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 *
 * @author harrik
 */
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PerusteenOsaViiteIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaViiteService service;
    @Autowired
    private PerusteenOsaViiteRepository repo;
    @PersistenceContext
    private EntityManager em;

    private Long perusteId = null;
    private Long juuriId = null;
    private Long lapsiId = null;
    private Long lapsenlapsiId = null;
    private Long tekstikappaleId = null;
    private Long tekstikappaleLapsenlapsiId = null;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Luodaan juuri viite. Juurelle lapsi, millä on tekstikappale sekä lapsenlapsi, millä on tekstikappale.
     */
    @Before
    public void setUp() {
        Peruste peruste = new Peruste();
        peruste.setTila(PerusteTila.LUONNOS);
        em.persist(peruste);
        perusteId = peruste.getId();

        Suoritustapa suoritustapa = new Suoritustapa();
        suoritustapa.setSuoritustapakoodi(Suoritustapakoodi.NAYTTO);
        em.persist(suoritustapa);
        peruste.setSuoritustavat(Collections.singleton(suoritustapa));

        PerusteenOsaViite juuri = new PerusteenOsaViite();
        juuri.setLapset(new ArrayList<PerusteenOsaViite>() );
        juuri.setVanhempi(null);


        juuri = repo.save(juuri);
        juuriId = juuri.getId();
        suoritustapa.setSisalto(juuri);

        PerusteenOsaViite lapsi = new PerusteenOsaViite();
        TekstiKappale tekstikappale = new TekstiKappale();
        tekstikappale.setTila(PerusteTila.LUONNOS);
        em.persist(tekstikappale);

        tekstikappaleId = tekstikappale.getId();
        lapsi.setPerusteenOsa(tekstikappale);
        lapsi.setVanhempi(juuri);
        lapsi.setLapset(new ArrayList<PerusteenOsaViite>());
        lapsi = repo.save(lapsi);
        lapsiId = lapsi.getId();

        PerusteenOsaViite lapsenlapsi = new PerusteenOsaViite();
        tekstikappale = new TekstiKappale();
        tekstikappale.setTila(PerusteTila.LUONNOS);
        em.persist(tekstikappale);

        tekstikappaleLapsenlapsiId = tekstikappale.getId();
        lapsenlapsi.setPerusteenOsa(tekstikappale);
        lapsenlapsi.setVanhempi(lapsi);
        lapsenlapsi.setLapset(new ArrayList<PerusteenOsaViite>());
        lapsenlapsi = repo.save(lapsenlapsi);
        lapsenlapsiId = lapsenlapsi.getId();

        lapsi.getLapset().add(lapsenlapsi);
        juuri.getLapset().add(lapsi);

        em.flush();
    }

    @Test
    @Rollback(true)
    public void testRemoveSisaltoOK() {
        service.removeSisalto(perusteId, Suoritustapakoodi.NAYTTO, lapsenlapsiId);
        assertNotEquals(null, repo.findOne(juuriId));
        assertNotEquals(null, repo.findOne(lapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleId));
        assertEquals(null, repo.findOne(lapsenlapsiId));
        assertEquals(null, em.find(TekstiKappale.class, tekstikappaleLapsenlapsiId));
    }

    @Test
    @Rollback(true)
    public void testRemoveSisaltoJuuri() throws BusinessRuleViolationException {
        thrown.expect(BusinessRuleViolationException.class);
	thrown.expectMessage("Suoritustavan juurielementtiä ei voi poistaa");
        service.removeSisalto(perusteId, Suoritustapakoodi.NAYTTO, juuriId);
        assertNotEquals(null, repo.findOne(juuriId));
        assertNotEquals(null, repo.findOne(lapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleId));
        assertNotEquals(null, repo.findOne(lapsenlapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleLapsenlapsiId));
    }

    @Test
    @Rollback(true)
    public void testRemoveSisaltoLapsia() throws BusinessRuleViolationException {
        thrown.expect(BusinessRuleViolationException.class);
	thrown.expectMessage("Sisällöllä on lapsia, ei voida poistaa");
        service.removeSisalto(perusteId, Suoritustapakoodi.NAYTTO, lapsiId);
        assertNotEquals(null, repo.findOne(juuriId));
        assertNotEquals(null, repo.findOne(lapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleId));
        assertNotEquals(null, repo.findOne(lapsenlapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleLapsenlapsiId));
    }

}
