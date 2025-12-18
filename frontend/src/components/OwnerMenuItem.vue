<template>
    <!-- 遮罩層 -->
    <Transition name="modal">
      <div v-if="show" class="modal-overlay" @click.self="closeModal">
        <div class="modal-container">
          
          <!-- 關閉按鈕 -->
          <button class="btn-close" @click="closeModal">✕</button>
  
          <div class="product-top">
            <div class="product-image" @click="triggerImageUpload"><!--圖片區-->
              <img v-if="editItem.imgUrl" :src="editItem.imgUrl" class="uploaded-image" alt="餐點圖片">
              <div v-else class="image-placeholder">
                <span>點擊上傳圖片</span>
              </div>
              <input 
                type="file" 
                id="image-upload" 
                @change="onImageChange" 
                accept="image/*" 
                style="display: none;"
              />
            </div>
            <div class="product-info"><!--資訊區-->
              <div class="form-group">
                <h2 class="product-name">{{ editItem.itemName || '' }}</h2>
                <div class="form-group">
                  <input type="text" v-model="editItem.itemName" placeholder="請輸入餐點名稱" />
                </div>
                <div class="form-group">
                  <input type="number" v-model.number="editItem.price" placeholder="請輸入價格" min="0" />
                </div>
              </div>
            </div>
          </div>

          <!-- 餐點簡介區 -->
          <div class="description-section">
            <div class="form-group">
              <label>餐點簡介:</label>
              <textarea 
                v-model="editItem.description" 
                placeholder="請輸入餐點簡介" 
                rows="4"
                class="description-textarea"
              ></textarea>
            </div>
          </div>
  
          <div class="product-content">
            <div class="edit-section">
              <div class="form-group">
                <label>餐點分類:</label>
                <select v-model="editItem.tag">
                  <option value="" disabled>請選擇分類</option>
                  <option v-for="category in categories" :key="category" :value="category">
                    {{ category }}
                  </option>
                </select>
              </div>
              <!-- 自訂組合選項 -->
             <div class="form-group">
                <label>自訂組合選項</label>
                <div class="customization-list">
                  <div 
                    v-for="(option, index) in editItem.customOptions" 
                    :key="index" 
                    class="customization-item"
                  >
                    <input 
                      type="text" 
                      v-model="option.name" 
                      placeholder="選項名稱" 
                      class="option-input"
                    />
                    <input 
                      type="number" 
                      v-model.number="option.price" 
                      placeholder="價格改變" 
                      class="price-input"
                    />
                    <button 
                      class="btn-remove" 
                      @click="removeCustomOption(index)"
                    >
                      ✕
                    </button>
                  </div>
                </div>
                <button class="btn-add-option" @click="addCustomOption">
                  + 新增自訂選項
                </button>
              </div>
              
              
              
              <div class="button-group">
                <button class="btn-save" @click="saveChanges">儲存變更</button>
                <button v-if="product.id" class="btn-delete" @click="deleteItem">刪除</button>
                <button class="btn-cancel" @click="closeModal">取消</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </template>
  
<script setup>
import { ref, watch } from 'vue'

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
      itemName: '',
      price: 0,
      tag: '',
      imgUrl: '',
      description: '',
      customOptions: []
    })
  },
  categories: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['close', 'save', 'delete'])

// 編輯中的資料
const editItem = ref({
  itemName: '',
  price: 0,
  tag: '',
  imgUrl: '',
  description: '',
  customOptions: []
})

// 監聽 show 變化，重新載入資料
watch(() => props.show, (newVal) => {
  if (newVal && props.product) {
    editItem.value = {
      itemName: props.product.itemName || '',
      price: props.product.price || 0,
      tag: props.product.tag || (props.categories.length > 0 ? props.categories[0] : ''),
      imgUrl: props.product.imgUrl || '',
      description: props.product.description || '',
      customOptions: props.product.customOptions ? JSON.parse(JSON.stringify(props.product.customOptions)) : []
    }
  }
})

const closeModal = () => {
  emit('close')
}

const saveChanges = () => {
  // 驗證必填欄位
  if (!editItem.value.itemName.trim()) {
    alert('請輸入餐點名稱')
    return
  }
  if (!editItem.value.price || editItem.value.price <= 0) {
    alert('請輸入價格')
    return
  }
  if (!editItem.value.tag) {
    alert('請選擇餐點分類')
    return
  }

  const updatedItem = {
    ...props.product,
    ...editItem.value
  }
  emit('save', updatedItem)
  closeModal()
}

const deleteItem = () => {
  if (confirm('確定要刪除此餐點嗎？')) {
    emit('delete', props.product.id)
    closeModal()
  }
}

