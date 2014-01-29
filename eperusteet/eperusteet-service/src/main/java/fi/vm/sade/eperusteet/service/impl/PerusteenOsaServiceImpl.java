package fi.vm.sade.eperusteet.service.impl;

import java.util.List;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.util.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional(readOnly = true)
public class PerusteenOsaServiceImpl implements PerusteenOsaService {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaServiceImpl.class);
    
    @Autowired
    private PerusteenOsaRepository perusteenOsat;
    
    @Autowired
    private DtoMapper mapper;

    @Override
    public List<PerusteenOsaDto> getAll() {
        return mapper.mapAsList(perusteenOsat.findAll(), PerusteenOsaDto.class);
    }

    @Override
    public PerusteenOsaDto get(final Long id) {
        return mapper.map(perusteenOsat.findOne(id), PerusteenOsaDto.class);
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteenOsaDto add(PerusteenOsaDto perusteenOsa) {
        return null;
    }

    @Override
    @Transactional(readOnly = false)
    public PerusteenOsaDto update(final Long id, PerusteenOsaDto osa) {
        return null;
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(final Long id) {
        LOG.info("delete" + id);
        perusteenOsat.delete(id);
    }

}
