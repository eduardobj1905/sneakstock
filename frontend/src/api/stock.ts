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

// Convierte la respuesta de error del backend (ProblemDetail) en un Error con un mensaje legible
async function toError(response: Response, fallback: string): Promise<Error> {
  const problem = await response.json().catch(() => null)

  // Errores de validación: un mensaje por cada campo que falla
  if (problem?.errors) {
    return new Error(Object.values(problem.errors).join('. '))
  }

  return new Error(problem?.detail ?? fallback)
}
