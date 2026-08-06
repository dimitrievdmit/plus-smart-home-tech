package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.model.Address;

import java.util.UUID;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static Address toEntity(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setCountry(dto.getCountry());
        address.setCity(dto.getCity());
        address.setStreet(dto.getStreet());
        address.setHouse(dto.getHouse());
        address.setFlat(dto.getFlat());
        return address;
    }

    public static AddressDto toDto(Address entity) {
        if (entity == null) {
            return null;
        }
        AddressDto dto = new AddressDto();
        dto.setCountry(entity.getCountry());
        dto.setCity(entity.getCity());
        dto.setStreet(entity.getStreet());
        dto.setHouse(entity.getHouse());
        dto.setFlat(entity.getFlat());
        return dto;
    }
}