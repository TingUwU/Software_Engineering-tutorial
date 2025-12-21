<template>
  <div class="merchant-order-page">
    <!-- 遮罩層 -->
    <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

    <!-- 左側側邊欄（商家） -->
    <div :class="['sidebar', { open: sidebarOpen }]">
      <div class="sidebar-user">
        <img class="sidebar-avatar" :src="require('@/assets/logo.png')" alt="merchant">
        <span class="username">商家管理員</span>
      </div>

      <ul>

        <li @click="openUserModal">使用者資訊</li>
        <router-link to="/store-management"><li>菜單設定</li></router-link>
        <router-link to="/merchant-order"><li>訂單管理</li></router-link>
        <router-link to="/store-setting"><li>編輯店家資訊</li></router-link>
      </ul>

      <div class="sidebar-logout">
        <button @click="logout">登出</button>
      </div>
    </div>

    <!-- 使用者資訊 Modal -->
    <div v-if="userModalOpen" class="modal-overlay" @click.self="closeUserModal">
      <div class="user-modal">
        <h3>使用者資訊</h3>
        <form @submit.prevent="updateUserInfo">
          <div class="form-group">
            <label>頭像:</label>
            <img :src="editCustomer.photo || require('@/assets/logo.png')" class="preview-avatar" alt="user">
            <input type="file" @change="onAvatarChange" accept="image/*">
          </div>
          <div class="form-group">
            <label>名稱:</label>
            <input type="text" v-model="editCustomer.nickname">
          </div>
          <div class="form-group">
            <label>電話:</label>
            <input type="text" v-model="editCustomer.phone">
          </div>
          <div class="form-group">
            <label>電子郵件:</label>
            <input type="email" v-model="editCustomer.email">
          </div>
          <div class="modal-actions">
            <button type="submit">儲存</button>
            <button type="button" @click="closeUserModal">關閉</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 左上角商家頭像 -->
    <img
      class="avatar"
      :src="require('@/assets/logo.png')"
      alt="merchant"
      @click="toggleSidebar"
    >

    <!-- 商家訂單管理 -->
    <div class="order-management">
      <div class="header-section">
        <h2>訂單管理（商家）</h2>
        <div class="filter-section">
          <label>狀態篩選：</label>
          <select v-model="filterState" @change="applyFilter">
            <option value="">全部</option>
            <option value="已送出">已送出</option>
            <option value="已接單">已接單</option>
            <option value="準備中">準備中</option>
            <option value="已完成">已完成</option>
            <option value="顧客已取餐">顧客已取餐</option>
            <option value="已取消">已取消</option>
          </select>
          <button @click.prevent.stop="handleRefreshOrders" class="refresh-btn">刷新訂單</button>
        </div>
      </div>

      <!-- 沒有訂單 -->
      <div v-if="!filteredOrders.length" class="empty">
        <p>目前沒有訂單</p>
      </div>

      <!-- 訂單列表 -->
      <div v-else class="order-cards">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-card"
        >
          <div class="order-header">
            <div class="order-info">
              <p><strong>訂單編號：</strong>{{ order.id }}</p>
              <p><strong>建立時間：</strong>{{ formatDate(order.createAt) }}</p>
              <p v-if="order.customerId">
                <strong>顧客ID：</strong>{{ order.customerId }}
              </p>
              <p v-if="order.customerId">
                <strong>顧客名稱：</strong>{{ order.customerNickname }}
              </p>
              <p v-else>
                <strong>訪客訂單</strong>
              </p>
              <p v-if="order.customerPhone">
                <strong>聯絡電話：</strong>{{ order.customerPhone }}
              </p>
            </div>
            <div class="order-status">
              <span :class="['status-badge', getStatusClass(order.state)]">
                {{ order.state }}
              </span>
            </div>
          </div>

          <div class="order-body">
            <!-- 餐點明細 -->
            <div class="order-items">
              <h4>餐點明細</h4>
              <ul>
                <li v-for="(item, idx) in order.items" :key="idx">
                  <span class="item-name">{{ item.itemName }}</span>
                  <span class="item-quantity">x {{ item.quantity }}</span>
                  <span class="item-price">$ {{ item.itemSubTotal }}</span>
                  <div v-if="item.customization && item.customization.length" class="item-custom">
                    客製化：{{ item.customization.join('、') }}
                  </div>
                </li>
              </ul>
            </div>

            <!-- 訂單資訊 -->
            <div class="order-details">
              <p><strong>用餐方式：</strong>{{ order.orderType }}</p>
              <p v-if="order.orderType === '內用' && order.dineInDetail">
                <strong>桌號：</strong>{{ order.dineInDetail.tableNumber }}
              </p>
              <p v-if="order.orderType === '外帶' && order.takeoutDetail">
                <strong>取餐時間：</strong>{{ formatTime(order.takeoutDetail.takeoutTime) }}
              </p>
              <p v-if="order.remarks">
                <strong>備註：</strong>{{ order.remarks }}
              </p>
              <p><strong>支付方式：</strong>{{ order.paymentMethod }}</p>
              <p class="total-amount"><strong>總金額：</strong>$ {{ order.totalAmount }}</p>
            </div>
          </div>

          <!-- 訂單操作 -->
          <div class="order-actions">
            <span v-if="order.state === '已取消'" class="cancelled-label">
              ✗ 訂單已取消
            </span>
            <template v-else>
              <button
                v-if="order.state === '已送出'"
                @click="updateOrderState(order.id, '已接單')"
                class="btn-accept"
              >
                接受訂單
              </button>
              <button
                v-if="order.state === '已接單'"
                @click="updateOrderState(order.id, '準備中')"
                class="btn-preparing"
              >
                開始準備
              </button>
              <button
                v-if="order.state === '準備中'"
                @click="updateOrderState(order.id, '已完成')"
                class="btn-complete"
              >
                製作完成
              </button>
              <button
                v-if="order.state === '已完成'"
                @click="updateOrderState(order.id, '顧客已取餐')"
                class="btn-picked"
              >
                顧客已取餐
              </button>
              <span v-if="order.state === '顧客已取餐'" class="completed-label">
                ✓ 訂單已完成
              </span>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()

