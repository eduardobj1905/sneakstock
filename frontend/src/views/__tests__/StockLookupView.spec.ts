import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import StockLookupView from '../StockLookupView.vue'
import { findAvailableStock, findStockInStore } from '@/api/stock'
import { findVariant, type Variant } from '@/api/catalog'

// Sustituimos las llamadas al backend por mocks, igual que con Mockito en Java
vi.mock('@/api/stock', () => ({ findAvailableStock: vi.fn(), findStockInStore: vi.fn() }))
vi.mock('@/api/catalog', () => ({ findVariant: vi.fn() }))

const dunkUs9: Variant = {
  barcode: '8400000000011',
  brand: 'Nike',
  model: 'Dunk Low',
  color: 'White/Black',
  sizeUs: '9',
  sizeEu: '42.5',
}

const granVia = { storeNumber: '0001', storeName: 'Gran Vía', phone: '+34 910 000 001' }

async function search(wrapper: VueWrapper, code: string) {
  await wrapper.find('input').setValue(code)
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

function otherStoresButton(wrapper: VueWrapper) {
  return wrapper.findAll('button').find((b) => b.text().includes('otras tiendas'))
}

describe('StockLookupView', () => {
  beforeEach(() => {
    vi.mocked(findAvailableStock).mockReset()
    vi.mocked(findStockInStore).mockReset()
    vi.mocked(findVariant).mockReset()
    vi.mocked(findVariant).mockResolvedValue(dunkUs9)
    vi.mocked(findStockInStore).mockResolvedValue({ ...granVia, quantity: 3 })
  })

  it('al escanear enseña la zapatilla y el stock de mi tienda, sin consultar las demás', async () => {
    const wrapper = mount(StockLookupView)

    await search(wrapper, '8400000000011')

    expect(findStockInStore).toHaveBeenCalledWith('8400000000011', '0001')
    expect(wrapper.text()).toContain('Nike Dunk Low')
    expect(wrapper.text()).toContain('En tu tienda')
    expect(wrapper.text()).toContain('3')
    expect(findAvailableStock).not.toHaveBeenCalled()
    expect(otherStoresButton(wrapper)).toBeDefined()
  })

  it('si en mi tienda hay 0, lo dice claramente', async () => {
    vi.mocked(findStockInStore).mockResolvedValue({ ...granVia, quantity: 0 })
    const wrapper = mount(StockLookupView)

    await search(wrapper, '8400000000011')

    expect(wrapper.text()).toContain('No te queda ningún par de esta talla')
  })

  it('el botón consulta las otras tiendas, sin repetir la mía', async () => {
    vi.mocked(findAvailableStock).mockResolvedValue([
      { storeNumber: '0002', storeName: 'Sol', phone: '+34 910 000 002', quantity: 1 },
      { ...granVia, quantity: 3 },
    ])
    const wrapper = mount(StockLookupView)
    await search(wrapper, '8400000000011')

    await otherStoresButton(wrapper)!.trigger('click')
    await flushPromises()

    expect(findAvailableStock).toHaveBeenCalledWith('8400000000011')
    expect(wrapper.findAll('li')).toHaveLength(1)
    expect(wrapper.find('li').text()).toContain('Sol')
    expect(otherStoresButton(wrapper)).toBeUndefined()
  })

  it('un código que no existe se avisa como error', async () => {
    vi.mocked(findVariant).mockRejectedValue(new Error('No existe ninguna talla con el código 999'))
    const wrapper = mount(StockLookupView)

    await search(wrapper, '999')

    expect(wrapper.text()).toContain('No existe ninguna talla con el código 999')
    expect(wrapper.text()).not.toContain('En tu tienda')
  })

  it('vacía el campo después de buscar, listo para la siguiente caja', async () => {
    const wrapper = mount(StockLookupView)

    await search(wrapper, '8400000000011')

    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('')
  })
})
