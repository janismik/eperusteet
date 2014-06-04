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

import fi.vm.sade.eperusteet.domain.Kommentti;
import fi.vm.sade.eperusteet.dto.KommenttiDto;
import fi.vm.sade.eperusteet.repository.KommenttiRepository;
import fi.vm.sade.eperusteet.service.KommenttiService;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Service
public class KommenttiServiceImpl implements KommenttiService {

    @Autowired
    KommenttiRepository kommentit;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public KommenttiDto get(Long kommenttiId) {
        Kommentti kommentti = kommentit.findOne(kommenttiId);
        return mapper.map(kommentti, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<KommenttiDto> getAllByPerusteenOsa(Long id, Long perusteenOsaId) {
        List<Kommentti> re = kommentit.findAllByPerusteenOsa(id, perusteenOsaId);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<KommenttiDto> getAllBySuoritustapa(Long id, String suoritustapa) {
        List<Kommentti> re = kommentit.findAllBySuoritustapa(id, suoritustapa);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<KommenttiDto> getAllByPerusteprojekti(Long id) {
        List<Kommentti> re = kommentit.findAllByPerusteprojekti(id);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<KommenttiDto> getAllByParent(Long id) {
        List<Kommentti> re = kommentit.findAllByParent(id);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<KommenttiDto> getAllByYlin(Long id) {
        List<Kommentti> re = kommentit.findAllByYlin(id);
        return mapper.mapAsList(re, KommenttiDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public KommenttiDto add(final KommenttiDto kommenttidto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Kommentti kommentti = new Kommentti();
        kommentti.setSisalto(kommenttidto.getSisalto());
        kommentti.setPerusteprojektiId(kommenttidto.getPerusteprojektiId());
        kommentti.setSuoritustapa(kommenttidto.getSuoritustapa());
        kommentti.setPerusteenOsaId(kommenttidto.getPerusteenOsaId());

        if (kommenttidto.getParentId() != null) {
            Kommentti parent = kommentit.findOne(kommenttidto.getParentId());
            kommentti.setParentId(parent.getId());
            kommentti.setYlinId(parent.getYlinId() == null ? parent.getId() : parent.getYlinId());
        }
        return mapper.map(kommentit.save(kommentti), KommenttiDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public KommenttiDto update(Long kommenttiId, final KommenttiDto kommenttidto) {
        Kommentti kommentti = kommentit.findOne(kommenttiId);
        kommentti.setSisalto(kommenttidto.getSisalto());
        return mapper.map(kommentit.save(kommentti), KommenttiDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void delete(Long kommenttiId) {
        Kommentti kommentti = kommentit.findOne(kommenttiId);
        kommentti.setSisalto("");
        kommentti.setPoistettu(true);
    }
}