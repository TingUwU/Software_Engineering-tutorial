// import { ref } from 'vue'
// import { createStore } from 'vuex'

const API_URL = 'http://localhost:8088/api/users'

export default {
  namespaced: true,
  state: {
    customer: {
      id: '',
      account: '',
      password: '',
      nickname: '',
      role: 'buyer',
      photo: '',
      phone: '',
      email: '',
      favorStores: [],
      favorItems: [],
      customCombos: [],
      isLoggedIn: false
    }
  },
  getters: {
    customer: state => state.customer,
    isLoggedIn: state => state.customer.isLoggedIn,
    isStoreFavorited: state => storeId => state.customer.favorStores.includes(storeId),
    isItemFavorited: state => (storeId, itemId) => {
      const storeItem = state.customer.favorItems.find(item => item.storeId === storeId && item.itemId === itemId)
      return !!storeItem
    }
  },
  mutations: {
    UPDATE_CUSTOMER(state, customer) {
      state.customer = { ...state.customer, ...customer }
    },
    LOGIN(state) {
      state.customer.isLoggedIn = true
    },
    LOGOUT(state) {
      state.customer = {
        id: '',
        account: '',
        password: '',
        nickname: '',
        role: 'buyer',
        photo: '',
        phone: '',
        email: '',
        favorStores: [],
        favorItems: [],
        customCombos: [],
        isLoggedIn: false
      }
    }
  },
  actions: {
    async register({ commit, dispatch }, registerData) {
      const res = await fetch(`${API_URL}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registerData)
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || '註冊失敗')
      }
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      commit('LOGIN')
      
      if (data.id) {
        dispatch('cart/setUserId', data.id, { root: true })
      }
      
      return data
    },

    async login({ commit, dispatch }, loginData) {
      const res = await fetch(`${API_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginData)
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || '登入失敗')
      }
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      commit('LOGIN')
      
      if (data.id) {
        dispatch('cart/setUserId', data.id, { root: true })
      }
      
      return data
    },

    logout({ commit }) {
      commit('LOGOUT')
    },

    async getUserProfile({ commit }, userId) {
      const res = await fetch(`${API_URL}/${userId}`)
      if (!res.ok) throw new Error('取得使用者資料失敗')
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      return data
    },

    async updateUser({ commit }, { userId, updates }) {
      console.log('Sending PATCH to:', `${API_URL}/${userId}`, updates);
      const res = await fetch(`${API_URL}/${userId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      });
      console.log('Response status:', res.status);

      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || '更新失敗');
      }
      const data = await res.json();
      console.log('Updated user data:', data);
      commit('UPDATE_CUSTOMER', data);
      return data;
    },

    // 收藏店家
    async toggleFavorStore({ commit, state }, storeId) {
      const userId = state.customer.id
      console.log('Toggle Favor Store - userId:', userId, 'storeId:', storeId)
      
      if (!userId) {
        throw new Error('用戶未登入或用戶ID不存在')
      }
      
      const exists = state.customer.favorStores.includes(storeId)
      const url = `${API_URL}/${userId}/favorite-stores/${storeId}`
      console.log('Request URL:', url, 'Method:', exists ? 'DELETE' : 'POST')
      
      const res = await fetch(url, { method: exists ? 'DELETE' : 'POST' })
      console.log('Response status:', res.status)
      
      if (!res.ok) {
        const errorText = await res.text()
        console.error('Error response:', errorText)
        throw new Error(`操作失敗 (${res.status}): ${errorText}`)
      }
      
      const data = await res.json()
      console.log('Success, updated data:', data)
      commit('UPDATE_CUSTOMER', data)
      return !exists
    },

    // 收藏商品
    async toggleFavorItem({ commit, state }, { storeId, itemId }) {
      const userId = state.customer.id
      console.log('Toggle Favor Item - userId:', userId, 'storeId:', storeId, 'itemId:', itemId)
      
      if (!userId) {
        throw new Error('用戶未登入或用戶ID不存在')
      }
      
      const exists = state.customer.favorItems.some(item => item.storeId === storeId && item.itemId === itemId)
      const url = `${API_URL}/${userId}/favorite-items/${storeId}/${itemId}`
      console.log('Request URL:', url, 'Method:', exists ? 'DELETE' : 'POST')
      
      const res = await fetch(url, { method: exists ? 'DELETE' : 'POST' })
      console.log('Response status:', res.status)
      
      if (!res.ok) {
        const errorText = await res.text()
        console.error('Error response:', errorText)
        throw new Error(`操作失敗 (${res.status}): ${errorText}`)
      }
      
      const data = await res.json()
      console.log('Success, updated data:', data)
      commit('UPDATE_CUSTOMER', data)
      return !exists
    },

    // 自訂組合操作
    async addCustomCombo({ commit, state }, comboName) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ comboName })
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || '新增失敗')
      }
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      return data
    },

    async updateCustomCombo({ commit, state }, { comboId, comboName }) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos/${comboId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ comboName })
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || '更新失敗')
      }
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      return data
    },

    async deleteCustomCombo({ commit, state }, comboId) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos/${comboId}`, { method: 'DELETE' })
      if (!res.ok) throw new Error('刪除失敗')
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      return data
    },

    async addCustomComboItem({ commit, state }, { comboId, storeId, itemId, customizations = [] }) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos/${comboId}/items/${storeId}/${itemId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ customizations })
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || '加入失敗')
      }
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      return data
    },

    async deleteCustomComboItem({ commit, state }, { comboId, storeId, itemId }) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos/${comboId}/items/${storeId}/${itemId}`, {
        method: 'DELETE'
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || '移除失敗')
      }
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      return data
    },

    async getCustomCombos({ commit, state }) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos`)
      if (!res.ok) throw new Error('取得自訂組合失敗')
      const data = await res.json()
      commit('UPDATE_CUSTOMER', { customCombos: data })
      return data
    },

    async getCustomComboDetail({ state }, comboId) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos/${comboId}`)
      if (!res.ok) throw new Error('取得組合詳細資訊失敗')
      return await res.json()
    },

    async updateCustomComboItem({ commit, state }, { comboId, storeId, itemId, updates }) {
      const userId = state.customer.id
      const res = await fetch(`${API_URL}/${userId}/custom-combos/${comboId}/items/${storeId}/${itemId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || '更新失敗')
      }
      const data = await res.json()
      commit('UPDATE_CUSTOMER', data)
      return data
    }
  }
}
