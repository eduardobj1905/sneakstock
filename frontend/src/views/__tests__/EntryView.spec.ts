import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import EntryView from '../EntryView.vue'
import { receive } from '@/api/stock'
import { findVariant, type Variant } from '@/api/catalog'

vi.mock('@/api/stock', () => ({ receive: vi.fn() }))
vi.mock('@/api/catalog', async (importOriginal) => ({
  ...(await importOriginal<typeof import('@/api/catalog')>()),
  findVariant: vi.fn(),
}))

const sambaUs9: Variant = {
  barcode: '8400000000035',
  brand: 'Adidas',
  model: 'Samba OG',
  color: 'White/Black',
  sizeUs: '9',
  sizeEu: '42 2/3',
}

async function scan(wrapper: ReturnType<typeof mount>, code: string) {
  await wrapper.find('input').setValue(code)
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

describe('EntryView', () => {
  beforeEach(() => {
    vi.mocked(receive).mockReset()
    vi.mocked(findVariant).mockReset()
  })

  it('al escanear enseña la zapatilla y todavía no registra nada', async () => {
    vi.mocked(findVariant).mockResolvedValue(sambaUs9)
    const wrapper = mount(EntryView)

    await scan(wrapper, '8400000000035')

    expect(wrapper.text()).toContain('Adidas Samba OG')
    expect(wrapper.text()).toContain('US 9 · EU 42 2/3')
    expect(wrapper.find('button[type="submit"]').text()).toBe('Registrar entrada')
    expect(receive).not.toHaveBeenCalled()
  })

  it('al confirmar registra la entrada y el mensaje dice qué zapatilla era', async () => {
    vi.mocked(findVariant).mockResolvedValue(sambaUs9)
    vi.mocked(receive).mockResolvedValue()
    const wrapper = mount(EntryView)

    await scan(wrapper, '8400000000035')
    await wrapper.find('button[aria-label="Añadir un par"]').trigger('click')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(receive).toHaveBeenCalledWith('8400000000035', '0001', 2)
    expect(wrapper.text()).toContain(
      'Entrada registrada: 2 pares de Adidas Samba OG · US 9 / EU 42 2/3 (8400000000035)',
    )
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('')
  })

  it('un código que no existe se avisa al escanear, sin registrar nada', async () => {
    vi.mocked(findVariant).mockRejectedValue(new Error('No existe ninguna talla con el código 999'))
    const wrapper = mount(EntryView)

    await scan(wrapper, '999')

    expect(wrapper.text()).toContain('No existe ninguna talla con el código 999')
    expect(receive).not.toHaveBeenCalled()
  })

  it('si se escanea otro código, desaparece la zapatilla anterior', async () => {
    vi.mocked(findVariant).mockResolvedValue(sambaUs9)
    const wrapper = mount(EntryView)

    await scan(wrapper, '8400000000035')
    await wrapper.find('input').setValue('8400000000011')

    expect(wrapper.text()).not.toContain('Adidas Samba OG')
    expect(wrapper.find('button[type="submit"]').text()).toBe('Comprobar código')
  })
})
