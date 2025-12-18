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
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

export default {
  setup() {
    const router = useRouter();
    const store = useStore();

    const goHome = () => {
      const isLoggedIn = store.state.user.isLoggedIn || !!localStorage.getItem('user');
      const user = JSON.parse(localStorage.getItem('user'));
      if (isLoggedIn&&user.role==='buyer') {
        router.push('/home');
      } else if (isLoggedIn&&user.role==='owner') {
        router.push('/store-management');
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
