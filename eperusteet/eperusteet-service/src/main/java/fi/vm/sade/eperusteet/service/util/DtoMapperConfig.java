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

import fi.vm.sade.eperusteet.dto.TekstiPalanenConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jhyoty
 */
@Configuration
public class DtoMapperConfig {

    @Bean
    public DtoMapper dtoMapper() {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.getConverterFactory().registerConverter(TekstiPalanenConverter.TO_MAP);
        return new DtoMapperImpl(factory.getMapperFacade());
    }

    public static class DtoMapperImpl implements DtoMapper {

        private final MapperFacade mapper;

        public DtoMapperImpl(MapperFacade mapper) {
            this.mapper = mapper;
        }

        @Override
        public <S, D> D map(S sourceObject, Class<D> destinationClass) {
            return mapper.map(sourceObject, destinationClass);
        }

        @Override
        public <S, D> void map(S sourceObject, D destinationObject) {
            mapper.map(sourceObject, destinationObject);
        }

        @Override
        public <M> M unwrap(Class<M> mapperClass) {
            return mapperClass.cast(mapper);
        }

    }
}
