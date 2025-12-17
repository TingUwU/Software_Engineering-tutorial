<template>
  <div class="cart-page">
    <!-- 遮罩層 -->
    <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

    <!-- 側邊欄（訪客） -->
    <div :class="['sidebar', { open: sidebarOpen }]">
      <div class="sidebar-user">
        <img class="sidebar-avatar" :src="require('@/assets/logo.png')" alt="guest">
        <span class="username">訪客，肚子餓了嗎</span>
      </div>

      <ul>
        <router-link to="/nologincart"><li>購物車</li></router-link>
        <li>訂單管理</li>
      </ul>
    </div>

    <!-- 左上角訪客頭像 -->
    <img
      class="avatar"
      :src="require('@/assets/logo.png')"
      alt="guest"
      @click="toggleSidebar"
    >

    <!-- 購物車內容 -->
    <div class="cart-container">
      <div class="cart-items">
        <template v-if="cart.items.length">
          <div
            v-for="(item, index) in cart.items"
            :key="index"
            class="cart-item"
          >
            <div class="item-image">
              <div class="image-placeholder">餐點圖片</div>
            </div>

            <div class="item-info">
              <h3 class="item-name">{{ item.itemName }}</h3>
              <p class="item-customization">
                {{ item.customization?.length ? item.customization.join('、') : '無客製化' }}
              </p>
            </div>

            <div class="item-quantity">
              <span>數量</span>
              <div class="quantity-row">
                <button class="btn-quantity" @click="decreaseQuantity(index)">-</button>
                <span class="quantity-display">{{ item.quantity }}</span>
                <button class="btn-quantity" @click="increaseQuantity(index)">+</button>
              </div>
              <div class="subtotal-row">
                小計：${{ item.itemSubTotal }}
              </div>
            </div>

            <div class="item-actions">
              <button class="btn-delete" @click="deleteItem(index)">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18"
                     fill="none" stroke="white" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round"
                     viewBox="0 0 24 24">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6L17.5 19H6.5L5 6"/>
                  <path d="M10 11v6M14 11v6"/>
                </svg>
              </button>
            </div>
          </div>
        </template>

        <div v-else class="empty-cart">購物車是空的</div>
      </div>

      <!-- 訂單區 -->
      <div class="order-section">
        <div class="order-row notes-row">
          <label>備註</label>
          <input v-model="remarks" placeholder="請輸入備註" />
        </div>

        <div class="order-row dining-row">
          <label><input type="radio" v-model="orderType" value="內用"> 內用</label>
          <label><input type="radio" v-model="orderType" value="外帶"> 外帶</label>

          <div v-if="orderType==='內用'">
            桌號 <input v-model="tableNumber" />
          </div>
          <div v-if="orderType==='外帶'">
            <div>
              取餐時間
              <input type="time" v-model="takeoutTime" />
            </div>

            <div>
              手機號碼
              <input
                type="tel"
                v-model="phoneNumber"
                placeholder="請輸入手機號碼"
              />
            </div>
          </div>
        </div>

        <div class="order-row total-row">
          <div>總金額：${{ cart.totalAmount }}</div>
          <select v-model="paymentMethod">
            <option disabled value="">選擇付款方式</option>
            <option>現金</option>
            <option>LinePay</option>
            <option>ApplePay</option>
          </select>
        </div>

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
const remarks = ref('')
const orderType = ref('外帶')
const tableNumber = ref('')
const takeoutTime = ref('')
const paymentMethod = ref('')
const phoneNumber = ref('')
// 購物車
const cart = computed(() => ({
  items: store.state.cart.items || [],
  totalAmount: store.getters['cart/totalAmount'] || 0
}))

const toggleSidebar = () => sidebarOpen.value = !sidebarOpen.value
const goBack = () => router.back()

const deleteItem = index => store.dispatch('cart/removeItem', index)
const increaseQuantity = index =>
  store.dispatch('cart/updateItemQuantity', {
    index,
    quantity: cart.value.items[index].quantity + 1
  })
const decreaseQuantity = index => {
  const q = cart.value.items[index].quantity
  if (q > 1) {
    store.dispatch('cart/updateItemQuantity', { index, quantity: q - 1 })
  }
}

const checkout = () => {
  if (!cart.value.items.length) return alert('購物車是空的')
  if (orderType.value === '內用' && !tableNumber.value) return alert('請輸入桌號')
 if (orderType.value === '外帶') {
  if (!takeoutTime.value) return alert('請選擇取餐時間')
  if (!phoneNumber.value) return alert('請輸入手機號碼')
}
  if (!paymentMethod.value) return alert('請選擇付款方式')
 
  const orderData = {
    storeId: store.state.cart.storeId,
    customerId: null,
    orderType: orderType.value,
    dineInDetail: orderType.value === '內用'
      ? { tableNumber: tableNumber.value }
      : null,
    takeoutDetail: orderType.value === '外帶'
      ? {
          takeoutTime: takeoutTime.value,
          phoneNumber: phoneNumber.value
        }
      : null,
    items: cart.value.items,
    totalAmount: cart.value.totalAmount,
    remarks: remarks.value,
    paymentMethod: paymentMethod.value
  }

  console.log('訪客訂單資料:', orderData)
  alert('訪客訂單資料已準備完成（請看 console）')
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
.sidebar li { color:#fff; cursor:pointer; padding:10px 0; border-radius:4px; text-align: left;}
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

