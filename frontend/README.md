# SneakStock · frontend

Aplicación Vue 3 + TypeScript + Tailwind CSS del proyecto. La descripción completa, cómo arrancarlo con el backend y los datos de ejemplo están en el [README principal](../README.md).

## Comandos

```bash
npm install          # instalar dependencias
npm run dev          # servidor de desarrollo en http://localhost:5173 (proxy /api → localhost:8080)
npm run test:unit    # tests con Vitest (modo vigilancia; añade -- --run para una sola pasada)
npm run type-check   # comprobación de tipos
npm run lint         # ESLint y Oxlint
npm run format       # formatear con Prettier
npm run build        # compilación para producción
```

## Estructura

```
src/
├── api/          llamadas al backend (stock, catálogo) y lectura de errores ProblemDetail
├── components/   piezas reutilizables: barra de navegación, tarjeta de la zapatilla
├── views/        pantallas: consultar, vender, entradas
├── router/       rutas de la aplicación
└── currentStore.ts   tienda en la que trabaja el dependiente (hasta que haya login)
```
