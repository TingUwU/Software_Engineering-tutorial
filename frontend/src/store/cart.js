// store/cart.js (使用 Vuex)
// { menuItemId, itemName, quantity, unitPrice, customization, itemSubTotal }
export default {
  namespaced: true,
  state: () => ({
    items: [],
    storeId: '' // 追踪当前购物车的店家ID
  }),
  getters: {
    totalAmount: (state) =>
      state.items.reduce((sum, i) => sum + i.itemSubTotal, 0)
  },
  mutations: {
    ADD_ITEM(state, item) {
      const existing = state.items.find(i => 
        i.menuItemId === item.menuItemId && 
        JSON.stringify(i.customization) === JSON.stringify(item.customization)
      )
      if (existing) {
        existing.quantity += item.quantity
        existing.itemSubTotal = existing.unitPrice * existing.quantity
      } else {
        state.items.push(item)
      }
    },
    REMOVE_ITEM(state, index) {
      state.items.splice(index, 1)
    },
    CLEAR_CART(state) {
      state.items = []
      state.storeId = ''
    },
    UPDATE_ITEM_QUANTITY(state, { index, quantity }) {
      if (state.items[index]) {
        state.items[index].quantity = quantity
        state.items[index].itemSubTotal = state.items[index].unitPrice * quantity
      }
    },
    SET_STORE_ID(state, storeId) {
      state.storeId = storeId
    }
  },
  actions: {
    addItem({ commit }, item) {
      commit('ADD_ITEM', item)
    },
    removeItem({ commit }, index) {
      commit('REMOVE_ITEM', index)
    },
    clearCart({ commit }) {
      commit('CLEAR_CART')
    },
    updateItemQuantity({ commit }, payload) {
      commit('UPDATE_ITEM_QUANTITY', payload)
    },
    setStoreId({ commit }, storeId) {
      commit('SET_STORE_ID', storeId)
    }
  }
}
