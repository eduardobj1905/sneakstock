package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.catalog.ProductVariant;
import com.eduardobernal.sneakstock.catalog.ProductVariantRepository;
import com.eduardobernal.sneakstock.common.BusinessRuleException;
import com.eduardobernal.sneakstock.common.NotFoundException;
import com.eduardobernal.sneakstock.store.Store;
import com.eduardobernal.sneakstock.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<StoreStockResponse> findAvailableByBarcode(String barcode) {
        return stockRepository.findAvailableByBarcode(barcode).stream()
                .map(StoreStockResponse::from)
                .toList();
    }

    /**
     * Pares de una talla en una tienda concreta. Si la tienda nunca ha tenido esa talla, devuelve 0.
     */
    @Transactional(readOnly = true)
    public StoreStockResponse findInStore(String barcode, String storeNumber) {
        Store store = storeRepository.findByStoreNumber(storeNumber)
                .orElseThrow(() -> new NotFoundException("No existe la tienda " + storeNumber));

        int quantity = stockRepository.findByVariantBarcodeAndStoreStoreNumber(barcode, storeNumber)
                .map(Stock::getQuantity)
                .orElse(0);

        return StoreStockResponse.of(store, quantity);
    }

    @Transactional
    public void sell(String barcode, String storeNumber, int units) {
        Stock stock = stockRepository.findByVariantBarcodeAndStoreStoreNumber(barcode, storeNumber)
                .orElseThrow(() -> new NotFoundException(
                        "La tienda " + storeNumber + " no tiene la talla " + barcode));

        if (!stock.getStore().isActive()) {
            throw new BusinessRuleException("La tienda " + storeNumber + " está cerrada");
        }

        stock.remove(units);

        stockMovementRepository.save(new StockMovement(stock, MovementType.SALE, -units, null));
    }

    @Transactional
    public void receive(String barcode, String storeNumber, int units) {
        Stock stock = stockRepository.findByVariantBarcodeAndStoreStoreNumber(barcode, storeNumber)
                .orElseGet(() -> createStock(barcode, storeNumber));

        if (!stock.getStore().isActive()) {
            throw new BusinessRuleException("La tienda " + storeNumber + " está cerrada");
        }

        stock.add(units);

        stockMovementRepository.save(new StockMovement(stock, MovementType.ENTRY, units, null));
    }

    /**
     * Registra todas las zapatillas de un albarán en una sola transacción:
     * si una línea falla (un código que no existe, por ejemplo), no entra nada del envío.
     */
    @Transactional
    public void receiveDelivery(String deliveryNote, String storeNumber, List<DeliveryRequest.Line> lines) {
        if (stockMovementRepository.existsByDeliveryNote(deliveryNote)) {
            throw new BusinessRuleException("El albarán " + deliveryNote + " ya ha sido registrado");
        }

        for (DeliveryRequest.Line line : lines) {
            Stock stock = stockRepository.findByVariantBarcodeAndStoreStoreNumber(line.barcode(), storeNumber)
                    .orElseGet(() -> createStock(line.barcode(), storeNumber));

            if (!stock.getStore().isActive()) {
                throw new BusinessRuleException("La tienda " + storeNumber + " está cerrada");
            }

            stock.add(line.units());

            stockMovementRepository.save(StockMovement.deliveryEntry(stock, line.units(), deliveryNote));
        }
    }

    @Transactional
    public Long sendTransfer(String barcode, String originStoreNumber, String destinationStoreNumber, int units) {
        if (originStoreNumber.equals(destinationStoreNumber)) {
            throw new IllegalArgumentException("La tienda de origen y la de destino no pueden ser la misma");
        }

        Stock origin = stockRepository.findByVariantBarcodeAndStoreStoreNumber(barcode, originStoreNumber)
                .orElseThrow(() -> new NotFoundException(
                        "La tienda " + originStoreNumber + " no tiene la talla " + barcode));

        if (!origin.getStore().isActive()) {
            throw new BusinessRuleException("La tienda " + originStoreNumber + " está cerrada");
        }

        Store destination = storeRepository.findByStoreNumber(destinationStoreNumber)
                .orElseThrow(() -> new NotFoundException("No existe la tienda " + destinationStoreNumber));

        if (!destination.isActive()) {
            throw new BusinessRuleException("La tienda destino " + destinationStoreNumber + " está cerrada");
        }

        origin.remove(units);

        // El id del envío es el número de traspaso que se apunta en la caja
        return stockMovementRepository.save(StockMovement.transferOut(origin, units, destination)).getId();
    }

    /**
     * Un traspaso por su número, para ver qué es y de dónde viene antes de recibirlo.
     */
    @Transactional(readOnly = true)
    public TransferResponse findTransfer(Long transferId) {
        StockMovement transferOut = stockMovementRepository.findById(transferId)
                .filter(movement -> movement.getType() == MovementType.TRANSFER_OUT)
                .orElseThrow(() -> new NotFoundException("No existe el traspaso " + transferId));

        return TransferResponse.from(transferOut, stockMovementRepository.existsByTransferOutId(transferId));
    }

    @Transactional
    public void receiveTransfer(Long transferId, String storeNumber, int units) {
        StockMovement transferOut = stockMovementRepository.findById(transferId)
                .orElseThrow(() -> new NotFoundException("No existe el traspaso " + transferId));

        if (transferOut.getType() != MovementType.TRANSFER_OUT) {
            throw new IllegalArgumentException("El movimiento " + transferId + " no es un envío de traspaso");
        }

        if (!transferOut.getDestinationStore().getStoreNumber().equals(storeNumber)) {
            throw new BusinessRuleException("El traspaso " + transferId + " no va dirigido a la tienda " + storeNumber);
        }

        if (stockMovementRepository.existsByTransferOutId(transferId)) {
            throw new BusinessRuleException("El traspaso " + transferId + " ya ha sido recibido");
        }

        int sent = -transferOut.getQuantity();
        if (units <= 0 || units > sent) {
            throw new IllegalArgumentException("Se pueden recibir entre 1 y " + sent + " pares");
        }

        String barcode = transferOut.getStock().getVariant().getBarcode();
        Stock destination = stockRepository.findByVariantBarcodeAndStoreStoreNumber(barcode, storeNumber)
                .orElseGet(() -> createStock(barcode, storeNumber));

        destination.add(units);

        stockMovementRepository.save(StockMovement.transferIn(destination, units, transferOut));
    }

    private Stock createStock(String barcode, String storeNumber) {
        ProductVariant variant = productVariantRepository.findByBarcode(barcode)
                .orElseThrow(() -> new NotFoundException("No existe ninguna talla con el código " + barcode));
        Store store = storeRepository.findByStoreNumber(storeNumber)
                .orElseThrow(() -> new NotFoundException("No existe la tienda " + storeNumber));

        return stockRepository.save(new Stock(variant, store));
    }
}