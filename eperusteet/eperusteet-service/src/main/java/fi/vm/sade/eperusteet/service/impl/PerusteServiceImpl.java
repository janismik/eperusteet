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
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkintonimikeKoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.Osaamisala;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusopetusPerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.PageDto;
import fi.vm.sade.eperusteet.dto.util.TutkinnonOsaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.OsaamisalaRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.RakenneRepository;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.TutkintonimikeKoodiRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.TutkinnonOsaViiteService;
import fi.vm.sade.eperusteet.service.event.PerusteUpdatedEvent;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.internal.SuoritustapaService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional
public class PerusteServiceImpl implements PerusteService, ApplicationListener<PerusteUpdatedEvent> {

    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String KOODISTO_RELAATIO_YLA = "relaatio/sisaltyy-ylakoodit/";
    private static final String KOODISTO_RELAATIO_ALA = "relaatio/sisaltyy-alakoodit/";
    private static final String[] AMMATILLISET_KOULUTUSTYYPPI_URIT = {"koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"};
    private static final String PERUSOPETUKSEN_KOULUTUSTYYPPI = "koulutustyyppi_16";
    private static final String ESIOPETUKSEN_KOULUTUSTYYPPI = "koulutustyyppi_15";
    private static final String KOULUTUSALALUOKITUS = "koulutusalaoph2002";
    private static final String OPINTOALALUOKITUS = "opintoalaoph2002";

    private static final List<String> ERIKOISTAPAUKSET = new ArrayList<>(Arrays.asList(new String[]{"koulutus_357802",
        "koulutus_327110", "koulutus_354803", "koulutus_324111", "koulutus_354710",
        "koulutus_324125", "koulutus_357709", "koulutus_327124", "koulutus_355904", "koulutus_324129", "koulutus_358903",
        "koulutus_327127",
        "koulutus_355412", "koulutus_324126", "koulutus_355413", "koulutus_324127", "koulutus_358412", "koulutus_327126",
        "koulutus_354708",
        "koulutus_324123", "koulutus_357707", "koulutus_327122"}));

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private KoulutusRepository koulutusRepo;

    @Autowired
    private SuoritustapaService suoritustapaService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private TutkinnonOsaViiteService tutkinnonOsaViiteService;

    @Autowired
    PerusteenOsaViiteRepository perusteenOsaViiteRepo;

    @Autowired
    OsaamisalaRepository osaamisalaRepo;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    @Koodisto
    private DtoMapper koodistoMapper;

    @Autowired
    private SuoritustapaRepository suoritustapaRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private TutkintonimikeKoodiRepository tutkintonimikeKoodiRepository;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    @Autowired
    private TekstiPalanenRepository tekstiPalanenRepository;

    @Autowired
    private LockManager lockManager;

    @Autowired
    private RakenneRepository rakenneRepository;

