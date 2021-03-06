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
package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.util.List;
import javax.persistence.CascadeType;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 *
 * @author jhyoty
 *
 */
@Entity
@Audited
@Table(name = "perusteenosaviite")
public class PerusteenOsaViite implements ReferenceableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ManyToOne
    @Getter
    @Setter
    private PerusteenOsaViite vanhempi;

    @ManyToOne
    @Getter
    @Setter
    private PerusteenOsa perusteenOsa;

    @OneToMany(mappedBy = "vanhempi", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderColumn
    @Getter
    @Setter
    private List<PerusteenOsaViite> lapset;

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    public PerusteenOsaViite getRoot() {
        PerusteenOsaViite root = this;
        while (root.getVanhempi() != null) {
            root = root.getVanhempi();
        }
        return root;
    }
}
