# 0001 — Reiniciar el modelo desde V1

- **Fecha:** 2026-10-19
- **Estado:** aceptada

## Contexto

En las primeras sesiones se generó de una vez un primer borrador: migraciones V2–V4 (variantes, stock y movimientos), entidades, servicios, controladores y tests. Nunca se llegó a commitear ni a subir a GitHub.

Ese borrador tenía dos problemas:

1. **Se quedaba corto para el dominio.** Guardaba el stock por talla, pero no por tienda. SneakStock es para una cadena de tiendas, con traspasos y unidades con desperfecto.
2. **No estaba escrito por mí.** El objetivo del proyecto es poder defender cada línea en una entrevista técnica.

## Decisión

Se aparta el borrador a una carpeta fuera del repositorio (`../sneakstock-referencia/`), solo para consultarlo. El repositorio vuelve a tener únicamente:

- La migración V1 (`product`).
- La configuración base.

El modelo de dominio completo se diseñará en la semana 2, con nuevas migraciones a partir de V2.

Como las migraciones V2–V4 solo existían en la base de datos local, rehacerlas no rompe ningún otro entorno. Se recreó la base de datos local desde cero.

## Alternativas descartadas

- **Conservar el borrador y adaptarlo con nuevas migraciones (V5, V6…).** Se descartó porque el historial de Flyway contaría una historia confusa: crear el stock sin tienda y justo después cambiarlo. Además, gran parte del código no sería mío.
- **Borrar el borrador sin guardarlo.** Se descartó porque sirve de referencia para piezas que se repetirán, como el manejo global de errores.
