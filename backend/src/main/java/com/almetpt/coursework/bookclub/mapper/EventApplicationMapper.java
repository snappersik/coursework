package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.model.ApplicationStatus; // Для маппинга статуса из DTO
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class EventApplicationMapper extends GenericMapper<EventApplication, EventApplicationDTO> {

    public EventApplicationMapper(ModelMapper modelMapper) {
        super(EventApplication.class, EventApplicationDTO.class, modelMapper);
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        // Кастомный конвертер для Entity -> DTO
        Converter<EventApplication, EventApplicationDTO> entityToDtoConverter = new Converter<EventApplication, EventApplicationDTO>() {
            public EventApplicationDTO convert(MappingContext<EventApplication, EventApplicationDTO> context) {
                EventApplication source = context.getSource();
                // ModelMapper должен создать экземпляр destination, если он null.
                // Если destination уже существует (например, при обновлении), он будет использован.
                EventApplicationDTO destination = context.getDestination() == null ? new EventApplicationDTO() : context.getDestination();

                log.debug("Mapping EventApplication (id: {}) to DTO. Source User: {}, Source Event: {}",
                        source.getId(),
                        source.getUser() != null ? "User(id=" + source.getUser().getId() + ")" : "null",
                        source.getEvent() != null ? "Event(id=" + source.getEvent().getId() + ")" : "null");

                // Ручное маппирование полей
                destination.setId(source.getId());

                if (source.getUser() != null) {
                    destination.setUserId(source.getUser().getId());
                } else {
                    destination.setUserId(null);
                    log.warn("Source EventApplication (id: {}) has null User.", source.getId());
                }

                if (source.getEvent() != null) {
                    destination.setEventId(source.getEvent().getId());
                } else {
                    destination.setEventId(null);
                    log.warn("Source EventApplication (id: {}) has null Event.", source.getId());
                }

                if (source.getStatus() != null) {
                    destination.setApplicationStatus(source.getStatus().name());
                } else {
                    destination.setApplicationStatus(null);
                    log.warn("Source EventApplication (id: {}) has null Status.", source.getId());
                }

                // Маппирование полей из GenericModel/GenericDTO
                destination.setCreatedBy(source.getCreatedBy());
                destination.setCreatedWhen(source.getCreatedWhen());
                destination.setDeletedBy(source.getDeletedBy());
                destination.setDeletedWhen(source.getDeletedWhen());
                destination.setDeleted(source.isDeleted());

                log.debug("Mapped DTO: {}", destination);
                return destination;
            }
        };

        // Применяем кастомный конвертер для TypeMap Entity -> DTO
        modelMapper.createTypeMap(EventApplication.class, EventApplicationDTO.class)
                .setConverter(entityToDtoConverter);

        // Настройка маппинга DTO -> Entity (остается как было или можно также уточнить)
        // Важно: toEntityConverter() вызывает mapSpecificFields(D, E)
        modelMapper.createTypeMap(EventApplicationDTO.class, EventApplication.class)
                .addMappings(mapper -> {
                    mapper.skip(EventApplication::setUser); // Устанавливается в сервисе
                    mapper.skip(EventApplication::setEvent); // Устанавливается в сервисе
                    // Пропускаем поля аудита, так как они устанавливаются GenericService.setAuditFields
                    // или при загрузке сущности из БД для обновления
                    mapper.skip(EventApplication::setCreatedBy);
                    mapper.skip(EventApplication::setCreatedWhen);
                    mapper.skip(EventApplication::setUpdatedBy);
                    mapper.skip(EventApplication::setUpdatedWhen);
                    mapper.skip(EventApplication::setDeletedBy);
                    mapper.skip(EventApplication::setDeletedWhen);
                    mapper.skip(EventApplication::setDeleted);
                    // Статус нужно будет обработать в mapSpecificFields(D, E) или здесь
                })
                .setPostConverter(toEntityConverter()); // toEntityConverter вызывает mapSpecificFields(D,E)
    }

    // Этот метод вызывается из toDTOConverter() родительского GenericMapper.
    // Поскольку мы определили .setConverter(entityToDtoConverter) для TypeMap,
    // toDTOConverter() и этот mapSpecificFields(E,D) больше не будут основными
    // для прямого вызова modelMapper.map(source, Destination.class).
    // Однако, он может быть вызван, если где-то используется context.getMapper().map(...).
    // Для чистоты оставим его пустым или удалим вызов PostConverter-а из TypeMap E->D,
    // если полностью полагаемся на .setConverter.
    // Пока оставим, но основной путь маппинга E->D теперь через entityToDtoConverter.
    @Override
    protected void mapSpecificFields(EventApplication source, EventApplicationDTO destination) {
        // Эта логика теперь находится в entityToDtoConverter.
        // Оставим на случай, если GenericMapper.toDTOs(List<E>) или другой код его вызовет.
        // Но по-хорошему, нужно убедиться, что вызывается только entityToDtoConverter.
        if (destination != null && source != null) { // Добавим проверку на null для безопасности
            destination.setId(source.getId());
            destination.setUserId(source.getUser() != null ? source.getUser().getId() : null);
            destination.setEventId(source.getEvent() != null ? source.getEvent().getId() : null);
            destination.setApplicationStatus(source.getStatus() != null ? source.getStatus().name() : null);

            destination.setCreatedBy(source.getCreatedBy());
            destination.setCreatedWhen(source.getCreatedWhen());
            destination.setDeletedBy(source.getDeletedBy());
            destination.setDeletedWhen(source.getDeletedWhen());
            destination.setDeleted(source.isDeleted());
        }
    }

    // Этот метод вызывается из toEntityConverter() родительского GenericMapper.
    @Override
    protected void mapSpecificFields(EventApplicationDTO source, EventApplication destination) {
        // Поля user и event устанавливаются в сервисе.
        // Поля аудита (createdBy, createdWhen и т.д.) обычно устанавливаются сервисом (setAuditFields),
        // поэтому здесь их маппить из DTO в Entity не нужно, если только DTO не предназначен для их обновления.

        // Маппинг статуса из строки DTO в Enum Entity
        if (source.getApplicationStatus() != null) {
            try {
                destination.setStatus(ApplicationStatus.valueOf(source.getApplicationStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid application status string in DTO during DTO->Entity mapping: '{}'. Setting status to null.", source.getApplicationStatus(), e);
                destination.setStatus(null); // или PENDING, или выбросить ошибку, в зависимости от бизнес-логики
            }
        } else {
            destination.setStatus(null); // или PENDING
        }
        // Остальные поля, специфичные для EventApplication, если они есть в DTO и нужны в Entity
    }

    @Override
    protected List<Long> getIds(EventApplication entity) {
        return Collections.emptyList(); // Пока не используется
    }
}
