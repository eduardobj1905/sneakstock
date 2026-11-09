package com.eduardobernal.sneakstock.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("Devuelve las tiendas abiertas con su número y nombre")
    void findOpenStores() {
        when(storeRepository.findByActiveTrueOrderByStoreNumber()).thenReturn(List.of(
                new Store("0001", "Gran Vía", "Calle Gran Vía 30, Madrid", "+34 910 000 001"),
                new Store("0002", "Sol", "Puerta del Sol 5, Madrid", "+34 910 000 002")));

        assertThat(storeService.findOpenStores()).containsExactly(
                new StoreResponse("0001", "Gran Vía"),
                new StoreResponse("0002", "Sol"));
    }
}
