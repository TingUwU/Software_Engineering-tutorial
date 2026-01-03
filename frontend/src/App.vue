<template>
  <div id="app">
    <nav>
      <img class="avatar"
           :src="require('@/assets/logo.png')"
           alt="logo"
           @click="goHome">
      <span class="logo-text" @click="goHome">早一點</span>
    </nav>

    <!-- HomeView.vue 或其他頁面會在這裡渲染 -->
    <router-view />
  </div>
</template>

<script>
import { onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

// 後端網址
const API_DOMAIN = 'https://breakfast-team5.onrender.com';

export default {
  setup() {
    const router = useRouter();
    const store = useStore();

    // 透過 computed 取得目前使用者狀態，這樣資料變動時會自動更新
    const customer = computed(() => store.state.user.customer);
    const isLoggedIn = computed(() => store.state.user.customer.isLoggedIn);

    // WebSocket Client 變數
    let stompClient = null;

    // 請求瀏覽器通知權限
    const requestNotificationPermission = () => {
      // 檢查瀏覽器是否支援且尚未授權
      if ('Notification' in window && Notification.permission !== 'granted') {
        Notification.requestPermission();
      }
    };

    // 顯示推播通知的函式
    const showNotification = (msg) => {
      // 如果有權限，跳出原生通知
      if ('Notification' in window && Notification.permission === 'granted') {
        new Notification('早一點 - 餐點通知', {
          body: msg
          // icon: require('@/assets/logo.png')
        });
      } else {
        // 如果使用者沒開權限，用 alert 替代
        alert(`【餐點通知】\n${msg}`);
      }
    };

    // -連線 WebSocket
    const connectWebSocket = (userId) => {
      // 避免重複連線
      if (stompClient && stompClient.connected) return;

      console.log('正在嘗試連線 WebSocket...');
      const socket = new SockJS(`${API_DOMAIN}/ws`);
      stompClient = Stomp.over(socket);

      stompClient.connect({}, (frame) => {
        console.log('WebSocket 已連線: ' + frame);

        // 訂閱該使用者的專屬頻道 (對應後端的 /topic/orders/{userId})
        stompClient.subscribe(`/topic/orders/${userId}`, (message) => {
          console.log('收到推播訊息:', message.body);
          showNotification(message.body);
        });
      }, (error) => {
        console.error('WebSocket 連線失敗:', error);
      });
    };

    // 斷線函式 (登出時用) 
    const disconnectWebSocket = () => {
      if (stompClient) {
        stompClient.disconnect();
        console.log('WebSocket 已斷線');
      }
    };

    // 監聽登入狀態改變
    // 當 isLoggedIn 變成 true (登入) -> 連線
    // 當 isLoggedIn 變成 false (登出) -> 斷線
    watch(isLoggedIn, (newVal) => {
      if (newVal && customer.value.id) {
        requestNotificationPermission();
        connectWebSocket(customer.value.id);
      } else {
        disconnectWebSocket();
      }
    });

    onMounted(async () => {
      console.log('App mounted. Checking authentication status...');
      
      try {
        await store.dispatch('user/checkAuth');
        console.log('Session check successful.');

        // 如果恢復登入成功，連線 WebSocket
        if (isLoggedIn.value && customer.value.id) {
          requestNotificationPermission();
          connectWebSocket(customer.value.id);
        }
      } catch (err) {
        console.log('No active session found (User is guest).');
      }
    });

    const goHome = () => {
      if (isLoggedIn.value) {
        if (customer.value.role === 'buyer') {
          router.push('/home');
        } else if (customer.value.role === 'owner') {
          router.push('/store-management');
        } else {
          router.push('/home');
        }
      } else {
        router.push('/nologinhome');
      }
    };

    return { goHome };
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

nav {
  padding: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

nav a, .logo-text {
  font-weight: bold;
  color: #000; /* 黑色字 */
  text-decoration: none; /* 去掉底線 */
  cursor: pointer;
}

nav a.router-link-exact-active {
  color: #0069D9; /* 藍色 */
}

nav a:hover, .logo-text:hover {
  color: #0069D9;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  vertical-align: middle;
  cursor: pointer;
}

.logo-text {
  font-size: 20px;
  vertical-align: middle;
}
</style>
