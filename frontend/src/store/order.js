const API_URL = 'http://localhost:3000/api/orders'

export default {
  namespaced: true,
  state: {
    orders: [],
    currentOrder: {}
  },
  getters: {
    allOrders: state => state.orders,
    getOrderById: state => orderId => state.orders.find(order => order.id === orderId)
  },
  mutations: {
    SET_ORDERS(state, orders) {
      state.orders = orders
    },
    SET_CURRENT_ORDER(state, order) {
      state.currentOrder = order
    },
    UPDATE_ORDER(state, updatedOrder) {
      const index = state.orders.findIndex(o => o.id === updatedOrder.id)
      if (index !== -1) state.orders.splice(index, 1, updatedOrder)
      if (state.currentOrder.id === updatedOrder.id) state.currentOrder = updatedOrder
    }
  },
  actions: {
    async fetchOrders({ commit }) {
      try {
        const res = await fetch(API_URL)
        if (!res.ok) throw new Error('取得訂單列表失敗')
        const data = await res.json()
        commit('SET_ORDERS', data)
        return data
      } catch (err) {
        console.error(err)
        throw err
      }
    },
    async fetchOrderById({ commit }, orderId) {
      try {
        const res = await fetch(`${API_URL}/${orderId}`)
        if (!res.ok) throw new Error('取得訂單資料失敗')
        const data = await res.json()
        commit('SET_CURRENT_ORDER', data)
        return data
      } catch (err) {
        console.error(err)
        throw err
      }
    },
    async createOrder({ commit, state }, orderData) {
      try {
        const res = await fetch(API_URL, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(orderData)
        })
        if (!res.ok) throw new Error('建立訂單失敗')
        const data = await res.json()
        commit('SET_ORDERS', [...state.orders, data])
        return data
      } catch (err) {
        console.error(err)
        throw err
      }
    },
    async updateOrder({ commit }, { orderId, updates }) {
      try {
        const res = await fetch(`${API_URL}/${orderId}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updates)
        })
        if (!res.ok) throw new Error('更新訂單失敗')
        const data = await res.json()
        commit('UPDATE_ORDER', data)
        return data
      } catch (err) {
        console.error(err)
        throw err
      }
    },
    async deleteOrder({ commit, state }, orderId) {
      try {
        const res = await fetch(`${API_URL}/${orderId}`, { method: 'DELETE' })
        if (!res.ok) throw new Error('刪除訂單失敗')
        commit('SET_ORDERS', state.orders.filter(order => order.id !== orderId))
        return true
      } catch (err) {
        console.error(err)
        throw err
      }
    }
  }
}
