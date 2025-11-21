<template>
  <div class="page">
    <h1>StoreSetting</h1>

    <form class="form" @submit.prevent="onSubmit">
      <section>
        <h2>基本資訊</h2>
        <div class="row">
          <label>店家名稱*</label>
          <input v-model="vm.store.name" type="text" required />
        </div>
        <div class="row">
          <label>店家簡介</label>
          <textarea v-model="vm.store.description" rows="3" />
        </div>
        <div class="row">
          <label>聯絡電話</label>
          <input v-model="vm.store.phone" type="tel" />
        </div>
        <div class="row">
          <label>聯絡信箱</label>
          <input v-model="vm.store.email" type="email" />
        </div>
      </section>

      <section>
        <h2>地址與座標</h2>
        <div class="row">
          <label>店家地址*</label>
          <input v-model="vm.store.address" type="text" required />
        </div>
        <div class="row two">
          <div>
            <label>經度*</label>
            <input v-model.number="vm.lng" type="number" step="0.000001" required />
          </div>
          <div>
            <label>緯度*</label>
            <input v-model.number="vm.lat" type="number" step="0.000001" required />
          </div>
        </div>
      </section>

      <section>
        <h2>營業時間</h2>
        <div v-for="(bh, i) in vm.store.businessHours" :key="i" class="card">
          <div class="row three">
            <div>
              <label>星期</label>
              <select v-model="bh.day">
                <option disabled value="">請選擇</option>
                <option v-for="d in WEEKDAYS" :key="d" :value="d">{{ d }}</option>
              </select>
            </div>
            <div>
              <label>開始時間</label>
              <input v-model="bh.start" type="time" min="00:00" max="23:59" step="60" />
            </div>
            <div>
              <label>結束時間</label>
              <input v-model="bh.end" type="time" min="00:00" max="23:59" step="60" />
            </div>
          </div>
          <div class="row note-row">
            <div>
              <label>備註/公休</label>
              <input v-model="bh.note" placeholder="例：中午休息 12:00-13:00 / 週日公休" />
            </div>
            <div class="actions-right">
              <button type="button" class="danger" @click="vm.removeBusinessHour(i)">刪除此時段</button>
            </div>
          </div>
        </div>
        <button type="button" @click="vm.addBusinessHour()">新增營業時間</button>
      </section>

      <section>
        <h2>狀態</h2>
        <div class="row inline">
          <label>是否啟用</label>
          <input v-model="vm.store.isActive" type="checkbox" />
        </div>
      </section>

      <section v-if="vm.validationErrors.length" class="errors">
        <strong>請修正以下錯誤:</strong>
        <ul>
          <li v-for="(e, i) in vm.validationErrors" :key="i">{{ e }}</li>
        </ul>
      </section>

      <div class="actions">
        <button type="submit">送出</button>
        <button type="button" class="secondary" @click="vm.resetForm()">重設</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import useStoreSettingViewModel from '../viewmodels/storeSettingViewModel.js';

const vm = useStoreSettingViewModel();
const WEEKDAYS = ['星期一','星期二','星期三','星期四','星期五','星期六','星期日'];

async function onSubmit() {
  const ok = await vm.submitForm();
  if (ok) {
    // TODO: 送出成功後的 UI 流程（例如導頁或提示），待與後端串接完成後實作
    alert('表單已送出（模擬）。請稍後串接後端 API。');
  }
}
</script>

<style scoped>
.page { max-width: 860px; margin: 0 auto; text-align: left; }
h1 { margin: 16px 0 24px; }
h2 { margin: 24px 0 8px; font-size: 18px; }
.form { display: grid; gap: 16px; }
.row { display: grid; gap: 8px; margin-bottom: 8px; }
.row.two { grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; }
.row.three { grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 12px; }
.row.inline { grid-template-columns: auto auto; align-items: center; gap: 12px; }
.row.note-row { grid-template-columns: 1fr auto; align-items: end; }
.card { border: 1px solid #ddd; padding: 12px; border-radius: 6px; margin-bottom: 12px; }
input[type="text"], input[type="tel"], input[type="email"], input[type="number"], textarea {
  padding: 6px 8px; border: 1px solid #ccc; border-radius: 4px; width: 100%;
}
select, input[type="time"] {
  padding: 6px 8px; border: 1px solid #ccc; border-radius: 4px; width: 100%;
  background: #fff;
}
.actions { display: flex; gap: 8px; }
.actions-right { display: flex; justify-content: flex-end; align-items: flex-end; }
button { padding: 6px 12px; cursor: pointer; }
button.secondary { background: #f2f2f2; }
button.danger { background: #ffe8e8; border: 1px solid #ffb3b3; }
.errors { background: #fff7e6; border: 1px solid #ffd591; padding: 12px; border-radius: 6px; }
</style>