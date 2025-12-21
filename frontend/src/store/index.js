import { createStore } from 'vuex'
import cart from './cart'
import user from './user'
import shops from './shops'
import order from './order'

export default createStore({
  state: {
  },
  getters: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
    cart,
    user,
    shops,
    order
  }
})
