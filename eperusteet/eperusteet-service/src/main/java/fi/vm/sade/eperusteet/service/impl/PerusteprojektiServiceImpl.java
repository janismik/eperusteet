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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTyoryhma;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.PerusteprojektiTyoryhma;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaTyoryhmaRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiTyoryhmaRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.KoodistoService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.KayttajanTietoParser;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Validointi;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Service
public class PerusteprojektiServiceImpl implements PerusteprojektiService {

    private final String authQueryPath = "/resources/henkilo?count=9999&index=0&org=";

    @Value("${cas.service.authentication-service:''}")
    private String authServiceUrl;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @Autowired
    private RestClientFactory restClientFactory;

    @Autowired
    private PerusteprojektiTyoryhmaRepository perusteprojektiTyoryhmaRepository;

    @Autowired
    private PerusteenOsaTyoryhmaRepository perusteenOsaTyoryhmaRepository;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Autowired
    private KoodistoService koodistoService;

    @Override
    @Transactional(readOnly = true)
    public List<PerusteprojektiInfoDto> getBasicInfo() {
        return mapper.mapAsList(repository.findAll(), PerusteprojektiInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerusteprojektiInfoDto> getOmatProjektit() {
        return mapper.mapAsList(repository.findAllKeskeneraiset(), PerusteprojektiInfoDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PerusteprojektiDto get(Long id) {
        Perusteprojekti p = repository.findOne(id);
        return mapper.map(p, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttajanTietoDto> getJasenet(Long id) {
        CachingRestClient crc = restClientFactory.get(authServiceUrl);
        Perusteprojekti p = repository.findOne(id);
        List<KayttajanTietoDto> kayttajat = null;
        ObjectMapper omapper = new ObjectMapper();

        if (p == null || p.getRyhmaOid() == null | p.getRyhmaOid().isEmpty()) {
            throw new BusinessRuleViolationException("Perusteprojektilla ei ole oid:a");
        }

        try {
            String url = authServiceUrl + authQueryPath + p.getRyhmaOid();
            String json = crc.getAsString(url);
            kayttajat = KayttajanTietoParser.parsiKayttajat(omapper.readTree(json).get("results"));
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Käyttäjien tietojen hakeminen epäonnistui");
        }
        return kayttajat;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> getJasenetTiedot(Long id) {
        CachingRestClient crc = restClientFactory.get(authServiceUrl);
        Perusteprojekti p = repository.findOne(id);

        if (p == null || p.getRyhmaOid() == null || p.getRyhmaOid().isEmpty()) {
            throw new BusinessRuleViolationException("Perusteprojektilla ei ole oid:a");
        }

        List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> kayttajat = new ArrayList<>();

        try {
            String url = authServiceUrl + authQueryPath + p.getRyhmaOid();
            ObjectMapper omapper = new ObjectMapper();
            JsonNode tree = omapper.readTree(crc.getAsString(url));
            for (JsonNode node : tree.get("results")) {
                String oid = node.get("oidHenkilo").asText();
                CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto> combined = new CombinedDto<>(
                    kayttajanTietoService.hae(oid),
                    kayttajanTietoService.haePerusteprojekti(oid, id)
                );
                kayttajat.add(combined);
            }
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Käyttäjien tietojen hakeminen epäonnistui");
        }
        return kayttajat;
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto) {
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);

        String koulutustyyppi = perusteprojektiDto.getKoulutustyyppi();
        LaajuusYksikko yksikko = perusteprojektiDto.getLaajuusYksikko();
        PerusteTyyppi tyyppi = perusteprojektiDto.getTyyppi() == null ? PerusteTyyppi.NORMAALI : perusteprojektiDto.getTyyppi();
        perusteprojekti.setTila(ProjektiTila.LAADINTA);
        perusteprojekti.setRyhmaOid(perusteprojektiDto.getRyhmaOid());

        if (tyyppi != PerusteTyyppi.POHJA) {
            if (koulutustyyppi != null && koulutustyyppi.equals(KoulutusTyyppi.PERUSTUTKINTO.toString()) && yksikko == null) {
                throw new BusinessRuleViolationException("Opetussuunnitelmalla täytyy olla yksikkö");
            }
            if (perusteprojektiDto.getDiaarinumero() == null) {
                throw new BusinessRuleViolationException("Diaarinumeroa ei ole asetettu");
            }
            onkoDiaarinumeroKaytossa(new Diaarinumero(perusteprojektiDto.getDiaarinumero()));
        }

        if (perusteprojektiDto.getRyhmaOid() == null) {
            throw new BusinessRuleViolationException("Organisaatioryhmää ei ole asetettu");
        }

        Peruste peruste;
        if (perusteprojektiDto.getPerusteId() == null) {
            peruste = perusteService.luoPerusteRunko(koulutustyyppi, yksikko, tyyppi);
        }
        else {
            Peruste pohjaPeruste = perusteRepository.findOne(perusteprojektiDto.getPerusteId());
            perusteprojektiDto.setKoulutustyyppi(pohjaPeruste.getKoulutustyyppi());
            peruste = perusteService.luoPerusteRunkoToisestaPerusteesta(perusteprojektiDto, tyyppi);
        }

        if (tyyppi == PerusteTyyppi.POHJA) {
            TekstiPalanen pnimi = TekstiPalanen.of(Kieli.FI, perusteprojektiDto.getNimi());
            peruste.setNimi(pnimi);
        }

        perusteprojekti.setPeruste(peruste);
        perusteprojekti = repository.save(perusteprojekti);

        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public void onkoDiaarinumeroKaytossa(Diaarinumero diaarinumero) {
        if (repository.findOneByDiaarinumero(diaarinumero) != null) {
            throw new BusinessRuleViolationException("Diaarinumero on jo käytössä");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteprojektiDto update(Long id, PerusteprojektiDto perusteprojektiDto) {
        Perusteprojekti vanhaProjekti = repository.findOne(id);
        if (vanhaProjekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        perusteprojektiDto.setId(id);
        perusteprojektiDto.setTila(vanhaProjekti.getTila());
        Perusteprojekti perusteprojekti = mapper.map(perusteprojektiDto, Perusteprojekti.class);
        perusteprojekti = repository.save(perusteprojekti);
        return mapper.map(perusteprojekti, PerusteprojektiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ProjektiTila> getTilat(Long id) {
        Perusteprojekti p = repository.findOne(id);
        if (p == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        return p.getTila().mahdollisetTilat(p.getPeruste().getTyyppi());
    }

    @Transactional(readOnly = true)
    public void tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen, final Set<Kieli> pakolliset, Map<String, String> virheellisetKielet) {
        tarkistaTekstipalanen(nimi, palanen, pakolliset, virheellisetKielet, false);
    }

    @Transactional(readOnly = true)
    public void tarkistaTekstipalanen(final String nimi, final TekstiPalanen palanen, final Set<Kieli> pakolliset, Map<String, String> virheellisetKielet, boolean pakollinen) {
        if (palanen == null || palanen.getTeksti() == null) {
            return;
        }

        // Oispa lambdat
        boolean onJollainVaditullaKielella = false;
        if (!pakollinen) {
            for (Kieli kieli : pakolliset) {
                if (onJollainVaditullaKielella) {
                    break;
                }
                for (Entry<Kieli, String> osa : palanen.getTeksti().entrySet()) {
                    if (osa.getValue() != null && !osa.getValue().isEmpty()) {
                        onJollainVaditullaKielella = true;
                        break;
                    }
                }
            }
        }

        if (pakollinen || onJollainVaditullaKielella) {
            for (Kieli kieli : pakolliset) {
                Map<Kieli, String> teksti = palanen.getTeksti();
                if (!teksti.containsKey(kieli) || teksti.get(kieli) == null || teksti.get(kieli).isEmpty()) {
                    virheellisetKielet.put(nimi, kieli.name());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public void tarkistaSisalto(final PerusteenOsaViite viite, final Set<Kieli> pakolliset, Map<String, String> virheellisetKielet) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
        if (perusteenOsa instanceof TekstiKappale && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            TekstiKappale tekstikappale = (TekstiKappale)perusteenOsa;
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-nimi", tekstikappale.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-teksti", tekstikappale.getTeksti(), pakolliset, virheellisetKielet);
        }
        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, virheellisetKielet);
        }
    }

    @Transactional(readOnly = true)
    public void tarkistaRakenne(final AbstractRakenneOsa aosa, final Set<Kieli> pakolliset, Map<String, String> virheellisetKielet) {
        tarkistaTekstipalanen("peruste-validointi-rakenneosa-kuvaus", aosa.getKuvaus(), pakolliset, virheellisetKielet);
        if (aosa instanceof RakenneModuuli) {
            RakenneModuuli osa = (RakenneModuuli)aosa;
            for (AbstractRakenneOsa lapsi : osa.getOsat()) {
                tarkistaRakenne(lapsi, pakolliset, virheellisetKielet);
            }
        }
    }

    @Transactional(readOnly = true)
    public Map<String, String> tarkistaPerusteenTekstipalaset(Peruste peruste) {
        if (peruste.getTyyppi() == PerusteTyyppi.POHJA) {
            return new HashMap<>();
        }

        Set<Kieli> vaaditutKielet = peruste.getKielet();
        Map<String, String> virheellisetKielet = new HashMap<>();
        tarkistaTekstipalanen("peruste-validointi-kuvaus", peruste.getKuvaus(), vaaditutKielet, virheellisetKielet);
        tarkistaTekstipalanen("peruste-validointi-nimi", peruste.getNimi(), vaaditutKielet, virheellisetKielet);

        for (Koulutus koulutus : peruste.getKoulutukset()) {
            tarkistaTekstipalanen("peruste-validointi-koulutus-nimi", koulutus.getNimi(), vaaditutKielet, virheellisetKielet);
        }

        for (Suoritustapa st : peruste.getSuoritustavat()) {
            PerusteenOsaViite sisalto = st.getSisalto();
            for (PerusteenOsaViite lapsi : sisalto.getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, virheellisetKielet);
            }

            tarkistaRakenne(st.getRakenne(), vaaditutKielet, virheellisetKielet);

            for (TutkinnonOsaViite tov : st.getTutkinnonOsat()) {
                TutkinnonOsa tosa = tov.getTutkinnonOsa();
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaidon-osoittamistavat", tosa.getAmmattitaidonOsoittamistavat(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-ammattitaitovaatimukset", tosa.getAmmattitaitovaatimukset(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-kuvaus", tosa.getKuvaus(), vaaditutKielet, virheellisetKielet);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-nimi", tosa.getNimi(), vaaditutKielet, virheellisetKielet, true);
                tarkistaTekstipalanen("peruste-validointi-tutkinnonosa-tavoitteet", tosa.getTavoitteet(), vaaditutKielet, virheellisetKielet);
            }
        }

        return virheellisetKielet;
    }

    @Override
    @Transactional(readOnly = false)
    public TilaUpdateStatus updateTila(Long id, ProjektiTila tila, Date siirtymaPaattyy) {

        TilaUpdateStatus updateStatus = new TilaUpdateStatus();
        updateStatus.setVaihtoOk(true);

        Perusteprojekti projekti = repository.findOne(id);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + id);
        }

        // Tarkistetaan mahdolliset tilat
        updateStatus.setVaihtoOk(projekti.getTila().mahdollisetTilat(projekti.getPeruste().getTyyppi()).contains(tila));
        if ( !updateStatus.isVaihtoOk() ) {
            String viesti = "Tilasiirtymä tilasta '" + projekti.getTila().toString() + "' tilaan '" + tila.toString() + "' ei mahdollinen";
            updateStatus.addStatus(viesti);
            return updateStatus;
        }

        // Tarkistetaan että perusteella on nimi ainakin suomeksi
        TekstiPalanen nimi = projekti.getPeruste().getNimi();
        if (tila != ProjektiTila.POISTETTU && tila != ProjektiTila.LAADINTA
            && (nimi == null || !nimi.getTeksti().containsKey(Kieli.FI) || nimi.getTeksti().get(Kieli.FI).isEmpty())) {
            updateStatus.addStatus("perusteelta-puuttuu-nimi");
            updateStatus.setVaihtoOk(false);
        }

        Set<String> tutkinnonOsienKoodit = new HashSet<>();

        // Perusteen validointi
        if (projekti.getPeruste() != null && projekti.getPeruste().getSuoritustavat() != null
            && tila != ProjektiTila.LAADINTA) {
            Validointi validointi;

            for (Suoritustapa suoritustapa : projekti.getPeruste().getSuoritustavat()) {
                // Rakenteiden validointi
                if (suoritustapa.getRakenne() != null) {
                    validointi = PerusteenRakenne.validoiRyhma(suoritustapa.getRakenne());
                    if (!validointi.ongelmat.isEmpty()) {
                        updateStatus.addStatus("rakenteen-validointi-virhe", suoritustapa.getSuoritustapakoodi(), validointi);
                        updateStatus.setVaihtoOk(false);
                    }
                }

                // Vapaiden tutkinnon osien tarkistus
                List<TutkinnonOsaViite> vapaatOsat = vapaatTutkinnonosat(suoritustapa);
                if (!vapaatOsat.isEmpty()) {
                    List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                    for (TutkinnonOsaViite viite : vapaatOsat) {
                        if (viite.getTutkinnonOsa().getNimi() != null) {
                           nimet.add(new LokalisoituTekstiDto(viite.getTutkinnonOsa().getNimi().getId(), viite.getTutkinnonOsa().getNimi().getTeksti()));
                        }
                    }
                    updateStatus.addStatus("liittamattomia-tutkinnon-osia", suoritustapa.getSuoritustapakoodi(), nimet);
                    updateStatus.setVaihtoOk(false);
                }

                // Tarkistetaan koodittomat tutkinnon osat
                List<TutkinnonOsa> koodittomatTutkinnonOsat = koodittomatTutkinnonosat(suoritustapa);
                if (!koodittomatTutkinnonOsat.isEmpty()) {
                    List<LokalisoituTekstiDto> nimet = new ArrayList<>();
                    for (TutkinnonOsa tutkinnonOsa : koodittomatTutkinnonOsat) {
                        if (tutkinnonOsa.getNimi() != null) {
                           nimet.add(new LokalisoituTekstiDto(tutkinnonOsa.getNimi().getId(), tutkinnonOsa.getNimi().getTeksti()));
                        }
                    }
                    updateStatus.addStatus("koodittomia-tutkinnon-osia", suoritustapa.getSuoritustapakoodi(), nimet);
                    updateStatus.setVaihtoOk(false);
                }

                // Kerätään tutkinnon osien koodit
                List<LokalisoituTekstiDto> virheellisetKoodistonimet = new ArrayList<>();
                for (TutkinnonOsaViite tov : suoritustapa.getTutkinnonOsat()) {
                    TutkinnonOsa tosa = tov.getTutkinnonOsa();
                    String uri = tosa.getKoodiUri();
                    String arvo = tosa.getKoodiArvo();

                    if (arvo != null && uri != null && uri.isEmpty()) {
                        uri = "tutkinnonosa_" + arvo;
                        tosa.setKoodiUri(uri);
                    }

                    if (tosa.getNimi() != null && (uri != null && arvo != null && !uri.isEmpty() && !arvo.isEmpty())) {
                        KoodistoKoodiDto koodi = null;
                        try {
                            koodi = koodistoService.get("tutkinnonosat", uri);
                        } catch (Exception e) {
                        }
                        if (koodi != null && koodi.getKoodiUri().equals(uri)) {
                            tutkinnonOsienKoodit.add(tov.getTutkinnonOsa().getKoodiArvo());
                        }
                        else {
                            virheellisetKoodistonimet.add(new LokalisoituTekstiDto(tosa.getNimi().getId(), tosa.getNimi().getTeksti()));
                        }
                    }
                }
                if (!virheellisetKoodistonimet.isEmpty()) {
                    updateStatus.addStatus("tutkinnon-osan-asetettua-koodia-ei-koodistossa", suoritustapa.getSuoritustapakoodi(), virheellisetKoodistonimet);
                    updateStatus.setVaihtoOk(false);
                }
            }

            // Tarkistetaan perusteen tutkinnon osien koodien ja tutkintonimikkeiden yhteys
            List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = perusteService.getTutkintonimikeKoodit(projekti.getPeruste().getId());
            List<String> koodit = new ArrayList<>();
            for (TutkintonimikeKoodiDto tnk : tutkintonimikeKoodit) {
                if (tnk.getTutkinnonOsaArvo() != null) {
                    koodit.add(tnk.getTutkinnonOsaArvo());
                }
            }
            if (!tutkinnonOsienKoodit.containsAll(koodit)) {
                updateStatus.addStatus("tutkintonimikkeen-vaatimaa-tutkinnonosakoodia-ei-loytynyt-tutkinnon-osilta");
                updateStatus.setVaihtoOk(false);
            }

            // Tarkista että kaikki vaadittu kielisisältö on asetettu
            Map<String, String> lokalisointivirheet = tarkistaPerusteenTekstipalaset(projekti.getPeruste());
            for (Entry<String, String> entry : lokalisointivirheet.entrySet()) {
                updateStatus.setVaihtoOk(false);
                updateStatus.addStatus(entry.getKey());
            }

            if (tila == ProjektiTila.JULKAISTU) {
                if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
                    updateStatus.addStatus("peruste-ei-voimassaolon-alkamisaikaa");
                    updateStatus.setVaihtoOk(false);
                } else {
                    updateStatus = korvaaPerusteet(projekti.getPeruste(), siirtymaPaattyy, updateStatus);
                }

                if (projekti.getPeruste().getDiaarinumero() == null) {
                    updateStatus.addStatus("peruste-ei-diaarinumeroa");
                    updateStatus.setVaihtoOk(false);
                }
            }

        }

        // Perusteen tilan muutos
        if ( !updateStatus.isVaihtoOk() ) {
            return updateStatus;
        }

        if (tila == ProjektiTila.JULKAISTU && projekti.getTila() == ProjektiTila.VALMIS) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
        }

        if (tila == ProjektiTila.POISTETTU) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.POISTETTU);
        }

        if (tila == ProjektiTila.LAADINTA && projekti.getTila() == ProjektiTila.POISTETTU) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.LUONNOS);
        }

        if (projekti.getPeruste().getTyyppi() == PerusteTyyppi.POHJA && tila == ProjektiTila.VALMIS
            && projekti.getTila() == ProjektiTila.LAADINTA) {
            setPerusteTila(projekti.getPeruste(), PerusteTila.VALMIS);
        }

        projekti.setTila(tila);
        repository.save(projekti);
        return updateStatus;
    }

    private void setPerusteTila(Peruste peruste, PerusteTila tila) {

        for (Suoritustapa suoritustapa : peruste.getSuoritustavat()) {
            setSisaltoTila(suoritustapa.getSisalto(), tila);
            for (TutkinnonOsaViite tutkinnonosaViite : suoritustapa.getTutkinnonOsat()) {
                setOsatTila(tutkinnonosaViite, tila);
            }
        }

        if (peruste.getPerusopetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste.getPerusopetuksenPerusteenSisalto().getSisalto(), tila);
        }

        if (peruste.getEsiopetuksenPerusteenSisalto() != null) {
            setSisaltoTila(peruste.getEsiopetuksenPerusteenSisalto().getSisalto(), tila);
        }

        peruste.asetaTila(tila);
    }

    private PerusteenOsaViite setSisaltoTila(PerusteenOsaViite sisaltoRoot, PerusteTila tila) {
        if (sisaltoRoot.getPerusteenOsa() != null) {
            sisaltoRoot.getPerusteenOsa().asetaTila(tila);
        }
        if (sisaltoRoot.getLapset() != null) {
            for (PerusteenOsaViite lapsi : sisaltoRoot.getLapset()) {
               setSisaltoTila(lapsi, tila);
            }
        }
        return sisaltoRoot;
    }

    private TutkinnonOsaViite setOsatTila(TutkinnonOsaViite osa, PerusteTila tila) {
        if (osa.getTutkinnonOsa()!= null) {
            osa.getTutkinnonOsa().asetaTila(tila);
        }
        return osa;
    }


    private List<TutkinnonOsaViite> vapaatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsaViite> viiteList = new ArrayList<>();
        RakenneModuuli rakenne = suoritustapa.getRakenne();
        if (rakenne != null) {
            for (TutkinnonOsaViite viite : getViitteet(suoritustapa)) {
                if (!rakenne.isInRakenne(viite, true) && !viite.isPoistettu()) {
                    viiteList.add(viite);
                }
            }
        }
        return viiteList;
    }

    private List<TutkinnonOsa> koodittomatTutkinnonosat(Suoritustapa suoritustapa) {
        List<TutkinnonOsa> koodittomatTutkinnonOsat = new ArrayList<>();

        if (suoritustapa.getTutkinnonOsat() != null) {
            for (TutkinnonOsaViite viite : getViitteet(suoritustapa)) {
                if (viite.getTutkinnonOsa().getKoodiArvo() == null || viite.getTutkinnonOsa().getKoodiArvo().trim().equals("")) {
                    koodittomatTutkinnonOsat.add(viite.getTutkinnonOsa());
                }
            }
        }
        return koodittomatTutkinnonOsat;
    }

    private Collection<TutkinnonOsaViite> getViitteet(Suoritustapa suoritustapa) {
        return Collections2.filter(suoritustapa.getTutkinnonOsat(), new Predicate<TutkinnonOsaViite>() {

            @Override
            public boolean apply(TutkinnonOsaViite input) {
                return Boolean.FALSE.equals(input.isPoistettu());
            }

        });
    }

    @Transactional
    @Override
    public List<TyoryhmaHenkiloDto> saveTyoryhma(Long perusteProjektiId, String tyoryhma, List<String> henkilot) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        removeTyoryhma(perusteProjektiId, tyoryhma);
        perusteprojektiTyoryhmaRepository.flush();
        List<PerusteprojektiTyoryhma> res = new ArrayList<>();

        for (String trh : henkilot) {
            res.add(perusteprojektiTyoryhmaRepository.save(new PerusteprojektiTyoryhma(pp, trh, tyoryhma)));
        }
        return mapper.mapAsList(res, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public TyoryhmaHenkiloDto saveTyoryhma(Long perusteProjektiId, TyoryhmaHenkiloDto tr) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteprojektiTyoryhma ppt = perusteprojektiTyoryhmaRepository.save(new PerusteprojektiTyoryhma(pp, tr.getKayttajaOid(), tr.getNimi()));
        return mapper.map(ppt, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteprojektiTyoryhma> tr = perusteprojektiTyoryhmaRepository.findAllByPerusteprojekti(pp);
        return mapper.mapAsList(tr, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId, String nimi) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteprojektiTyoryhma> tr = perusteprojektiTyoryhmaRepository.findAllByPerusteprojektiAndNimi(pp, nimi);
        return mapper.mapAsList(tr, TyoryhmaHenkiloDto.class);
    }

    @Transactional
    @Override
    public void removeTyoryhma(Long perusteProjektiId, String nimi) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        perusteprojektiTyoryhmaRepository.deleteAllByPerusteprojektiAndNimi(pp, nimi);
    }

    @Transactional
    @Override
    public List<String> setPerusteenOsaViiteTyoryhmat(Long perusteProjektiId, Long perusteenOsaId, List<String> nimet) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteenOsa po = perusteenOsaRepository.findOne(perusteenOsaId);
        Set<String> uniques = new HashSet<>(nimet);
        perusteenOsaTyoryhmaRepository.deleteAllByPerusteenosaAndPerusteprojekti(po, pp);
        perusteenOsaTyoryhmaRepository.flush();
        List<String> res = new ArrayList<>();

        for (String nimi : uniques) {
            PerusteenOsaTyoryhma pot = new PerusteenOsaTyoryhma();
            pot.setNimi(nimi);
            pot.setPerusteprojekti(pp);
            pot.setPerusteenosa(po);
            if (perusteprojektiTyoryhmaRepository.findAllByPerusteprojektiAndNimi(pp, nimi).isEmpty()) {
                throw new BusinessRuleViolationException("Perusteprojekti ryhmää ei ole olemassa: "  + nimi);
            }
            res.add(perusteenOsaTyoryhmaRepository.save(pot).getNimi());
        }
        return res;
    }

