package ru.ds.education.currency.core.dto.mapper;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.stereotype.Component;
import ru.ds.education.currency.core.dto.CursDto;
import ru.ds.education.currency.model.Curs;

@Component
public class CursMapper extends ConfigurableMapper {
    @Override
    protected void configure(MapperFactory factory) {
        factory.classMap(Curs.class, CursDto.class)
                .byDefault()
                .register();
    }
}
