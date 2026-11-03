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

    @Transactional
    public void sendTransfer(String barcode, String originStoreNumber, String destinationStoreNumber, int units) {
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

        stockMovementRepository.save(StockMovement.transferOut(origin, units, destination));
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