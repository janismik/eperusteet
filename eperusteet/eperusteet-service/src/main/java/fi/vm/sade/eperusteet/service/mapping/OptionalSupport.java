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
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Collection;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.NullFilter;
import ma.glasnost.orika.metadata.Type;

/**
 *
 * @author jhyoty
 */
public final class OptionalSupport {

    private OptionalSupport() {};

    public static void register(MapperFactory factory) {
        factory.getConverterFactory().registerConverter(new TekstiConverter());
        factory.getConverterFactory().registerConverter(new ToOptionalConverter());
        factory.registerFilter(new Filter());
        factory.registerFilter(new CollectionFilter());
        factory.registerMapper(new Mapper());
    }

    static final class Mapper extends CustomMapper<Optional<?>, Object> {

        @Override
        public void mapAtoB(Optional<?> a, Object b, MappingContext context) {
            if (a.isPresent()) {
                mapperFacade.map(a.get(), b, context);
            } else {
                throw new MappingException("Optional.absent havaittu");
            }
        }

        @Override
        //ToOptionalConverter hoitaa tämän suunnan
        public void mapBtoA(Object b, Optional<?> a, MappingContext context) {
            throw new MappingException("mapBtoA ei ole tuettu");
        }

    }

    static final class Filter extends NullFilter<Optional<?>, Object> {

        @Override
        public <S extends Optional<?>> S filterSource(S sourceValue, Type<S> sourceType, String sourceName, Type<?> destType, String destName, MappingContext mappingContext) {
            if (sourceValue != null && !sourceValue.isPresent()) {
                return null;
            }
            return sourceValue;
        }

        @Override
        public boolean filtersSource() {
            return true;
        }

        @Override
        public <S extends Optional<?>, D> boolean shouldMap(Type<S> sourceType, String sourceName, S source, Type<D> destType, String destName, D dest, MappingContext mappingContext) {
            return source != null;
        }

    }

    static final class CollectionFilter extends NullFilter<Collection<?>, Collection<?>> {

        @Override
        public <S extends Collection<?>, D extends Collection<?>> boolean shouldMap(Type<S> sourceType, String sourceName, S source, Type<D> destType, String destName, D dest, MappingContext mappingContext) {
            return source != null;
        }

    }

    static final class TekstiConverter extends CustomConverter<Optional<LokalisoituTekstiDto>, TekstiPalanen> {

        @Override
        public TekstiPalanen convert(Optional<LokalisoituTekstiDto> source, Type<? extends TekstiPalanen> destinationType) {
            if (source != null && source.isPresent()) {
                return mapperFacade.map(source.get(), TekstiPalanen.class);
            }
            return null;
        }

    }

    static final class ToOptionalConverter extends CustomConverter<Object, Optional<?>> {

        @Override
        public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
            return this.destinationType.isAssignableFrom(destinationType);
        }

        @Override
        public Optional<?> convert(Object source, Type<? extends Optional<?>> destinationType) {
            if (source != null) {
                return Optional.of(mapperFacade.map(source, destinationType.getComponentType().getRawType()));
            }
            return null;
        }
    }
}
