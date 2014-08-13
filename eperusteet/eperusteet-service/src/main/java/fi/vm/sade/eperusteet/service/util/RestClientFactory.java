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

package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.generic.rest.CachingRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author nkala
 */
@Component
public class RestClientFactory {
    @Value("${fi.vm.sade.eperusteet.oph_username}")
    private String username;

    @Value("${fi.vm.sade.eperusteet.oph_password}")
    private String password;

    @Value("${web.url.cas}")
    private String casUrl;

    public CachingRestClient create(String service) {
        CachingRestClient crc = new CachingRestClient();
        crc.setUsername(username);
        crc.setPassword(password);
        crc.setWebCasUrl(casUrl);
        crc.setCasService(service + "/j_spring_cas_security_check");
        return crc;
    }
}
