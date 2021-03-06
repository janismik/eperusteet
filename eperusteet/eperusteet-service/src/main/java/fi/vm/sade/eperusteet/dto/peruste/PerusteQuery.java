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
package fi.vm.sade.eperusteet.dto.peruste;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Getter
@Setter
public class PerusteQuery {

    private int sivu = 0;
    private int sivukoko = 25;
    private boolean siirtyma = false;
    private String nimi;
    private List<String> koulutusala;
    private List<String> tyyppi;
    private String kieli = "fi";
    private List<String> opintoala;
    private String suoritustapa;
    private String tila;
    private String koulutuskoodi;
    private String perusteTyyppi;
    private Long muokattu;
}
