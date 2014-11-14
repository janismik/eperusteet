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
package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.OppiaineenVuosiluokkakokonaisuusRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
public class OppiaineLockServiceImpl extends AbstractLockService<OppiaineLockContext> {

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository repository;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private OppiaineenVuosiluokkakokonaisuusRepository vuosiluokkakokonaisuusRepository;

    @Override
    protected final ReferenceableEntity validateCtx(OppiaineLockContext ctx) {
        permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.MUOKKAUS);

        PerusopetuksenPerusteenSisalto s = repository.findByPerusteId(ctx.getPerusteId());
        Oppiaine aine = oppiaineRepository.findOne(ctx.getOppiaineId());
        if (s == null || !s.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("Virheellinen lukitus");
        }

        if (ctx.getKokonaisuusId() != null) {
            OppiaineenVuosiluokkaKokonaisuus ovk = vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(ctx.getKokonaisuusId(), ctx.getOppiaineId());
            if (ovk == null) {
                throw new BusinessRuleViolationException("Virheellinen lukitus");
            }
            return ovk;
        }
        return aine;
    }

}