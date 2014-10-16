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
package fi.vm.sade.eperusteet.service.mapping;

import com.google.common.base.Optional;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

/**
 *
 * @author jhyoty
 */
public class ToOptionalConverter extends CustomConverter<Object, Optional<?>> {

    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return this.destinationType.isAssignableFrom(destinationType);
    }

    @Override
    public Optional<?> convert(Object source, Type<? extends Optional<?>> destinationType) {
        if ( source != null ) {
            return Optional.of(mapperFacade.map(source, destinationType.getComponentType().getRawType()));
        }
        return null;
    }



}
