package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.catalog.Product;
import com.eduardobernal.sneakstock.catalog.ProductVariant;
import com.eduardobernal.sneakstock.catalog.ProductVariantRepository;
import com.eduardobernal.sneakstock.common.BusinessRuleException;
import com.eduardobernal.sneakstock.common.NotFoundException;
import com.eduardobernal.sneakstock.store.Store;
import com.eduardobernal.sneakstock.store.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    // Catálogo: Dunk Low en US 9 y US 10, Samba OG en US 9
    private ProductVariant dunkUs9;
    private ProductVariant dunkUs10;
    private ProductVariant sambaUs9;

    // Tiendas: Alcalá está cerrada
    private Store granVia;
    private Store sol;
    private Store goya;
    private Store alcala;

    @BeforeEach
    void setUp() {
        Product dunk = new Product("DD1391-100", "Nike", "Dunk Low", "White/Black", new BigDecimal("119.99"));
        Product samba = new Product("IE3437", "Adidas", "Samba OG", "White/Black", new BigDecimal("120.00"));
        dunkUs9 = new ProductVariant(dunk, "9", "42.5", "8400000000011");
        dunkUs10 = new ProductVariant(dunk, "10", "44", "8400000000028");
        sambaUs9 = new ProductVariant(samba, "9", "42 2/3", "8400000000035");

        granVia = new Store("0001", "Gran Vía", "Calle Gran Vía 30, Madrid", "+34 910 000 001");
        sol = new Store("0002", "Sol", "Puerta del Sol 5, Madrid", "+34 910 000 002");
        goya = new Store("0003", "Goya", "Calle Goya 50, Madrid", "+34 910 000 003");
        alcala = new Store("0004", "Alcalá", "Calle Alcalá 100, Madrid", "+34 910 000 004");
        alcala.close();
    }

    // --- Venta ---

    @Test
    @DisplayName("No vende si hay menos pares de los que se piden, y no apunta ningún movimiento")
    void sellWithInsufficientStock() {
        // Arrange: Goya tiene 1 par del Dunk US 9
        Stock stock = new Stock(dunkUs9, goya);
        stock.add(1);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0003"))
                .thenReturn(Optional.of(stock));

        // Act + Assert: intentar vender 5 lanza la excepción
        assertThatThrownBy(() -> stockService.sell("8400000000011", "0003", 5))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuficiente");

        // Assert: el stock no cambia y no se guarda ningún movimiento
        assertThat(stock.getQuantity()).isEqualTo(1);
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Vende si hay pares suficientes: resta el stock y apunta el movimiento")
    void sellWithEnoughStock() {
        // Arrange: Sol tiene 3 pares del Dunk US 9
        Stock stock = new Stock(dunkUs9, sol);
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
        // Arrange: Alcalá (cerrada) tiene 6 pares del Dunk US 9
        Stock stock = new Stock(dunkUs9, alcala);
        stock.add(6);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0004"))
                .thenReturn(Optional.of(stock));

        // Act + Assert: vender lanza la excepción de tienda cerrada
        assertThatThrownBy(() -> stockService.sell("8400000000011", "0004", 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("está cerrada");

        // Assert: el stock no cambia y no se guarda ningún movimiento
        assertThat(stock.getQuantity()).isEqualTo(6);
        verify(stockMovementRepository, never()).save(any());
    }

    // --- Entrada de mercancía ---

    @Test
    @DisplayName("Entrada de una talla que la tienda ya tenía: suma los pares sin crear fila nueva")
    void receiveExistingStock() {
        // Arrange: Gran Vía tiene 2 pares del Dunk US 10
        Stock stock = new Stock(dunkUs10, granVia);
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
        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000035", "0001"))
                .thenReturn(Optional.empty());
        when(productVariantRepository.findByBarcode("8400000000035"))
                .thenReturn(Optional.of(sambaUs9));
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

    // --- Envío de traspaso ---

    @Test
    @DisplayName("Envía un traspaso: resta en origen y apunta TRANSFER_OUT con la tienda destino")
    void sendTransfer() {
        // Arrange: Sol tiene 2 pares del Dunk US 9
        Stock solStock = new Stock(dunkUs9, sol);
        solStock.add(2);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0002"))
                .thenReturn(Optional.of(solStock));
        when(storeRepository.findByStoreNumber("0001"))
                .thenReturn(Optional.of(granVia));
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Sol envía 1 par a Gran Vía
        stockService.sendTransfer("8400000000011", "0002", "0001", 1);

        // Assert: Sol pasa a 1 y el movimiento guardado es correcto
        assertThat(solStock.getQuantity()).isEqualTo(1);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertThat(movement.getType()).isEqualTo(MovementType.TRANSFER_OUT);
        assertThat(movement.getQuantity()).isEqualTo(-1);
        assertThat(movement.getDestinationStore()).isSameAs(granVia);
    }

    @Test
    @DisplayName("No envía un traspaso a la misma tienda, y ni siquiera consulta la base de datos")
    void sendTransferToSameStore() {
        assertThatThrownBy(() -> stockService.sendTransfer("8400000000011", "0002", "0002", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden ser la misma");

        verifyNoInteractions(stockRepository, storeRepository, stockMovementRepository);
    }

    @Test
    @DisplayName("No envía un traspaso a una tienda cerrada: ni resta ni apunta movimiento")
    void sendTransferToClosedStore() {
        // Arrange: Sol tiene 2 pares; Alcalá está cerrada
        Stock solStock = new Stock(dunkUs9, sol);
        solStock.add(2);

        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0002"))
                .thenReturn(Optional.of(solStock));
        when(storeRepository.findByStoreNumber("0004"))
                .thenReturn(Optional.of(alcala));

        // Act + Assert
        assertThatThrownBy(() -> stockService.sendTransfer("8400000000011", "0002", "0004", 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("destino");

        assertThat(solStock.getQuantity()).isEqualTo(2);
        verify(stockMovementRepository, never()).save(any());
    }

    // --- Recepción de traspaso ---

    @Test
    @DisplayName("Recibe un traspaso completo: suma en destino y apunta TRANSFER_IN enlazado al envío")
    void receiveTransfer() {
        // Arrange: Sol envió 2 pares del Dunk US 9 a Gran Vía (envío 3); Gran Vía tiene 0
        StockMovement transferOut = StockMovement.transferOut(new Stock(dunkUs9, sol), 2, granVia);
        Stock granViaStock = new Stock(dunkUs9, granVia);

        when(stockMovementRepository.findById(3L)).thenReturn(Optional.of(transferOut));
        when(stockMovementRepository.existsByTransferOutId(3L)).thenReturn(false);
        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0001"))
                .thenReturn(Optional.of(granViaStock));

        // Act: Gran Vía recibe los 2 pares
        stockService.receiveTransfer(3L, "0001", 2);

        // Assert: Gran Vía pasa a 2 y el movimiento va enlazado al envío, sin nota
        assertThat(granViaStock.getQuantity()).isEqualTo(2);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement transferIn = captor.getValue();

        assertThat(transferIn.getType()).isEqualTo(MovementType.TRANSFER_IN);
        assertThat(transferIn.getQuantity()).isEqualTo(2);
        assertThat(transferIn.getTransferOut()).isSameAs(transferOut);
        assertThat(transferIn.getNote()).isNull();
    }

    @Test
    @DisplayName("Recibe un traspaso incompleto: suma solo los pares que llegan y apunta cuántos faltan")
    void receiveTransferWithMissingPairs() {
        // Arrange: Sol envió 3 pares a Gran Vía (envío 3), pero solo llegan 2
        StockMovement transferOut = StockMovement.transferOut(new Stock(dunkUs9, sol), 3, granVia);
        Stock granViaStock = new Stock(dunkUs9, granVia);

        when(stockMovementRepository.findById(3L)).thenReturn(Optional.of(transferOut));
        when(stockMovementRepository.existsByTransferOutId(3L)).thenReturn(false);
        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0001"))
                .thenReturn(Optional.of(granViaStock));

        // Act
        stockService.receiveTransfer(3L, "0001", 2);

        // Assert: entran 2 y la nota deja constancia del par que falta
        assertThat(granViaStock.getQuantity()).isEqualTo(2);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getNote()).isEqualTo("Faltan 1 de 3 pares enviados");
    }

    @Test
    @DisplayName("No recibe dos veces el mismo traspaso")
    void receiveTransferTwice() {
        // Arrange: el envío 3 ya tiene su recepción
        StockMovement transferOut = StockMovement.transferOut(new Stock(dunkUs9, sol), 1, granVia);

        when(stockMovementRepository.findById(3L)).thenReturn(Optional.of(transferOut));
        when(stockMovementRepository.existsByTransferOutId(3L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> stockService.receiveTransfer(3L, "0001", 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya ha sido recibido");

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Solo la tienda destino puede recibir el traspaso")
    void receiveTransferInWrongStore() {
        // Arrange: el envío 3 va a Gran Vía, pero intenta recibirlo Goya
        StockMovement transferOut = StockMovement.transferOut(new Stock(dunkUs9, sol), 1, granVia);

        when(stockMovementRepository.findById(3L)).thenReturn(Optional.of(transferOut));

        // Act + Assert
        assertThatThrownBy(() -> stockService.receiveTransfer(3L, "0003", 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no va dirigido a la tienda 0003");

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("No recibe más pares de los que se enviaron")
    void receiveMorePairsThanSent() {
        // Arrange: se enviaron 2 pares a Gran Vía
        StockMovement transferOut = StockMovement.transferOut(new Stock(dunkUs9, sol), 2, granVia);

        when(stockMovementRepository.findById(3L)).thenReturn(Optional.of(transferOut));
        when(stockMovementRepository.existsByTransferOutId(3L)).thenReturn(false);

        // Act + Assert: intentar recibir 3
        assertThatThrownBy(() -> stockService.receiveTransfer(3L, "0001", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1 y 2");

        verify(stockMovementRepository, never()).save(any());
    }

    // --- Recepción de albarán ---

    @Test
    @DisplayName("Registra todas las líneas de un albarán y guarda su número en cada movimiento")
    void receiveDelivery() {
        // Arrange: Gran Vía tiene 2 pares del Dunk US 10 y 0 del Dunk US 9
        Stock us9Stock = new Stock(dunkUs9, granVia);
        Stock us10Stock = new Stock(dunkUs10, granVia);
        us10Stock.add(2);

        when(stockMovementRepository.existsByDeliveryNote("ALB-0045")).thenReturn(false);
        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0001"))
                .thenReturn(Optional.of(us9Stock));
        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000028", "0001"))
                .thenReturn(Optional.of(us10Stock));

        // Act: llegan 3 del US 9 y 4 del US 10
        stockService.receiveDelivery("ALB-0045", "0001", List.of(
                new DeliveryRequest.Line("8400000000011", 3),
                new DeliveryRequest.Line("8400000000028", 4)));

        // Assert: se suman los pares y cada movimiento lleva el número de albarán
        assertThat(us9Stock.getQuantity()).isEqualTo(3);
        assertThat(us10Stock.getQuantity()).isEqualTo(6);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(StockMovement::getType, StockMovement::getDeliveryNote)
                .containsOnly(tuple(MovementType.ENTRY, "ALB-0045"));
    }

    @Test
    @DisplayName("No registra dos veces el mismo albarán")
    void receiveDeliveryTwice() {
        when(stockMovementRepository.existsByDeliveryNote("ALB-0045")).thenReturn(true);

        assertThatThrownBy(() -> stockService.receiveDelivery("ALB-0045", "0001",
                List.of(new DeliveryRequest.Line("8400000000011", 1))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya ha sido registrado");

        verifyNoInteractions(stockRepository);
        verify(stockMovementRepository, never()).save(any());
    }

    // --- Consulta del stock en una tienda ---

    @Test
    @DisplayName("Devuelve los pares de una talla en una tienda")
    void findInStore() {
        Stock stock = new Stock(dunkUs9, granVia);
        stock.add(3);
        when(storeRepository.findByStoreNumber("0001")).thenReturn(Optional.of(granVia));
        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000011", "0001"))
                .thenReturn(Optional.of(stock));

        StoreStockResponse response = stockService.findInStore("8400000000011", "0001");

        assertThat(response.storeName()).isEqualTo("Gran Vía");
        assertThat(response.quantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Si la tienda nunca ha tenido esa talla, devuelve 0 pares en lugar de un error")
    void findInStoreWithoutStock() {
        when(storeRepository.findByStoreNumber("0001")).thenReturn(Optional.of(granVia));
        when(stockRepository.findByVariantBarcodeAndStoreStoreNumber("8400000000035", "0001"))
                .thenReturn(Optional.empty());

        StoreStockResponse response = stockService.findInStore("8400000000035", "0001");

        assertThat(response.quantity()).isZero();
    }

    @Test
    @DisplayName("Una tienda que no existe lanza NotFoundException")
    void findInUnknownStore() {
        when(storeRepository.findByStoreNumber("9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.findInStore("8400000000011", "9999"))
                .isInstanceOf(NotFoundException.class);
    }

    // --- Consulta de un traspaso ---

    @Test
    @DisplayName("Devuelve un traspaso con su origen, destino, zapatilla y pares enviados")
    void findTransfer() {
        StockMovement transferOut = StockMovement.transferOut(new Stock(dunkUs9, sol), 2, granVia);
        when(stockMovementRepository.findById(3L)).thenReturn(Optional.of(transferOut));
        when(stockMovementRepository.existsByTransferOutId(3L)).thenReturn(false);

        TransferResponse response = stockService.findTransfer(3L);

        assertThat(response.originStoreName()).isEqualTo("Sol");
        assertThat(response.destinationStoreNumber()).isEqualTo("0001");
        assertThat(response.variant().model()).isEqualTo("Dunk Low");
        assertThat(response.sentUnits()).isEqualTo(2);
        assertThat(response.received()).isFalse();
    }

    @Test
    @DisplayName("Un movimiento que no es un envío de traspaso se trata como si no existiera")
    void findTransferThatIsASale() {
        Stock stock = new Stock(dunkUs9, sol);
        StockMovement sale = new StockMovement(stock, MovementType.SALE, -1, null);
        when(stockMovementRepository.findById(5L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> stockService.findTransfer(5L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe el traspaso 5");
    }
}