const sidebarOpen = ref(false)
const filterState = ref('')
const orders = ref([])
const userModalOpen = ref(false)
const editCustomer = ref({ photo: '' })

// 從 Vuex 或 localStorage 獲取商家資訊
const storeId = computed(() => {
  // 這裡假設商家資訊存在 localStorage 或 Vuex 的 user store
  // 你可以根據實際情況調整
  const storeInfo = localStorage.getItem('currentStore')
  if (storeInfo) {
    return JSON.parse(storeInfo).id
  }
  // 或者從 Vuex 獲取
  return store.state.user?.storeId || null
})

const customer = computed(() => store.getters['user/customer'] || {})

const filteredOrders = computed(() => {
  if (!filterState.value) return orders.value
  return orders.value.filter(order => order.state === filterState.value)
})

onMounted(async () => {
  // 檢查商家資訊是否已載入
  if (!storeId.value) {
    console.log('商家資訊未載入，嘗試重新載入...')
    // 可以嘗試重新載入商家資訊
    const user = store.state.user?.customer
    if (user && user.role === 'owner') {
      console.log('檢測到商家用戶，建議先訪問店家設定頁面')
      alert('請先到「店家設定」頁面確認店家資訊已正確載入，然後再回來查看訂單')
      router.push('/store-management')
      return
    }
  }

  await loadOrders()
})

const loadOrders = async () => {
  try {
    if (!storeId.value) {
      console.warn('未找到商家ID，嘗試重新載入店家資訊...')
      // 嘗試從 Vuex store 重新載入店家資訊
      const user = store.state.user?.customer
      if (user && user.role === 'owner') {
        console.log('嘗試重新載入店家資訊...')
        // 這裡可以添加重新載入店家資訊的邏輯
        alert('請先到「店家設定」頁面確認店家資訊已正確載入')
        router.push('/store-management')
        return
      } else {
        alert('請先登入商家帳號')
        router.push('/login')
        return
      }
    }

    // 獲取商家訂單
    const data = await store.dispatch('order/fetchStoreOrders', storeId.value)
    orders.value = data || []
  } catch (err) {
    console.error('載入訂單失敗:', err)
    alert('載入訂單失敗: ' + err.message)
  }
}

const handleRefreshOrders = async (event) => {
  console.log('點擊刷新訂單按鈕', event)
  event.preventDefault()
  event.stopPropagation()

  console.log('開始刷新訂單...')
  try {
    await loadOrders()
    console.log('訂單刷新完成')
    alert('訂單已刷新')
  } catch (error) {
    console.error('刷新訂單失敗:', error)
    alert('刷新訂單失敗: ' + error.message)
  }
}



const updateOrderState = async (orderId, newState) => {
  try {
    await store.dispatch('order/updateOrder', {
      orderId,
      updates: { state: newState }
    })
    
    // 更新本地訂單狀態
    const order = orders.value.find(o => o.id === orderId)
    if (order) {
      order.state = newState
    }
    
    alert(`訂單狀態已更新為：${newState}`)
  } catch (err) {
    console.error('更新訂單狀態失敗:', err)
    alert('更新失敗: ' + err.message)
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const getStatusClass = (state) => {
  const statusMap = {
    '已送出': 'status-new',
    '已接單': 'status-accepted',
    '準備中': 'status-preparing',
    '已完成': 'status-complete',
    '顧客已取餐': 'status-picked',
    '已取消': 'status-cancelled'
  }
  return statusMap[state] || ''
}

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const openUserModal = () => {
  editCustomer.value = { ...customer.value }
  userModalOpen.value = true
}

const closeUserModal = () => {
  userModalOpen.value = false
}

const updateUserInfo = async () => {
  try {
    // 驗證電子郵件格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (editCustomer.value.email && !emailRegex.test(editCustomer.value.email)) {
      alert('請輸入有效的電子郵件地址')
      return
    }

    const userId = editCustomer.value.id
    const updates = { ...editCustomer.value }
    delete updates.id

    await store.dispatch('user/updateUser', { userId, updates })
    alert('使用者資訊已更新！')
    closeUserModal()
  } catch (err) {
    console.error(err)
    alert('更新失敗，請稍後再試: ' + err.message)
  }
}

const onAvatarChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = e => editCustomer.value.photo = e.target.result
    reader.readAsDataURL(file)
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('currentStore')
  router.push('/login')
}
</script>
  
