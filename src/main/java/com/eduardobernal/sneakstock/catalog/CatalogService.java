package com.eduardobernal.sneakstock.catalog;

import com.eduardobernal.sneakstock.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ProductVariantRepository productVariantRepository;

    @Transactional(readOnly = true)
    public VariantResponse findVariant(String barcode) {
        return productVariantRepository.findByBarcode(barcode)
                .map(VariantResponse::from)
                .orElseThrow(() -> new NotFoundException("No existe ninguna talla con el código " + barcode));
    }
}
