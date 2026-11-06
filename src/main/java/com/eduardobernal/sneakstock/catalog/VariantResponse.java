package com.eduardobernal.sneakstock.catalog;

public record VariantResponse(
        String barcode,
        String brand,
        String model,
        String color,
        String sizeUs,
        String sizeEu
) {

    public static VariantResponse from(ProductVariant variant) {
        Product product = variant.getProduct();
        return new VariantResponse(
                variant.getBarcode(),
                product.getBrand(),
                product.getModel(),
                product.getColor(),
                variant.getSizeUs(),
                variant.getSizeEu()
        );
    }
}
