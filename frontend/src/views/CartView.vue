<template>
  <div class="cart-page">
    <div class="cart-container">
      <div class="header">
        <button class="btn-back" @click="goBack">返回</button>
        <button class="btn-avatar" @click="showProfile">頭像</button>
      </div>

      <div class="cart-items">
        <div v-for="(item, index) in cart.items" :key="index" class="cart-item">
          <div class="item-image">
            <div class="image-placeholder">餐點圖片</div>
          </div>

          <div class="item-info">
            <h3 class="item-name">{{ item.itemName || '餐點名稱' }}</h3>
            <p class="item-customization">{{ item.customization.join('、') || '無客製化' }}</p>
          </div>

          <div class="item-quantity">
            <span class="quantity-label">數量</span>
            <div class="quantity-row">
              <button class="btn-quantity" @click="decreaseQuantity(index)">-</button>
              <input type="number" v-model.number="item.quantity" min="1" @change="updateQuantity(index)" />
              <button class="btn-quantity" @click="increaseQuantity(index)">+</button>
            </div>
            
            <div class="subtotal-row">
              <span class="item-subtotal">小計金額：${{ item.itemSubTotal }}</span>
            </div>
          </div>

          <div class="item-actions">
            <button class="btn-delete" @click="deleteItem(index)">刪除餐點</button>
          </div>
        </div>

        <div v-if="cart.items.length === 0" class="empty-cart">
          購物車是空的
        </div>
      </div>

      <div class="order-section">
        <div class="top-row">
          <div class="notes-section">
            <label for="remarks">備註</label>
            <input type="text" id="remarks" v-model="remarks" placeholder="請輸入備註" />
          </div>

          <div class="total-amount">
            總金額：${{ cart.totalAmount }}
          </div>
        </div>

        <div class="bottom-row">
          <div class="dining-options">
            <div class="dining-option-row">
              <label class="radio-item">
                <input type="radio" v-model="orderType" value="內用" />
                <span>內用</span>
              </label>
              <button class="btn-option" @click="inputTableNumber">輸入桌號</button>
            </div>

            <div class="dining-option-row">
              <label class="radio-item">
                <input type="radio" v-model="orderType" value="外帶" />
                <span>外帶</span>
              </label>
              <button class="btn-option" @click="setPickupTime">設定取餐時間</button>
              <button class="btn-option highlight" @click="inputPhoneNumber">輸入電話號碼</button>
            </div>
          </div>

          <div class="checkout-buttons">
            <button class="btn-option" @click="selectPaymentMethod">選擇支付方式</button>
            <button class="btn-checkout" @click="checkout">前往結帳</button>
          </div>
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

const remarks = ref('')
const orderType = ref('外帶')

const storeId = computed(() => store.state.cart.storeId) 
const customerId = ref('') 
const customerPhone = ref('')


const tableNumber = ref('')

const takeoutTime = ref(null) // Date

const paymentMethod = ref('店內付款')

const cart = computed(() => ({
  items: store.state.cart.items,
  totalAmount: store.getters['cart/totalAmount']
}))

const goBack = () => {
  router.back()
}

const showProfile = () => {
  alert('個人資料功能未實作')
}

const inputTableNumber = () => {
  const input = prompt('請輸入桌號:')
  if (input) {
    tableNumber.value = input
    alert(`桌號已設定為: ${input}`)
  }
}

const setPickupTime = () => {
  const input = prompt('請輸入取餐時間 (例如: 2024-01-01 12:00):')
  if (input) {
    takeoutTime.value = new Date(input)
    alert(`取餐時間已設定為: ${input}`)
  }
}

const inputPhoneNumber = () => {
  const input = prompt('請輸入電話號碼:')
  if (input) {
    customerPhone.value = input
    alert(`電話號碼已設定為: ${input}`)
  }
}

const selectPaymentMethod = () => {
  alert('目前僅支持店內付款')
  paymentMethod.value = '店內付款'
}

const deleteItem = (index) => {
  store.dispatch('cart/removeItem', index)
}

const increaseQuantity = (index) => {
  const newQuantity = store.state.cart.items[index].quantity + 1
  store.dispatch('cart/updateItemQuantity', { index, quantity: newQuantity })
}

const decreaseQuantity = (index) => {
  if (store.state.cart.items[index].quantity > 1) {
    const newQuantity = store.state.cart.items[index].quantity - 1
    store.dispatch('cart/updateItemQuantity', { index, quantity: newQuantity })
  }
}

const updateQuantity = (index) => {
  const quantity = store.state.cart.items[index].quantity
  if (quantity < 1) {
    store.dispatch('cart/updateItemQuantity', { index, quantity: 1 })
  } else {
    store.dispatch('cart/updateItemQuantity', { index, quantity })
  }
}

