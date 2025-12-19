const API_URL = 'http://localhost:8088/api/carts'

export default {
  namespaced: true,
  state: () => ({
    items: [],
    storeId: '',
    userId: null,
    totalPrice: 0,
    cartId: null
  }),
  getters: {
    totalAmount: (state) => state.totalPrice,
    itemCount: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
    isEmpty: (state) => state.items.length === 0
  },
  mutations: {
    SET_CART(state, cart) {
      if (!cart) {
        state.items = []
        state.storeId = ''
        state.totalPrice = 0
        state.cartId = null
        return
      }
      state.cartId = cart._id || cart.id
      state.userId = cart.userId
      state.storeId = cart.storeId || ''
      state.totalPrice = cart.totalPrice || 0
      
      state.items = (cart.items || []).map(item => ({
        itemId: item.itemId,
        menuItemId: item.itemId,
        itemName: item.itemName,
        price: item.price,
        unitPrice: item.price,
        quantity: item.quantity,
        subtotal: item.subtotal,
        itemSubTotal: item.subtotal,
        customization: item.description 
          ? item.description.split('、').filter(s => s.trim()) 
          : []
      }))
    },

    CLEAR_CART(state) {
      state.items = []
      state.storeId = ''
      state.totalPrice = 0
      state.cartId = null
    },

    SET_USER_ID(state, userId) {
      state.userId = userId
    }
  },

  actions: {
    setUserId({ commit }, userId) {
      commit('SET_USER_ID', userId)
    },

    async fetchCart({ commit, state }) {
      if (!state.userId) {
        console.warn('⚠️ 未設定使用者ID，無法獲取購物車')
        return null
      }

      try {
        const res = await fetch(`${API_URL}/${state.userId}`)

        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: 獲取購物車失敗`)
        }

        const data = await res.json()
        console.log(' 成功獲取購物車:', data)
        commit('SET_CART', data)
        return data
      } catch (err) {
        console.error(' 獲取購物車失敗:', err)
        throw err
      }
    },

    async addItem({ commit, state }, { item, storeId }) {
      if (!state.userId) {
        throw new Error('未設定使用者ID，無法加入商品')
      }

      const backendItem = {
        itemId: item.menuItemId || item.itemId,
        itemName: item.itemName,
        price: item.unitPrice || item.price,
        quantity: item.quantity || 1,
        description: Array.isArray(item.customization) 
          ? item.customization.filter(s => s).join('、')
          : (item.customization || '')
      }

      try {
        const res = await fetch(`${API_URL}/${state.userId}/items?storeId=${storeId}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(backendItem)
        })

        if (!res.ok) {
          const errorText = await res.text()
          throw new Error(errorText || '加入商品失敗')
        }
        
        const data = await res.json()
        console.log('成功加入商品:', data)
        commit('SET_CART', data)
        return data
      } catch (err) {
        console.error('加入商品失敗:', err)
        throw err
      }
    },

    async removeItem({ commit, state }, itemId) {
      if (!state.userId) {
        throw new Error('未設定使用者ID，無法移除商品')
      }
      
      try {
        const res = await fetch(`${API_URL}/${state.userId}/items/${itemId}`, {
          method: 'DELETE'
        })

        if (!res.ok) {
          const errorText = await res.text()
          throw new Error(errorText || '移除商品失敗')
        }

        const data = await res.json()
        console.log('成功移除商品:', data)
        commit('SET_CART', data)
        return data
      } catch (err) {
        console.error('移除商品失敗:', err)
        throw err
      }
    },

    async updateItemQuantity({ state, dispatch }, { index, quantity }) {
      if (!state.userId) {
        throw new Error('未設定使用者ID，無法更新商品數量')
      }
      
      const item = state.items[index]
      if (!item) {
        throw new Error('商品不存在')
      }

      if (quantity <= 0) {
        return await dispatch('removeItem', item.itemId || item.menuItemId)
      }

      try {
        await dispatch('removeItem', item.itemId || item.menuItemId)
        await dispatch('addItem', {
          item: { ...item, quantity },
          storeId: state.storeId
        })
        console.log('成功更新商品數量:', quantity)
      } catch (err) {
        console.error('更新商品數量失敗:', err)
        await dispatch('fetchCart')
        throw err
      }
    },

    async clearCart({ commit, state }) {
      if (!state.userId) {
        commit('CLEAR_CART')
        return
      }
      
      try {
        const res = await fetch(`${API_URL}/${state.userId}`, {
          method: 'DELETE'
        })
        
        if (!res.ok) {
          throw new Error('清空購物車失敗')
        }

        console.log('成功清空購物車')
        commit('CLEAR_CART')
        return true
      } catch (err) {
        console.error('清空購物車失敗:', err)
        throw err
      }
    },

    setStoreId({ state }, storeId) {
      state.storeId = storeId
    }
  }
}











