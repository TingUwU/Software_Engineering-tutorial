<template>
  <!-- 遮罩層 -->
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        
        <!-- 關閉按鈕 -->
        <button class="btn-close" @click="closeModal">✕</button>

        <div class="product-top">
          <div class="product-image"><!--圖片區-->
            <img v-if="productItem.imgUrl" :src="productItem.imgUrl" class="product-image-display" alt="餐點圖片">
            <div v-else class="image-placeholder">餐點圖片</div>
          </div>
          <div class="product-info"><!--資訊區-->
            <h2 class="product-name">{{ productItem.itemName }}</h2>
            <p class="product-price">餐點金額：${{ productItem.price }}</p>
            <div v-if="productItem.description" class="product-description">
              <p>{{ productItem.description }}</p>
            </div>
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
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

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
  }
})

// Emits
const emit = defineEmits(['close', 'add-to-cart'])

// 使用 props 中的 product
const productItem = computed(() => props.product)

const quantity = ref(1)
const selectedCustomizations = ref([])

// 當彈窗關閉時重置數據
watch(() => props.show, (newVal) => {
  if (!newVal) {
    quantity.value = 1
    selectedCustomizations.value = []
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
    itemSubTotal: (productItem.value.price + extraPrice) * quantity.value,
    imgUrl: productItem.value.imgUrl
  }

  emit('add-to-cart', cartItem)
  // 移除 alert，交由父組件處理
  closeModal()
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

.product-image-display {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
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
</style>
