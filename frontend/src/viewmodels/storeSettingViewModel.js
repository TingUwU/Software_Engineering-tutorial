import { reactive, ref, watch } from 'vue';
import { createEmptyStore, defaultBusinessHour } from '../models/store.model.js';

export default function useStoreSettingViewModel() {
  const store = reactive(createEmptyStore());

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

  async function submitForm() {
    validationErrors.value = validate();
    if (validationErrors.value.length) return false;

    store.updatedAt = new Date();

    try {
      // 呼叫後端 API 建立店家
      const res = await fetch('http://localhost:3000/api/stores', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${store.ownerId}`
        },
        body: JSON.stringify({
          ...store,
          updatedAt: store.updatedAt.toISOString(),
        }),
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || '建立店家失敗');
      }

      const data = await res.json();
      console.log('店家建立成功:', data);
      
      return true;
    } catch (err) {
      console.error('建立店家失敗:', err);
      validationErrors.value.push(err.message);
      return false;
    }
  }

  // TODO: 若為編輯頁，可於此加入載入既有店家資料的動作（例如根據路由參數取得 _id）
  // 例：
  // async function loadStore(id) {
  //   const res = await fetch(`/api/stores/${id}`);
  //   const data = await res.json();
  //   // 將後端資料設回 store
  // }

  return {
    store,
    lng,
    lat,
    validationErrors,
    addBusinessHour,
    removeBusinessHour,
    resetForm,
    submitForm,
    // loadStore, // 需要時再暴露
  };
}