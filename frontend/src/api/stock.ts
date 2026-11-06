import { toError } from './http'

// Lo que devuelve GET /api/stock: una tienda con pares de la talla buscada
export interface StoreStock {
  storeNumber: string
  storeName: string
  phone: string
  quantity: number
}

export async function findAvailableStock(barcode: string): Promise<StoreStock[]> {
  const response = await fetch(`/api/stock?barcode=${encodeURIComponent(barcode)}`)

  if (!response.ok) {
    throw await toError(response, 'No se ha podido consultar el stock')
  }

  return response.json()
}

export async function findStockInStore(barcode: string, storeNumber: string): Promise<StoreStock> {
  const response = await fetch(
    `/api/stock/stores/${encodeURIComponent(storeNumber)}?barcode=${encodeURIComponent(barcode)}`,
  )

  if (!response.ok) {
    throw await toError(response, 'No se ha podido consultar el stock de la tienda')
  }

  return response.json()
}

export async function sell(barcode: string, storeNumber: string, units: number): Promise<void> {
  const response = await fetch('/api/stock/sales', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ barcode, storeNumber, units }),
  })

  if (!response.ok) {
    throw await toError(response, 'No se ha podido registrar la venta')
  }
}

export async function receive(barcode: string, storeNumber: string, units: number): Promise<void> {
  const response = await fetch('/api/stock/entries', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ barcode, storeNumber, units }),
  })

  if (!response.ok) {
    throw await toError(response, 'No se ha podido registrar la entrada')
  }
}

export interface DeliveryLine {
  barcode: string
  units: number
}

export async function receiveDelivery(
  deliveryNote: string,
  storeNumber: string,
  lines: DeliveryLine[],
): Promise<void> {
  const response = await fetch('/api/stock/deliveries', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ deliveryNote, storeNumber, lines }),
  })

  if (!response.ok) {
    throw await toError(response, 'No se ha podido registrar el albarán')
  }
}
