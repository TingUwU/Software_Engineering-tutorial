<template>
  <div class="shop">
    <!-- 遮罩層 -->
    <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

    <!-- 左側側邊欄（訪客） -->
    <div :class="['sidebar', { open: sidebarOpen }]">
      <div class="sidebar-user">
        <img class="sidebar-avatar" :src="require('@/assets/logo.png')" alt="guest">
        <span class="username">訪客，肚子餓了嗎</span>
      </div>
      <ul>
        <router-link to="/nologincart"><li>購物車</li></router-link>
        <li @click="needLogin">訂單管理</li>
      </ul>
    </div>

    <!-- 左上角訪客頭像 -->
    <img
      class="avatar"
      :src="require('@/assets/logo.png')"
      alt="guest"
      @click="toggleSidebar"
    >

    <!-- 店家名稱 -->
    <div class="shop-header">
      <h2 class="shop-name">{{ shop.name }}</h2>

      <!-- 收藏（訪客提示登入） -->
      <button class="favorite-btn" @click="needLogin">
        🤍
      </button>
    </div>

    <!-- 店家資訊 -->
    <div class="shop-info">
      <p>營業時間: {{ todayBusiness.start || '未營業' }} ~ {{ todayBusiness.close || '未營業' }}</p>
      <p>地址: {{ shop.address }}</p>
    </div>

    <!-- 搜尋 -->
    <div class="search-section">
      <input class="search-bar" placeholder="搜尋菜品…" v-model="keyword">
      <button class="search-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
            </button>
      <ul
        v-if="searchSuggestions.length"
        class="search-suggestions"
      >
        <li
          v-for="item in searchSuggestions"
          :key="item.id"
          @click="selectSuggestion(item)"
        >
          {{ item.itemName }}
        </li>
      </ul>
    </div>

    <!-- 菜品分類 -->
    <section
      v-for="category in filteredCategories"
      :key="category.name"
      class="category-section"
    >
      <h2 class="category-title">{{ category.name }}</h2>

      <div class="slider-container">
        <button class="scroll-btn left" @click="scrollLeft(category.name)">&#8249;</button>

        <div :ref="el => categoryRefs[category.name] = el" class="slider">
          <div
            v-for="dish in category.dishes"
            :key="dish.id"
            class="dish-card"
            @click="openMenuItem(dish)"
          >
            <img :src="dish.imgUrl || require('@/assets/logo.png')" class="shop-img">
            <p class="dish-name">{{ dish.itemName }}</p>
            <p class="dish-price">{{ dish.price }} 元</p>
          </div>
        </div>

        <button class="scroll-btn right" @click="scrollRight(category.name)">&#8250;</button>
      </div>
    </section>

    <!-- 右下角購物車 -->
    <router-link to="/nologincart" class="cart-btn">🛒</router-link>

    <!-- MenuItem Modal -->
    <MenuItem
      :show="menuItemModalOpen"
      :product="selectedProduct"
      :isFavorited="false"
      @close="closeMenuItem"
      @add-to-cart="handleAddToCart"
      @toggle-favorite="needLogin"
    />
  </div>
</template>

<script>
import MenuItem from '@/components/MenuItem.vue'

