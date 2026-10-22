# Modelo de datos

Esquema de la base de datos, creado con las migraciones de Flyway V1–V5 (`src/main/resources/db/migration`). Las decisiones detrás de cada tabla están en [0002 — Modelo de stock](decisiones/0002-modelo-de-stock.md).

```mermaid
erDiagram
    product ||--o{ product_variant : "tiene tallas"
    product_variant ||--o{ stock : "se guarda en"
    store ||--o{ stock : "tiene"
    stock ||--o{ stock_movement : "registra"
    store |o--o{ stock_movement : "es destino de"
    stock_movement |o--o{ stock_movement : "recibe el envío"

    product {
        bigint id PK
        varchar style_code UK "código genérico del modelo"
        varchar brand
        varchar model
        varchar color
        decimal price "mayor que 0"
        datetime created_at
    }

    product_variant {
        bigint id PK
        bigint product_id FK
        varchar size_us
        varchar size_eu
        varchar barcode UK "código de barras de la caja"
        datetime created_at
    }

    store {
        bigint id PK
        varchar store_number UK "número asignado por la empresa"
        varchar name
        varchar address
        varchar phone
        boolean active "false = cerrada (borrado lógico)"
        datetime created_at
    }

    stock {
        bigint id PK
        bigint variant_id FK
        bigint store_id FK
        int quantity "nunca negativa"
        datetime created_at
        datetime updated_at
    }

    stock_movement {
        bigint id PK
        bigint stock_id FK
        varchar type "ENTRY, SALE, ADJUSTMENT, TRANSFER_OUT, TRANSFER_IN"
        int quantity "con signo, nunca 0"
        bigint destination_store_id FK "solo en TRANSFER_OUT"
        bigint transfer_out_id FK "solo en TRANSFER_IN"
        varchar note
        datetime created_at
    }
```

## Cómo se lee

- `||--o{` significa "uno a muchos". Por ejemplo, un modelo tiene cero o muchas tallas, y cada talla pertenece a un solo modelo.
- `|o--o{` significa que la relación es opcional. Por ejemplo, un movimiento solo tiene tienda destino si es un `TRANSFER_OUT`.
- `PK` es la clave primaria, `FK` una clave foránea y `UK` un valor único.

## Reglas que el diagrama no muestra

| Tabla | Regla | Para qué |
|---|---|---|
| `product_variant` | `UNIQUE (product_id, size_us)` | Un modelo no puede tener la misma talla dos veces. |
| `stock` | `UNIQUE (variant_id, store_id)` | Una sola fila de stock por talla y tienda. |
| `stock` | `CHECK (quantity >= 0)` | La base de datos rechaza un stock negativo. |
| `stock_movement` | `CHECK (type IN (...))` | Solo se admiten los cinco tipos de movimiento. |
| `stock_movement` | `CHECK (quantity <> 0)` | No se guardan movimientos que no mueven nada. |
| `product` | `CHECK (price > 0)` | El precio tiene que ser positivo. |
