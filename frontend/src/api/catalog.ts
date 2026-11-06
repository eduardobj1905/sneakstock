import { toError } from './http'

// Lo que devuelve GET /api/variants/{barcode}: qué zapatilla y qué talla es una caja
export interface Variant {
  barcode: string
  brand: string
  model: string
  color: string
  sizeUs: string
  sizeEu: string
}

export async function findVariant(barcode: string): Promise<Variant> {
  const response = await fetch(`/api/variants/${encodeURIComponent(barcode)}`)

  if (!response.ok) {
    throw await toError(response, 'No se ha podido consultar el código de barras')
  }

  return response.json()
}

// "Nike Dunk Low · US 9 / EU 42.5"
export function describeVariant(variant: Variant): string {
  return `${variant.brand} ${variant.model} · US ${variant.sizeUs} / EU ${variant.sizeEu}`
}
