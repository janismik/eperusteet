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

package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KommenttiDto {
    private String nimi;
    private String muokkaaja;
    private String sisalto;
    private Date luotu;
    private Date muokattu;
    private Long id;
    private Long ylinId;
    private Long parentId;
    private Long perusteprojektiId;
    private Boolean poistettu;
    private Long perusteenOsaId;
    private String suoritustapa;
}
