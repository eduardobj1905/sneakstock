import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import EntryView from '../EntryView.vue'
import { receiveDelivery } from '@/api/stock'
import { findVariant, type Variant } from '@/api/catalog'

vi.mock('@/api/stock')
vi.mock('@/api/catalog')

const dunkUs9: Variant = {
  barcode: '8400000000011',
  brand: 'Nike',
  model: 'Dunk Low',
  color: 'White/Black',
  sizeUs: '9',
  sizeEu: '42.5',
}

const sambaUs9: Variant = {
  barcode: '8400000000035',
  brand: 'Adidas',
  model: 'Samba OG',
  color: 'White/Black',
  sizeUs: '9',
  sizeEu: '42 2/3',
}

async function startDelivery(wrapper: VueWrapper, note: string) {
  await wrapper.find('input').setValue(note)
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

async function scan(wrapper: VueWrapper, code: string) {
  await wrapper.find('input').setValue(code)
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

function registerButton(wrapper: VueWrapper) {
  return wrapper.findAll('button').find((button) => button.text().startsWith('Registrar entrada'))!
}

describe('EntryView', () => {
  beforeEach(() => {
    vi.mocked(receiveDelivery).mockReset()
    vi.mocked(findVariant).mockReset()
    vi.mocked(findVariant).mockImplementation(async (code) => {
      if (code === dunkUs9.barcode) return dunkUs9
      if (code === sambaUs9.barcode) return sambaUs9
      throw new Error(`No existe ninguna talla con el código ${code}`)
    })
  })

  it('escanear la misma zapatilla dos veces suma unidades en la misma línea', async () => {
    const wrapper = mount(EntryView)
    await startDelivery(wrapper, 'ALB-0045')

    await scan(wrapper, '8400000000011')
    await scan(wrapper, '8400000000011')
    await scan(wrapper, '8400000000035')

    expect(wrapper.findAll('li')).toHaveLength(2)
    expect(findVariant).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('3 unidades')
    expect(receiveDelivery).not.toHaveBeenCalled()
  })

  it('un código que no existe se avisa y no entra en la lista', async () => {
    const wrapper = mount(EntryView)
    await startDelivery(wrapper, 'ALB-0045')

    await scan(wrapper, '999')

    expect(wrapper.text()).toContain('No existe ninguna talla con el código 999')
    expect(wrapper.findAll('li')).toHaveLength(0)
    expect(registerButton(wrapper).attributes('disabled')).toBeDefined()
  })

  it('al registrar envía todo el albarán de una vez y vuelve a empezar', async () => {
    vi.mocked(receiveDelivery).mockResolvedValue()
    const wrapper = mount(EntryView)
    await startDelivery(wrapper, 'ALB-0045')
    await scan(wrapper, '8400000000011')
    await scan(wrapper, '8400000000011')
    await scan(wrapper, '8400000000035')

    await registerButton(wrapper).trigger('click')
    await flushPromises()

    expect(receiveDelivery).toHaveBeenCalledWith('ALB-0045', '0001', [
      { barcode: '8400000000035', units: 1 },
      { barcode: '8400000000011', units: 2 },
    ])
    expect(wrapper.text()).toContain('Albarán ALB-0045 registrado: 3 unidades de 2 referencias')
    expect(wrapper.find('input').attributes('placeholder')).toContain('número de albarán')
  })

  it('si el backend rechaza el albarán, la lista se conserva para corregirla', async () => {
    vi.mocked(receiveDelivery).mockRejectedValue(
      new Error('El albarán ALB-0045 ya ha sido registrado'),
    )
    const wrapper = mount(EntryView)
    await startDelivery(wrapper, 'ALB-0045')
    await scan(wrapper, '8400000000011')

    await registerButton(wrapper).trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('El albarán ALB-0045 ya ha sido registrado')
    expect(wrapper.findAll('li')).toHaveLength(1)
  })
})
