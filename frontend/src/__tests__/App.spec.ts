import { describe, it, expect } from 'vitest'

import { mount } from '@vue/test-utils'
import App from '../App.vue'
import router from '../router'

describe('App', () => {
  it('muestra la cabecera con el nombre de la app y la tienda', () => {
    const wrapper = mount(App, { global: { plugins: [router] } })

    expect(wrapper.text()).toContain('SneakStock')
    expect(wrapper.text()).toContain('Tienda 0001')
  })
})
