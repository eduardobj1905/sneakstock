import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import SaleView from '../SaleView.vue'
import { sell } from '@/api/stock'

vi.mock('@/api/stock', () => ({ sell: vi.fn() }))

describe('SaleView', () => {
  beforeEach(() => {
    vi.mocked(sell).mockReset()
  })

  it('registra la venta en la tienda del dependiente y deja el formulario limpio', async () => {
    vi.mocked(sell).mockResolvedValue()
    const wrapper = mount(SaleView)

    await wrapper.find('input').setValue('8400000000011')
    await wrapper.find('button[aria-label="Añadir un par"]').trigger('click')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(sell).toHaveBeenCalledWith('8400000000011', '0001', 2)
    expect(wrapper.text()).toContain('Venta registrada: 2 pares de 8400000000011')
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('')
  })

  it('no deja bajar de 1 par', async () => {
    const wrapper = mount(SaleView)
    const decrease = wrapper.find('button[aria-label="Quitar un par"]')

    expect(decrease.attributes('disabled')).toBeDefined()
  })

  it('muestra el error del backend, por ejemplo stock insuficiente', async () => {
    vi.mocked(sell).mockRejectedValue(new Error('Stock insuficiente: hay 1 pares y se piden 5'))
    const wrapper = mount(SaleView)

    await wrapper.find('input').setValue('8400000000011')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Stock insuficiente: hay 1 pares y se piden 5')
  })
})
