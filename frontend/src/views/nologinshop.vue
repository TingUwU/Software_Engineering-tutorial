<template>
  <div class="shop">
    <!-- 加載狀態 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>載入中...</p>
    </div>

    <!-- 錯誤狀態 -->
    <div v-else-if="!shop" class="error-container">
      <p>找不到該店家</p>
      <button @click="$router.push('/')">返回首頁</button>
    </div>

    <!-- 主要內容 -->
    <div v-else>
      <div
        class="shop-hero"
        :style="{
          backgroundImage: `url(${shop.coverImage || require('@/assets/logo.png')})`
        }"
      >
  <div class="shop-hero-overlay"></div>
</div>
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
        <router-link to="/nologinorder"><li>訂單管理</li></router-link>
      </ul>
      <!-- 登入按鈕 -->
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

    <!-- 店家名稱 -->
    <div class="shop-header">
      <h2 class="shop-name">{{ shop.name }}</h2>

    </div>

    <!-- 店家資訊 -->
    <div class="shop-info">
      <div v-if="shop.description" class="shop-description">
        <h4>店家簡介</h4>
        <p class="description-text">{{ shop.description }}</p>
      </div>
      <div class="business-hours">
        <h4>營業時間</h4>
        <pre class="business-hours-text">{{ formattedBusinessHours }}</pre>
      </div>
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
    <router-link to="/nologincart" class="cart-btn">
      <svg xmlns="http://www.w3.org/2000/svg"
                 fill="none" stroke="white" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round"
                 width="24" height="24">
                <circle cx="9" cy="21" r="1"/>
                <circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
    </router-link>

    <!-- MenuItem Modal -->
    <MenuItem
      :show="menuItemModalOpen"
      :product="selectedProduct"
      @close="closeMenuItem"
      @add-to-cart="handleAddToCart"
    />
    </div>
  </div>
</template>

<script>
import MenuItem from '@/components/nologinMenuItem.vue'

export default {
  components: { MenuItem },

  data() {
    return {
      sidebarOpen: false,
      menuItemModalOpen: false,
      keyword: '',
      shop: null,
      loading: true, // 加載狀態
      selectedProduct: {},
      categoryRefs: {}
    }
  },

  async created() {
    // 設置訪客模式
    this.$store.dispatch('cart/setGuestMode', true)
    await this.loadShop()
  },

  watch: {
    '$route.params.id': async function(newId) {
      if (newId) {
        await this.loadShop()
      }
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
      const days = ['星期日','星期一','星期二','星期三','星期四','星期五','星期六']
      const today = days[new Date().getDay()]
      return this.shop.businessHours.find(h => h.day === today) || {}
    },

    // 格式化營業時間顯示
    formattedBusinessHours() {
      if (!this.shop || !this.shop.businessHours || this.shop.businessHours.length === 0) {
        return '暫無營業時間資訊';
      }

      // 將營業時間按時間分組
      const timeGroups = {};
      this.shop.businessHours.forEach(bh => {
        const key = `${bh.start || ''}-${bh.end || ''}-${bh.note || ''}`;
        if (!timeGroups[key]) {
          timeGroups[key] = {
            start: bh.start,
            end: bh.end,
            note: bh.note,
            days: []
          };
        }
        timeGroups[key].days.push(bh.day);
      });

      // 格式化輸出
      const result = [];
      Object.values(timeGroups).forEach(group => {
        if (group.note && group.note.trim() !== '') {
          // 有備註（如公休）
          result.push(`${group.days.join('、')}：${group.note}`);
        } else if (group.start && group.end) {
          // 有營業時間
          result.push(`${group.days.join('、')}：${group.start} ~ ${group.end}`);
        } else {
          // 無營業時間
          result.push(`${group.days.join('、')}：未營業`);
        }
      });

      return result.join('\n');
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
    async loadShop() {
      this.loading = true;
      const shopId = this.$route.params.id;

      try {
        // 首先嘗試從 store 中獲取
        let shop = this.$store.getters['shops/getShopById'](shopId);

        if (!shop) {
          // 如果 store 中沒有，嘗試從 API 獲取
          console.log('Store 中找不到店家，嘗試從 API 獲取:', shopId);
          shop = await this.$store.dispatch('shops/fetchShopById', shopId);

          // 同時獲取所有店家列表，確保其他頁面能正常工作
          await this.$store.dispatch('shops/fetchAllShops');
        }

        this.shop = shop;
        console.log('成功載入店家:', shop.name);
      } catch (error) {
        console.error('獲取店家失敗:', error);
        this.shop = null; // 確保 shop 為 null，觸發錯誤顯示
      } finally {
        this.loading = false; // 結束加載
      }
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

    async handleAddToCart(item) {
      const result = await this.$store.dispatch('cart/addItem', { item, storeId: this.shop.id })

      // 如果 result 不是 false，表示添加成功
      if (result !== false) {
        alert('已加入購物車')
      }
      // 如果 result 是 false，表示用戶取消了操作，不顯示 alert
    },
    goLogin() {
      this.sidebarOpen = false; // 點擊後關閉側邊欄
      this.$router.push('/login'); // 導向登入頁面
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

    /* 店家資訊 */
    .shop-info {
        margin-top: 20px;
        padding: 20px;
        background: #f8f9fa;
        border-radius: 8px;
    }

    .shop-description {
        margin-bottom: 20px;
    }

    .shop-description h4 {
        margin: 0 0 8px 0;
        color: #0069D9;
        font-size: 16px;
        font-weight: 600;
    }

    .description-text {
        margin: 0;
        font-size: 14px;
        line-height: 1.6;
        color: #555;
    }

    .shop-info p {
        margin: 4px 0;
    }

    .business-hours {
        margin-bottom: 15px;
    }

    .business-hours h4 {
        margin: 0 0 10px 0;
        color: #0069D9;
        font-size: 16px;
        font-weight: 600;
    }

    .business-hours-text {
        margin: 0;
        font-family: inherit;
        font-size: 14px;
        line-height: 1.6;
        white-space: pre-line;
        color: #333;
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
      .shop-hero {
    position: relative;
    width: 100%;
    height: 180px;                 /* 高度可自行調整 */
    background-image: url('@/assets/logo.png'); /* 預設圖 */
    background-size: cover;
    background-position: center;
    border-radius: 0 0 16px 16px;
    margin-bottom: 20px;
}

/* 半透明遮罩（關鍵） */
.shop-hero-overlay {
    position: absolute;
    inset: 0;
    background: rgba(255, 255, 255, 0.65); /* 白色半透明 */
    backdrop-filter: blur(2px);            /* 可要可不要 */
}

/* 加載和錯誤狀態 */
.loading-container, .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 60vh;
    padding: 40px;
    text-align: center;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    border: 4px solid #f3f3f3;
    border-top: 4px solid #0069D9;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20px;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.loading-container p, .error-container p {
    font-size: 18px;
    color: #666;
    margin-bottom: 20px;
}

.error-container button {
    padding: 10px 20px;
    background-color: #0069D9;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.3s;
}

.error-container button:hover {
    background-color: #0056b3;
}

</style>
