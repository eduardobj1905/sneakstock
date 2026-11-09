import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import TransferSend from '../TransferSend.vue'
import { sendTransfer } from '@/api/stock'
import { findVariant, type Variant } from '@/api/catalog'
import { findOpenStores } from '@/api/stores'

vi.mock('@/api/stock', () => ({ sendTransfer: vi.fn() }))
vi.mock('@/api/stores', () => ({ findOpenStores: vi.fn() }))
vi.mock('@/api/catalog', async (importOriginal) => ({
  ...(await importOriginal<typeof import('@/api/catalog')>()),
  findVariant: vi.fn(),
}))

const dunkUs9: Variant = {
  barcode: '8400000000011',
  brand: 'Nike',
  model: 'Dunk Low',
  color: 'White/Black',
  sizeUs: '9',
  sizeEu: '42.5',
}

async function scan(wrapper: VueWrapper, code: string) {
  await wrapper.find('input').setValue(code)
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

describe('TransferSend', () => {
  beforeEach(() => {
    vi.mocked(sendTransfer).mockReset()
    vi.mocked(findVariant).mockReset().mockResolvedValue(dunkUs9)
    vi.mocked(findOpenStores)
      .mockReset()
      .mockResolvedValue([
        { storeNumber: '0001', name: 'Gran Vía' },
        { storeNumber: '0002', name: 'Sol' },
      ])
  })

  it('no ofrece mi propia tienda como destino', async () => {
    const wrapper = mount(TransferSend)
    await flushPromises()
    await scan(wrapper, '8400000000011')

    const options = wrapper.findAll('option').map((o) => o.text())
    expect(options).toContain('Sol (0002)')
    expect(options).not.toContain('Gran Vía (0001)')
  })

  it('no envía si no se ha elegido la tienda destino', async () => {
    const wrapper = mount(TransferSend)
    await flushPromises()
    await scan(wrapper, '8400000000011')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Elige la tienda a la que envías el traspaso')
    expect(sendTransfer).not.toHaveBeenCalled()
  })

  it('envía el traspaso y enseña el número que hay que apuntar en la caja', async () => {
    vi.mocked(sendTransfer).mockResolvedValue(12)
    const wrapper = mount(TransferSend)
    await flushPromises()
    await scan(wrapper, '8400000000011')
    await wrapper.find('select').setValue('0002')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(sendTransfer).toHaveBeenCalledWith('8400000000011', '0001', '0002', 1)
    expect(wrapper.text()).toContain('Traspaso nº 12 enviado a Sol')
    expect(wrapper.text()).toContain('Apunta el nº 12 en la caja')
  })
})
