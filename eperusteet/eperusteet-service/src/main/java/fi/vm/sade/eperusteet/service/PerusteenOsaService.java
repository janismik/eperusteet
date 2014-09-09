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

import fi.vm.sade.eperusteet.dto.LukkoDto;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaamistavoiteLaajaDto;

import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author jhyoty
 */
public interface PerusteenOsaService {

    //yleiset haut sallittu kaikille -- palauttaa vain julkaistuja osia
    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto> getAllByKoodiUri(final String koodiUri);

    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto> getAll();

    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto> getAllWithName(final String name);

    @PreAuthorize("hasPermission(#po.dto.id, 'perusteenosa', 'MUOKKAUS')")
    <T extends PerusteenOsaDto> T update(@P("po") UpdateDto<T> perusteenOsaDto);

    @PreAuthorize("hasPermission(#po.id, 'perusteenosa', 'MUOKKAUS')")
    <T extends PerusteenOsaDto> T update(@P("po") T perusteenOsaDto);

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasPermission(returnObject.id, 'perusteenosa', 'MUOKKAUS')")
    <T extends PerusteenOsaDto> T add(T perusteenOsaDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'POISTO')")
    void delete(final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    Integer getLatestRevision(final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    PerusteenOsaDto revertToVersio(@P("id") Long id, Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto get(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<Revision> getVersiot(Long id);

    //TODO: versiotietojen lukuoikeus?
    @PreAuthorize("returnObject?.tila == T(fi.vm.sade.eperusteet.domain.PerusteTila).VALMIS or hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto getVersio(@P("id") final Long id, final Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    LukkoDto lock(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    void unlock(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    LukkoDto getLock(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    OsaAlueLaajaDto addTutkinnonOsaOsaAlue(@P("id") final Long id, OsaAlueLaajaDto osaAlueDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    OsaAlueLaajaDto updateTutkinnonOsaOsaAlue(@P("id") final Long id, final Long osaAlueId, OsaAlueLaajaDto osaAlue);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<OsaAlueLaajaDto> getTutkinnonOsaOsaAlueet(final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    public OsaamistavoiteLaajaDto addOsaamistavoite(@P("id") final Long id, final Long osaAlueId, OsaamistavoiteLaajaDto osaamistavoiteDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    public OsaamistavoiteLaajaDto updateOsaamistavoite(@P("id") final Long id, final Long osaAlueId, final Long osaamistavoiteId, OsaamistavoiteLaajaDto osaamistavoite);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    public List<OsaamistavoiteLaajaDto> getOsaamistavoitteet(final Long id, final Long osaAlueId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    public void removeOsaamistavoite(@P("id") final Long id, final Long osaAlueId, final Long osaamistavoiteId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    public void removeOsaAlue(@P("id") final Long id, final Long osaAlueId);
}
