import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import SaleView from '../SaleView.vue'
import { sell } from '@/api/stock'
import { findVariant, type Variant } from '@/api/catalog'

vi.mock('@/api/stock')
vi.mock('@/api/catalog', async (importOriginal) => ({
  ...(await importOriginal<typeof import('@/api/catalog')>()),
  findVariant: vi.fn<typeof import('@/api/catalog').findVariant>(),
}))

const dunkUs9: Variant = {
  barcode: '8400000000011',
  brand: 'Nike',
  model: 'Dunk Low',
  color: 'White/Black',
  sizeUs: '9',
  sizeEu: '42.5',
}

async function scan(wrapper: ReturnType<typeof mount>, code: string) {
  await wrapper.find('input').setValue(code)
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

describe('SaleView', () => {
  beforeEach(() => {
    vi.mocked(sell).mockReset()
    vi.mocked(findVariant).mockReset()
    vi.mocked(findVariant).mockResolvedValue(dunkUs9)
  })

  it('registra la venta en la tienda del dependiente y el mensaje dice qué zapatilla era', async () => {
    vi.mocked(sell).mockResolvedValue()
    const wrapper = mount(SaleView)

    await scan(wrapper, '8400000000011')
    await wrapper.find('button[aria-label="Añadir un par"]').trigger('click')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(sell).toHaveBeenCalledWith('8400000000011', '0001', 2)
    expect(wrapper.text()).toContain(
      'Venta registrada: 2 pares de Nike Dunk Low · US 9 / EU 42.5 (8400000000011)',
    )
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('')
  })

  it('no deja bajar de 1 par', async () => {
    const wrapper = mount(SaleView)

    await scan(wrapper, '8400000000011')

    expect(wrapper.find('button[aria-label="Quitar un par"]').attributes('disabled')).toBeDefined()
  })

  it('muestra el error del backend, por ejemplo stock insuficiente', async () => {
    vi.mocked(sell).mockRejectedValue(new Error('Stock insuficiente: hay 1 pares y se piden 5'))
    const wrapper = mount(SaleView)

    await scan(wrapper, '8400000000011')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Stock insuficiente: hay 1 pares y se piden 5')
  })
})
