<template>
  <!-- 遮罩層 -->
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        
        <!-- 關閉按鈕 -->
        <button class="btn-close" @click="closeModal">✕</button>

        <div class="product-top">
          <div class="product-image"><!--圖片區-->
            <div class="image-placeholder">餐點圖片</div>
          </div>
          <div class="product-info"><!--資訊區-->
            <h2 class="product-name">{{ productItem.itemName }}</h2>
            <p class="product-price">餐點金額：${{ productItem.price }}</p>
            <div v-if="productItem.description" class="product-description">
              <p>{{ productItem.description }}</p>
            </div>
          </div>
          <div class="action-buttons"><!--按鈕區-->
            <button class="btn-action" @click="addToCustom">加入自訂組合</button>
            <button class="btn-action" @click="addToFavorite">{{ isFavorited ? '取消收藏' : '收藏' }}</button>
          </div>
        </div>

        <div class="product-content">
          <div class="customization" v-if="productItem.customOptions && productItem.customOptions.length > 0">
            <h3>客製化選項</h3>
            <div class="checkbox-group">
              <label class="checkbox-item" v-for="(option, index) in productItem.customOptions" :key="index">
                <input type="checkbox" :value="index" v-model="selectedCustomizations" />
                <span>{{ option.name }}{{ option.price > 0 ? ` (+$${option.price})` : option.price < 0 ? ` (-$${Math.abs(option.price)})` : '' }}</span>
              </label>
            </div>
          </div>

          <div class="quantity-section">
            <div class="quantity-left">
              <h3>數量</h3>
              <div class="quantity-control">
                <button class="btn-quantity" @click="decreaseQuantity">-</button>
                <input type="number" v-model.number="quantity" min="1" />
                <button class="btn-quantity" @click="increaseQuantity">+</button>
              </div>
            </div>
            <div class="quantity-right">
              <span class="subtotal">小計金額：${{ subtotal }}</span>
              <button class="btn-add-to-cart" @click="addToCart">加入購物車</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 自訂組合選擇 Modal -->
      <div v-if="showCustomComboModal" class="custom-combo-modal" @click.self="closeCustomComboModal">
        <div class="custom-combo-modal-content">
          <h3>選擇自訂組合</h3>
          <p class="combo-subtitle">選擇要加入的組合（僅顯示可加入的組合）</p>

          <div v-if="availableCombos.length > 0" class="combo-list">
            <div v-for="combo in availableCombos"
                 :key="combo.comboId"
                 class="combo-item"
                 @click="selectCombo(combo)">
              <div class="combo-info">
                <h4>{{ combo.comboName }}</h4>
                <p v-if="combo.storeId" class="combo-store">
                  店家：{{ getStoreName(combo.storeId) }}
                </p>
                <p v-else class="combo-empty">尚未設定店家</p>
                <p class="combo-items-count">
                  商品數量：{{ combo.items ? combo.items.length : 0 }}
                </p>
              </div>
              <div class="combo-select-btn">+</div>
            </div>
          </div>

          <div v-else class="no-combos">
            <p>沒有可用的組合</p>
            <p class="no-combos-hint">請先在收藏頁面建立自訂組合，或選擇同店家的組合</p>
          </div>

          <div class="combo-modal-actions">
            <button class="cancel-btn" @click="closeCustomComboModal">取消</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  product: {
    type: Object,
    default: () => ({
      id: '',
      itemName: 'null',
      price: 50,
      customOptions: []
    })
  },
  isFavorited: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['close', 'add-to-cart', 'toggle-favorite'])

const store = useStore()

// 使用 props 中的 product
const productItem = computed(() => props.product)

// 獲取用戶自訂組合
const userCustomCombos = computed(() => store.getters['user/customer'].customCombos || [])

// 篩選可用的組合（同店家或空組合）
const availableCombos = computed(() => {
  return userCustomCombos.value.filter(combo => {
    // 如果組合還沒有設定店家，可以加入
    if (!combo.storeId) return true
    // 如果組合的店家與當前商品的店家相同，可以加入
    return combo.storeId === productItem.value.storeId
  })
})

const quantity = ref(1)
const selectedCustomizations = ref([])
const showCustomComboModal = ref(false)
const selectedCombo = ref(null)

// 當彈窗關閉時重置數據
watch(() => props.show, (newVal) => {
  if (!newVal) {
    quantity.value = 1
    selectedCustomizations.value = []
    showCustomComboModal.value = false
    selectedCombo.value = null
  }
})

const subtotal = computed(() => {
  let total = productItem.value.price * quantity.value
  selectedCustomizations.value.forEach(optionIndex => {
    const option = productItem.value.customOptions[optionIndex]
    if (option && option.price) {
      total += option.price * quantity.value
    }
  })
  return total
})

