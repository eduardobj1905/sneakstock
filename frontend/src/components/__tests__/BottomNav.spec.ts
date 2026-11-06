import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BottomNav from '../BottomNav.vue'
import router from '@/router'

describe('BottomNav', () => {
  it('enlaza a cada pantalla y resalta la actual', async () => {
    await router.push('/vender')
    const wrapper = mount(BottomNav, { global: { plugins: [router] } })

    const links = wrapper.findAll('a')
    expect(links.map((link) => link.text())).toEqual(['Consultar', 'Vender'])
    expect(links[1]?.classes()).toContain('text-navy-600')
    expect(links[0]?.classes()).not.toContain('text-navy-600')
  })
})
