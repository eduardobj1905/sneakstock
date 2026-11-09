package com.eduardobernal.sneakstock.catalog;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Catálogo", description = "Zapatillas y tallas por código de barras")
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/{barcode}")
    public VariantResponse findVariant(@PathVariable String barcode) {
        return catalogService.findVariant(barcode);
    }
}
