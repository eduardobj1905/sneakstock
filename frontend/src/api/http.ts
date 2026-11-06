// Convierte la respuesta de error del backend (ProblemDetail) en un Error con un mensaje legible
export async function toError(response: Response, fallback: string): Promise<Error> {
  const problem = await response.json().catch(() => null)

  // Errores de validación: un mensaje por cada campo que falla
  if (problem?.errors) {
    return new Error(Object.values(problem.errors).join('. '))
  }

  return new Error(problem?.detail ?? fallback)
}
