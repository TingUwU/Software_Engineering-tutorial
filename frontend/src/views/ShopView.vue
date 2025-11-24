<template>
    <div class="shop">
        <!-- 遮罩層 -->
        <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

        <!-- 左側側邊欄 -->
        <div :class="['sidebar', { open: sidebarOpen }]">
            <div class="sidebar-user">
                <img class="sidebar-avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user">
                <span class="username">{{ customer.nickname }}, 肚子餓了嗎</span>
            </div>
            <ul>
                <li @click="openUserModal">使用者資訊</li>
                <router-link to="/cart"><li>購物車</li></router-link>
                <li>訂單管理</li>
                <li>歷史</li>
                <li>收藏</li>
            </ul>
            <div class="sidebar-logout">
                <button @click="logout">登出</button>
            </div>
        </div>

        <!-- 左上角顧客頭像 -->
        <img class="avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user" @click="toggleSidebar">

        <!-- 店家名稱 + 收藏 -->
        <div class="shop-header">
            <h2 class="shop-name">{{ shop.name }}</h2>
            <button class="favorite-btn"
                    :class="{ active: isFavorited(shop), animate: animateFavorites[shop.id] }"
                    @click="toggleFavoriteWithAnimation(shop)">
                <span v-if="isFavorited(shop)">❤️</span>
                <span v-else>🤍</span>
            </button>
        </div>

        <!-- 店家資訊 -->
        <div class="shop-info">
            <p>營業時間: {{ todayBusiness.start || '未營業' }} ~ {{ todayBusiness.close || '未營業' }}</p>
            <p>地址: {{ shop.address }}</p>
        </div>

        <!-- 搜尋商品 -->
        <div class="search-section">
            <input type="text" class="search-bar" placeholder="搜尋菜品…" v-model="keyword">
            <button class="search-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
            </button>
        </div>

        <!-- 菜品分類與滾輪 -->
        <section v-for="category in filteredCategories" :key="category.name" class="category-section">
            <h2 class="category-title">{{ category.name }}</h2>
            <div class="slider-container">
                <button class="scroll-btn left" @click="scrollLeft(category.name)">&#8249;</button>
                <div :ref="el => categoryRefs[category.name] = el" class="slider">
                    <div v-for="dish in category.dishes" :key="dish.id" class="dish-card" @click="openMenuItem(dish)">
                        <img :src="dish.imgUrl || require('@/assets/logo.png')" class="shop-img" alt="菜品圖片">
                        <p class="dish-name">{{ dish.itemName }}</p>
                        <p class="dish-price">{{ dish.price }} 元</p>
                    </div>
                </div>
                <button class="scroll-btn right" @click="scrollRight(category.name)">&#8250;</button>
            </div>
        </section>

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
                        <button type="submit">儲存</button>
                        <button type="button" @click="closeUserModal">關閉</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- 右下角購物車快捷 -->
        <router-link to="/cart" class="cart-btn">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="24" height="24">
                <circle cx="9" cy="21" r="1" />
                <circle cx="20" cy="21" r="1" />
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
            </svg>
        </router-link>

        <!-- MenuItem Modal -->
        <MenuItem
            :show="menuItemModalOpen"
            :product="selectedProduct"
            :isFavorited="isItemFavorited(selectedProduct.id)"
            @close="closeMenuItem"
            @add-to-cart="handleAddToCart"
            @toggle-favorite="toggleItemFavorite"
        />
    </div>
</template>