const increaseQuantity = () => {
  quantity.value++
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const closeModal = () => {
  emit('close')
}

const addToCart = () => {
  const customizationText = selectedCustomizations.value.map(index => {
    const option = productItem.value.customOptions[index]
    return option.name + (option.price > 0 ? ` (+$${option.price})` : option.price < 0 ? ` (-$${Math.abs(option.price)})` : '')
  })

  const extraPrice = selectedCustomizations.value.reduce((total, index) => {
    const option = productItem.value.customOptions[index]
    return total + (option.price || 0)
  }, 0)

  const cartItem = {
    menuItemId: productItem.value.id || productItem.value._id,
    itemName: productItem.value.itemName,
    unitPrice: productItem.value.price + extraPrice,
    quantity: quantity.value,
    customization: customizationText,
    itemSubTotal: (productItem.value.price + extraPrice) * quantity.value
  }

  emit('add-to-cart', cartItem)
  alert('已加入購物車')
  closeModal()
}

const addToCustom = () => {
  showCustomComboModal.value = true
}

const closeCustomComboModal = () => {
  showCustomComboModal.value = false
  selectedCombo.value = null
}

const selectCombo = async (combo) => {
  selectedCombo.value = combo

  // 獲取已選的客製化選項
  const customizationText = selectedCustomizations.value.map(index => {
    const option = productItem.value.customOptions[index]
    return option.name + (option.price > 0 ? ` (+$${option.price})` : option.price < 0 ? ` (-$${Math.abs(option.price)})` : '')
  })

  try {
    await store.dispatch('user/addCustomComboItem', {
      comboId: combo.comboId,
      storeId: productItem.value.storeId,
      itemId: productItem.value.id,
      customizations: customizationText, // 添加客製化選項
      quantity: quantity.value // 添加數量
    })
    alert('商品已加入自訂組合！')
    closeCustomComboModal()
  } catch (err) {
    console.error(err)
    alert('加入失敗：' + err.message)
  }
}

const getStoreName = (storeId) => {
  const stores = store.getters['shops/allShops']
  const storeObj = stores.find(s => s.id === storeId)
  return storeObj ? storeObj.name : '未知店家'
}

const addToFavorite = () => {
  emit('toggle-favorite')
}
</script>

<style scoped>
/* 遮罩層 - 半透明背景 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

/* 彈窗容器 */
.modal-container {
  background-color: white;
  border-radius: 15px;
  padding: 30px;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  color: black;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

/* 關閉按鈕 */
.btn-close {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 35px;
  height: 35px;
  border: none;
  background-color: #0069D9;
  color: white;
  font-weight: bold;
  font-size: 24px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s;
  z-index: 10;
}

.btn-close:hover {
  background-color: #ff6666;
}

.product-top {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-top: 30px;
}

.product-content {
  width: 100%;
}

.product-image {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border: 2px solid #e0e0e0;
}

.image-placeholder {
  text-align: center;
  color: #aea5a5;
}

.product-info {
  display: flex; 
  flex-direction: column;
  justify-content: flex-start;
  align-self: flex-start;
  
}

.product-name {
  font-size: 35px;
  font-weight: 800;
  margin-bottom: 1px;
  align-items: flex-start;
}

.product-price {
  font-size: 18px;
  color: #0069D9;
  font-weight: 600;
}

.product-description {
  margin-top: 10px;
}

.product-description p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-left: auto;
}

.btn-action {
  width: 140px;
  height: 50px;
  background-color: #0069D9;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 25px;
  color: white;
  font-weight: bold;
  font-size: 16px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;
  white-space: nowrap;
  transition: background-color 0.3s;
  margin-bottom: 15px;
}

.btn-action:hover {
  background-color: #0056b3;
}

.customization {
  margin-bottom: 20px;
  text-align: left;
}

.customization h3 {
  margin-bottom: 15px;
  font-size: 20px;
  font-weight: 600;
}

.checkbox-group {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  
}

.checkbox-item input {
  margin-right: 10px;
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.checkbox-item span {
  font-size: 16px;
}

.quantity-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 30px;
  margin-bottom: 20px;
}

.quantity-left h3 {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
}

.quantity-right {
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: flex-end;
}

.btn-quantity {
  width: 40px;
  height: 40px;
  background: #0069D9;
  border: 1px solid #ccc;
  border-radius: 50%;
  cursor: pointer;
  font-size: 20px;
  color: white;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: background-color 0.3s;
}

.btn-quantity:hover {
  background-color: #0056b3;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 15px;
}

.quantity-control input {
  width: 80px;
  padding: 10px;
  text-align: center;
  font-size: 16px;
  background: white;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.quantity-control input::-webkit-inner-spin-button,
.quantity-control input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.quantity-control input[type=number] {
  -moz-appearance: textfield;
  appearance: textfield;
}

.subtotal {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  color: #0069D9;
}

.btn-add-to-cart {
  padding: 12px 30px;
  background-color: #0069D9;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  transition: background-color 0.3s;
}

.btn-add-to-cart:hover {
  background-color: #0056b3;
}

/* 動畫效果 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9);
}

/* 自訂組合 Modal 樣式 */
.custom-combo-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.custom-combo-modal-content {
  background-color: white;
  border-radius: 15px;
  padding: 25px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.custom-combo-modal-content h3 {
  color: #0069D9;
  margin-bottom: 10px;
  text-align: center;
  font-size: 24px;
}

.combo-subtitle {
  color: #666;
  text-align: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.combo-list {
  margin-bottom: 20px;
}

.combo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.combo-item:hover {
  background-color: #f8f9fa;
}

.combo-info {
  flex: 1;
}

.combo-info h4 {
  margin: 0 0 5px 0;
  color: #0069D9;
  font-size: 18px;
}

.combo-store {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 14px;
}

.combo-empty {
  margin: 0 0 5px 0;
  color: #28a745;
  font-size: 14px;
  font-style: italic;
}

.combo-items-count {
  margin: 0;
  color: #999;
  font-size: 12px;
}

.combo-select-btn {
  width: 30px;
  height: 30px;
  background-color: #28a745;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.no-combos {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.no-combos-hint {
  font-size: 14px;
  margin-top: 10px;
  line-height: 1.5;
}

.combo-modal-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.cancel-btn {
  padding: 10px 20px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn:hover {
  background-color: #5a6268;
}
</style>
