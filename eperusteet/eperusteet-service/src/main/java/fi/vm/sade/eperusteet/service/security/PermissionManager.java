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
package fi.vm.sade.eperusteet.service.security;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.authorization.PerusteprojektiPermissionRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.Pair;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.KOMMENTOINTI;
import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.KORJAUS;
import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.LUKU;
import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.LUONTI;
import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.MUOKKAUS;
import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.POISTO;
import static fi.vm.sade.eperusteet.service.security.PermissionManager.Permission.TILANVAIHTO;

/**
 *
 * @author harrik
 */
@Service
public class PermissionManager {

    @Autowired
    private PermissionHelper helper;

    @Autowired
    private PerusteprojektiRepository projektiRepository;

    @Autowired
    private PerusteprojektiPermissionRepository perusteProjektit;

    @Autowired
    TutkinnonOsaViiteRepository viiteRepository;

    @Autowired
    PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionManager.class);

    public enum Permission {

        LUKU("luku"),
        POISTO("poisto"),
        MUOKKAUS("muokkaus"),
        KOMMENTOINTI("kommentointi"),
        LUONTI("luonti"),
        KORJAUS("korjaus"),
        TILANVAIHTO("tilanvaihto");

        private final String permission;

        private Permission(String permission) {
            this.permission = permission;
        }

        @Override
        public String toString() {
            return permission;
        }
    }

    public enum Target {

        PERUSTEPROJEKTI("perusteprojekti"),
        PERUSTE("peruste"),
        PERUSTEENMETATIEDOT("perusteenmetatiedot"),
        PERUSTEENOSA("perusteenosa"),
        TUTKINNONOSAVIITE("tutkinnonosaviite"),
        PERUSTEENOSAVIITE("perusteenosaviite"),
        TIEDOTE("tiedote");

        private final String target;

        private Target(String target) {
            this.target = target;
        }

        @Override
        public String toString() {
            return target;
        }
    }

    //TODO: oikeidet kovakodattua, oikeuksien tarkastaus.
    private static final Map<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> allowedRoles;

    static {
        Map<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> allowedRolesTmp = new EnumMap<>(Target.class);

        Set<String> r0 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001");
        Set<String> r1 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001", "ROLE_APP_EPERUSTEET_CRUD_<oid>");
        Set<String> r2 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001", "ROLE_APP_EPERUSTEET_CRUD_<oid>", "ROLE_APP_EPERUSTEET_READ_UPDATE_<oid>");
        Set<String> r3 = Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001", "ROLE_APP_EPERUSTEET_CRUD_<oid>", "ROLE_APP_EPERUSTEET_READ_UPDATE_<oid>", "ROLE_APP_EPERUSTEET_READ_<oid>");
        Set<String> r4 = Sets.newHashSet("ROLE_VIRKAILIJA", "ROLE_APP_EPERUSTEET");
        Set<String> r5 = Sets.newHashSet("ROLE_VIRKAILIJA", "ROLE_APP_EPERUSTEET", "ROLE_ANONYMOUS");

        //perusteenosa, peruste (näiden osalta oletetaan että peruste tai sen osa on tilassa LUONNOS
        {
            EnumMap<ProjektiTila, Map<Permission, Set<String>>> tmp = new EnumMap<>(ProjektiTila.class);
            Map<Permission, Set<String>> perm;

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r2);
            perm.put(POISTO, r2);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.LAADINTA, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.KOMMENTOINTI, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(KOMMENTOINTI, r2);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            tmp.put(ProjektiTila.VALMIS, perm);

            perm = Maps.newHashMap();
            perm.put(KORJAUS, r0);
            perm.put(LUKU, r5);
            tmp.put(ProjektiTila.JULKAISTU, perm);

            tmp.put(ProjektiTila.POISTETTU, Collections.<Permission, Set<String>>emptyMap());

            allowedRolesTmp.put(Target.PERUSTE, tmp);
            allowedRolesTmp.put(Target.PERUSTEENOSA, tmp);
            allowedRolesTmp.put(Target.TUTKINNONOSAVIITE, tmp);
            allowedRolesTmp.put(Target.PERUSTEENOSAVIITE, tmp);
        }
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();

            perm.put(LUONTI, Sets.newHashSet("ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001"));
            tmp.put(null, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r2);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.LAADINTA, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.KOMMENTOINTI, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(TILANVAIHTO, r1);
            tmp.put(ProjektiTila.VALMIS, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r0);
            tmp.put(ProjektiTila.JULKAISTU, perm);

            perm = Maps.newHashMap();
            perm.put(TILANVAIHTO, r1);
            tmp.put(ProjektiTila.POISTETTU, perm);

            allowedRolesTmp.put(Target.PERUSTEPROJEKTI, tmp);
        }
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r2);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.LAADINTA, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.KOMMENTOINTI, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            perm.put(KOMMENTOINTI, r3);
            tmp.put(ProjektiTila.VIIMEISTELY, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r3);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.VALMIS, perm);

            perm = Maps.newHashMap();
            perm.put(LUKU, r5);
            perm.put(MUOKKAUS, r1);
            tmp.put(ProjektiTila.JULKAISTU, perm);
            allowedRolesTmp.put(Target.PERUSTEENMETATIEDOT, tmp);
        }
        {
            Map<ProjektiTila, Map<Permission, Set<String>>> tmp = new IdentityHashMap<>();
            Map<Permission, Set<String>> perm = Maps.newHashMap();
            perm.put(LUONTI, r0);
            perm.put(LUKU, r4);
            perm.put(MUOKKAUS, r0);
            perm.put(POISTO, r0);
            tmp.put(null, perm);
            allowedRolesTmp.put(Target.TIEDOTE, tmp);
        }

        if (LOG.isTraceEnabled()) {
            assert (allowedRolesTmp.keySet().containsAll(EnumSet.allOf(Target.class)));
            for (Map.Entry<Target, Map<ProjektiTila, Map<Permission, Set<String>>>> t : allowedRolesTmp.entrySet()) {
                for (Map.Entry<ProjektiTila, Map<Permission, Set<String>>> p : t.getValue().entrySet()) {
                    for (Map.Entry<Permission, Set<String>> per : p.getValue().entrySet()) {
                        LOG.trace(t.getKey() + ":" + p.getKey() + ":" + per.getKey() + ":" + Arrays.toString(per.getValue().toArray()));
                    }
                }
            }
        }
        allowedRoles = Collections.unmodifiableMap(allowedRolesTmp);
    }

    private static Set<String> getAllowedRoles(Target target, Permission permission) {
        return getAllowedRoles(target, null, permission);
    }

    private static Set<String> getAllowedRoles(Target target, ProjektiTila tila, Permission permission) {
        Map<Permission, Set<String>> t = allowedRoles.get(target).get(tila);
        if (t != null) {
            Set<String> r = t.get(permission);
            if (r != null) {
                return r;
            }
        }
        return Collections.emptySet();
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Authentication authentication, Serializable targetId, Target targetType, Permission permission) {

        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("Checking permission %s to %s{id=%s} by %s", permission, targetType, targetId, authentication));
        }

        if (Target.TIEDOTE == targetType) {
            return hasAnyRole(authentication, getAllowedRoles(targetType, permission));
        }

        if (Target.TUTKINNONOSAVIITE.equals(targetType)) {
            // Haetaan perusteen osa mihin viitataan osaviitteessä ja jatketaan luvan tutkimista perusteen osan tiedoilla.
            TutkinnonOsaViite t = viiteRepository.findOne((Long) targetId);
            if (t == null || t.getTutkinnonOsa() == null) {
                return false;
            }
            targetId = t.getTutkinnonOsa().getId();
            targetType = Target.PERUSTEENOSA;
        }

        if (Target.PERUSTEENOSAVIITE.equals(targetType)) {
            // Haetaan perusteen osa mihin viitataan osaviitteessä ja jatketaan luvan tutkimista perusteen osan tiedoilla.
            PerusteenOsaViite p = perusteenOsaViiteRepository.findOne((Long) targetId);
            if (p == null || p.getPerusteenOsa() == null) {
                return false;
            }
            targetId = p.getPerusteenOsa().getId();
            targetType = Target.PERUSTEENOSA;
        }

        if (LUKU.equals(permission)) {
            //tarkistetaan onko lukuoikeus suoraan julkaistu -statuksen perusteella
            if (PerusteTila.VALMIS == helper.findPerusteTilaFor(targetType, targetId)) {
                return true;
            }
        }

        if (targetId == null) {
            return hasAnyRole(authentication, getAllowedRoles(targetType, permission));
        } else {
            boolean allowed = false;
            for (Pair<String, ProjektiTila> ppt : findPerusteProjektiTila(targetType, targetId)) {
                allowed = allowed | hasAnyRole(authentication, ppt.getFirst(), getAllowedRoles(targetType, ppt.getSecond(), permission));
            }
            return allowed;
        }

    }

    private boolean hasAnyRole(Authentication authentication, Collection<String> roles) {
        return hasAnyRole(authentication, null, roles);
    }

    private boolean hasAnyRole(Authentication authentication, String perusteProjektiRyhmaOid, Collection<String> roles) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (String role : roles) {
                GrantedAuthority auth = new SimpleGrantedAuthority(perusteProjektiRyhmaOid == null ? role : role.replace("<oid>", perusteProjektiRyhmaOid));
                if (authentication.getAuthorities().contains(auth)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Map<Target, Set<Permission>> getProjectPermissions(Long id) {

        Map<Target, Set<Permission>> permissionMap = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Perusteprojekti projekti = projektiRepository.findOne(id);
        if (projekti == null) {
            throw new BusinessRuleViolationException("Perusteprojektia ei ole olemassa");
        }
        permissionMap.put(Target.PERUSTEPROJEKTI, getPermissions(authentication, projekti.getId(), Target.PERUSTEPROJEKTI, projekti.getTila()));

        Peruste peruste = projekti.getPeruste();
        if (peruste != null) {
            permissionMap.put(Target.PERUSTE, getPermissions(authentication, peruste.getId(), Target.PERUSTE, projekti.getTila()));
            permissionMap.put(Target.PERUSTEENMETATIEDOT, getPermissions(authentication, peruste.getId(), Target.PERUSTEENMETATIEDOT, projekti.getTila()));
        } else {
            throw new BusinessRuleViolationException("Perustetta ei ole olemassa");
        }

        return permissionMap;
    }

    /**
     *
     * @param authentication
     * @param targetId
     * @param targetType
     * @param tila
     * @return
     */
    // TODO: tila parametrin voisi varmaan karsia pois
    private Set<Permission> getPermissions(Authentication authentication, Serializable targetId, Target targetType, ProjektiTila tila) {
        Set<Permission> permission = new HashSet<>();

        Map<ProjektiTila, Map<Permission, Set<String>>> tempTargetKohtaiset = allowedRoles.get(targetType);
        Map<Permission, Set<String>> tempProjektitilaKohtaiset = tempTargetKohtaiset.get(tila);
        final Set<Pair<String, ProjektiTila>> projektitila = findPerusteProjektiTila(targetType, targetId);

        for (Map.Entry<Permission, Set<String>> per : tempProjektitilaKohtaiset.entrySet()) {
            boolean hasRole = false;
            for (Pair<String, ProjektiTila> ppt : projektitila) {
                hasRole = hasRole | hasAnyRole(authentication, ppt.getFirst(), per.getValue());
            }
            if (hasRole) {
                permission.add(per.getKey());
            }
        }

        return permission;
    }

    private Set<Pair<String, ProjektiTila>> findPerusteProjektiTila(Target targetType, Serializable targetId) {

        if (!(targetId instanceof Long)) {
            throw new IllegalArgumentException("Expected Long");
        }
        final Long id = (Long) targetId;

        final Set<Pair<String, ProjektiTila>> empty = Collections.emptySet();
        switch (targetType) {

            case PERUSTEENMETATIEDOT:
            case PERUSTE: {
                return setOf(perusteProjektit.findByPeruste(id));
            }
            case PERUSTEPROJEKTI: {
                return setOf(perusteProjektit.findById(id));
            }
            case PERUSTEENOSA: {
                return setOf(perusteProjektit.findTilaByPerusteenOsaId(id));
            }
            default:
                throw new IllegalArgumentException(targetType.toString());
        }
    }

    private static <T> Set<T> setOf(Collection<T> c) {
        if (c == null || c.isEmpty()) {
            return Collections.emptySet();
        }
        if (c.size() == 1) {
            return Collections.singleton(c.iterator().next());
        }
        return new HashSet<>(c);
    }
}
