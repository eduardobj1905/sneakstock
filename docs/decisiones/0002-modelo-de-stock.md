# 0002 — Modelo de stock

- **Fecha:** 2026-10-22
- **Estado:** aceptada

## Contexto

SneakStock gestiona el stock de una cadena de tiendas de zapatillas. El alcance se limita a lo que necesita un dependiente en su día a día:

1. Ver el stock de mi tienda por talla.
2. Si no hay en mi tienda, ver en qué otras tiendas hay.
3. Hacer traspasos sencillos entre tiendas.

El objetivo es un modelo pequeño que entienda y pueda defender entero, no un sistema completo de inventario. Las tablas se crean en las migraciones V2–V5:

```
product ──< product_variant ──< stock >── store
                                  │
                           stock_movement
```

## Decisiones

### 1. Las tiendas no se borran: columna `active`

Una tienda cerrada tiene detrás ventas y traspasos que apuntan a ella. Si se borra, o la base de datos lo impide por las claves foráneas, o se pierde el historial. Además, la empresa está obligada a conservar sus registros de ventas. Por eso cerrar una tienda es poner `active = false`: deja de aparecer en la app, pero su historial sigue intacto (borrado lógico).

**Descartado:**
- *Borrar la tienda.* Rompe el historial.
- *Guardar la fecha de cierre (`closed_at`).* No la necesitamos (YAGNI). Si hiciera falta, se añadiría con una migración nueva.

### 2. Tallas US y EU, cada una en su columna

En tienda se trabaja con la talla US, pero la equivalencia US → EU cambia según la marca. Por ejemplo, una US 9 es una EU 42.5 en Nike y una EU 42 2/3 en Adidas. Por eso se guardan las dos tal como vienen en la caja.

**Descartado:**
- *Solo la talla US.* No permitiría mostrar la EU exacta al cliente.
- *Calcular la EU con una tabla de conversión.* Sería distinta para cada marca.

### 3. Dos códigos distintos: `style_code` y `barcode`

- `product.style_code` es el código genérico del modelo, el que se teclea para buscarlo.
- `product_variant.barcode` es el código de barras de la caja, que es distinto en cada talla. Al escanear una caja se sabe directamente qué talla es.

### 4. Si no se hacen cuentas con un dato, es texto

`store_number`, `phone`, `barcode` y las tallas son `VARCHAR`. Un número entero perdería los ceros a la izquierda ("0423" se guardaría como 423) y no admite símbolos como `+`. En cambio, `stock.quantity` es `INT`, porque con el stock sí se suma y se resta.

### 5. Stock actual más historial de movimientos

- La tabla `stock` guarda la cantidad actual, con una fila por talla y tienda. Así consultar es inmediato: al escanear una caja se lee una sola fila.
- La tabla `stock_movement` guarda cada cambio con su signo (+6 si entra mercancía, -1 si se vende un par). Así se puede explicar cada número del stock.

Las dos tablas se actualizan siempre en la misma transacción. El `CHECK (quantity >= 0)` es la última defensa contra un stock negativo.

**Descartado:**
- *Solo la tabla de stock.* Si el stock no cuadra, no se sabría por qué.
- *Solo el historial, calculando el stock al consultarlo.* Habría que sumar miles de filas en cada consulta, y sería más difícil impedir que dos ventas se lleven el último par a la vez.

### 6. El traspaso son dos movimientos enlazados

Funciona así:

- **Al enviar:** se crea un `TRANSFER_OUT` con cantidad negativa en la tienda origen y con la tienda destino en `destination_store_id`. El stock sale de la tienda en ese momento.
- **Al recibir:** la tienda destino crea un `TRANSFER_IN` con los pares que han llegado de verdad, y lo enlaza con el envío mediante `transfer_out_id`.
- **Si faltan pares:** se ve comparando las dos cantidades (por ejemplo, se enviaron 5 y llegaron 4).
- **Pendiente de recibir:** es un `TRANSFER_OUT` al que todavía no apunta ningún `TRANSFER_IN`.

**Descartado:**
- *Una tabla `transfer` aparte, con estados.* Añade complejidad que no necesitamos.
- *Un traspaso inmediato.* No refleja que los pares tardan días en llegar.

## Fuera de alcance por ahora

- **Tiendas cercanas.** De momento se muestran todas las tiendas con stock. Más adelante se añadiría `city` a `store` con un `ALTER TABLE`.
- **Desperfectos, devoluciones como tipo propio y roles de usuario.** Solo si sobra tiempo al final.