export default {
  components: { MenuItem },

  data() {
    return {
      sidebarOpen: false,
      menuItemModalOpen: false,
      keyword: '',
      shop: null,
      selectedProduct: {},
      categoryRefs: {}
    }
  },

  created() {
    this.loadShop()
  },

  watch: {
    '$route.params.id'() {
      this.loadShop()
    }
  },

  computed: {
    searchSuggestions() {
        const key = this.keyword.trim().toLowerCase();
        if (!key || !this.shop || !this.shop.menu) return [];

        return this.shop.menu
            .filter(item =>
                item.itemName.toLowerCase().includes(key)
            )
            .slice(0, 5); // 最多 5 筆
    },
    todayBusiness() {
      if (!this.shop) return {}
      const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
      const today = days[new Date().getDay()]
      return this.shop.businessHours.find(h => h.day === today) || {}
    },

    filteredCategories() {
      if (!this.shop?.menu) return []
      const map = {}
      this.shop.menu.forEach(item => {
        if (this.keyword && !item.itemName.includes(this.keyword)) return
        if (!map[item.tag]) map[item.tag] = []
        map[item.tag].push(item)
      })
      return Object.keys(map).map(tag => ({ name: tag, dishes: map[tag] }))
    }
  },

  methods: {
    selectSuggestion(item) {
        this.keyword = item.itemName;

        this.openMenuItem(item); // 直接開啟 MenuItem Modal
    },
    loadShop() {
      const shopId = this.$route.params.id
      const shop = this.$store.getters['shops/getShopById'](shopId)
      if (!shop) {
        alert('找不到店家')
        this.$router.push('/')
      }
      this.shop = shop
    },

    toggleSidebar() {
      this.sidebarOpen = !this.sidebarOpen
    },

    needLogin() {
      alert('請先登入會員')
      this.$router.push('/login')
    },

    scrollLeft(cat) {
      this.categoryRefs[cat]?.scrollBy({ left: -200, behavior: 'smooth' })
    },

    scrollRight(cat) {
      this.categoryRefs[cat]?.scrollBy({ left: 200, behavior: 'smooth' })
    },

    openMenuItem(dish) {
      this.selectedProduct = dish
      this.menuItemModalOpen = true
    },

    closeMenuItem() {
      this.menuItemModalOpen = false
    },

    handleAddToCart(item) {
      this.$store.dispatch('cart/setStoreId', this.shop.id)
      this.$store.dispatch('cart/addItem', item)
    }
  }
}
</script>
<style scoped>
    .shop {
        background-color: #fff;
        padding: 20px;
        font-family: "Microsoft JhengHei","PingFang TC","Noto Sans TC",sans-serif;
        position: relative;
        overflow: hidden;
    }

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
        margin: 0;
        display: flex;
        flex-direction: column;
        gap: 15px;
        width: 100%;
    }

    .sidebar li {
        color: #fff;
        cursor: pointer;
        font-size: 16px;
        padding: 10px 0;
        border-radius: 4px;
        text-align: left;
    }

        .sidebar li:hover {
            background-color: #001633;
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

    /* 店家名稱 + 收藏 */
    .shop-header {
        display: flex;
        justify-content: center; /* 置中 */
        align-items: center;
        margin-top: 20px;
        gap: 10px; /* 收藏按鈕和店名間距 */
    }

    .shop-name {
        font-size: 28px;
        font-weight: bold;
        color: #0069D9; /* 藍色 */
    }

    .favorite-btn {
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
    }

    /* 店家資訊 */
    .shop-info p {
        margin: 4px 0;
    }

    /* 搜尋欄 */
    .search-section {
        margin-top: 20px;
        display: flex;
        justify-content: center;
        position: relative;
    }

    .search-bar {
        width: 90%;
        padding: 14px;
        border: 2px solid #0069D9;
        border-radius: 10px;
        font-size: 16px;
        padding-right: 50px;
    }

    .search-btn {
        position: absolute;
        right: calc(5%);
        top: 50%;
        transform: translateY(-50%);
        width: 36px;
        height: 36px;
        background-color: #0069D9;
        border: none;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        cursor: pointer;
    }

    /* 分類區塊 */
    .category-section {
        margin-top: 30px;
    }

    .category-title {
        color: #0069D9;
        font-size: 22px;
        margin-bottom: 10px;
    }

    /* 滑動區塊 */
    .slider-container {
        position: relative;
        width: 100%;
        display: flex;
        align-items: center;
    }

    .slider {
        display: flex;
        gap: 15px;
        overflow-x: auto;
        scroll-behavior: smooth;
        -webkit-overflow-scrolling: touch;
        flex-wrap: nowrap;
        padding-bottom: 10px;
    }

        .slider::-webkit-scrollbar {
            display: none;
        }

    .scroll-btn {
        background-color: #0069D9;
        color: #fff;
        border: none;
        border-radius: 50%;
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        z-index: 10;
    }

        .scroll-btn.left {
            left: -18px;
        }

        .scroll-btn.right {
            right: -18px;
        }

    /* 菜品卡片 */
    .dish-card {
        min-width: 160px;
        flex-shrink: 0;
        border-radius: 12px;
        background: #fff;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        text-align: center;
        padding-bottom: 10px;
        transition: transform 0.3s;
        cursor: pointer;
    }

        .dish-card:hover {
            transform: scale(1.05);
        }

    .shop-card {
        min-width: 160px;
        flex-shrink: 0;
        border-radius: 12px;
        background: #fff;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        text-align: center;
        padding-bottom: 10px;
        transition: transform 0.3s;
        text-decoration: none;
    }

        .shop-card:hover {
            transform: scale(1.05);
        }

    .dish-img {
        width: 100%;
        height: 110px;
        border-radius: 12px 12px 0 0;
        object-fit: cover;
    }

    .shop-img {
        width: 100%;
        height: 110px;
        border-radius: 12px 12px 0 0;
        object-fit: cover;
    }

    .shop-name {
        margin-top: 8px;
        font-weight: bold;
        color: #0069D9; /* 藍色文字 */
        text-decoration: none; /* 去掉底線 */
    }

    .dish-name {
        margin-top: 4px;
        font-weight: bold;
        color: #000; /* 藍色文字 */
        text-decoration: none; /* 去掉底線 */
    }

    .dish-price {
        margin-top: 2px;
        font-weight: normal;
        color: #333;
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

        .modal-actions button:first-child {
            background-color: #0069D9;
            color: #fff;
            padding: 6px 12px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }

        .modal-actions button:last-child {
            background-color: #ccc;
            color: #333;
            padding: 6px 12px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }

    /* 右下角購物車 */
    .cart-btn {
        position: fixed;
        right: 20px;
        bottom: 20px;
        width: 56px;
        height: 56px;
        background-color: #0069D9;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 150;
        cursor: pointer;
        box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        color: white;
        font-size: 28px;
        text-align: center;
        line-height: 56px;
    }

    .favorite-btn {
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        transition: transform 0.2s;
    }

        .favorite-btn.animate {
            animation: pop 0.3s ease forwards;
        }

    @keyframes pop {
        0% {
            transform: scale(1);
        }

        50% {
            transform: scale(1.5);
        }

        100% {
            transform: scale(1);
        }
    }

    .favorite-btn.active span {
        color: red; /* 已收藏顯示紅色 */
    }
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
      .search-suggestions {
          position: absolute;
          top: 100%;
          width: 90%;
          background: #fff;
          border: 1px solid #ddd;
          border-radius: 8px;
          margin-top: 6px;
          padding: 0;
          list-style: none;
          z-index: 120;
          box-shadow: 0 4px 10px rgba(0,0,0,0.1);
      }

      .search-suggestions li {
          padding: 10px 14px;
          cursor: pointer;
          font-size: 15px;
          text-align: left;
      }

      .search-suggestions li:hover {
          background-color: #f2f6ff;
      }
</style>
