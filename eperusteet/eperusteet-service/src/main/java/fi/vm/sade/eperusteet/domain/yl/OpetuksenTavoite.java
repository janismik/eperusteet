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
package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "yl_opetuksen_tavoite")
@Audited
public class OpetuksenTavoite extends AbstractReferenceableEntity {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen tavoite;

    @Getter
    @Setter
    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<KeskeinenSisaltoalue> sisaltoalueet = new HashSet<>();

    @Getter
    @Setter
    @ManyToMany
    private Set<LaajaalainenOsaaminen> laajattavoitteet = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TavoitteenArviointi> arvioinninkohteet = new HashSet<>();

    @Getter
    @Setter
    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<OpetuksenKohdealue> kohdealueet = new HashSet<>();

    public Set<TavoitteenArviointi> getArvioinninkohteet() {
        return new HashSet<>(arvioinninkohteet);
    }

    public void setArvioinninkohteet(Set<TavoitteenArviointi> kohteet) {
        this.arvioinninkohteet.clear();
        if (kohteet != null) {
            this.arvioinninkohteet.addAll(kohteet);
        }
    }

}
