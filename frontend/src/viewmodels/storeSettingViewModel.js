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
    if (!store.address?.trim()) errs.push('店家地址為必填');
    const [L, A] = store.coordinates || [];
    if (!Number.isFinite(L) || !Number.isFinite(A)) errs.push('經緯度需為數值');
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

    // TODO: 呼叫後端 API 建立或更新店家設定
    // 範例：
    // const res = await fetch('/api/stores', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({
    //     ...store,
    //     updatedAt: new Date().toISOString(),
    //   }),
    // });
    // const data = await res.json();

    // 模擬：先以 console 代替
    console.log('[TODO] 送往後端的資料:', {
      ...store,
      updatedAt: store.updatedAt.toISOString(),
    });

    return true;
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