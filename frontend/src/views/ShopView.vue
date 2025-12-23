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

        <!-- 左側側邊欄 -->
        <div :class="['sidebar', { open: sidebarOpen }]">
            <div class="sidebar-user">
                <img class="sidebar-avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user">
                <span class="username">{{ customer.nickname }}, 肚子餓了嗎</span>
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

        <!-- 搜尋商品 -->
        <div class="search-section">
            <input type="text" class="search-bar" placeholder="搜尋菜品…" v-model="keyword">
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
                loading: true, // 加載狀態
                categoryRefs: {},
                animateFavorites: {}, // 每個店家動畫狀態
            }
        },
        async created() {
            // 組件創建時載入店家資料
            await this.loadShop()

            const userId = this.customer.id;
            if (userId) {
                this.$store.dispatch('cart/setUserId', userId);
            }
        },
        watch: {
            // 監聽路由變化，切換店家時重新載入資料
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
            // 從 Vuex 獲取用戶資料
            customer() {
                return this.$store.getters['user/customer']
            },
            // 取得今天營業時間
            todayBusiness() {
                if (!this.shop) return {};//防止還沒加載出來就被訪問
                const days = ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"];
                const today = days[new Date().getDay()];
                return this.shop.businessHours.find(h => h.day === today) || {};//找到今天營業時間來決定顯示是否營業中
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
            selectSuggestion(item) {
                this.keyword = item.itemName;

                this.openMenuItem(item); // 直接開啟 MenuItem Modal
            },
            // 根據路由參數載入店家資料
            async loadShop() {
                this.loading = true; // 開始加載
                const shopId = this.$route.params.id;

                try {
                    // 首先嘗試從 store 中獲取
                    let shop = this.$store.getters['shops/getShopById'](shopId);

                    if (!shop) {
                        // 如果 store 中沒有，嘗試從 API 獲取
                        console.log('Store 中找不到店家，嘗試從 API 獲取:', shopId);
                        shop = await this.$store.dispatch('shops/fetchShopById', shopId);
                        // fetchShopById 現在會自動將店家加入到 allShops 中，無需額外呼叫 fetchAllShops
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

            toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; },
            openUserModal() { this.editCustomer = { ...this.customer }; this.userModalOpen = true; },
            closeUserModal() { this.userModalOpen = false; },
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
                    price: dish.price,
                    description: dish.description,
                    customOptions: dish.customOptions || [],
                    imgUrl: dish.imgUrl, // 添加圖片URL
                    storeId: this.shop.id // 添加店家ID
                };
                this.menuItemModalOpen = true;
            },
            closeMenuItem() {
                this.menuItemModalOpen = false;
            },
            async handleAddToCart(cartItem) {
                console.log('加入購物車:', cartItem);

                const userId = this.customer.id;
                if (!userId) {
                    alert('請先登入');
                    return;
                }
                
                // 確保 storeId 存在
                const storeId = this.shop?.id;
                if (!storeId) {
                    console.error('店家 ID 不存在:', this.shop);
                    alert('無法取得店家資訊，請重新整理頁面');
                    return;
                }
                
                console.log('準備加入購物車 - 店家ID:', storeId, '商品:', cartItem);
                
                try {
                    // 確保購物車數據是最新的，然後檢查跨店
                    await this.$store.dispatch('cart/fetchCart');
                    const currentStoreId = this.$store.state.cart.storeId;
                    const hasItems = this.$store.state.cart.items.length > 0;
                    
                    // 數據異常：有商品但 storeId 為空
                    if (hasItems && (!currentStoreId || currentStoreId === '')) {
                        const confirmed = confirm(
                            `購物車數據異常（店家資訊遺失）\n` +
                            `是否清空購物車並加入新商品？\n\n` +
                            `點擊「確定」清空購物車並繼續\n` +
                            `點擊「取消」放棄操作`
                        );
                        
                        if (!confirmed) {
                            return;
                        }
                        
                        // 清空購物車
                        await this.$store.dispatch('cart/clearCart');
                    }
                    // 正常跨店檢查
                    else if (hasItems && currentStoreId && currentStoreId !== storeId) {
                        const confirmed = confirm(
                            `購物車中已有「${this.getStoreName(currentStoreId)}」的商品\n` +
                            `是否清空購物車並加入「${this.shop.name}」的商品？\n\n` +
                            `點擊「確定」清空購物車並繼續\n` +
                            `點擊「取消」放棄操作`
                        );
                        
                        if (!confirmed) {
                            return;
                        }
                        
                        // 清空購物車
                        await this.$store.dispatch('cart/clearCart');
                    }
                    
                    await this.$store.dispatch('cart/addItem', {
                        item: cartItem,
                        storeId: storeId
                    });

                    // 確保狀態同步
                    await this.$store.dispatch('cart/fetchCart');

                    alert('已加入購物車');
                } catch (err) {
                    console.error('加入購物車失敗:', err);
                    
                    // 檢查是否是跨店錯誤
                    if (err.message && err.message.includes('跨店')) {
                        alert('購物車不可跨店點餐，請先清空購物車');
                    } else {
                        alert('加入購物車失敗: ' + err.message);
                    }
                }
            },
            
            // 獲取店家名稱（用於跨店提示）
            getStoreName(storeId) {
                const shop = this.$store.getters['shops/getShopById'](storeId);
                return shop ? shop.name : '其他店家';
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
                sessionStorage.removeItem('token');    // 如果有 token
                sessionStorage.removeItem('user');
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
        justify-content: center;
        align-items: center;
        margin-top: 20px;
        gap: 10px; 
    }

    .shop-name {
        font-size: 28px;
        font-weight: bold;
        color: #0069D9; 
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

    /* Shop info */
    .shop-info {
        margin-top: 20px;
        padding: 20px;
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
