<template>
  <div class="order-page">
    <!-- 左側側邊欄 -->
    <div :class="['sidebar', { open: sidebarOpen }]">
      <div class="sidebar-user">
        <img class="sidebar-avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user">
        <span class="username">{{ customer.nickname || '訪客' }}</span>
      </div>
      <ul>
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
      <h2>訂單管理</h2>

      <div class="order-cards">
        <div class="order-card" v-for="order in orders" :key="order.id">
          <div class="order-header">
            <p><strong>訂單編號:</strong> {{ order.id }}</p>
            <p><strong>日期:</strong> {{ order.date }}</p>
            <p><strong>狀態:</strong> <span :class="'status-'+order.status">{{ order.status }}</span></p>
          </div>

          <div class="order-items">
            <h4>餐點明細:</h4>
            <ul>
              <li v-for="item in order.items" :key="item.id">
                {{ item.name }} x {{ item.quantity }} - {{ item.price * item.quantity }} 元
              </li>
            </ul>
          </div>

          <p><strong>總金額:</strong> {{ order.total }} 元</p>

          <p>
            <strong>用餐方式:</strong>
            {{ order.takeIn ? '內用' : '外帶' }}
            <span v-if="order.takeIn"> - 桌號: {{ order.tableNumber }}</span>
            <span v-else> - 取餐時間: {{ order.pickupTime }}</span>
          </p>
          <p><strong>支付方式:</strong> {{ order.payment }}</p>

          <div class="order-actions">
            <button @click="viewOrder(order)">查看詳情</button>
            <button @click="deleteOrder(order.id)">刪除</button>
          </div>
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
        const data = await this.$store.dispatch('order/fetchOrders')
        this.orders = data
      } catch (err) {
        console.error(err)
        alert('取得訂單失敗: ' + err.message)
      }
    },

    viewOrder(order) {
      alert(`查看訂單 ${order.id} (模擬功能)`)
    },

    async deleteOrder(orderId) {
      if (!confirm('確定要刪除這筆訂單嗎？')) return
      try {
        await this.$store.dispatch('order/deleteOrder', orderId)
        alert('刪除成功')
        await this.fetchOrders() // 更新列表
      } catch (err) {
        console.error(err)
        alert('刪除失敗: ' + err.message)
      }
    },
    openUserModal() {
  this.editCustomer = { ...this.customer } // 深拷貝
  this.userModalOpen = true
},
  async updateUser() {
                try {
                    const userId = this.editCustomer.id;
                    const updates = { ...this.editCustomer };
                    delete updates.id;

                    console.log('Sending updates:', userId, updates); // ✅

                    const result = await this.$store.dispatch('user/updateUser', { userId, updates });

                    console.log('Update result:', result); // ✅
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

.order-management { margin-top: 80px; }
.order-cards { display: flex; flex-direction: column; gap: 20px; }
.order-card {
    background: #fff;
    padding: 15px 20px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.order-header { display: flex; justify-content: space-between; flex-wrap: wrap; margin-bottom: 10px; }
.order-items ul {
    list-style: none; /* 取消原本的點 */
    padding: 0;
    margin: 5px 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.order-items li {
    display: flex;
    justify-content: center; /* 水平置中 */
    align-items: center;     /* 垂直置中 */
    position: relative;
    padding-left: 12px; /* 點符號空間 */
}

/* 自訂點符號 */
.order-items li::before {
    content: "•";           /* 使用黑色圓點 */
    color: #0069D9;         /* 可以自訂顏色 */
    font-weight: bold;
    display: inline-block;
    width: 12px;             /* 和 padding-left 配合置中 */
    text-align: center;
    margin-right: 6px;       /* 點與文字間距 */
}
.order-card button { margin-top: 10px; padding: 6px 12px; background-color: #0069D9; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.order-card button:hover { opacity: 0.9; }

.status-已完成 { color: green; font-weight: bold; }
.status-處理中 { color: orange; font-weight: bold; }
.status-已取消 { color: red; font-weight: bold; }

.modal-overlay {
    position: fixed;
    top: 0; left: 0;
    width: 100%; height: 100%;
    background-color: rgba(0,0,0,0.5);
    display: flex; justify-content: center; align-items: center;
    z-index: 200;
}
.user-modal { background-color: #fff; padding: 20px 30px; border-radius: 12px; width: 300px; max-width: 90%; text-align: left; box-shadow: 0 5px 15px rgba(0,0,0,0.3); }
.user-modal h3 { color: #0069D9; margin-bottom: 15px; }
.form-group { margin-bottom: 10px; display: flex; flex-direction: column; }
.form-group label { margin-bottom: 4px; font-weight: bold; }
.form-group input { padding: 6px 8px; border-radius: 6px; border: 1px solid #ccc; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 15px; }
.modal-actions button { padding: 6px 12px; border-radius: 6px; border: none; cursor: pointer; }
.modal-actions button:first-child { background-color: #0069D9; color: #fff; }
.modal-actions button:last-child { background-color: #ccc; color: #333; }

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
</style>
