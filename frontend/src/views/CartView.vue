<template>
  <div class="cart-page">
    <!-- 遮罩層 -->
    <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

    <!-- 左側側邊欄 -->
    <div :class="['sidebar', { open: sidebarOpen }]">
      <div class="sidebar-user">
        <img class="sidebar-avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user">
        <span class="username">{{ customer.nickname || '訪客' }}, 肚子餓了嗎</span>
      </div>
      <ul>
        <li @click="openUserModal">使用者資訊</li>
        <router-link to="/cart"><li>購物車</li></router-link>
        <li>訂單管理</li>
        <li>歷史</li>
        <router-link to="/favorite"><li>收藏</li></router-link>
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

    <!-- 左上角顧客頭像 -->
    <img class="avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user" @click="toggleSidebar">

    <!-- 購物車內容 -->
    <div class="cart-container">

      <div class="cart-items">
        <template v-if="cart.items && cart.items.length > 0">
          <div
            v-for="(item, index) in cart.items"
            :key="index"
            class="cart-item"
          >
            <div class="item-image">
              <div class="image-placeholder">餐點圖片</div>
            </div>

            <div class="item-info">
              <h3 class="item-name">{{ item.itemName || '餐點名稱' }}</h3>
              <p class="item-customization">
                {{ (item.customization && item.customization.length > 0) ? item.customization.join('、') : '無客製化' }}
              </p>
            </div>

            <div class="item-quantity">
              <span class="quantity-label">數量</span>
              <div class="quantity-row">
                <button class="btn-quantity" @click="decreaseQuantity(index)">-</button>
                <span class="quantity-display">{{ item.quantity }}</span>
                <button class="btn-quantity" @click="increaseQuantity(index)">+</button>
              </div>
              <div class="subtotal-row">
                <span class="item-subtotal">小計金額：${{ item.itemSubTotal || 0 }}</span>
              </div>
            </div>

            <div class="item-actions">
              <button class="btn-delete" @click="deleteItem(index)">
                <!-- 使用 Font Awesome 或 SVG 垃圾桶圖案 -->
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-trash-2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6L17.5 19H6.5L5 6m5 0V4a2 2 0 0 1 2-2h0a2 2 0 0 1 2 2v2"/></svg>
              </button>
            </div>
          </div>
        </template>

        <div v-else class="empty-cart">
          購物車是空的
        </div>
      </div>
    


      <div class="order-section">
        <!-- 第一行：備註 -->
        <div class="order-row notes-row">
          <label for="remarks">備註</label>
          <input type="text" id="remarks" v-model="remarks" placeholder="請輸入完整備註" />
        </div>

        <!-- 第二行：用餐方式 -->
        <div class="order-row dining-row">
          <div class="dining-option">
            <label>
              <input type="radio" v-model="orderType" value="內用" />
              內用
            </label>
            <label>
              <input type="radio" v-model="orderType" value="外帶" />
              外帶
            </label>
          </div>
          <div class="time-setting" v-if="orderType==='內用'">
            桌號: <input type="text" v-model="tableNumber" placeholder="桌號"/>
          </div>
          <div class="time-setting" v-if="orderType==='外帶'">
            取餐時間: <input type="time" v-model="takeoutTime"/>
          </div>
        </div>

        <!-- 第三行：總金額 + 支付方式 -->
        <div class="order-row total-row">
          <div class="total-amount">總金額：${{ cart.totalAmount || 0 }}</div>
          <div class="payment-method">
            支付方式:
            <select v-model="paymentMethod">
              <option value="現金">現金</option>
              <option value="LinePay">LinePay</option>
              <option value="ApplePay">ApplePay</option>
            </select>
          </div>
        </div>

        <!-- 第四行：按鈕 -->
        <div class="order-row action-row">
          <button class="btn-back" @click="goBack">繼續購物</button>
          <button class="btn-checkout" @click="checkout">前往結帳</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()

const sidebarOpen = ref(false)
const userModalOpen = ref(false)
const editCustomer = ref({ photo: '' })

const remarks = ref('')
const orderType = ref('外帶')
const tableNumber = ref('')
const takeoutTime = ref(null)
const customerPhone = ref('')
const paymentMethod = ref('店內付款')

// Cart computed
const cart = computed(() => ({
  items: store.state.cart.items || [],
  totalAmount: store.getters['cart/totalAmount'] || 0
}))

const customer = computed(() => store.getters['user/customer'] || {})

// Methods
const toggleSidebar = () => (sidebarOpen.value = !sidebarOpen.value)
const openUserModal = () => { editCustomer.value = { ...customer.value }; userModalOpen.value = true }
const closeUserModal = () => (userModalOpen.value = false)

const onAvatarChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = e => editCustomer.value.photo = e.target.result
    reader.readAsDataURL(file)
  }
}

