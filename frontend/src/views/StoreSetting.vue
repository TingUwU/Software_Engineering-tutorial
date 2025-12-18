<template>
  <div class="page">
    <!-- 遮罩層 -->
    <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

    <!-- 左側側邊欄 -->
    <div :class="['sidebar', { open: sidebarOpen }]">
        <div class="sidebar-user" v-if="customer">
            <img class="sidebar-avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user">
            <span class="username">{{ customer.nickname }}, 老闆好</span>
        </div>
        <ul>
            <li @click="openUserModal">使用者資訊</li>
            <router-link to="/store-management"><li>主頁面</li></router-link>
            
        </ul>
        <div class="sidebar-logout">
            <button @click="logout">登出</button>
        </div>
    </div>

    <!-- 左上角顧客頭像 -->
    <img v-if="customer" class="avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user" @click="toggleSidebar">

    <h1>StoreSetting</h1>

    <form class="form" @submit.prevent="onSubmit">
      <section>
        <h2>基本資訊</h2>
        <div class="row">
          <label>店家名稱*</label>
          <input v-model="vm.store.name" type="text" required />
        </div>
        <div class="row">
          <label>店家種類*</label>
          <select v-model="vm.store.category" required>
            <option disabled value="">請選擇</option>
            <option value="中式">中式</option>
            <option value="西式">西式</option>
          </select>
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

    <!-- 使用者資訊 Modal -->
    <div v-if="userModalOpen" class="modal-overlay" @click.self="closeUserModal">
        <div class="user-modal">
            <h3>使用者資訊</h3>
            <form @submit.prevent="updateUser">
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
                    <button type="submit" @click="updateUserInfo">儲存</button>
                    <button type="button" @click="closeUserModal">關閉</button>
                </div>
            </form>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import useStoreSettingViewModel from '../viewmodels/storeSettingViewModel.js';

const store = useStore();
const router = useRouter();
const vm = useStoreSettingViewModel();
const WEEKDAYS = ['星期一','星期二','星期三','星期四','星期五','星期六','星期日'];

// 页面加载时设置 ownerId
onMounted(() => {
  const userId = store.getters['user/customer']?.id;
  if (userId) {
    vm.store.ownerId = userId;
  } else {
    alert('請先登入');
    router.push('/login');
  }
});

// Sidebar 和使用者相關狀態
const sidebarOpen = ref(false);
const userModalOpen = ref(false);
const editCustomer = ref({
  photo: ""
});

// 計算屬性
const customer = computed(() => store.getters['user/customer']);

// 方法
function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value;
}

function openUserModal() {
  if (customer.value) {
    editCustomer.value = { ...customer.value };
    userModalOpen.value = true;
  }
}

function closeUserModal() {
  userModalOpen.value = false;
}

async function updateUserInfo() {
  try {
    const userId = editCustomer.value.id;
    const updates = { ...editCustomer.value };
    delete updates.id;

    console.log('Sending updates:', userId, updates);

    const result = await store.dispatch('user/updateUser', { userId, updates });

    console.log('Update result:', result);
    alert('使用者資訊已更新！');
    closeUserModal();
  } catch (err) {
    console.error(err);
    alert('更新失敗，請稍後再試: ' + err.message);
  }
}

function onAvatarChange(event) {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = e => {
      editCustomer.value.photo = e.target.result;
    };
    reader.readAsDataURL(file);
  }
}

function logout() {
  store.dispatch('user/logout');
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  router.push('/login');
}

async function onSubmit() {
  const ok = await vm.submitForm();
  if (ok) {
    alert('店家建立成功！');
    router.push('/store-management');
  } else {
    alert('建立失敗，請檢查表單錯誤');
  }
}
</script>

<style scoped>
.page { 
  max-width: 860px; 
  margin: 0 auto; 
  text-align: left; 
  padding: 20px;
  position: relative;
}
h1 { margin: 60px 0 24px; text-align: center; }
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

/* 遮罩層 */
.overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.5);
    z-index: 50;
}

/* 左上角顧客頭像 */
.avatar {
    position: fixed;
    top: 20px;
    left: 20px;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    cursor: pointer;
    z-index: 101;
}

.preview-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    object-fit: cover;
    display: block;
    margin-bottom: 8px;
}

/* 側邊欄 */
.sidebar {
    position: fixed;
    top: 0;
    left: 0;
    width: 250px;
    height: 100%;
    background-color: #002244;
    padding: 20px;
    box-sizing: border-box;
    z-index: 100;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.sidebar.open {
    transform: translateX(0);
}

/* sidebar 使用者資訊 */
.sidebar-user {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
}

.sidebar-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
}

.username {
    color: #fff;
    font-size: 16px;
    font-weight: bold;
}

/* sidebar 選項 */
.sidebar ul {
    list-style: none;
    padding: 0;
    margin: 0;
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.sidebar li {
    color: #fff;
    cursor: pointer;
    font-size: 16px;
    width: 100%;
    padding: 10px 0;
    text-align: left;
    border-radius: 4px;
}

.sidebar li:hover {
    background-color: #001633;
}

.sidebar-logout {
    margin-top: auto;
    width: 100%;
}

.sidebar-logout button {
    width: 100%;
    padding: 10px 0;
    background-color: #fff;
    color: black;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
}

.sidebar-logout button:hover {
    background-color: #0069D9;
    color: white;
}

/* Modal */
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
</style>