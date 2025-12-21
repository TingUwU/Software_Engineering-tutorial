<template>
  <div class="order-page">
    <!-- 左側側邊欄 -->
    <div :class="['sidebar', { open: sidebarOpen }]">
      <div class="sidebar-user">
        <img class="sidebar-avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user">
        <span class="username">{{ customer.nickname || '訪客' }},肚子餓了嗎</span>
      </div>
      <ul>
                <router-link to="/home"><li>首頁</li></router-link>
                <li @click="openUserModal">使用者資訊</li>
                <router-link to="/cart"><li>購物車</li></router-link>
                <router-link to="/order"><li>訂單管理</li></router-link>
                <router-link to="/favorite"><li>收藏</li></router-link>
            </ul>
            <div class="sidebar-logout">
                <button @click="logout">登出</button>
            </div>
    </div>
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
                        <button type="submit" @click="updateUser">儲存</button>
                        <button type="button" @click="closeUserModal">關閉</button>
                    </div>
                </form>
            </div>
        </div>
    <!-- 左上角顧客頭像 -->
    <img class="avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user" @click="toggleSidebar">

    <!-- 訂單列表 -->
    <div class="order-management">
      <div class="header-section">
        <h2>訂單管理</h2>
        <button @click="fetchOrders" class="refresh-btn">刷新訂單</button>
      </div>
      
      <!-- 沒有訂單 -->
      <div v-if="!orders || orders.length === 0" class="empty">
        <p>目前沒有訂單</p>
        <router-link to="/home" class="back-btn">回首頁點餐</router-link>
      </div>
      
      <!-- 有訂單 -->
      <div v-else class="order-cards">
        <div class="order-card" v-for="order in orders" :key="order.id">
          <div class="order-header">
            <div class="order-info">
              <p><strong>訂單編號:</strong> {{ order.id }}</p>
              <p><strong>建立時間:</strong> {{ formatDate(order.createAt) }}</p>
            </div>
            <div class="order-status">
              <span :class="['status-badge', getStatusClass(order.state)]">
                {{ order.state }}
              </span>
            </div>
          </div>

          <div class="order-items">
            <h4>餐點明細:</h4>
            <ul>
              <li v-for="(item, idx) in order.items" :key="idx">
                <span class="item-name">{{ item.itemName }}</span>
                <span class="item-quantity">x {{ item.quantity }}</span>
                <span class="item-price">$ {{ item.itemSubTotal }}</span>
                <div v-if="item.customization && item.customization.length" class="item-custom">
                  客製化：{{ item.customization.join('、') }}
                </div>
              </li>
            </ul>
          </div>

          <p><strong>用餐方式:</strong> {{ order.orderType }}</p>
          <p v-if="order.orderType === '內用' && order.dineInDetail">
            <strong>桌號:</strong> {{ order.dineInDetail.tableNumber }}
          </p>
          <p v-if="order.orderType === '外帶' && order.takeoutDetail">
            <strong>取餐時間:</strong> {{ formatTime(order.takeoutDetail.takeoutTime) }}
          </p>
          <p v-if="order.remarks"><strong>備註:</strong> {{ order.remarks }}</p>
          <p><strong>支付方式:</strong> {{ order.paymentMethod }}</p>
          <p class="total-amount"><strong>總金額:</strong> $ {{ order.totalAmount }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  data() {
    return {
      sidebarOpen: false,
      userModalOpen: false,
      orders: [],
       editCustomer: {}
    }
  },
  computed: {
    ...mapGetters('user', ['customer'])
  },
  async created() {
    if (this.customer && this.customer.id) {
      await this.fetchOrders()
    }
  },
  methods: {
    toggleSidebar() { this.sidebarOpen = !this.sidebarOpen },

    closeUserModal() { this.userModalOpen = false },

    async fetchOrders() {
      try {
        if (!this.customer || !this.customer.id) {
          console.warn('未找到顧客ID')
          return
        }
        const data = await this.$store.dispatch('order/fetchCustomerOrders', this.customer.id)
        this.orders = data || []
      } catch (err) {
        console.error(err)
        alert('取得訂單失敗: ' + err.message)
      }
    },

    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },

    formatTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },

    getStatusClass(state) {
      const statusMap = {
        '已送出': 'status-new',
        '已接單': 'status-accepted',
        '準備中': 'status-preparing',
        '已完成': 'status-complete',
        '顧客已取餐': 'status-picked'
      }
      return statusMap[state] || ''
    },
    openUserModal() {
  this.editCustomer = { ...this.customer } // 深拷貝
  this.userModalOpen = true
},
  async updateUser() {
                try {
                    // 驗證電子郵件格式
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (this.editCustomer.email && !emailRegex.test(this.editCustomer.email)) {
                        alert('請輸入有效的電子郵件地址');
                        return;
                    }

                    const userId = this.editCustomer.id;
                    const updates = { ...this.editCustomer };
                    delete updates.id;

                    console.log('Sending updates:', userId, updates);

                    const result = await this.$store.dispatch('user/updateUser', { userId, updates });

                    console.log('Update result:', result);
                    alert('使用者資訊已更新！');
                    this.closeUserModal();
                } catch (err) {
                    console.error(err);
                    alert('更新失敗，請稍後再試: ' + err.message);
                }
            },
    logout() {
                this.$store.dispatch('user/logout'); // 呼叫 Vuex logout
                localStorage.removeItem('token');    // 如果有 token
                localStorage.removeItem('user');
                this.$router.push('/login');         // 導向登入頁
            },
    onAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return

  this.editCustomer.photo = URL.createObjectURL(file)
}

  }
}
</script>
<style scoped>
.order-page {
    font-family: "Microsoft JhengHei","PingFang TC","Noto Sans TC",sans-serif;
    position: relative;
    background: #f5f5f5;
    padding: 20px;
}
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
.sidebar.open { transform: translateX(0); }
.sidebar-user { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.sidebar-avatar { width: 50px; height: 50px; border-radius: 50%; }
.username { color: #fff; font-weight: bold; }
.sidebar ul { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 15px; width: 100%; }
.sidebar li { color: #fff; cursor: pointer; font-size: 16px; padding: 10px 0; border-radius: 4px; text-align: left;}
.sidebar li:hover { background-color: #001633; }
.sidebar-logout { margin-top: auto; width: 100%; }
.sidebar-logout button { width: 100%; padding: 10px 0; background-color: #fff; color: black; border: none; border-radius: 6px; cursor: pointer; }
.sidebar-logout button:hover { background-color: #0069D9; color: #fff; }
.avatar { position: fixed; top: 20px; left: 20px; width: 50px; height: 50px; border-radius: 50%; cursor: pointer; z-index: 101; }

.order-management { 
  margin-top: 80px; 
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header-section h2 {
  color: #002244;
  font-size: 28px;
  margin: 0;
}

.refresh-btn {
  padding: 8px 16px;
  background: #0069D9;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.refresh-btn:hover {
  background: #0056b3;
}

.order-cards { display: flex; flex-direction: column; gap: 20px; }
.order-card {
    background: #fff;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    transition: all 0.3s;
}
.order-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.order-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: flex-start;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 15px; 
}

.order-info p {
  margin: 5px 0;
  color: #333;
}

.order-status {
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 14px;
}

.status-new {
  background: #fff3cd;
  color: #856404;
}

.status-accepted {
  background: #d1ecf1;
  color: #0c5460;
}

.status-preparing {
  background: #d4edda;
  color: #155724;
}

.status-complete {
  background: #d1ecf1;
  color: #0c5460;
}

.status-picked {
  background: #d4edda;
  color: #155724;
}

.order-items h4 {
  color: #002244;
  margin-top: 0;
  margin-bottom: 12px;
}

.order-items ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.order-items li {
  padding: 8px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.item-name {
  font-weight: bold;
  color: #333;
}

.item-quantity {
  color: #666;
  margin: 0 10px;
}

.item-price {
  color: #0069D9;
  font-weight: bold;
}

.item-custom {
  width: 100%;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.total-amount {
  font-size: 18px;
  color: #0069D9;
  font-weight: bold;
  margin-top: 15px !important;
  padding-top: 10px;
  border-top: 2px solid #f0f0f0;
}

.order-card button { margin-top: 10px; padding: 6px 12px; background-color: #0069D9; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.order-card button:hover { opacity: 0.9; }


.overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); z-index: 50; }

.cart-btn { position: fixed; right: 20px; bottom: 20px; width: 56px; height: 56px; background-color: #0069D9; border-radius: 50%; display: flex; justify-content: center; align-items: center; z-index: 150; cursor: pointer; box-shadow: 0 4px 12px rgba(0,0,0,0.2); color: white; font-size: 28px; }
.sidebar-logout {
        margin-top: auto; /* 推到底部 */
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

.preview-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  margin-bottom: 8px;
}
</style>
