import { toError } from './http'

export interface StoreSummary {
  storeNumber: string
  name: string
}

// Tiendas abiertas de la cadena (las únicas a las que se puede enviar un traspaso)
export async function findOpenStores(): Promise<StoreSummary[]> {
  const response = await fetch('/api/stores')

  if (!response.ok) {
    throw await toError(response, 'No se han podido cargar las tiendas')
  }

  return response.json()
}
