<template>
  <div class="order-page">
    <!-- 遮罩層 -->
    <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

    <!-- 左側側邊欄（訪客） -->
    <div :class="['sidebar', { open: sidebarOpen }]">
      <div class="sidebar-user">
        <img class="sidebar-avatar" :src="require('@/assets/logo.png')" alt="guest">
        <span class="username">訪客</span>
      </div>

      <ul>
        <router-link to="/nologincart"><li>購物車</li></router-link>
        <li class="active">訂單管理</li>
      </ul>

      <div class="sidebar-login">
        <button @click="goLogin">登入</button>
      </div>
    </div>

    <!-- 左上角訪客頭像 -->
    <img
      class="avatar"
      :src="require('@/assets/logo.png')"
      alt="guest"
      @click="toggleSidebar"
    >

    <!-- 訂單管理 -->
    <div class="order-management">
      <h2>訂單管理（訪客）</h2>

      <!-- 沒有訂單 -->
      <div v-if="!order" class="empty">
        <p>目前沒有進行中的訂單</p>
        <router-link to="/nologinhome" class="back-btn">回首頁點餐</router-link>
      </div>

      <!-- 有訂單 -->
      <div v-else class="order-cards">
        <div class="order-card">
          <div class="order-header">
            <p><strong>訂單編號：</strong>{{ order.id }}</p>
            <p><strong>建立時間：</strong>{{ formatDate(order.createAt) }}</p>
            <p>
              <strong>狀態：</strong>
              <span class="status-processing">{{ order.state || '處理中' }}</span>
            </p>
          </div>

          <div class="order-items">
            <h4>餐點明細</h4>
            <ul>
              <li v-for="(item, idx) in order.items" :key="idx">
                {{ item.itemName || item.name }} x {{ item.quantity }}
                - {{ item.unitPrice || item.price }} 元
              </li>
            </ul>
          </div>

          <p><strong>總金額：</strong>{{ order.totalAmount }} 元</p>

          <p>
            <strong>用餐方式：</strong>
            {{ order.orderType }}
            <span v-if="order.orderType === '內用' && order.dineInDetail">
              - 桌號 {{ order.dineInDetail.tableNumber }}
            </span>
            <span v-if="order.orderType === '外帶' && order.takeoutDetail">
              - 取餐時間 {{ formatTime(order.takeoutDetail.takeoutTime) }}
            </span>
          </p>

          <p v-if="order.remarks"><strong>備註：</strong>{{ order.remarks }}</p>
          <p><strong>支付方式：</strong>{{ order.paymentMethod }}</p>

          <div class="order-actions">
            <button @click="clearOrder">清除訂單</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GuestOrder',
  data() {
    return {
      sidebarOpen: false,
      order: null
    }
  },
  created() {
    const savedOrder = sessionStorage.getItem('guestOrder')
    if (savedOrder) {
      this.order = JSON.parse(savedOrder)
    }
  },
  methods: {
    toggleSidebar() {
      this.sidebarOpen = !this.sidebarOpen
    },
    goLogin() {
      this.sidebarOpen = false
      this.$router.push('/login')
    },
    async clearOrder() {
      if (!confirm('確定要取消這筆訂單嗎？\n\n取消後商家將會收到通知，訂單將被標記為「已取消」。')) return

      try {
        // 調用後端API取消訂單
        await this.$store.dispatch('order/updateOrder', {
          orderId: this.order.id,
          updates: { state: '已取消' }
        })

        // 清空本地存儲
        sessionStorage.removeItem('guestOrder')
        this.order = null
        alert('訂單已取消，商家將會收到通知')
      } catch (err) {
        console.error('取消訂單失敗:', err)
        alert('取消訂單失敗，請稍後再試: ' + err.message)
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    formatTime(timeString) {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleTimeString('zh-TW', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.order-page {
  font-family: "Microsoft JhengHei","PingFang TC","Noto Sans TC",sans-serif;
  background: #f5f5f5;
  padding: 20px;
}

/* Sidebar */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 250px;
  height: 100%;
  background-color: #002244;
  padding: 20px;
  transform: translateX(-100%);
  transition: transform 0.3s;
  z-index: 100;
  display: flex;
  flex-direction: column;
}
.sidebar.open {
  transform: translateX(0);
}
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
  font-weight: bold;
}
.sidebar ul {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.sidebar li {
  color: #fff;
  padding: 10px;
  cursor: pointer;
  border-radius: 6px;
  text-align: left;
}
.sidebar li.active {
  background-color: #001633;
}
.sidebar-login {
  margin-top: auto; /* 推到最下面 */
  width: 100%;            /* 確保整個區塊滿寬 */
  padding: 0 0;           /* 避免多餘 padding */
}

.sidebar-login button {
  width: 100%;           /* 滿寬 */
  padding: 20px 0;       /* 高度增加，點擊範圍更大 */
  font-size: 20px;       /* 文字更大 */
  background: #0069D9;   /* 主色 */
  color: #fff;
  border: none;
  border-radius: 16px;   /* 圓角更大 */
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
  text-align: center;    /* 文字置中 */
}

.sidebar-login button:hover {
  background: #0056b3;   /* hover 顏色 */
}

/* Avatar */
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

/* Order */
.order-management {
  margin-top: 80px;
}
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.order-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.order-header {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
.order-items ul {
  list-style: none;
  padding: 0;
}
.order-items li {
  margin: 4px 0;
}
.order-actions {
  margin-top: 15px;
}
.order-actions button {
  padding: 8px 14px;
  background: #d9534f;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

/* 狀態 */
.status-processing {
  color: orange;
  font-weight: bold;
}

/* 空狀態 */
.empty {
  text-align: center;
  margin-top: 80px;
}
.back-btn {
  display: inline-block;
  margin-top: 15px;
  padding: 10px 18px;
  background: #0069D9;
  color: #fff;
  border-radius: 8px;
  text-decoration: none;
}

/* Overlay */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 50;
}
</style>
