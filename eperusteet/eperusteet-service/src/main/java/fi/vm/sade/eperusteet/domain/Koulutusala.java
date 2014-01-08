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

import java.io.Serializable;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 *
 * @author harrik
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@Table(name = "koulutusala")
public class Koulutusala implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    
    private String koodi;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "koulutusala_opintoala", 
            joinColumns = @JoinColumn(name = "koulutusala_id"), 
            inverseJoinColumns = @JoinColumn(name = "opintoala_id"))
    private List<Opintoala> opintoalat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKoodi() {
        return koodi;
    }

    public void setKoodi(String koodi) {
        this.koodi = koodi;
    }

    public List<Opintoala> getOpintoalat() {
        return opintoalat;
    }

    public void setOpintoalat(List<Opintoala> opintoalat) {
        this.opintoalat = opintoalat;
    }
    
    @Override
    public String toString() {
        return "fi.vm.sade.eperusteet.domain.Koulutusala[ id=" + id + " ]";
    }
    
}
