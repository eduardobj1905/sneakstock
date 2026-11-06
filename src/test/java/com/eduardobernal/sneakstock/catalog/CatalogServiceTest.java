package com.eduardobernal.sneakstock.catalog;

import com.eduardobernal.sneakstock.common.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    @DisplayName("Devuelve el modelo y las tallas de un código de barras")
    void findVariant() {
        Product dunk = new Product("DD1391-100", "Nike", "Dunk Low", "White/Black", new BigDecimal("119.99"));
        ProductVariant us9 = new ProductVariant(dunk, "9", "42.5", "8400000000011");
        when(productVariantRepository.findByBarcode("8400000000011")).thenReturn(Optional.of(us9));

        VariantResponse response = catalogService.findVariant("8400000000011");

        assertThat(response).isEqualTo(
                new VariantResponse("8400000000011", "Nike", "Dunk Low", "White/Black", "9", "42.5"));
    }

    @Test
    @DisplayName("Un código de barras que no existe lanza NotFoundException")
    void findUnknownVariant() {
        when(productVariantRepository.findByBarcode("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.findVariant("999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }
}
