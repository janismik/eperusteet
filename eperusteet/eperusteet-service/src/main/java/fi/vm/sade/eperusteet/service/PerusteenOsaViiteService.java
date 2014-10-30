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

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author harrik
 */
public interface PerusteenOsaViiteService {

    @PreAuthorize("hasPermission(#peruste,'peruste','MUOKKAUS')")
    PerusteenOsaViiteDto.Laaja kloonaaTekstiKappale(@P("peruste") Long perusteId, Long id);

    @PreAuthorize("hasPermission(#peruste,'peruste','MUOKKAUS')")
    TutkinnonOsaViiteDto kloonaaTutkinnonOsa(@P("peruste") Long perusteId, Suoritustapakoodi tapa,Long id);

    @PreAuthorize("hasPermission(#peruste,'peruste','MUOKKAUS')")
    void reorderSubTree(@P("peruste") Long perusteId, Long rootViiteId, PerusteenOsaViiteDto.Puu<?,?> uusi);

    <T extends PerusteenOsaViiteDto<?>> T getSisalto(Long perusteId, Long viiteId, Class<T> view);

    @PreAuthorize("hasPermission(#peruste,'peruste','MUOKKAUS')")
    void removeSisalto(@P("peruste") Long perusteId, Long id);

    @PreAuthorize("hasPermission(#peruste,'peruste','MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala viiteDto);
}