    @Autowired
    private PerusopetuksenPerusteenSisaltoService perusopetuksenSisaltoService;

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteDto> getAll(PageRequest page, String kieli) {
        return findBy(page, new PerusteQuery());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteInfoDto> getAllInfo() {
        return mapper.mapAsList(perusteet.findAll(), PerusteInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteInfoDto> getAllPerusopetusInfo() {
        List<Peruste> res = new ArrayList<>();
        List<Peruste> perusopetus = perusteet.findAllByKoulutustyyppi(KoulutusTyyppi.PERUSOPETUS.toString());
        for (Peruste p : perusopetus) {
            if (p.getTila() == PerusteTila.VALMIS) {
                res.add(p);
            }
        }

        return mapper.mapAsList(res, PerusteInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteDto> findBy(PageRequest page, PerusteQuery pquery) {
        Page<Peruste> result = perusteet.findBy(page, pquery);
        return new PageDto<>(result, PerusteDto.class, page, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery) {
        Page<Peruste> result = perusteet.findBy(page, pquery);
        return new PageDto<>(result, PerusteInfoDto.class, page, mapper);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto get(final Long id) {
        Peruste p = perusteet.findById(id);
        return mapper.map(p, PerusteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteInfoDto getByDiaari(Diaarinumero diaarinumero) {
        Peruste p = perusteet.findByDiaarinumero(diaarinumero);
        return mapper.map(p, PerusteInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteKaikkiDto getKokoSisalto(final Long id) {
        Peruste peruste = perusteet.findById(id);
        if ( peruste == null ) {
            return null;
        }

        PerusteKaikkiDto perusteDto = mapper.map(peruste, PerusteKaikkiDto.class);
        Set<TutkinnonOsa> tutkinnonOsat = new LinkedHashSet<>();

        for (Suoritustapa st : peruste.getSuoritustavat()) {
            for (TutkinnonOsaViite t : st.getTutkinnonOsat()) {
                tutkinnonOsat.add(t.getTutkinnonOsa());
            }
        }

        perusteDto.setTutkinnonOsat(mapper.mapAsList(tutkinnonOsat, TutkinnonOsaDto.class));
        return perusteDto;
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteDto getByIdAndSuoritustapa(final Long id, Suoritustapakoodi suoritustapakoodi) {
        Peruste p = perusteet.findPerusteByIdAndSuoritustapakoodi(id, suoritustapakoodi);
        return mapper.map(p, PerusteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends PerusteenOsaViiteDto.Puu<?,?>> T getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi, Class<T> view) {
        PerusteenOsaViite entity = perusteet.findSisaltoByIdAndSuoritustapakoodi(perusteId, suoritustapakoodi);
        return mapper.map(entity, view);
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends PerusteenOsaViiteDto.Puu<?,?>> T getSuoritustapaSisaltoUUSI(Long perusteId, Suoritustapakoodi suoritustapakoodi, Class<T> view) {

        Peruste peruste = perusteet.findOne(perusteId);
        if (peruste == null) {
            throw new BusinessRuleViolationException("Perustetta ei ole olemassa");
        }
        return mapper.map(peruste.getSisalto(suoritustapakoodi), view);
    }

    @Override
    @Transactional
    public void onApplicationEvent(PerusteUpdatedEvent event) {
        Peruste peruste = perusteet.findById(event.getPerusteId());
        if (peruste.getTila() == PerusteTila.VALMIS) {
            perusteet.setRevisioKommentti("perustetta muokattu");
            peruste.muokattu();
        }
        LOG.debug("EVENT " + event);
    }

    @Override
    public PerusteDto update(long id, PerusteDto perusteDto) {
        Peruste perusteVanha = perusteet.findById(id);
        if (perusteVanha == null) {
            throw new BusinessRuleViolationException("Päivitettävää perustetta ei ole olemassa");
        }

        perusteet.lock(perusteVanha);
        perusteDto.setId(id);

        Peruste peruste = mapper.map(perusteDto, Peruste.class);
        peruste = checkIfKoulutuksetAlreadyExists(peruste);
        peruste.setSuoritustavat(perusteVanha.getSuoritustavat());

        peruste = perusteet.save(peruste);
        return mapper.map(peruste, PerusteDto.class);
    }

    private Peruste checkIfKoulutuksetAlreadyExists(Peruste peruste) {
        Set<Koulutus> koulutukset = new HashSet<>();
        Koulutus koulutusTemp;

        if (peruste != null && peruste.getKoulutukset() != null) {
            for (Koulutus koulutus : peruste.getKoulutukset()) {
                koulutusTemp = koulutusRepo.findOneByKoulutuskoodiArvo(koulutus.getKoulutuskoodiArvo());
                if (koulutusTemp != null) {
                    koulutukset.add(koulutusTemp);
                } else {
                    koulutukset.add(koulutus);
                }
            }
            peruste.setKoulutukset(koulutukset);
        }
        return peruste;
    }

    @Override
    @Transactional(readOnly = true)
    public fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja getSuoritustapaSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        return getSuoritustapaSisalto(perusteId, suoritustapakoodi, PerusteenOsaViiteDto.Laaja.class);
    }

    @Override
    @Transactional(readOnly = true)
    public SuoritustapaDto getSuoritustapa(Long perusteId, Suoritustapakoodi suoritustapakoodi) {
        Suoritustapa entity = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        return mapper.map(entity, SuoritustapaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public RakenneModuuliDto getTutkinnonRakenne(Long perusteid, Suoritustapakoodi suoritustapakoodi, Integer eTag) {

        Long rakenneId = rakenneRepository.getRakenneIdWithPerusteAndSuoritustapa(perusteid, suoritustapakoodi);
        if (rakenneId == null) {
            throw new BusinessRuleViolationException("Rakennetta ei ole olemassa");
        }
        Integer rakenneVersioId = rakenneRepository.getLatestRevisionId(rakenneId);
        if (eTag != null && rakenneVersioId != null && rakenneVersioId.equals(eTag)) {
            return null;
        }

        RakenneModuuli rakenne = rakenneRepository.findOne(rakenneId);
        RakenneModuuliDto rakenneModuuliDto = mapper.map(rakenne, RakenneModuuliDto.class);
        rakenneModuuliDto.setVersioId(rakenneVersioId);
        return rakenneModuuliDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Revision> getRakenneVersiot(Long id, Suoritustapakoodi suoritustapakoodi) {
        List<Revision> versiot = new ArrayList<>();

        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            throw new EntityNotFoundException("Perustetta ei löytynyt id:llä: " + id);
        }
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new EntityNotFoundException("Suoritustapaa " + suoritustapakoodi.toString() + " ei löytynyt");
        }
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            versiot = rakenneRepository.getRevisions(rakenne.getId());
        }

        return versiot;
    }

    @Transactional(readOnly = true)
    private RakenneModuuli haeRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        RakenneModuuli rakenneVersio = null;
        Peruste peruste = perusteet.findOne(id);
        if (peruste == null) {
            throw new EntityNotFoundException("Perustetta ei löytynyt id:llä: " + id);
        }
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        if (suoritustapa == null) {
            throw new EntityNotFoundException("Suoritustapaa " + suoritustapakoodi.toString() + " ei löytynyt");
        }
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            rakenneVersio = rakenneRepository.findRevision(rakenne.getId(), versioId);
        }
        return rakenneVersio;
    }

    @Override
    @Transactional(readOnly = true)
    public RakenneModuuliDto getRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        return mapper.map(haeRakenneVersio(id, suoritustapakoodi, versioId), RakenneModuuliDto.class);
    }

    @Transactional(readOnly = true)
    private void haeTutkinnonOsaViitteetRakenteesta(AbstractRakenneOsa rakenne, Set<TutkinnonOsaViite> tovat) {
        if (rakenne instanceof RakenneModuuli) {
            for (AbstractRakenneOsa lapsi : ((RakenneModuuli) rakenne).getOsat()) {
                haeTutkinnonOsaViitteetRakenteesta(lapsi, tovat);
            }
        } else if (rakenne instanceof RakenneOsa) {
            TutkinnonOsaViite viite = ((RakenneOsa) rakenne).getTutkinnonOsaViite();
            tovat.add(viite);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PerusopetusPerusteKaikkiDto getPerusopetusKokoSisalto(Long id) {
        Peruste peruste = perusteet.findById(id);
        PerusopetusPerusteKaikkiDto pdto = mapper.map(peruste, PerusopetusPerusteKaikkiDto.class);
        return pdto;
    }

    @Override
    @Transactional
    public RakenneModuuliDto revertRakenneVersio(Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId) {
        RakenneModuuli rakenneVersio = haeRakenneVersio(id, suoritustapakoodi, versioId);
        Peruste peruste = perusteet.findOne(id);
        Set<TutkinnonOsaViite> rakenneTovat = new HashSet<>();
        haeTutkinnonOsaViitteetRakenteesta(rakenneVersio, rakenneTovat);

        Map<Long, TutkinnonOsaViite> tovat = new HashMap<>();
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        Set<TutkinnonOsaViite> tutkinnonOsat = suoritustapa.getTutkinnonOsat();
        for (TutkinnonOsaViite tov : tutkinnonOsat) {
            tovat.put(tov.getId(), tov);
        }

        for (TutkinnonOsaViite tov : rakenneTovat) {
            TutkinnonOsaViite utov = tovat.get(tov.getId());
            utov.setPoistettu(false);
        }
        return updateTutkinnonRakenne(id, suoritustapakoodi, mapper.map(rakenneVersio, RakenneModuuliDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutkinnonOsaViiteDto> getTutkinnonOsat(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        Peruste peruste = perusteet.findOne(perusteid);
        Suoritustapa suoritustapa = peruste.getSuoritustapa(suoritustapakoodi);
        return mapper.mapAsList(suoritustapa.getTutkinnonOsat(), TutkinnonOsaViiteDto.class);
    }

    @Value("${fi.vm.sade.eperusteet.tutkinnonrakenne.maksimisyvyys}")
    private int maxRakenneDepth;

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, UpdateDto<RakenneModuuliDto> rakenne) {
        RakenneModuuliDto updated = updateTutkinnonRakenne(perusteId, suoritustapakoodi, rakenne.getDto());
        if (rakenne.getMetadata() != null) {
            perusteet.setRevisioKommentti(rakenne.getMetadata().getKommentti());
        }
        return updated;
    }

    @Override
    @Transactional
    public RakenneModuuliDto updateTutkinnonRakenne(Long perusteId, Suoritustapakoodi suoritustapakoodi, RakenneModuuliDto rakenne) {

        Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        lockManager.ensureLockedByAuthenticatedUser(suoritustapa.getRakenne().getId());

        rakenne.foreach(new VisitorImpl(maxRakenneDepth));
        RakenneModuuli moduuli = mapper.map(rakenne, RakenneModuuli.class);

        if (!moduuli.isSame(suoritustapa.getRakenne())) {
            RakenneModuuli current = suoritustapa.getRakenne();
            moduuli = checkIfOsaamisalatAlreadyExists(moduuli);
            if (current != null) {
                current.mergeState(moduuli);
            } else {
                current = moduuli;
                suoritustapa.setRakenne(current);
            }
            rakenneRepository.save(current);
        }

        return mapper.map(moduuli, RakenneModuuliDto.class);
    }

    private RakenneModuuli checkIfOsaamisalatAlreadyExists(RakenneModuuli rakenneModuuli) {
        Osaamisala osaamisalaTemp;
        if (rakenneModuuli != null) {
            if (rakenneModuuli.getOsaamisala() != null && rakenneModuuli.getOsaamisala().getOsaamisalakoodiArvo() != null) {
                osaamisalaTemp = osaamisalaRepo.findOneByOsaamisalakoodiArvo(rakenneModuuli.getOsaamisala().getOsaamisalakoodiArvo());
                if (osaamisalaTemp != null) {
                    rakenneModuuli.setOsaamisala(osaamisalaTemp);
                } else {
                    rakenneModuuli.setOsaamisala(osaamisalaRepo.save(rakenneModuuli.getOsaamisala()));
                }
            } else {
                rakenneModuuli.setOsaamisala(null);
            }
            for (AbstractRakenneOsa osa : rakenneModuuli.getOsat()) {
                if (osa instanceof RakenneModuuli) {
                    osa = checkIfOsaamisalatAlreadyExists((RakenneModuuli) osa);
                }
            }
        }
        return rakenneModuuli;
    }

    @Override
    @Transactional
    public void removeTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, Long osaId) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        //varmistetaan että rakenteen muokkaus ei ole käynnissä.
        lockManager.lock(suoritustapa.getId());
        //workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa.
        suoritustapaRepository.lock(suoritustapa);
        try {
            TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osaId);
            if ( suoritustapa.getTutkinnonOsat().contains(viite) ) {
                //TODO: poistamisen refaktorointi (viitteen poisto oikeasti)
                viite.setPoistettu(true);
            } else {
                throw new BusinessRuleViolationException("Tutkinnonosa ei kuulu tähän suoritustapaan");
            }
        } finally {
            lockManager.unlock(suoritustapa.getId());
        }
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto addTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        //workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa.
        suoritustapaRepository.lock(suoritustapa);
        TutkinnonOsaViite viite = mapper.map(osa, TutkinnonOsaViite.class);

        if (viite.getTutkinnonOsa() == null && osa.getTutkinnonOsaDto() != null) {
            viite.setTutkinnonOsa(mapper.map(osa.getTutkinnonOsaDto(), TutkinnonOsa.class));
            viite.getTutkinnonOsa().setId(null);
        }

        if (viite.getTutkinnonOsa() == null) {
            TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
            if (osa.getTyyppi() != null) {
                tutkinnonOsa.setTyyppi(osa.getTyyppi());
            }
            viite.setTutkinnonOsa(tutkinnonOsa);
        }
        viite.setSuoritustapa(suoritustapa);
        viite.setMuokattu(new Date());
        if (suoritustapa.getTutkinnonOsat().add(viite)) {
            viite = tutkinnonOsaViiteRepository.save(viite);
        } else {
            throw new BusinessRuleViolationException("Viite tutkinnon osaan on jo olemassa");
        }
        return mapper.map(viite, TutkinnonOsaViiteDto.class);

    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto attachTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        //workaround jolla estetään versiointiongelmat yhtäaikaisten muokkausten tapauksessa.
        suoritustapaRepository.lock(suoritustapa);
        TutkinnonOsaViite viite = mapper.map(osa, TutkinnonOsaViite.class);
        viite.setSuoritustapa(suoritustapa);
        viite.setMuokattu(new Date());
        viite.setPoistettu(Boolean.FALSE);
        if (suoritustapa.getTutkinnonOsat().add(viite)) {
            viite = tutkinnonOsaViiteRepository.save(viite);
        } else {
            throw new BusinessRuleViolationException("Viite tutkinnon osaan on jo olemassa");
        }
        return mapper.map(viite, TutkinnonOsaViiteDto.class);
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto updateTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteDto osa) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(id, suoritustapakoodi);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(osa.getId());

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)
            || !viite.getTutkinnonOsa().getReference().equals(osa.getTutkinnonOsa())) {
            throw new BusinessRuleViolationException("Virheellinen viite");
        }

        return tutkinnonOsaViiteService.update(osa);
    }

    @Override
    @Transactional
    public TutkinnonOsaViiteDto updateTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi, TutkinnonOsaViiteUpdateDto osa) {
        TutkinnonOsaViiteDto updated = updateTutkinnonOsa(id, suoritustapakoodi, osa.getDto());

        if (osa.getMetadata() != null) {
            tutkinnonOsaViiteRepository.setRevisioKommentti(osa.getMetadata().getKommentti());
        }
        return updated;

    }

    @Override
    @Transactional(readOnly = true)
    public TutkinnonOsaViiteDto getTutkinnonOsaViite(Long perusteId, Suoritustapakoodi suoritustapakoodi, Long viiteId) {
        final Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        TutkinnonOsaViite viite = tutkinnonOsaViiteRepository.findOne(viiteId);

        if (viite == null || !viite.getSuoritustapa().equals(suoritustapa)) {
            throw new BusinessRuleViolationException("Virheellinen viiteId");
        }

        TutkinnonOsaViiteDto viiteDto = mapper.map(viite, TutkinnonOsaViiteDto.class);
        TutkinnonOsaDto tutkinnonOsaDto = mapper.map(viite.getTutkinnonOsa(), TutkinnonOsaDto.class);
        viiteDto.setTutkinnonOsaDto(tutkinnonOsaDto);

        return viiteDto;
    }

    private Suoritustapa getSuoritustapaEntity(Long perusteid, Suoritustapakoodi suoritustapakoodi) {
        if (!perusteet.exists(perusteid)) {
            throw new BusinessRuleViolationException("Perustetta ei ole olemassa");
        }
        Suoritustapa suoritustapa = suoritustapaRepository.findByPerusteAndKoodi(perusteid, suoritustapakoodi);
        if (suoritustapa == null) {
            throw new BusinessRuleViolationException("Perusteella " + perusteid + " + ei ole suoritustapaa "
                + suoritustapa);
        }
        return suoritustapa;
    }

    @Autowired
    EntityManager em;

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite) {
        Suoritustapa suoritustapa = getSuoritustapaEntity(perusteId, suoritustapakoodi);
        if (suoritustapa == null) {
            throw new BusinessRuleViolationException("Suoritustapaa ei ole");
        }
        return perusteenOsaViiteService.addSisalto(perusteId, suoritustapa.getSisalto().getId(), viite);
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisaltoUUSI(Long perusteId, Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite) {
        Peruste peruste = perusteet.findOne(perusteId);
        if (peruste == null) {
            throw new BusinessRuleViolationException("Perustetta ei ole olemassa");
        }
        PerusteenOsaViite sisalto = peruste.getSisalto(suoritustapakoodi);
        if (sisalto == null) {
            throw new BusinessRuleViolationException("Perusteen sisältörakennetta ei ole olemassa");
        }
        return perusteenOsaViiteService.addSisalto(perusteId, sisalto.getId(), viite);
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisaltoLapsi(Long perusteId, Long perusteenosaViiteId, PerusteenOsaViiteDto.Matala viite) {
        return perusteenOsaViiteService.addSisalto(perusteId, perusteenosaViiteId, viite);
    }

    private void getLocksPerusteenOsat(PerusteenOsaViite sisalto, Map<Long, LukkoDto> map) {
        for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
            LukkoDto lock = perusteenOsaService.getLock(lapsi.getPerusteenOsa().getId());
            if (lock != null) {
                map.put(lapsi.getPerusteenOsa().getId(), lock);
            }
        }
    }

    @Override
    public List<TutkintonimikeKoodiDto> getTutkintonimikeKoodit(Long perusteId) {
        List<TutkintonimikeKoodi> koodit = tutkintonimikeKoodiRepository.findByPerusteId(perusteId);
        return mapper.mapAsList(koodit, TutkintonimikeKoodiDto.class);
    }

    @Override
    public TutkintonimikeKoodiDto addTutkintonimikeKoodi(Long perusteId, TutkintonimikeKoodiDto dto) {
        Peruste peruste = perusteet.findOne(perusteId);
        dto.setPeruste(peruste.getReference());
        TutkintonimikeKoodi tnk = mapper.map(dto, TutkintonimikeKoodi.class);
        TutkintonimikeKoodi saved = tutkintonimikeKoodiRepository.save(tnk);
        return mapper.map(saved, TutkintonimikeKoodiDto.class);
    }

    @Override
    public void removeTutkintonimikeKoodi(Long perusteId, Long tutkintonimikeKoodiId) {
        TutkintonimikeKoodi tnk = tutkintonimikeKoodiRepository.findOne(tutkintonimikeKoodiId);
        if (Objects.equals(tnk.getPeruste().getId(), perusteId)) {
            tutkintonimikeKoodiRepository.delete(tutkintonimikeKoodiId);
        }
    }

    private void lisaaTutkinnonMuodostuminen(Peruste peruste) {
        if (KoulutusTyyppi.PERUSOPETUS.toString().equals(peruste.getKoulutustyyppi())) {
            return;
        }

        for (Suoritustapa st : peruste.getSuoritustavat()) {
            PerusteenOsaViite sisalto = st.getSisalto();
            List<PerusteenOsaViite> lapset = sisalto.getLapset();
            TekstiKappale tk = new TekstiKappale();
            HashMap<Kieli, String> hm = new HashMap<>();
            hm.put(Kieli.FI, "Tutkinnon muodostuminen");
            tk.setNimi(tekstiPalanenRepository.save(TekstiPalanen.of(hm)));
            tk.setTunniste(PerusteenOsaTunniste.RAKENNE);
            PerusteenOsaViite pov = perusteenOsaViiteRepo.save(new PerusteenOsaViite());
            pov.setPerusteenOsa(perusteenOsaRepository.save(tk));
            pov.setVanhempi(sisalto);
            lapset.add(pov);
        }
    }

    /**
     * Luo uuden perusteen perusrakenteella.
     *
     * @param koulutustyyppi
     * @param yksikko
     * @param tyyppi
     * @return Palauttaa 'tyhjän' perusterungon
     */
    @Override
    public Peruste luoPerusteRunko(String koulutustyyppi, LaajuusYksikko yksikko, PerusteTyyppi tyyppi) {
        Peruste peruste = new Peruste();
        peruste.setKoulutustyyppi(koulutustyyppi);
        peruste.setTyyppi(tyyppi);
        Set<Suoritustapa> suoritustavat = new HashSet<>();

        if (Arrays.asList(AMMATILLISET_KOULUTUSTYYPPI_URIT).indexOf(koulutustyyppi) != -1) {
            suoritustavat.add(suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.NAYTTO, null));
        }

        if (koulutustyyppi != null) {
            Suoritustapa st = null;
            if (koulutustyyppi.equals(AMMATILLISET_KOULUTUSTYYPPI_URIT[0])) {
                st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.OPS, yksikko != null ? yksikko : LaajuusYksikko.OSAAMISPISTE);
            } else if (koulutustyyppi.equals(PERUSOPETUKSEN_KOULUTUSTYYPPI)) {
                peruste.setPerusopetuksenPerusteenSisalto(new PerusopetuksenPerusteenSisalto());
            } else if (koulutustyyppi.equals(ESIOPETUKSEN_KOULUTUSTYYPPI)) {
                peruste.setEsiopetuksenPerusteenSisalto(new EsiopetuksenPerusteenSisalto());
            }

            if (st != null) {
                suoritustavat.add(st);
            }
        }
        else {
            throw new BusinessRuleViolationException("Koulutustyyppiä ei ole asetettu");
        }

        peruste.setSuoritustavat(suoritustavat);
        perusteet.save(peruste);
        lisaaTutkinnonMuodostuminen(peruste);
        return peruste;
    }

    @Override
    public Peruste luoPerusteRunkoToisestaPerusteesta(PerusteprojektiLuontiDto luontiDto, PerusteTyyppi tyyppi) {
        Peruste vanha = perusteet.getOne(luontiDto.getPerusteId());
        Peruste peruste = new Peruste();
        peruste.setTyyppi(tyyppi);
        peruste.setKuvaus(vanha.getKuvaus());
        peruste.setNimi(vanha.getNimi());
        peruste.setKoulutustyyppi(vanha.getKoulutustyyppi());

        Set<Koulutus> vanhatKoulutukset = vanha.getKoulutukset();
        Set<Koulutus> koulutukset = new HashSet<>();

        if (vanhatKoulutukset != null) {
            for (Koulutus vanhaKoulutus : vanhatKoulutukset) {
                koulutukset.add(vanhaKoulutus);
            }
            peruste.setKoulutukset(koulutukset);
        }

        Set<Suoritustapa> suoritustavat = vanha.getSuoritustavat();
        Set<Suoritustapa> uudetSuoritustavat = new HashSet<>();

        for (Suoritustapa st : suoritustavat) {
            uudetSuoritustavat.add(suoritustapaService.createFromOther(st.getId()));
        }

        for (Suoritustapa st : uudetSuoritustavat) {
            st.setLaajuusYksikko(luontiDto.getLaajuusYksikko());
        }

        peruste.setSuoritustavat(uudetSuoritustavat);

        peruste = perusteet.save(peruste);
        lisaaTutkinnonMuodostuminen(peruste);
        return peruste;
    }

    private static class VisitorImpl implements AbstractRakenneOsaDto.Visitor {

        private final int maxDepth;

        public VisitorImpl(int maxDepth) {
            this.maxDepth = maxDepth;
        }

        @Override
        public void visit(final AbstractRakenneOsaDto dto, final int depth) {
            if (depth >= maxDepth) {
                throw new BusinessRuleViolationException("Tutkinnon rakennehierarkia ylittää maksimisyvyyden");
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(PerusteServiceImpl.class);

}
