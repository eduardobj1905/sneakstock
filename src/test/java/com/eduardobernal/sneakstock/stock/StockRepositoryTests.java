package com.eduardobernal.sneakstock.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql("/sql/stock-disponible.sql")
class StockRepositoryTests {

    @Autowired
    private StockRepository stockRepository;

    @Test
    @DisplayName("Al escanear una caja devuelve solo tiendas abiertas con stock, de más a menos pares")
    void findAvailableByBarcode() {
        List<Stock> result = stockRepository.findAvailableByBarcode("0000000000001");

        assertThat(result)
                .extracting(stock -> stock.getStore().getStoreNumber())
                .containsExactly("T002", "T001");

        assertThat(result)
                .extracting(Stock::getQuantity)
                .containsExactly(5, 3);
    }
}