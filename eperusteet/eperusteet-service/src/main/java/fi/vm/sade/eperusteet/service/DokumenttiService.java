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

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.DokumenttiDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author jussini
 */
public interface DokumenttiService {

    @PreAuthorize("hasPermission(#dto.perusteId, 'peruste', 'LUKU')")
    public void setStarted(@P("dto") DokumenttiDto dto);

    @PreAuthorize("hasPermission(#dto.perusteId, 'peruste', 'LUKU')")
    @Async(value = "docTaskExecutor")
    public void generateWithDto(@P("dto") DokumenttiDto dto);

    @PreAuthorize("hasPermission(#id, 'peruste', 'LUKU')")
    public DokumenttiDto createDtoFor(@P("id") final long id, Kieli kieli, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("isAuthenticated()")
    public byte[] get(Long id);

    @PreAuthorize("isAuthenticated()")
    public DokumenttiDto query(Long id);

    @PreAuthorize("isAuthenticated()")
    public DokumenttiDto findLatest(Long id, Kieli kieli);
}