const checkout = () => {
  if (store.state.cart.items.length === 0) {
    alert('購物車是空的')
    return
  }

  // 验证必填字段
  if (!storeId.value) {
    alert('錯誤：缺少店家ID（系統錯誤）')
    return
  }

  if (!customerId.value && !customerPhone.value) {
    alert('請輸入電話號碼（未登入用戶必填）')
    return
  }

  if (orderType.value === '內用' && !tableNumber.value) {
    alert('請輸入桌號')
    return
  }

  if (orderType.value === '外帶' && !takeoutTime.value) {
    alert('請設定取餐時間')
    return
  }

  const orderData = {
    storeId: storeId.value,
    customerId: customerId.value || null,
    customerPhone: customerPhone.value || '',
    orderType: orderType.value,
    dineInDetail: orderType.value === '內用' ? { 
      tableNumber: tableNumber.value 
    } : null,
    takeoutDetail: orderType.value === '外帶' ? { 
      takeoutTime: takeoutTime.value 
    } : null,
    items: store.state.cart.items,
    totalAmount: store.getters['cart/totalAmount'],
    remarks: remarks.value,
    paymentMethod: paymentMethod.value
  }

  console.log('準備送出的訂單數據:', orderData)
  
  alert('訂單數據已準備完成，請查看控制台')
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background-color: #fff;
  color: #333;
  padding: 20px;
  font-family: "Microsoft JhengHei", "PingFang TC", "Noto Sans TC", sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.btn-back {
  padding: 10px 20px;
  border: 2px solid #0069D9;
  background: #0069D9;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-back:hover {
  background: #0056b3;
  border-color: #0056b3;
}

.btn-avatar {
  width: 50px;
  height: 50px;
  border: none;
  background-color: #0069D9;
  color: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s;
}

.btn-avatar:hover {
  background-color: #0056b3;
}

.cart-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
  border-radius: 10px;
  background-color: #f9f9f9;
  box-shadow: 0 2px 8px rgba(0, 105, 217, 0.1);
}

.cart-items {
  margin-bottom: 30px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

.cart-item {/*圖和字和按鈕間怎麼排*/ 
  display: flex;
  gap: 20px;
  padding: 20px;
  margin-bottom: 20px;
  align-items: center;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
}

.cart-item:hover {
  box-shadow: 0 4px 12px rgba(0, 105, 217, 0.15);
}

.item-image {
  width: 100px;
  height: 100px;
  border: 2px solid #0069D9;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background-color: #f0f7ff;
}

.image-placeholder {
  text-align: center;
  color: #0069D9;
  font-size: 12px;
}

.item-info {/*字之間怎麼排*/ 
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
  color: #333;
}

.item-customization {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.item-quantity {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: auto;
  align-items: center;
}

.quantity-label {
  font-size: 16px;
  font-weight: 600;
  color: #0069D9;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.subtotal-row {
  display: flex;
  align-items: center;
}

.item-actions {
  display: flex;
  align-items: center;
}

.btn-delete {
  padding: 8px 16px;
  background-color: white;
  border: 2px solid #ff4444;
  color: #ff4444;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.3s;
}

.btn-delete:hover {
  background-color: #ff4444;
  color: white;
}

.btn-quantity {
  width: 35px;
  height: 35px;
  border: 2px solid #0069D9;
  background: #0069D9;
  color: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: all 0.3s;
}

.btn-quantity:hover {
  background: #0056b3;
  border-color: #0056b3;
}

.quantity-row input {
  width: 60px;
  padding: 8px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  border: 2px solid #0069D9;
  background: white;
  color: #333;
  border-radius: 8px;
}

.quantity-row input:focus {
  outline: none;
  border-color: #0056b3;
}

.quantity-row input::-webkit-inner-spin-button,
.quantity-row input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.quantity-row input[type=number] {
  -moz-appearance: textfield;
  appearance: textfield;
}

.item-subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #0069D9;
}

.empty-cart {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #999;
}

.order-section {
  border-top: 3px solid #0069D9;
  padding-top: 30px;
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  margin-top: 20px;
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 25px;
  gap: 30px;
}

.notes-section {
  flex: 1;
}

.notes-section label {
  display: block;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #0069D9;
}

.notes-section input {
  width: 100%;
  padding: 12px;
  background: white;
  border: 2px solid #0069D9;
  color: #333;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.notes-section input:focus {
  outline: none;
  border-color: #0056b3;
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  white-space: nowrap;
  color: #0069D9;
}

.bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 30px;
}

.dining-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: flex-start;
}

.dining-option-row {
  display: flex;
  align-items: center;
  gap: 15px;
}

.checkout-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: flex-end;
}

.radio-item {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.radio-item input {
  margin-right: 10px;
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #0069D9;
}

.radio-item span {
  font-size: 18px;
  color: #333;
}

.btn-option {
  padding: 12px 24px;
  background: #0069D9;
  border: 2px solid #0069D9;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  height: 45px;
  align-items: center;
  justify-content: center;
  display: flex;
  transition: all 0.3s;
}

.btn-option:hover {
  background: #0056b3;
  border-color: #0056b3;
}

.btn-option.highlight {
  background-color: #0069D9;
  border-color: #0069D9;
  color: white;
}

.btn-option.highlight:hover {
  background-color: #0056b3;
  border-color: #0056b3;
}

.btn-checkout {
  padding: 12px 30px;
  background: #0069D9;
  border: 2px solid #0069D9;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-checkout:hover {
  background: #0056b3;
  border-color: #0056b3;
}
</style>

