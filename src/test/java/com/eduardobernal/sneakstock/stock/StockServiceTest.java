package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.catalog.Product;
import com.eduardobernal.sneakstock.catalog.ProductVariant;
import com.eduardobernal.sneakstock.store.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.eduardobernal.sneakstock.catalog.ProductVariantRepository;
import com.eduardobernal.sneakstock.store.StoreRepository;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    @DisplayName("No vende si hay menos pares de los que se piden, y no apunta ningún movimiento")
    void sellWithInsufficientStock() {
        // Arrange: Goya tiene 1 par del Dunk US 9
        Product dunk = new Product("DD1391-100", "Nike", "Dunk Low", "White/Black", new BigDecimal("119.99"));
        ProductVariant us9 = new ProductVariant(dunk, "9", "42.5", "8400000000011");
        Store goya = new Store("0003", "Goya", "Calle Goya 50, Madrid", "+34 910 000 003");
        Stock stock = new Stock(us9, goya);
        stock.add(1);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0003"))
                .thenReturn(Optional.of(stock));

        // Act + Assert: intentar vender 5 lanza la excepción
        assertThatThrownBy(() -> stockService.sell("8400000000011", "0003", 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        // Assert: el stock no cambia y no se guarda ningún movimiento
        assertThat(stock.getQuantity()).isEqualTo(1);
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Vende si hay pares suficientes: resta el stock y apunta el movimiento")
    void sellWithEnoughStock() {
        // Arrange: Sol tiene 3 pares del Dunk US 9
        Product dunk = new Product("DD1391-100", "Nike", "Dunk Low", "White/Black", new BigDecimal("119.99"));
        ProductVariant us9 = new ProductVariant(dunk, "9", "42.5", "8400000000011");
        Store sol = new Store("0002", "Sol", "Puerta del Sol 5, Madrid", "+34 910 000 002");
        Stock stock = new Stock(us9, sol);
        stock.add(3);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0002"))
                .thenReturn(Optional.of(stock));

        // Act: vender 1 par
        stockService.sell("8400000000011", "0002", 1);

        // Assert: quedan 2 y se ha guardado un movimiento
        assertThat(stock.getQuantity()).isEqualTo(2);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }
    @Test
    @DisplayName("No vende si la tienda está cerrada, aunque tenga pares")
    void sellInClosedStore() {
        // Arrange: Alcalá tiene 6 pares del Dunk US 9, pero está cerrada
        Product dunk = new Product("DD1391-100", "Nike", "Dunk Low", "White/Black", new BigDecimal("119.99"));
        ProductVariant us9 = new ProductVariant(dunk, "9", "42.5", "8400000000011");
        Store alcala = new Store("0004", "Alcalá", "Calle Alcalá 100, Madrid", "+34 910 000 004");
        alcala.close();
        Stock stock = new Stock(us9, alcala);
        stock.add(6);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0004"))
                .thenReturn(Optional.of(stock));

        // Act + Assert: vender lanza la excepción de tienda cerrada
        assertThatThrownBy(() -> stockService.sell("8400000000011", "0004", 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("está cerrada");

        // Assert: el stock no cambia y no se guarda ningún movimiento
        assertThat(stock.getQuantity()).isEqualTo(6);
        verify(stockMovementRepository, never()).save(any());
    }
    @Test
    @DisplayName("Entrada de una talla que la tienda ya tenía: suma los pares sin crear fila nueva")
    void receiveExistingStock() {
        // Arrange: Gran Vía tiene 2 pares del Dunk US 10
        Product dunk = new Product("DD1391-100", "Nike", "Dunk Low", "White/Black", new BigDecimal("119.99"));
        ProductVariant us10 = new ProductVariant(dunk, "10", "44", "8400000000028");
        Store granVia = new Store("0001", "Gran Vía", "Calle Gran Vía 30, Madrid", "+34 910 000 001");
        Stock stock = new Stock(us10, granVia);
        stock.add(2);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000028", "0001"))
                .thenReturn(Optional.of(stock));

        // Act: llegan 4 pares
        stockService.receive("8400000000028", "0001", 4);

        // Assert: hay 6, se apunta el movimiento y NO se crea ninguna fila de stock
        assertThat(stock.getQuantity()).isEqualTo(6);
        verify(stockMovementRepository).save(any(StockMovement.class));
        verify(stockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Entrada de una talla que la tienda nunca ha tenido: crea la fila con los pares recibidos")
    void receiveNewSize() {
        // Arrange: Gran Vía nunca ha tenido el Samba US 9
        Product samba = new Product("IE3437", "Adidas", "Samba OG", "White/Black", new BigDecimal("120.00"));
        ProductVariant us9 = new ProductVariant(samba, "9", "42 2/3", "8400000000035");
        Store granVia = new Store("0001", "Gran Vía", "Calle Gran Vía 30, Madrid", "+34 910 000 001");

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000035", "0001"))
                .thenReturn(Optional.empty());
        when(productVariantRepository.findByBarcode("8400000000035"))
                .thenReturn(Optional.of(us9));
        when(storeRepository.findByStoreNumber("0001"))
                .thenReturn(Optional.of(granVia));
        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: llegan 4 pares
        stockService.receive("8400000000035", "0001", 4);

        // Assert: se ha creado una fila de stock en Gran Vía con 4 pares, y se apunta el movimiento
        ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(captor.capture());
        Stock created = captor.getValue();

        assertThat(created.getStore()).isSameAs(granVia);
        assertThat(created.getQuantity()).isEqualTo(4);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }
}