// 圖片上傳處理
const onImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (!file.type.startsWith('image/')) {
      alert('請選擇圖片檔案')
      return
    }
    
    const reader = new FileReader()
    reader.onload = (e) => {
      editItem.value.imgUrl = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const triggerImageUpload = () => {
  document.getElementById('image-upload').click()
}

// 新增自訂選項
const addCustomOption = () => {
  editItem.value.customOptions.push({
    name: '',
    price: 0
  })
}

// 移除自訂選項
const removeCustomOption = (index) => {
  editItem.value.customOptions.splice(index, 1)
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
    margin-bottom: 20px;
    padding-top: 30px;
  }
  
  .product-content {
    width: 100%;
  }
  
  .product-image {
    width: 250px;
    height: 250px;
    flex-shrink: 0;
    border-radius: 10px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border: 2px dashed #0069D9;
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 10px;
    margin-left: 40px;
  }

  .product-image:hover {
    background-color: #e8f4ff;
    border-color: #0056b3;
  }
  
  .image-placeholder {
    text-align: center;
    color: #0069D9;
    display: flex;
    flex-direction: column;
    gap: 10px;
    font-size: 14px;
  }



  .uploaded-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .product-info {
    flex: 1;
    display: flex; 
    flex-direction: column;
    justify-content: flex-start;
    align-self: flex-start;
  }

  .description-section {
    width: 100%;
    margin-bottom: 20px;
  }

  .description-textarea {
    width: 100%;
    padding: 12px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
    font-family: inherit;
    resize: vertical;
    transition: border-color 0.3s;
    min-height: 100px;
    box-sizing: border-box;
  }

  .description-textarea:focus {
    outline: none;
    border-color: #0069D9;
  }
  
  .product-name {
    font-size: 35px;
    font-weight: 800;
    margin-bottom: 1px;
    text-align: left;
    width: 50%;
  }
  
  .product-price {
    font-size: 18px;
    color: #0069D9;
    font-weight: 600;
  }
  
  .edit-section {
    padding: 20px 0;
    align-items: center;
  }

 

  .customization-editor h3 {
    margin-bottom: 15px;
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }

  .customization-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 15px;
    align-items: center;
  }

  .customization-item {
    display: flex;
    gap: 10px;
    align-items: center;
    padding: 10px;
    border:none;
    width: 50%;
  }

  .option-input {
    flex: 2;
    padding: 10px;
    border: 2px solid #e0e0e0;
    border-radius: 6px;
    font-size: 14px;
    transition: border-color 0.3s;

  }

  .option-input:focus {
    outline: none;
    border-color: #0069D9;
  }

  .price-input {
    flex: 1;
    padding: 10px;
    border: 2px solid #e0e0e0;
    border-radius: 6px;
    font-size: 14px;
    width: 50%;
  }

  .price-input:focus {
    outline: none;
    border-color: #0069D9;
  }

  .btn-remove {
    width: 20px;
    height: 20px;
    background-color: #ff6666;
    color: white;
    border: none;
    border-radius: 50%;
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.3s;
    flex-shrink: 0;
  }

  .btn-remove:hover {
    background-color: #ff4444;
  }

  .btn-add-option {
    width: 100%;
    padding: 12px;
    background-color: #0069D9;
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 16px;
    font-weight: 600;
    transition: background-color 0.3s;
  }

  .btn-add-option:hover {
    background-color: #0056b3;
  }
  
  .form-group {
    margin-bottom: 20px;
    margin-left: auto;
    margin-right: auto;
    width: 90%;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    font-size: 16px;
    color: #333;
  }
  
  .form-group input,
  .form-group select {
    width: 100%;
    padding: 12px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 16px;
    transition: border-color 0.3s;
  }
  
  .form-group input:focus,
  .form-group select:focus {
    outline: none;
    border-color: #0069D9;
  }
  
  .button-group {
    display: flex;
    flex-direction: row;
    align-items: flex-end;
    margin-top: 30px;
    gap: 15px;
    justify-content: flex-end;
  }

  /* Chrome, Edge, Safari */
input[type=number]::-webkit-inner-spin-button,
input[type=number]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Firefox */
input[type=number] {
  -moz-appearance: textfield;
  appearance: textfield;
}

  
  .btn-save,
  .btn-cancel,
  .btn-delete  {
    padding: 12px 30px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 16px;
    font-weight: 600;
    transition: background-color 0.3s;

  }
  
  .btn-save {
    background-color: #0069D9;
    color: white;
  }
  
  .btn-save:hover {
    background-color: #0056b3;
  }
  
  .btn-cancel {
    background-color: #e0e0e0;
    
  }
  
  .btn-cancel:hover {
    background-color: #d0d0d0;
  }
  .btn-delete {
    background-color:#ff6666;;
    color: white;
  }
  
  .btn-cancel:hover {
    background-color: #d0d0d0;
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
  