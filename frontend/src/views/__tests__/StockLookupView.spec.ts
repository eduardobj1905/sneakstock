import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import StockLookupView from '../StockLookupView.vue'
import { findAvailableStock } from '@/api/stock'

// Sustituimos la llamada al backend por un mock, igual que con Mockito en Java
vi.mock('@/api/stock', () => ({ findAvailableStock: vi.fn() }))

describe('StockLookupView', () => {
  beforeEach(() => {
    vi.mocked(findAvailableStock).mockReset()
  })

  it('muestra una tarjeta por tienda y resalta la tienda del dependiente', async () => {
    vi.mocked(findAvailableStock).mockResolvedValue([
      { storeNumber: '0002', storeName: 'Sol', phone: '+34 910 000 002', quantity: 3 },
      { storeNumber: '0001', storeName: 'Gran Vía', phone: '+34 910 000 001', quantity: 1 },
    ])
    const wrapper = mount(StockLookupView)

    await wrapper.find('input').setValue('8400000000011')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(findAvailableStock).toHaveBeenCalledWith('8400000000011')
    expect(wrapper.findAll('li')).toHaveLength(2)
    expect(wrapper.text()).toContain('Tu tienda')
    expect(wrapper.text()).toContain('Resultados para 8400000000011')
  })

  it('vacía el campo después de buscar, listo para la siguiente caja', async () => {
    vi.mocked(findAvailableStock).mockResolvedValue([])
    const wrapper = mount(StockLookupView)

    await wrapper.find('input').setValue('8400000000011')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('')
    expect(wrapper.text()).toContain('No hay pares de esta talla')
  })

  it('muestra el mensaje de error del backend', async () => {
    vi.mocked(findAvailableStock).mockRejectedValue(new Error('No se ha podido consultar el stock'))
    const wrapper = mount(StockLookupView)

    await wrapper.find('input').setValue('8400000000011')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('No se ha podido consultar el stock')
  })
})