const logout = () => {
  store.dispatch('user/logout')
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const goBack = () => router.back()

const deleteItem = (index) => store.dispatch('cart/removeItem', index)
const increaseQuantity = (index) => store.dispatch('cart/updateItemQuantity', {
  index,
  quantity: (store.state.cart.items[index]?.quantity || 0) + 1
})
const decreaseQuantity = (index) => {
  const qty = store.state.cart.items[index]?.quantity || 1
  if (qty > 1) store.dispatch('cart/updateItemQuantity', { index, quantity: qty - 1 })
}




const updateUserInfo = async () => {
  try {
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

const checkout = () => {
  if (!cart.value.items.length) return alert('購物車是空的')
  const orderData = {
    storeId: store.state.cart.storeId,
    customerId: customer.value.id || null,
    customerPhone: customerPhone.value || '',
    orderType: orderType.value,
    dineInDetail: orderType.value === '內用' ? { tableNumber: tableNumber.value } : null,
    takeoutDetail: orderType.value === '外帶' ? { takeoutTime: takeoutTime.value } : null,
    items: cart.value.items,
    totalAmount: cart.value.totalAmount,
    remarks: remarks.value,
    paymentMethod: paymentMethod.value
  }
  console.log('準備送出的訂單數據:', orderData)
  alert('訂單數據已準備完成，請查看控制台')
}
</script>

<style scoped>
.cart-page { min-height: 100vh; padding: 20px; font-family: "Microsoft JhengHei", sans-serif; background: #fff; color: #333; }

/* Cart item 排版 */
.cart-item { display: flex; gap: 20px; padding: 20px; margin-bottom: 20px; background-color: white; border-radius: 12px; align-items: center; }
.item-image { width: 100px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #f0f7ff; border-radius: 8px; border: 2px solid #0069D9; }
.image-placeholder { text-align: center; color: #0069D9; font-size: 12px; }
.item-info { flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 8px; }
.item-name { font-size: 18px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-customization { font-size: 14px; color: #666; white-space: normal; word-break: break-word; }

.item-actions { width: 90px; flex-shrink: 0; display: flex; justify-content: end; }
.quantity-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-quantity {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: #0069D9;
  color: #fff;
  border: none;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-quantity:hover {
  background: #0056b3;
}

.quantity-display {
  width: 40px;
  text-align: center;
  font-weight: bold;
  font-size: 16px;
}
.btn-delete {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: #ff4444; /* 紅底 */
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-delete:hover {
  background: #cc0000;
}

.btn-delete svg {
  width: 16px;
  height: 16px;
  stroke: white; /* 白色線條 */
}
/* Sidebar */
.overlay { position: fixed; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.5); z-index:50; }
.sidebar { position: fixed; top:0; left:0; width:250px; height:100%; background:#002244; padding:20px; box-sizing:border-box; transform: translateX(-100%); transition: transform 0.3s ease; z-index:100; display:flex; flex-direction:column; }
.sidebar.open { transform: translateX(0); }
.sidebar-user { display:flex; align-items:center; gap:10px; margin-bottom:20px; }
.sidebar-avatar { width:50px; height:50px; border-radius:50%; }
.username { color:#fff; font-weight:bold; }
.sidebar ul { list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:15px; }
.sidebar li { color:#fff; cursor:pointer; padding:10px 0; border-radius:4px; }
.sidebar li:hover { background:#001633; }
.sidebar-logout { margin-top:auto; }
.sidebar-logout button { width:100%; padding:10px 0; background:#fff; color:black; border:none; border-radius:6px; cursor:pointer; }
.sidebar-logout button:hover { background:#0069D9; color:#fff; }
.avatar { position: fixed; top:20px; left:20px; width:50px; height:50px; border-radius:50%; cursor:pointer; z-index:101; }
.preview-avatar { width:80px; height:80px; border-radius:50%; object-fit:cover; margin-bottom:8px; }

/* Order section */
.top-row { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:25px; gap:30px; }
.notes-section { flex:1; }
.notes-section input { width:100%; padding:12px; border:2px solid #0069D9; border-radius:8px; }
.total-amount { font-size:24px; font-weight:bold; color:#0069D9; }
.bottom-row { display:flex; justify-content:space-between; align-items:flex-start; gap:30px; }
.dining-options { display:flex; flex-direction:column; gap:15px; align-items:flex-start; }
.dining-option-row { display:flex; align-items:center; gap:15px; }
.radio-item { display:flex; align-items:center; cursor:pointer; }
.radio-item input { margin-right:10px; width:20px; height:20px; cursor:pointer; accent-color:#0069D9; }
.radio-item span { font-size:18px; color:#333; }
.btn-option { padding:12px 24px; background:#0069D9; border:2px solid #0069D9; color:#fff; border-radius:8px; cursor:pointer; font-weight:600; display:flex; align-items:center; justify-content:center; }
.btn-option:hover { background:#0056b3; border-color:#0056b3; }
.btn-option.highlight { background:#0069D9; border-color:#0069D9; color:#fff; }
.btn-checkout { padding:12px 30px; background:#0069D9; border:2px solid #0069D9; color:#fff; border-radius:8px; cursor:pointer; font-size:18px; font-weight:600; }
.btn-checkout:hover { background:#0056b3; border-color:#0056b3; }

.empty-cart { text-align:center; padding:40px; font-size:18px; color:#999; }
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
            .order-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.order-row {
  display: flex;
  align-items: center;
  gap: 15px;
}

.notes-row input {
  flex: 1;
  padding: 10px;
  border: 2px solid #0069D9;
  border-radius: 8px;
}

.dining-row .dining-option label {
  margin-right: 15px;
  font-size: 16px;
}

.time-setting input {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.total-row {
  justify-content: space-between;
  font-size: 18px;
  font-weight: 600;
}

.payment-method select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

.action-row {
  display: flex;
  gap: 10px;
}

.action-row button {
  flex: 1;
  padding: 12px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 8px;
  cursor: pointer;
}

.btn-back {
  background: #ccc;
  color: #333;
  border: none;
}

.btn-checkout {
  background: #0069D9;
  color: #fff;
  border: none;
}
.quantity-row input[type=number]::-webkit-outer-spin-button,
.quantity-row input[type=number]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.quantity-row input[type=number] {
  -moz-appearance: textfield; /* Firefox */
  text-align: center;
  width: 50px;
  border: 2px solid #0069D9;
  border-radius: 6px;
  padding: 6px;
}
</style>
