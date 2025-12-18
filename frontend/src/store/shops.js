const API_URL = 'http://localhost:8088/api/stores'

export default {
  namespaced: true,
  state: {
    allShops: [],
    currentShop: null,
    loading: false,
    error: null,
  },

  getters: {
    // 獲取所有店家
    allShops: state => state.allShops,
    // 根據ID獲取單個店家
    getShopById: state => id => {
      return state.allShops.find(shop => shop.id === id)
    },
    // 根據菜單項目ID獲取商品信息
    getMenuItem: state => (shopId, itemId) => {
      const shop = state.allShops.find(s => s.id === shopId)
      if (shop) {
        return shop.menu.find(item => item.id === itemId)
      }
      return null
    },
    currentShop:state=>state.currentShop,//獲取當前店家
    isLoading: state=>state.loading,//獲取載入狀態
    error: state=> state.error,//獲取錯誤訊息

  },
  mutations: {
    SET_SHOPS(state, shops){
      state.allShops = shops;
    },
    SET_CURRENT_SHOP(state,shop){
      state.currentShop=shop;
    },
    SET_LOADING(state,loading){
      state.loading=loading;
    },
    SET_ERROR(state,error){
      state.error=error;
    },
    CLEAR_ERROR(state){
      state.error=null;
    },
    

  },
  actions: {
    async fetchAllShops({commit}){
      commit('SET_LOADING',true)
      commit('CLEAR_ERROR')

      try{
        const res = await fetch(API_URL)

        if(!res.ok){
          throw new Error('取得店家失敗');
        }
        const shops = await res.json()
        commit('SET_SHOPS', shops)

        console.log('成功取得店家:', shops.length, '家店');
        return shops;
      }catch(err){
        console.error('取得店家失敗:', err);
        commit('SET_ERROR', err.message)
        throw err;
      }finally{
        commit('SET_LOADING',false)
      }

    },
    async fetchShopById({commit}, shopId){
      commit('SET_LOADING', true)
      commit('CLEAR_ERROR')

      try{
        const res = await fetch(`${API_URL}/${shopId}`)

        if(!res.ok){
          throw new Error('取得店家詳情失敗');
        }
        const shop = await res.json()
        commit('SET_CURRENT_SHOP', shop)
        console.log('成功取得店家:', shop.name);
        return shop;

      }catch(err){
        console.error('取得店家詳情失敗:', err);
        commit('SET_ERROR', err.message)
        throw err;
      }finally{
        commit('SET_LOADING', false)
      }
    },

    async searchProducts({commit},{shopId, keyword}){
      commit('SET_LOADING',true)
      commit('CLEAR_ERROR')

      try{
        const url=`${API_URL}/${shopId}/search-product${keyword?`?keyword=${encodeURIComponent(keyword)}`:''}`
        const res=await fetch(url)

        if(!res.ok){
          throw new Error('搜尋商品失敗');
        }
        const products= await res.json()

        console.log('成功搜尋商品:',products.length,'個商品');
        return products;
      }catch(err){
        console.error('搜尋商品失敗:',err);
        commit('SET_ERROR', err.message)
        throw err;
      }finally{
        commit('SET_LOADING',false)
      }
    },
    clearCurrentShop({commit}){
      commit('SET_CURRENT_SHOP',null)
    },
    clearError({commit}){
      commit('CLEAR_ERROR')
    },
    
  }
}

