package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.common.BusinessRuleException;
import com.eduardobernal.sneakstock.common.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la capa web: que cada error llegue al cliente con su código HTTP y su mensaje.
 * Solo arranca el controlador y el GlobalExceptionHandler; el servicio es un mock.
 */
@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    @DisplayName("Una venta sin código de barras ni unidades devuelve 400 con el error de cada campo")
    void invalidSaleReturns400() throws Exception {
        mockMvc.perform(post("/api/stock/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeNumber\": \"0003\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.barcode").value("El código de barras es obligatorio"))
                .andExpect(jsonPath("$.errors.units").value("Las unidades son obligatorias"));

        verifyNoInteractions(stockService);
    }

    @Test
    @DisplayName("Una regla de negocio incumplida devuelve 409 con el motivo")
    void businessRuleReturns409() throws Exception {
        doThrow(new BusinessRuleException("Stock insuficiente: hay 1 pares y se piden 5"))
                .when(stockService).sell("8400000000011", "0003", 5);

        mockMvc.perform(post("/api/stock/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"barcode\": \"8400000000011\", \"storeNumber\": \"0003\", \"units\": 5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Stock insuficiente: hay 1 pares y se piden 5"));
    }

    @Test
    @DisplayName("Algo que no existe devuelve 404 con el motivo")
    void notFoundReturns404() throws Exception {
        doThrow(new NotFoundException("No existe la tienda 9999"))
                .when(stockService).receive("8400000000011", "9999", 1);

        mockMvc.perform(post("/api/stock/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"barcode\": \"8400000000011\", \"storeNumber\": \"9999\", \"units\": 1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la tienda 9999"));
    }

    @Test
    @DisplayName("Una petición sin sentido para el negocio devuelve 400 con el motivo")
    void illegalArgumentReturns400() throws Exception {
        doThrow(new IllegalArgumentException("La tienda de origen y la de destino no pueden ser la misma"))
                .when(stockService).sendTransfer("8400000000011", "0002", "0002", 1);

        mockMvc.perform(post("/api/stock/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"barcode": "8400000000011", "originStoreNumber": "0002",
                                 "destinationStoreNumber": "0002", "units": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La tienda de origen y la de destino no pueden ser la misma"));
    }

    @Test
    @DisplayName("Un albarán sin zapatillas devuelve 400")
    void emptyDeliveryReturns400() throws Exception {
        mockMvc.perform(post("/api/stock/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryNote\": \"ALB-0045\", \"storeNumber\": \"0001\", \"lines\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lines").value("El albarán tiene que tener al menos una zapatilla"));

        verifyNoInteractions(stockService);
    }
}
