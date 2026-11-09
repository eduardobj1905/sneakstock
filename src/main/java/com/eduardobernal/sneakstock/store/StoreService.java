package com.eduardobernal.sneakstock.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<StoreResponse> findOpenStores() {
        return storeRepository.findByActiveTrueOrderByStoreNumber().stream()
                .map(StoreResponse::from)
                .toList();
    }
}
