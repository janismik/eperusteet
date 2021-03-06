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

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.PerusopetusPerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.TutkinnonOsaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author harrik
 */
public interface PerusteService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, Long osaId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkinnonOsaViiteDto updateTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteUpdateDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkinnonOsaViiteDto updateTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasPermission(returnObject.tutkinnonOsaDto.id, 'perusteenosa', 'LUKU')")
    TutkinnonOsaViiteDto getTutkinnonOsaViite(Long perusteId, Suoritustapakoodi suoritustapakoodi, Long viiteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkinnonOsaViiteDto attachTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkinnonOsaViiteDto addTutkinnonOsa(@P("perusteId") Long id, Suoritustapakoodi koodi, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteDto get(@P("perusteId") final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteKaikkiDto getKokoSisalto(@P("perusteId") final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    PerusteDto update(@P("perusteId") long perusteId, PerusteDto perusteDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteDto getByIdAndSuoritustapa(@P("perusteId") final Long id, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("permitAll()")
    Page<PerusteDto> getAll(PageRequest page, String kieli);

    @PreAuthorize("permitAll()")
    List<PerusteInfoDto> getAllInfo();

    @PreAuthorize("permitAll()")
    Page<PerusteDto> findBy(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("permitAll()")
    PerusopetusPerusteKaikkiDto getPerusopetusKokoSisalto(Long id);

    @PreAuthorize("permitAll()")
    public List<PerusteInfoDto> getAllPerusopetusInfo();

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisaltoUUSI(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisaltoLapsi(@P("perusteId") final Long perusteId, final Long perusteenosaViiteId, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteenOsaViiteDto.Laaja getSuoritustapaSisalto(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends PerusteenOsaViiteDto.Puu<?, ?>> T getSuoritustapaSisalto(@P("perusteId") Long perusteId, Suoritustapakoodi suoritustapakoodi, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends PerusteenOsaViiteDto.Puu<?, ?>> T getSuoritustapaSisaltoUUSI(@P("perusteId") Long perusteId, Suoritustapakoodi suoritustapakoodi, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    SuoritustapaDto getSuoritustapa(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    RakenneModuuliDto getTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, Integer eTag);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final UpdateDto<RakenneModuuliDto> rakenne);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final RakenneModuuliDto rakenne);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    RakenneModuuliDto revertRakenneVersio(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getRakenneVersiot(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    RakenneModuuliDto getRakenneVersio(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    @PostFilter("filterObject.poistettu != true")
    List<TutkinnonOsaViiteDto> getTutkinnonOsat(@P("perusteId") Long perusteid, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("isAuthenticated()") //XXX ei julkinen rajapinta
    Peruste luoPerusteRunko(String koulutustyyppi, LaajuusYksikko yksikko, PerusteTyyppi tyyppi);

    @PreAuthorize("isAuthenticated()") //XXX ei julkinen rajapinta
    Peruste luoPerusteRunkoToisestaPerusteesta(PerusteprojektiLuontiDto luontiDto, PerusteTyyppi tyyppi);

    @PreAuthorize("permitAll()")
    Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'LUKU')")
    public List<TutkintonimikeKoodiDto> getTutkintonimikeKoodit(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    public TutkintonimikeKoodiDto addTutkintonimikeKoodi(Long perusteId, TutkintonimikeKoodiDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    public void removeTutkintonimikeKoodi(Long perusteId, Long tutkintonimikeKoodiId);

    @PreAuthorize("isAuthenticated()")
    public PerusteInfoDto getByDiaari(Diaarinumero diaarinumero);
}
