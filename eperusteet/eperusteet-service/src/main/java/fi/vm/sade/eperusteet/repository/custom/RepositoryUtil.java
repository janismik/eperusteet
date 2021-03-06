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
package fi.vm.sade.eperusteet.repository.custom;

/**
 *
 * @author jhyoty
 */
public final class RepositoryUtil {

    private RepositoryUtil() {
        //apuluokka
    }

    public static final char ESCAPE_CHAR = '\\';

    public static String kuten(String teksti) {
        StringBuilder b = new StringBuilder("%");
        b.append(teksti.toLowerCase().replace("" + ESCAPE_CHAR, "" + ESCAPE_CHAR + ESCAPE_CHAR).replace("_", ESCAPE_CHAR
                + "_").replace("%", ESCAPE_CHAR + "%"));
        b.append("%");
        return b.toString();
    }

}
