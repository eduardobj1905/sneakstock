package com.eduardobernal.sneakstock.stock;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StoreStockResponse> findAvailable(@RequestParam String barcode) {
        return stockService.findAvailableByBarcode(barcode);
    }

    @GetMapping("/stores/{storeNumber}")
    public StoreStockResponse findInStore(@PathVariable String storeNumber, @RequestParam String barcode) {
        return stockService.findInStore(barcode, storeNumber);
    }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public void sell(@Valid @RequestBody SaleRequest request) {
        stockService.sell(request.barcode(), request.storeNumber(), request.units());
    }

    @PostMapping("/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public void receive(@Valid @RequestBody EntryRequest request) {
        stockService.receive(request.barcode(), request.storeNumber(), request.units());
    }

    @PostMapping("/deliveries")
    @ResponseStatus(HttpStatus.CREATED)
    public void receiveDelivery(@Valid @RequestBody DeliveryRequest request) {
        stockService.receiveDelivery(request.deliveryNote(), request.storeNumber(), request.lines());
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferCreatedResponse sendTransfer(@Valid @RequestBody TransferRequest request) {
        Long transferId = stockService.sendTransfer(request.barcode(), request.originStoreNumber(),
                request.destinationStoreNumber(), request.units());
        return new TransferCreatedResponse(transferId);
    }

    @GetMapping("/transfers/{transferId}")
    public TransferResponse findTransfer(@PathVariable Long transferId) {
        return stockService.findTransfer(transferId);
    }

    @PostMapping("/transfers/{transferId}/receptions")
    @ResponseStatus(HttpStatus.CREATED)
    public void receiveTransfer(@PathVariable Long transferId,
                                @Valid @RequestBody TransferReceptionRequest request) {
        stockService.receiveTransfer(transferId, request.storeNumber(), request.units());
    }
}