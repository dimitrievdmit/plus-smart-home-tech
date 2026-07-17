package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.AddressDto;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final String currentAddress;

    public AddressDto getAddress() {
        return new AddressDto(currentAddress, currentAddress, currentAddress, currentAddress, currentAddress);
    }
}