import { reactive, ref, watch } from 'vue';
import { createEmptyStore, defaultBusinessHour } from '../models/store.model.js';

export default function useStoreSettingViewModel() {
  const store = reactive(createEmptyStore());
  const isEditMode = ref(false);
  const storeId = ref(null);

  // 經緯度輸入（分開輸入、同步回陣列）
  const lng = ref(0);
  const lat = ref(0);
  watch([lng, lat], ([lngVal, latVal]) => {
    const L = Number.isFinite(+lngVal) ? +lngVal : 0;
    const A = Number.isFinite(+latVal) ? +latVal : 0;
    store.coordinates = [L, A];
  }, { immediate: true });

  const validationErrors = ref([]);

  function validate() {
    const errs = [];
    if (!store.name?.trim()) errs.push('店家名稱為必填');
    if (!store.category?.trim()) errs.push('店家種類為必填');
    if (!store.address?.trim()) errs.push('店家地址為必填');
    const [L, A] = store.coordinates || [];
    if (!Number.isFinite(L) || !Number.isFinite(A)) errs.push('經緯度需為數值');
    if (!store.ownerId?.trim()) errs.push('店主ID缺失（請重新登入）');
    return errs;
  }

  function addBusinessHour() {
    store.businessHours.push({ ...defaultBusinessHour });
  }
  function removeBusinessHour(index) {
    store.businessHours.splice(index, 1);
  }

  function resetForm() {
    Object.assign(store, createEmptyStore());
    lng.value = store.coordinates[0] ?? 0;
    lat.value = store.coordinates[1] ?? 0;
    validationErrors.value = [];
  }

  async function loadStore(id) {
    // 清空之前的錯誤
    validationErrors.value = [];
    try {
      const res = await fetch(`https://breakfast-team5.onrender.com/api/stores/${id}`, {
        credentials: 'include'
      });
      if (!res.ok) {
        let errorMessage = `HTTP ${res.status}: `;
        try {
          const errorText = await res.text();
          if (errorText === 'STORE_NOT_FOUND' || res.status === 404) {
            errorMessage += '找不到該店家';
          } else {
            errorMessage += errorText || '載入店家資料失敗';
          }
        } catch {
          errorMessage += '載入店家資料失敗';
        }
        throw new Error(errorMessage);
      }
      const data = await res.json();
      
      // 將後端資料設回 store
      Object.assign(store, data);
      
      // 修正 businessHours 數據格式（處理舊數據中的 close 字段）
      if (data.businessHours && Array.isArray(data.businessHours)) {
        store.businessHours = data.businessHours.map(bh => ({
          day: bh.day || '',
          start: bh.start || '',
          end: bh.end || bh.close || '',  // 兼容舊數據的 close 字段
          note: bh.note || ''
        }));
      }
      
      // 設定經緯度
      if (data.coordinates && data.coordinates.length === 2) {
        lng.value = data.coordinates[0];
        lat.value = data.coordinates[1];
      }
      
      // 設定為編輯模式
      isEditMode.value = true;
      storeId.value = id;
      
      console.log('成功載入店家資料:', data);
      return true;
    } catch (err) {
      console.error('載入店家失敗:', err);
      // 處理網絡錯誤
      if (err.message.includes('Failed to fetch') || err.name === 'TypeError') {
        validationErrors.value.push('網絡連接失敗，請檢查後端服務是否啟動');
      } else {
        validationErrors.value.push(err.message);
      }
      return false;
    }
  }

  async function submitForm() {
    validationErrors.value = validate();
    if (validationErrors.value.length) return false;

    store.updatedAt = new Date();

    try {
      const url = isEditMode.value
        ? `https://breakfast-team5.onrender.com/api/stores/${storeId.value}`
        : 'https://breakfast-team5.onrender.com/api/stores';

      const method = isEditMode.value ? 'PUT' : 'POST';

      // 準備要發送的數據，只包含應該更新的字段
      const dataToSend = {
        name: store.name,
        description: store.description,
        phone: store.phone,
        email: store.email,
        address: store.address,
        category: store.category,
        coordinates: store.coordinates,
        businessHours: store.businessHours,
        avatar: store.avatar,
        isActive: store.isActive,
        updatedAt: store.updatedAt.toISOString(),
      };

      // 如果是編輯模式，不要發送 ownerId（因為它已經在路徑中驗證）
      // 如果是創建模式，才發送 ownerId
      if (!isEditMode.value) {
        dataToSend.ownerId = store.ownerId;
      }

      // 獲取當前用戶ID用於認證
      const currentUserId = store.ownerId; // 這個值在 StoreSetting.vue 的 onMounted 中設置

      const res = await fetch(url, {
        method: method,
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${currentUserId}`
        },
        body: JSON.stringify(dataToSend),
      });

      if (!res.ok) {
        let errorMessage = `HTTP ${res.status}: `;
        try {
          const errorText = await res.text();
          // 處理後端返回的特定錯誤
          if (errorText === 'FORBIDDEN') {
            errorMessage += '沒有權限操作此店家';
          } else if (errorText === 'STORE_NOT_FOUND') {
            errorMessage += '找不到該店家';
          } else {
            errorMessage += errorText || (isEditMode.value ? '更新店家失敗' : '建立店家失敗');
          }
        } catch {
          errorMessage += isEditMode.value ? '更新店家失敗' : '建立店家失敗';
        }
        throw new Error(errorMessage);
      }

      const data = await res.json();
      console.log(isEditMode.value ? '店家更新成功:' : '店家建立成功:', data);
      
      return true;
    } catch (err) {
      console.error(isEditMode.value ? '更新店家失敗:' : '建立店家失敗:', err);
      // 處理網絡錯誤
      if (err.message.includes('Failed to fetch') || err.name === 'TypeError') {
        validationErrors.value.push('網絡連接失敗，請檢查後端服務是否啟動');
      } else {
        validationErrors.value.push(err.message);
      }
      return false;
    }
  }

  return {
    store,
    lng,
    lat,
    validationErrors,
    isEditMode,
    storeId,
    addBusinessHour,
    removeBusinessHour,
    resetForm,
    submitForm,
    loadStore,
  };
}