<script>
    import MenuItem from '@/components/MenuItem.vue';

    export default {
        components: {
            MenuItem
        },
        data() {
            return {
                sidebarOpen: false,
                userModalOpen: false,
                menuItemModalOpen: false,
                keyword: "",
                selectedProduct: {
                    id: '',
                    itemName: '',
                    price: 0
                },
                editCustomer: { photo: "" },
                shop: null, // 初始為 null，等待從 store 載入
                categoryRefs: {},
                animateFavorites: {}, // 每個店家動畫狀態
            }
        },
        created() {
            // 組件創建時載入店家資料
            this.loadShop()
        },
        watch: {
            // 監聽路由變化，切換店家時重新載入資料
            '$route.params.id': function(newId) {
                if (newId) {
                    this.loadShop()
                }
            }
        },
        computed: {
            // 從 Vuex 獲取用戶資料
            customer() {
                return this.$store.getters['user/customer']
            },
            // 取得今天營業時間
            todayBusiness() {
                if (!this.shop) return {};//防止還沒加載出來就被訪問
                const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
                const today = days[new Date().getDay()];
                return this.shop.businessHours.find(h => h.day === today) || {};//找到今天營業時間來決定顯示是否營業中
            },
            // 根據關鍵字過濾
            filteredCategories() {
                if (!this.shop || !this.shop.menu) return [];
                const categoriesMap = {};
                this.shop.menu.forEach(item => {
                    if (this.keyword && !item.itemName.includes(this.keyword)) return;
                    if (!categoriesMap[item.tag]) categoriesMap[item.tag] = [];
                    categoriesMap[item.tag].push(item);
                });
                return Object.keys(categoriesMap).map(tag => ({ name: tag, dishes: categoriesMap[tag] }));
            }
        },
        methods: {
            // 根據路由參數載入店家資料
            loadShop() {
                const shopId = this.$route.params.id;
                const shop = this.$store.getters['shops/getShopById'](shopId);
                if (shop) {
                    this.shop = shop;
                } else {
                    // 如果找不到店家，顯示錯誤或導航回首頁
                    alert('找不到該店家');
                    this.$router.push('/');
                }
            },

            toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; },
            openUserModal() { this.editCustomer = { ...this.customer }; this.userModalOpen = true; },
            closeUserModal() { this.userModalOpen = false; },
            updateUser() {
                this.$store.dispatch('user/updateCustomer', this.editCustomer);
                this.closeUserModal();
                alert("使用者資訊已更新！");
                // TODO: call API to save user info
            },
            
            onAvatarChange(event) {
                const file = event.target.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = e => { this.editCustomer.photo = e.target.result; };
                    reader.readAsDataURL(file);
                }
            },
            scrollLeft(category) {
                const slider = this.categoryRefs[category];
                if (slider) slider.scrollBy({ left: -200, behavior: 'smooth' });
            },
            scrollRight(category) {
                const slider = this.categoryRefs[category];
                if (slider) slider.scrollBy({ left: 200, behavior: 'smooth' });
            },
            isFavorited(shop) {
                return this.$store.getters['user/isStoreFavorited'](shop.id);
            },
            toggleFavorite(shop) {
                this.$store.dispatch('user/toggleFavorStore', shop.id);
                // TODO: call API to save favorStores
            },
            toggleFavoriteWithAnimation(shop) {
                this.toggleFavorite(shop);

                // 保持白心或紅心
                this.animateFavorites = { ...this.animateFavorites, [shop.id]: true };

                setTimeout(() => {
                    this.animateFavorites = { ...this.animateFavorites, [shop.id]: false };
                }, 300);
            },
            openMenuItem(dish) {
                this.selectedProduct = {
                    id: dish.id,
                    itemName: dish.itemName,
                    price: dish.price
                };
                this.menuItemModalOpen = true;
            },
            closeMenuItem() {
                this.menuItemModalOpen = false;
            },
            handleAddToCart(cartItem) {
                console.log('加入購物車:', cartItem);
                // 設定店家 ID 到購物車
                this.$store.dispatch('cart/setStoreId', this.shop.id);
                // 加入商品到購物車
                this.$store.dispatch('cart/addItem', cartItem);
            },
            // 檢查商品是否已收藏
            isItemFavorited(itemId) {
                return this.$store.getters['user/isItemFavorited'](this.shop.id, itemId);
            },
            // 切換商品收藏狀態
            toggleItemFavorite() {
                const itemId = this.selectedProduct.id;
                const storeId = this.shop.id;
                
                this.$store.dispatch('user/toggleFavorItem', { storeId, itemId }).then(isFavorited => {
                    if (isFavorited) {
                        alert('已加入收藏');
                    } else {
                        alert('已取消收藏');
                    }
                });
                // TODO: call API to save favorItems
            },
            logout() {
                this.$store.dispatch('user/logout'); // 呼叫 Vuex logout
                localStorage.removeItem('token');    // 如果有 token
                localStorage.removeItem('user');
                this.$router.push('/login');         // 導向登入頁
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
</style>