<style scoped>
.merchant-order-page {
  font-family: "Microsoft JhengHei","PingFang TC","Noto Sans TC",sans-serif;
  background: #f5f5f5;
  padding: 20px;
  min-height: 100vh;
}

/* Sidebar */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 250px;
  height: 100%;
  background-color: #002244;
  padding: 20px;
  transform: translateX(-100%);
  transition: transform 0.3s;
  z-index: 100;
  display: flex;
  flex-direction: column;
}
.sidebar.open {
  transform: translateX(0);
}
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.sidebar-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
}
.username {
  color: #fff;
  font-weight: bold;
}
.sidebar ul {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.sidebar li {
  color: #fff;
  padding: 10px;
  cursor: pointer;
  border-radius: 6px;
  text-align: left;
}
.sidebar li:hover {
  background-color: #003366;
}
.sidebar li.active {
  background-color: #001633;
}
.sidebar-logout {
    margin-top: auto; /* 推到底部 */
    width: 100%;
}

    .sidebar-logout button {
        width: 100%;
        padding: 10px 0;
        background-color: #fff;
        color: black;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 16px;
    }

        .sidebar-logout button:hover {
            background-color: #0069D9;
        }

/* Avatar */
.avatar {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  z-index: 101;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

/* Order Management */
.order-management {
  margin-top: 80px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.header-section h2 {
  color: #002244;
  font-size: 28px;
  margin: 0;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-section label {
  font-weight: bold;
  color: #333;
}

.filter-section select {
  padding: 8px 12px;
  border: 2px solid #0069D9;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.refresh-btn {
  padding: 8px 16px;
  background: #0069D9;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.refresh-btn:hover {
  background: #0056b3;
}

/* Order Cards */
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 15px;
}

.order-info p {
  margin: 5px 0;
  color: #333;
  text-align: left;
}

.order-status {
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 14px;
}

.status-new {
  background: #fff3cd;
  color: #856404;
}

.status-accepted {
  background: #d1ecf1;
  color: #0c5460;
}

.status-preparing {
  background: #d4edda;
  color: #155724;
}

.status-complete {
  background: #d1ecf1;
  color: #0c5460;
}

.status-picked {
  background: #d4edda;
  color: #155724;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

/* Order Body */
.order-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .order-body {
    grid-template-columns: 1fr;
  }
}

.order-items h4 {
  color: #002244;
  margin-top: 0;
  margin-bottom: 12px;
}

.order-items ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.order-items li {
  padding: 8px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.item-name {
  font-weight: bold;
  color: #333;
}

.item-quantity {
  color: #666;
  margin: 0 10px;
}

.item-price {
  color: #0069D9;
  font-weight: bold;
}

.item-custom {
  width: 100%;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.order-details p {
  margin: 8px 0;
  color: #333;
}

.total-amount {
  font-size: 18px;
  color: #0069D9;
  font-weight: bold;
  margin-top: 15px !important;
  padding-top: 10px;
  border-top: 2px solid #f0f0f0;
}

/* Order Actions */
.order-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  align-items: center;
}

.order-actions button {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-accept {
  background: #28a745;
  color: #fff;
}

.btn-accept:hover {
  background: #218838;
}

.btn-preparing {
  background: #007bff;
  color: #fff;
}

.btn-preparing:hover {
  background: #0056b3;
}

.btn-complete {
  background: #17a2b8;
  color: #fff;
}

.btn-complete:hover {
  background: #138496;
}

.btn-picked {
  background: #6c757d;
  color: #fff;
}

.btn-picked:hover {
  background: #5a6268;
}

.completed-label {
  color: #28a745;
  font-weight: bold;
  font-size: 16px;
}

.cancelled-label {
  color: #dc3545;
  font-weight: bold;
  font-size: 16px;
}

/* Empty State */
.empty {
  text-align: center;
  margin-top: 80px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.empty p {
  font-size: 18px;
  color: #666;
}

/* Overlay */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 50;
}

/* User Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 200;
}

.user-modal {
  background-color: #fff;
  padding: 20px 30px;
  border-radius: 12px;
  width: 300px;
  max-width: 90%;
  text-align: left;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}

.user-modal h3 {
  color: #0069D9;
  margin-bottom: 15px;
}

.form-group {
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 4px;
  font-weight: bold;
}

.form-group input {
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
}

.modal-actions button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.modal-actions button:first-child {
  background-color: #0069D9;
  color: #fff;
}

.modal-actions button:last-child {
  background-color: #ccc;
  color: #333;
}

.preview-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 8px;
}
</style>
  