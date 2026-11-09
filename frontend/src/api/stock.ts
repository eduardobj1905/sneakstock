import { toError } from './http'
import type { Variant } from './catalog'

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

// Lo que devuelve GET /api/stock/transfers/{id}: un traspaso visto desde la tienda que lo recibe
export interface Transfer {
  transferId: number
  originStoreNumber: string
  originStoreName: string
  destinationStoreNumber: string
  destinationStoreName: string
  variant: Variant
  sentUnits: number
  received: boolean
}

// Devuelve el número de traspaso, que hay que apuntar en la caja
export async function sendTransfer(
  barcode: string,
  originStoreNumber: string,
  destinationStoreNumber: string,
  units: number,
): Promise<number> {
  const response = await fetch('/api/stock/transfers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ barcode, originStoreNumber, destinationStoreNumber, units }),
  })

  if (!response.ok) {
    throw await toError(response, 'No se ha podido enviar el traspaso')
  }

  const body: { transferId: number } = await response.json()
  return body.transferId
}

export async function findTransfer(transferId: number): Promise<Transfer> {
  const response = await fetch(`/api/stock/transfers/${transferId}`)

  if (!response.ok) {
    throw await toError(response, 'No se ha podido consultar el traspaso')
  }

  return response.json()
}

export async function receiveTransfer(
  transferId: number,
  storeNumber: string,
  units: number,
): Promise<void> {
  const response = await fetch(`/api/stock/transfers/${transferId}/receptions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ storeNumber, units }),
  })

  if (!response.ok) {
    throw await toError(response, 'No se ha podido recibir el traspaso')
  }
}
