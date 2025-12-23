const API_URL = 'http://localhost:8088/api/carts'

export default {
  namespaced: true,
  state: () => ({
    items: [],
    storeId: '',
    userId: null,
    totalPrice: 0,
    cartId: null,
    isGuest: false // 訪客模式標記
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
      
      console.log('SET_CART mutation - 收到的 cart:', cart)
      console.log('SET_CART mutation - cart.storeId:', cart.storeId)
      
      state.cartId = cart._id || cart.id
      state.userId = cart.userId
      state.storeId = cart.storeId || ''
      state.totalPrice = cart.totalPrice || 0
      
      console.log('SET_CART mutation - 設置後 state.storeId:', state.storeId)
      
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
          : [],
        imgUrl: item.imgUrl
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
    },
    
    SET_STORE_ID(state, storeId) {
      state.storeId = storeId
    },

    SET_GUEST_MODE(state, isGuest) {
      state.isGuest = isGuest
      if (isGuest) {
        state.userId = null // 訪客模式清除用戶ID
      }
    }
  },

  actions: {
    setUserId({ commit }, userId) {
      commit('SET_USER_ID', userId)
    },

    async fetchCart({ commit, state }) {
      if (!state.userId) {
        console.warn('未設定使用者ID，無法獲取購物車')
        return null
      }

      try {
        const res = await fetch(`${API_URL}/${state.userId}`)

        if (!res.ok) {
          throw new Error(`HTTP ${res.status}: 獲取購物車失敗`)
        }

        const data = await res.json()
        console.log('成功獲取購物車:', data)
        console.log('購物車 storeId:', data.storeId)
        commit('SET_CART', data)
        return data
      } catch (err) {
        console.error('獲取購物車失敗:', err)
        throw err
      }
    },

    async addItem({ commit, state, dispatch }, { item, storeId }) {
      // 訪客模式：使用前端狀態管理，並檢查跨店限制
      if (state.isGuest) {
        const itemMenuItemId = item.menuItemId || item.id || item._id
        const existingItemIndex = state.items.findIndex(i =>
          i.menuItemId === itemMenuItemId &&
          JSON.stringify(i.customization) === JSON.stringify(item.customization)
        )

        // 檢查跨店限制
        const currentStoreId = state.storeId
        const hasItems = state.items.length > 0

        // 數據異常：有商品但 storeId 為空
        if (hasItems && (!currentStoreId || currentStoreId === '')) {
          const confirmed = confirm(
            `購物車數據異常（店家資訊遺失）\n` +
            `是否清空購物車並加入新商品？\n\n` +
            `點擊「確定」清空購物車並繼續\n` +
            `點擊「取消」放棄操作`
          )

          if (!confirmed) {
            return false // 取消操作
          }

          // 清空購物車
          await dispatch('clearCart')
        }
        // 正常跨店檢查
        else if (hasItems && currentStoreId && currentStoreId !== storeId) {
          const storeName = await dispatch('getStoreName', currentStoreId)
          const confirmed = confirm(
            `購物車中已有「${storeName}」的商品\n` +
            `是否清空購物車並加入新商品？\n\n` +
            `點擊「確定」清空購物車並繼續\n` +
            `點擊「取消」放棄操作`
          )

          if (!confirmed) {
            return false // 取消操作
          }

          // 清空購物車
          await dispatch('clearCart')
        }

        // 添加商品到購物車
        if (existingItemIndex >= 0) {
          // 商品已存在，增加數量
          const existingItem = state.items[existingItemIndex]
          const newQuantity = existingItem.quantity + (item.quantity || 1)
          const updatedItem = {
            ...existingItem,
            quantity: newQuantity,
            itemSubTotal: existingItem.unitPrice * newQuantity
          }
          state.items.splice(existingItemIndex, 1, updatedItem)
        } else {
          // 新增商品
          const newItem = {
            menuItemId: itemMenuItemId,
            itemName: item.itemName,
            unitPrice: item.unitPrice || item.price,
            quantity: item.quantity || 1,
            customization: item.customization || [],
            itemSubTotal: (item.unitPrice || item.price) * (item.quantity || 1),
            imgUrl: item.imgUrl
          }
          state.items.push(newItem)
        }

        // 更新總價和店家ID
        state.totalPrice = state.items.reduce((sum, item) => sum + item.itemSubTotal, 0)
        state.storeId = storeId

        console.log('訪客模式：成功加入商品到前端購物車')
        return { items: state.items, totalPrice: state.totalPrice, storeId: state.storeId }
      }

      // 會員模式：使用後端API
      if (!state.userId) {
        console.warn('未設定使用者ID，無法添加商品到購物車')
        return null
      }

      try {
        // 準備要發送的商品數據
        const cartItem = {
          itemId: item.menuItemId || item.id || item._id,
          itemName: item.itemName,
          price: item.unitPrice || item.price,
          quantity: item.quantity || 1,
          description: Array.isArray(item.customization)
            ? item.customization.join('、')
            : (item.customization || ''),
          imgUrl: item.imgUrl || ''
        }

        console.log('準備添加商品到購物車:', cartItem)

        const res = await fetch(`${API_URL}/${state.userId}/items?storeId=${storeId}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(cartItem)
        })

        if (!res.ok) {
          const errorText = await res.text()
          throw new Error(errorText || `HTTP ${res.status}: 添加商品失敗`)
        }

        const data = await res.json()
        console.log('成功添加商品到購物車:', data)
        commit('SET_CART', data)
        return data
      } catch (err) {
        console.error('添加商品到購物車失敗:', err)
        throw err
      }
    },

    async removeItem({ commit, state }, itemId) {
      // 訪客模式：前端狀態管理
      if (state.isGuest) {
        const itemIndex = state.items.findIndex(item =>
          item.menuItemId === itemId || item.itemId === itemId || item._id === itemId
        )

        if (itemIndex >= 0) {
          state.items.splice(itemIndex, 1)
          state.totalPrice = state.items.reduce((sum, item) => sum + item.itemSubTotal, 0)
          console.log('訪客模式：成功從前端購物車移除商品')
          return { items: state.items, totalPrice: state.totalPrice }
        } else {
          throw new Error('商品不存在')
        }
      }

      // 會員模式：使用後端API
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
      // 訪客模式：前端狀態管理
      if (state.isGuest) {
        const item = state.items[index]
        if (!item) {
          throw new Error('商品不存在')
        }

        if (quantity <= 0) {
          return await dispatch('removeItem', item.menuItemId || item.itemId)
        }

        // 更新數量和總價
        item.quantity = quantity
        item.itemSubTotal = item.unitPrice * quantity
        state.totalPrice = state.items.reduce((sum, item) => sum + item.itemSubTotal, 0)

        console.log('訪客模式：成功更新商品數量:', quantity)
        return { items: state.items, totalPrice: state.totalPrice }
      }

      // 會員模式：使用後端API
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

      let storeId = state.storeId

      // 如果 storeId 是空的，說明數據異常
      if (!storeId || storeId === '') {
        console.error('檢測到購物車數據異常：有商品但 storeId 為空')
        alert('購物車數據異常，建議清空購物車重新添加商品')
        throw new Error('購物車數據異常，請清空購物車後重新添加')
      }

      console.log('更新商品數量 - 使用 storeId:', storeId)

      try {
        await dispatch('removeItem', item.itemId || item.menuItemId)
        await dispatch('addItem', {
          item: { ...item, quantity },
          storeId: storeId
        })
        console.log('成功更新商品數量:', quantity)
      } catch (err) {
        console.error('更新商品數量失敗:', err)
        await dispatch('fetchCart')
        throw err
      }
    },

    async clearCart({ commit, state }) {
      // 訪客模式：直接清空前端狀態
      if (state.isGuest) {
        commit('CLEAR_CART')
        console.log('訪客模式：成功清空前端購物車')
        return true
      }

      // 會員模式：使用後端API
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

    setStoreId({ commit, state }, storeId) {
      if (state.storeId !== storeId) {
        commit('SET_STORE_ID', storeId)
      }
    },

    setGuestMode({ commit }, isGuest) {
      commit('SET_GUEST_MODE', isGuest)
    },

    // 獲取店家名稱（用於跨店提示）
    getStoreName({ rootGetters }, storeId) {
      const shop = rootGetters['shops/getShopById'](storeId);
      return shop ? shop.name : '其他店家';
    }
  }
}











