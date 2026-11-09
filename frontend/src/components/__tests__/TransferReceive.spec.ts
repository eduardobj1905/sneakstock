import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import TransferReceive from '../TransferReceive.vue'
import { findTransfer, receiveTransfer, type Transfer } from '@/api/stock'

vi.mock('@/api/stock', () => ({ findTransfer: vi.fn(), receiveTransfer: vi.fn() }))

const fromSol: Transfer = {
  transferId: 12,
  originStoreNumber: '0002',
  originStoreName: 'Sol',
  destinationStoreNumber: '0001',
  destinationStoreName: 'Gran Vía',
  variant: {
    barcode: '8400000000011',
    brand: 'Nike',
    model: 'Dunk Low',
    color: 'White/Black',
    sizeUs: '9',
    sizeEu: '42.5',
  },
  sentUnits: 3,
  received: false,
}

async function lookup(wrapper: VueWrapper, number: string) {
  await wrapper.find('input').setValue(number)
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

function receiveButton(wrapper: VueWrapper) {
  return wrapper.findAll('button').find((b) => b.text() === 'Recibir traspaso')!
}

describe('TransferReceive', () => {
  beforeEach(() => {
    vi.mocked(findTransfer).mockReset()
    vi.mocked(receiveTransfer).mockReset()
  })

  it('enseña de dónde viene, qué zapatilla es y cuántos pares se enviaron', async () => {
    vi.mocked(findTransfer).mockResolvedValue(fromSol)
    const wrapper = mount(TransferReceive)

    await lookup(wrapper, '12')

    expect(findTransfer).toHaveBeenCalledWith(12)
    expect(wrapper.text()).toContain('Desde Sol (0002)')
    expect(wrapper.text()).toContain('Nike Dunk Low')
    expect(wrapper.text()).toContain('Enviados: 3')
    expect(receiveTransfer).not.toHaveBeenCalled()
  })

  it('si llega uno menos, recibe solo los que han llegado y lo dice', async () => {
    vi.mocked(findTransfer).mockResolvedValue(fromSol)
    vi.mocked(receiveTransfer).mockResolvedValue()
    const wrapper = mount(TransferReceive)
    await lookup(wrapper, '12')

    await wrapper.find('button[aria-label="Quitar una unidad"]').trigger('click')
    await receiveButton(wrapper).trigger('click')
    await flushPromises()

    expect(receiveTransfer).toHaveBeenCalledWith(12, '0001', 2)
    expect(wrapper.text()).toContain('Traspaso nº 12 recibido: 2 pares')
    expect(wrapper.text()).toContain('Queda anotado que faltan 1')
  })

  it('no deja recibir más pares de los enviados', async () => {
    vi.mocked(findTransfer).mockResolvedValue(fromSol)
    const wrapper = mount(TransferReceive)

    await lookup(wrapper, '12')

    expect(
      wrapper.find('button[aria-label="Añadir una unidad"]').attributes('disabled'),
    ).toBeDefined()
  })

  it('avisa si el traspaso va a otra tienda o ya se recibió', async () => {
    vi.mocked(findTransfer).mockResolvedValueOnce({
      ...fromSol,
      destinationStoreNumber: '0003',
      destinationStoreName: 'Goya',
    })
    const wrapper = mount(TransferReceive)
    await lookup(wrapper, '12')
    expect(wrapper.text()).toContain('va dirigido a Goya (0003), no a esta tienda')

    vi.mocked(findTransfer).mockResolvedValueOnce({ ...fromSol, received: true })
    await lookup(wrapper, '12')
    expect(wrapper.text()).toContain('El traspaso nº 12 ya se recibió')
  })
})