    @Transactional
    @Override
    public List<String> getPerusteenOsaViiteTyoryhmat(Long perusteProjektiId, Long perusteenOsaId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        PerusteenOsa po = perusteenOsaRepository.findOne(perusteenOsaId);
        List<PerusteenOsaTyoryhma> tyoryhmat = perusteenOsaTyoryhmaRepository.findAllByPerusteenosaAndPerusteprojekti(po, pp);
        List<String> res = new ArrayList<>();
        for (PerusteenOsaTyoryhma s : tyoryhmat) {
            res.add(s.getNimi());
        }
        return res;
    }

    @Transactional
    @Override
    public List<PerusteenOsaTyoryhmaDto> getSisallonTyoryhmat(Long perusteProjektiId) {
        Perusteprojekti pp = repository.findOne(perusteProjektiId);
        List<PerusteenOsaTyoryhma> tyoryhmat = perusteenOsaTyoryhmaRepository.findAllByPerusteprojekti(pp);
        return mapper.mapAsList(tyoryhmat, PerusteenOsaTyoryhmaDto.class);
    }

    private TilaUpdateStatus korvaaPerusteet(Peruste peruste, Date siirtymaPaattyy, TilaUpdateStatus updateStatus) {

        if (peruste != null && peruste.getKorvattavatDiaarinumerot() != null) {

            // tarkastetaan ettei oma diaari ole korvattavien listalla
            if (peruste.getKorvattavatDiaarinumerot().contains(peruste.getDiaarinumero())) {
                updateStatus.addStatus("korvattava-diaari-on-oma-diaari");
                updateStatus.setVaihtoOk(false);
            }

            Peruste korvattavaPeruste;
            for (Diaarinumero diaari : peruste.getKorvattavatDiaarinumerot()) {
                    korvattavaPeruste = perusteRepository.findByDiaarinumero(diaari);
                    if (korvattavaPeruste != null) {
                        if (!peruste.getKoulutustyyppi().equals(korvattavaPeruste.getKoulutustyyppi())) {
                            Arrays.asList(mapper.map(korvattavaPeruste.getNimi(), LokalisoituTekstiDto.class));
                            updateStatus.addStatus("korvattava-peruste-on-eri-koulutustyyppia", null, Arrays.asList(mapper.map(korvattavaPeruste.getNimi(), LokalisoituTekstiDto.class)));
                            updateStatus.setVaihtoOk(false);
                        }
                    }
                }

            if (updateStatus.isVaihtoOk()) {
                for (Diaarinumero diaari : peruste.getKorvattavatDiaarinumerot()) {
                    korvattavaPeruste = perusteRepository.findByDiaarinumero(diaari);
                    if (korvattavaPeruste != null) {
                        if (siirtymaPaattyy != null) {
                            korvattavaPeruste.setSiirtymaPaattyy(siirtymaPaattyy);
                        } else {
                            korvattavaPeruste.setSiirtymaPaattyy(peruste.getVoimassaoloAlkaa());
                        }
                        korvattavaPeruste.setVoimassaoloLoppuu(peruste.getVoimassaoloAlkaa());
                    }
                }
            }

        }

        return updateStatus;
    }

}
