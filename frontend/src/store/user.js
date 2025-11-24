export default {
  namespaced: true,
  state: {
    customer: {
      id: "user001",
      account: "user001",
      password: "",
      nickname: "使用者名稱",
      role: "buyer",
      photo: "",
      phone: "0912345678",
      email: "user001@test.com",
      favorStores: ["store001", "store002"], // Array<String> - 收藏店家ID列表（初始測試數據）
      favorItems: [ // Array<{storeId: String, itemId: Array<String>}>（初始測試數據）
        {
          storeId: "store003",
          itemId: ["item301", "item302"]
        },
        {
          storeId: "store004",
          itemId: ["item401"]
        }
      ],
      customCombos: [],
      updatedAt: new Date(),
      isLoggedIn: false
    }
  },
  getters: {
    customer: state => state.customer,
    favorStores: state => state.customer.favorStores,
    favorItems: state => state.customer.favorItems,
    // 檢查店家是否已收藏
    isStoreFavorited: state => storeId => {
      return state.customer.favorStores.includes(storeId)
    },
    // 檢查商品是否已收藏
    isItemFavorited: state => (storeId, itemId) => {
      const storeItem = state.customer.favorItems.find(item => item.storeId === storeId)
      return storeItem ? storeItem.itemId.includes(itemId) : false
    }
  },
  mutations: {
    // 更新用戶資料
    UPDATE_CUSTOMER(state, customer) {
      state.customer = { ...state.customer, ...customer }
    },
    // 添加收藏店家
    ADD_FAVOR_STORE(state, storeId) {
      if (!state.customer.favorStores.includes(storeId)) {
        state.customer.favorStores.push(storeId)
      }
    },
    // 移除收藏店家
    REMOVE_FAVOR_STORE(state, storeId) {
      state.customer.favorStores = state.customer.favorStores.filter(id => id !== storeId)
    },
    // 添加收藏商品
    ADD_FAVOR_ITEM(state, { storeId, itemId }) {
      const storeItemIndex = state.customer.favorItems.findIndex(item => item.storeId === storeId)
      
      if (storeItemIndex !== -1) {
        // 該店家已有收藏項目
        if (!state.customer.favorItems[storeItemIndex].itemId.includes(itemId)) {
          state.customer.favorItems[storeItemIndex].itemId.push(itemId)
        }
      } else {
        // 該店家還沒有收藏項目，創建新的
        state.customer.favorItems.push({
          storeId: storeId,
          itemId: [itemId]
        })
      }
    },
    // 移除收藏商品
    REMOVE_FAVOR_ITEM(state, { storeId, itemId }) {
      const storeItemIndex = state.customer.favorItems.findIndex(item => item.storeId === storeId)
      
      if (storeItemIndex !== -1) {
        const itemIndex = state.customer.favorItems[storeItemIndex].itemId.indexOf(itemId)
        if (itemIndex !== -1) {
          state.customer.favorItems[storeItemIndex].itemId.splice(itemIndex, 1)
          // 如果該店家沒有任何收藏商品了，移除整個店家項目
          if (state.customer.favorItems[storeItemIndex].itemId.length === 0) {
            state.customer.favorItems.splice(storeItemIndex, 1)
          }
        }
      }
    },
    LOGIN(state) {
          state.isLoggedIn = true
    },
      LOGOUT(state) {
          state.isLoggedIn = false
          state.customer = {
              id: "",
              account: "",
              password: "",
              nickname: "",
              role: "buyer",
              photo: "",
              phone: "",
              email: "",
              favorStores: [],
              favorItems: [],
              customCombos: [],
              updatedAt: new Date()
          }
      }
  },
  actions: {
    // 切換店家收藏狀態
    toggleFavorStore({ commit, state }, storeId) {
      if (state.customer.favorStores.includes(storeId)) {
        commit('REMOVE_FAVOR_STORE', storeId)
        return false // 已取消收藏
      } else {
        commit('ADD_FAVOR_STORE', storeId)
        return true // 已加入收藏
      }
    },
    // 切換商品收藏狀態
    toggleFavorItem({ commit, getters }, { storeId, itemId }) {
      if (getters.isItemFavorited(storeId, itemId)) {
        commit('REMOVE_FAVOR_ITEM', { storeId, itemId })
        return false // 已取消收藏
      } else {
        commit('ADD_FAVOR_ITEM', { storeId, itemId })
        return true // 已加入收藏
      }
    },
    // 更新用戶資料
      updateUser({ commit }, editCustomer) {
          return fetch(`http://localhost:8080/api/users/${editCustomer.id}`, {
              method: 'PATCH',
              headers: {
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${localStorage.getItem('token')}`
              },
              body: JSON.stringify(editCustomer)
          })
              .then(response => {
                  if (!response.ok) {
                      return response.json().then(err => {
                          throw new Error(err.message || '更新失敗');
                      });
                  }
                  return response.json();
              })
              .then(data => {
                  commit('UPDATE_CUSTOMER', data);
                  return data;
              });
      },
    login({ commit }, customer) {
          commit('UPDATE_CUSTOMER', customer)
          commit('LOGIN')
    },
    logout({ commit }) {
          commit('LOGOUT')
    }
  }
}

