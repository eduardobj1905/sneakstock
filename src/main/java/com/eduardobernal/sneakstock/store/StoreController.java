package com.eduardobernal.sneakstock.store;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // Solo las tiendas abiertas: son las únicas a las que se puede enviar un traspaso
    @GetMapping
    public List<StoreResponse> findOpenStores() {
        return storeService.findOpenStores();
    }
}
