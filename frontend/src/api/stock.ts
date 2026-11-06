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
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? 'No se ha podido consultar el stock')
  }

  return response.json()
}
