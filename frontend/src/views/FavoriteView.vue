<template>
    <div class="home">
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

        <!-- LOGO + 系統名稱 -->
        <header class="header">
            <div class="logo-container">
                <h1 class="logo">收藏</h1>
            </div>
        </header>

        <!-- 搜尋區 -->
        <div class="search-section">
            <input type="text" class="search-bar" placeholder="搜尋收藏的餐廳/商品…" v-model="keyword">
            <button class="search-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
            </button>
        </div>

        <!-- 收藏的餐廳 -->
        <section class="category-section">
            <h2 class="category-title">餐廳 ({{ favoriteStores.length }})</h2>
            <div v-if="favoriteStores.length > 0" class="slider-container">
                <button class="scroll-btn left" @click="scrollLeft('stores')">&#8249;</button>
                <div ref="storesSlider" class="slider">
                    <div v-for="store in favoriteStores"
                         :key="store.id"
                         class="shop-card"
                         @click="goToStore(store.id)">
                        <img :src="store.menu[0]?.imgUrl || require('@/assets/logo.png')" class="shop-img" alt="店家圖片">
                        <p class="shop-name">{{ store.name }}</p>
                    </div>
                </div>
                <button class="scroll-btn right" @click="scrollRight('stores')">&#8250;</button>
            </div>
            <div v-else class="empty-message">
                <p>尚無收藏的餐廳</p>
            </div>
        </section>

        <!-- 收藏的商品 -->
        <section class="category-section">
            <h2 class="category-title">商品 ({{ favoriteItems.length }})</h2>
            <div v-if="favoriteItems.length > 0" class="slider-container">
                <button class="scroll-btn left" @click="scrollLeft('items')">&#8249;</button>
                <div ref="itemsSlider" class="slider">
                    <div v-for="item in favoriteItems"
                         :key="item.id"
                         class="shop-card"
                         @click="openMenuItem(item)">
                        <img :src="item.imgUrl || require('@/assets/logo.png')" class="shop-img" alt="商品圖片">
                        <div class="item-info">
                            <p class="shop-name">{{ item.itemName }}</p>
                            <p class="item-store">{{ item.storeName }}</p>
                            <p class="item-price">NT$ {{ item.price }}</p>
                        </div>
                    </div>
                </div>
                <button class="scroll-btn right" @click="scrollRight('items')">&#8250;</button>
            </div>
            <div v-else class="empty-message">
                <p>尚無收藏的商品</p>
            </div>
        </section>

        <!-- 自訂組合 -->
        <section class="category-section">
            <div class="combo-header">
                <h2 class="category-title">自訂組合 ({{ customer.customCombos.length }})</h2>
                <button class="add-combo-btn" @click="openAddComboModal">+ 新增組合</button>
            </div>
            <div v-if="customer.customCombos.length > 0" class="slider-container">
                <button class="scroll-btn left" @click="scrollLeft('combos')">&#8249;</button>
                <div ref="combosSlider" class="slider">
                    <div v-for="combo in customer.customCombos"
                         :key="combo.comboId"
                         class="shop-card combo-card"
                         @click="openComboModal(combo)">
                        <div class="combo-preview">
                            <div class="combo-name-preview">{{ combo.comboName }}</div>
                            <div class="combo-info-preview">
                                <span class="combo-count">{{ combo.items ? combo.items.length : 0 }} 項商品</span>
                                <span class="combo-price">NT$ {{ getComboTotal(combo) }}</span>
                            </div>
                        </div>
                        <div class="combo-actions-overlay">
                            <button class="edit-btn" @click.stop="openEditComboModal(combo)">編輯</button>
                            <button class="delete-btn" @click.stop="deleteCombo(combo.comboId)">刪除</button>
                        </div>
                    </div>
                </div>
                <button class="scroll-btn right" @click="scrollRight('combos')">&#8250;</button>
            </div>
            <div v-else class="empty-message">
                <p>尚無自訂組合</p>
                <button class="add-combo-btn empty-add-btn" @click="openAddComboModal">+ 建立第一個組合</button>
            </div>
        </section>

        <!-- 組合詳情 Modal -->
        <div v-if="comboModalOpen" class="modal-overlay" @click.self="closeComboModal">
            <div class="combo-detail-modal">
                <button class="btn-close" @click="closeComboModal">✕</button>
                <h3>{{ selectedCombo?.comboName }}</h3>
                <div v-if="selectedCombo?.items && selectedCombo.items.length > 0">
                    <div class="combo-items-detail">
                        <div v-for="(comboItem, index) in selectedCombo.items"
                             :key="`${comboItem.itemId}-${index}`"
                             class="combo-item-detail"
                             :class="{ 'editing': editingItemIndex === index }">
                            <div class="item-detail-info">
                                <div v-if="editingItemIndex !== index">
                                    <!-- 正常顯示模式 -->
                                    <div class="item-header">
                                        <span class="item-name">{{ getItemName(comboItem) }}</span>
                                        <div class="item-actions">
                                            <button class="edit-item-btn" @click="startEditItem(index)">編輯</button>
                                            <button class="delete-item-btn" @click="deleteItemFromCombo(selectedCombo.comboId, index)">刪除</button>
                                        </div>
                                    </div>
                                    <div v-if="comboItem.customizations && comboItem.customizations.length > 0" class="item-customizations">
                                        <small v-for="custom in comboItem.customizations" :key="custom" class="custom-tag">{{ custom }}</small>
                                    </div>
                                    <div class="item-quantity-info">
                                        <span>數量: {{ comboItem.quantity || 1 }}</span>
                                    </div>
                                </div>
                                <div v-else>
                                    <!-- 編輯模式 -->
                                    <div class="item-edit-form">
                                        <h4>{{ getItemName(comboItem) }}</h4>
                                        <div class="edit-quantity">
                                            <label>數量:</label>
                                            <div class="quantity-control">
                                                <button @click="editItemQuantity = Math.max(1, editItemQuantity - 1)">-</button>
                                                <input type="number" v-model.number="editItemQuantity" min="1" />
                                                <button @click="editItemQuantity++">+</button>
                                            </div>
                                        </div>
                            
                                        <div class="edit-actions">
                                            <button class="save-btn" @click="saveItemEdit(selectedCombo.comboId, index)">儲存</button>
                                            <button class="cancel-btn" @click="cancelItemEdit">取消</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <span class="item-price">NT$ {{ getItemTotalPrice(comboItem) }}</span>
                        </div>
                    </div>
                    <div class="combo-total-detail">
                        <span>總計：NT$ {{ getComboTotal(selectedCombo) }}</span>
                        <button class="order-combo-btn" @click="orderCombo(selectedCombo)">訂購組合</button>
                    </div>
                </div>
                <div v-else class="empty-combo-detail">
                    <p>尚未添加商品</p>
                </div>
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
                        <button type="submit">儲存</button>
                        <button type="button" @click="closeUserModal">關閉</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- 商品詳情 Modal -->
        <MenuItem
            :show="menuItemModalOpen"
            :product="selectedProduct"
            :isFavorited="true"
            @close="closeMenuItem"
            @add-to-cart="handleAddToCart"
            @toggle-favorite="toggleItemFavorite"
        />

        <!-- 新增組合 Modal -->
        <div v-if="addComboModalOpen" class="modal-overlay" @click.self="closeAddComboModal">
            <div class="combo-modal">
                <h3>新增自訂組合</h3>
                <form @submit.prevent="addCombo">
                    <div class="form-group">
                        <label>組合名稱:</label>
                        <input type="text" v-model="newComboName" placeholder="請輸入組合名稱" required>
                    </div>
                    <div class="modal-actions">
                        <button type="submit">新增</button>
                        <button type="button" @click="closeAddComboModal">取消</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- 編輯組合 Modal -->
        <div v-if="editComboModalOpen" class="modal-overlay" @click.self="closeEditComboModal">
            <div class="combo-modal">
                <h3>編輯組合名稱</h3>
                <form @submit.prevent="updateCombo">
                    <div class="form-group">
                        <label>組合名稱:</label>
                        <input type="text" v-model="editComboName" placeholder="請輸入組合名稱" required>
                    </div>
                    <div class="modal-actions">
                        <button type="submit">更新</button>
                        <button type="button" @click="closeEditComboModal">取消</button>
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
                selectedProduct: {
                    id: '',
                    itemName: '',
                    price: 0,
                    description: '',
                    imgUrl: '',
                    storeId: ''
                },
                keyword: "",
                editCustomer: {
                    photo: ""
                },
                comboModalOpen: false,
                addComboModalOpen: false,
                editComboModalOpen: false,
                addItemModalOpen: false,
                selectedCombo: null,
                newComboName: "",
                editComboName: "",
                editingItemIndex: -1,
                editItemQuantity: 1,
                editItemCustomizations: []
            };
        },
        computed: {
            //獲取用戶資料
            customer() {
                return this.$store.getters['user/customer']
            },
            //獲取所有店家資料
            stores() {
                return this.$store.getters['shops/allShops']
            },
            favoriteStores() {
                return this.stores.filter(store => 
                    this.customer.favorStores.includes(store.id)
                );
            },
            favoriteItems() {
                const items = [];
                this.customer.favorItems.forEach(favorItem => {
                    const store = this.stores.find(s => s.id === favorItem.storeId);
                    if (store) {
                        const menuItem = store.menu.find(m => m.id === favorItem.itemId);
                        if (menuItem) {
                            items.push({
                                storeId: store.id,
                                storeName: store.name,
                                ...menuItem
                            });
                        }
                    }
                });
                return items;
            }
        },
        methods: {
            toggleSidebar() {
                this.sidebarOpen = !this.sidebarOpen;
            },
            openUserModal() {
                this.editCustomer = { ...this.customer };
                this.userModalOpen = true;
            },
            closeUserModal() {
                this.userModalOpen = false;
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

                    await this.$store.dispatch('user/updateUser', { userId, updates });
                    this.closeUserModal();
                    alert('使用者資訊已更新！');
                } catch (err) {
                    console.error(err);
                    alert('更新失敗，請稍後再試: ' + err.message);
                }
            },
            scrollLeft(type) {
                const slider = type === 'stores' ? this.$refs.storesSlider :
                             type === 'items' ? this.$refs.itemsSlider :
                             this.$refs.combosSlider;
                if (slider) {
                    slider.scrollBy({ left: -200, behavior: 'smooth' });
                }
            },
            scrollRight(type) {
                const slider = type === 'stores' ? this.$refs.storesSlider :
                             type === 'items' ? this.$refs.itemsSlider :
                             this.$refs.combosSlider;
                if (slider) {
                    slider.scrollBy({ left: 200, behavior: 'smooth' });
                }
            },
            goToCart(){
                this.$router.push('/cart');
            },
            goToStore(storeId) {
                this.$router.push({ name: 'ShopView', params: { id: storeId } });
            },
            openMenuItem(item) {
                this.selectedProduct = {
                    ...item
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
                const storeId = cartItem.storeId || this.selectedProduct.storeId;
                if (!storeId) {
                    console.error('店家 ID 不存在:', cartItem);
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
                            `是否清空購物車並加入「${this.getStoreName(storeId)}」的商品？\n\n` +
                            `點擊「確定」清空購物車並繼續\n` +
                            `點擊「取消」放棄操作`
                        );

                        if (!confirmed) {
                            return;
                        }

                        // 清空購物車
                        await this.$store.dispatch('cart/clearCart');
                    }

                    // 確保 cartItem 包含 storeId
                    const cartItemWithStoreId = {
                        ...cartItem,
                        storeId: storeId
                    };

                    await this.$store.dispatch('cart/addItem', {
                        item: cartItemWithStoreId,
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
            toggleItemFavorite() {
                const itemId = this.selectedProduct.id;
                const storeId = this.selectedProduct.storeId;
                
                this.$store.dispatch('user/toggleFavorItem', { storeId, itemId }).then(isFavorited => {
                    if (isFavorited) {
                        alert('已加入收藏');
                    } else {
                        alert('已取消收藏');
                        // 關閉 Modal（因為已經取消收藏）
                        this.closeMenuItem();
                    }
                });
                // TODO: call API to save favorItems
            },
            onAvatarChange(event) {
                const file = event.target.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = e => {
                        this.editCustomer.photo = e.target.result; // base64 字串
                    };
                    reader.readAsDataURL(file);
                }
            },
            logout() {
                this.$store.dispatch('user/logout'); // 呼叫 Vuex logout
                sessionStorage.removeItem('token');    // 如果有 token
                sessionStorage.removeItem('user');
                this.$router.push('/login');         // 導向登入頁
            },

            // 組合詳情 Modal 方法
            openComboModal(combo) {
                this.selectedCombo = combo;
                this.comboModalOpen = true;
            },
            closeComboModal() {
                this.comboModalOpen = false;
                this.selectedCombo = null;
            },

            // 獲取單個商品的總價格（基礎價格 + 客製化選項價格）× 數量
            getItemTotalPrice(comboItem) {
                const store = this.stores.find(s => s.id === comboItem.storeId);
                if (store) {
                    const menuItem = store.menu.find(m => m.id === comboItem.itemId);
                    if (menuItem) {
                        let unitPrice = menuItem.price;

                        // 計算客製化選項的價格
                        if (comboItem.customizations && comboItem.customizations.length > 0) {
                            comboItem.customizations.forEach(customText => {
                                const priceMatch = customText.match(/[+-]\$\d+/);
                                if (priceMatch) {
                                    const priceStr = priceMatch[0];
                                    const priceValue = parseInt(priceStr.substring(2));
                                    if (priceStr.startsWith('+')) {
                                        unitPrice += priceValue;
                                    } else if (priceStr.startsWith('-')) {
                                        unitPrice -= priceValue;
                                    }
                                }
                            });
                        }

                        const quantity = comboItem.quantity || 1;
                        return unitPrice * quantity;
                    }
                }
                return 0;
            },

            // 編輯商品相關方法
            startEditItem(index) {
                const comboItem = this.selectedCombo.items[index];
                this.editingItemIndex = index;
                this.editItemQuantity = comboItem.quantity || 1;
                this.editItemCustomizations = [...(comboItem.customizations || [])];
            },

            cancelItemEdit() {
                this.editingItemIndex = -1;
                this.editItemQuantity = 1;
                this.editItemCustomizations = [];
            },

            async saveItemEdit(comboId, itemIndex) {
                try {
                    const comboItem = this.selectedCombo.items[itemIndex];
                    const originalCustomizations = [...comboItem.customizations];

                    // 呼叫後端API更新組合中的商品信息
                    await this.$store.dispatch('user/updateCustomComboItem', {
                        comboId: comboId,
                        storeId: comboItem.storeId,
                        itemId: comboItem.itemId,
                        updates: {
                            originalCustomizations: originalCustomizations, // 用於識別具體的商品實例
                            quantity: this.editItemQuantity,
                            customizations: this.editItemCustomizations
                        }
                    });

                    // 更新本地狀態以反映變化
                    comboItem.quantity = this.editItemQuantity;
                    comboItem.customizations = [...this.editItemCustomizations];

                    alert('商品資訊已更新！');
                    this.cancelItemEdit();
                } catch (err) {
                    console.error(err);
                    alert('更新失敗：' + err.message);
                }
            },

            async deleteItemFromCombo(comboId, itemIndex) {
                if (!confirm('確定要從組合中移除這個商品嗎？')) {
                    return;
                }

                try {
                    const comboItem = this.selectedCombo.items[itemIndex];
                    await this.$store.dispatch('user/deleteCustomComboItem', {
                        comboId: comboId,
                        storeId: comboItem.storeId,
                        itemId: comboItem.itemId,
                        customizations: comboItem.customizations
                    });

                    // 如果組合中沒有商品了，重置店家ID
                    if (this.selectedCombo.items.length === 1) {
                        // 重新獲取組合數據以反映最新的狀態
                        await this.$store.dispatch('user/getCustomCombos');
                    }

                    alert('商品已從組合中移除！');
                    this.closeComboModal();
                } catch (err) {
                    console.error(err);
                    alert('移除失敗：' + err.message);
                }
            },

            // 自訂組合相關方法
            openAddComboModal() {
                this.newComboName = "";
                this.addComboModalOpen = true;
            },

            closeAddComboModal() {
                this.addComboModalOpen = false;
                this.newComboName = "";
            },

            async addCombo() {
                if (!this.newComboName.trim()) {
                    alert('請輸入組合名稱');
                    return;
                }
                try {
                    await this.$store.dispatch('user/addCustomCombo', this.newComboName.trim());
                    this.closeAddComboModal();
                    alert('組合新增成功！');
                } catch (err) {
                    console.error(err);
                    alert('新增失敗：' + err.message);
                }
            },

            openEditComboModal(combo) {
                this.selectedCombo = combo;
                this.editComboName = combo.comboName;
                this.editComboModalOpen = true;
            },

            closeEditComboModal() {
                this.editComboModalOpen = false;
                this.selectedCombo = null;
                this.editComboName = "";
            },

            async updateCombo() {
                if (!this.editComboName.trim()) {
                    alert('請輸入組合名稱');
                    return;
                }
                try {
                    await this.$store.dispatch('user/updateCustomCombo', {
                        comboId: this.selectedCombo.comboId,
                        comboName: this.editComboName.trim()
                    });
                    this.closeEditComboModal();
                    alert('組合更新成功！');
                } catch (err) {
                    console.error(err);
                    alert('更新失敗：' + err.message);
                }
            },

            async deleteCombo(comboId) {
                if (!confirm('確定要刪除這個組合嗎？')) {
                    return;
                }
                try {
                    await this.$store.dispatch('user/deleteCustomCombo', comboId);
                    alert('組合刪除成功！');
                } catch (err) {
                    console.error(err);
                    alert('刪除失敗：' + err.message);
                }
            },

            openAddItemModal(combo) {
                this.selectedCombo = combo;
                this.addItemModalOpen = true;
            },

            closeAddItemModal() {
                this.addItemModalOpen = false;
                this.selectedCombo = null;
            },

            async addItemToCombo(item) {
                try {
                    await this.$store.dispatch('user/addCustomComboItem', {
                        comboId: this.selectedCombo.comboId,
                        storeId: item.storeId,
                        itemId: item.id
                    });
                    alert('商品已加入組合！');
                } catch (err) {
                    console.error(err);
                    alert('加入失敗：' + err.message);
                }
            },

            async removeItemFromCombo(comboId, comboItem) {
                try {
                    await this.$store.dispatch('user/deleteCustomComboItem', {
                        comboId: comboId,
                        storeId: comboItem.storeId,
                        itemId: comboItem.itemId
                    });
                    alert('商品已從組合移除！');
                } catch (err) {
                    console.error(err);
                    alert('移除失敗：' + err.message);
                }
            },

            getItemName(comboItem) {
                const store = this.stores.find(s => s.id === comboItem.storeId);
                if (store) {
                    const menuItem = store.menu.find(m => m.id === comboItem.itemId);
                    if (menuItem) {
                        return menuItem.itemName;
                    }
                }
                return '未知商品';
            },

            getComboTotal(combo) {
                let total = 0;
                combo.items.forEach(comboItem => {
                    total += this.getItemTotalPrice(comboItem);
                });
                return total;
            },

            async orderCombo(combo) {
                // 檢查組合中的所有商品是否來自同一個店家
                const storeIds = [...new Set(combo.items.map(item => item.storeId))];
                if (storeIds.length > 1) {
                    alert('組合中包含來自不同店家的商品，無法一次訂購。請分開訂購。');
                    return;
                }

                const storeId = storeIds[0];

                try {
                    // 檢查購物車是否已有其他店家的商品
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
                        const storeName = await this.getStoreName(currentStoreId);
                        const confirmed = confirm(
                            `購物車中已有「${storeName}」的商品\n` +
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

                    // 依次添加每個商品到購物車
                    for (const comboItem of combo.items) {
                        const store = this.stores.find(s => s.id === comboItem.storeId);
                        if (store) {
                            const menuItem = store.menu.find(m => m.id === comboItem.itemId);
                            if (menuItem) {
                                let finalPrice = menuItem.price;

                                // 計算客製化選項的價格
                                if (comboItem.customizations && comboItem.customizations.length > 0) {
                                    comboItem.customizations.forEach(customText => {
                                        const priceMatch = customText.match(/[+-]\$\d+/);
                                        if (priceMatch) {
                                            const priceStr = priceMatch[0];
                                            const price = parseInt(priceStr.substring(2));
                                            if (priceStr.startsWith('+')) {
                                                finalPrice += price;
                                            } else if (priceStr.startsWith('-')) {
                                                finalPrice -= price;
                                            }
                                        }
                                    });
                                }

                                const cartItem = {
                                    menuItemId: menuItem.id,
                                    itemName: menuItem.itemName,
                                    unitPrice: finalPrice, // 使用包含客製化選項的價格
                                    quantity: comboItem.quantity || 1, // 使用組合中儲存的數量
                                    customization: comboItem.customizations || [], // 使用儲存的客製化選項
                                    itemSubTotal: finalPrice * (comboItem.quantity || 1) // 總價 = 單價 × 數量
                                };

                                // 使用統一的店家ID
                                await this.$store.dispatch('cart/addItem', { item: cartItem, storeId: storeId });
                            }
                        }
                    }

                    // 跳轉到購物車頁面
                    this.$router.push('/cart');
                    alert('組合商品已加入購物車！');
                } catch (err) {
                    console.error('添加組合商品到購物車失敗:', err);
                    alert('添加商品到購物車失敗：' + err.message);
                }
            },

            // 獲取店家名稱（用於跨店提示）
            getStoreName(storeId) {
                const shop = this.$store.getters['shops/getShopById'](storeId);
                return shop ? shop.name : '其他店家';
            }
        }
    };
</script>

<style scoped>
    .home {
        background-color: #fff;
        padding: 20px;
        font-family: "Microsoft JhengHei", "PingFang TC", "Noto Sans TC", sans-serif;
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
                color: #fff;
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

    /* LOGO + 系統名稱 */
    .header {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 20px;
    }

    .logo-container {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .logo-img {
        width: 40px;
        height: 40px;
    }

    .logo {
        color: #0069D9;
        font-size: 28px;
        font-weight: bold;
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
        cursor: pointer;
    }

        .search-btn svg {
            stroke: white;
            width: 20px;
            height: 20px;
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

    /* 左右滑動區塊 */
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

    /* 滑動按鈕 */
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

    /* 店家卡片 */
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
        cursor: pointer;
    }

        .shop-card:hover {
            transform: scale(1.05);
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
        color: #000; /* 黑色文字 */
        text-decoration: none; /* 去掉底線 */
    }

    /* 商品資訊 */
    .item-info {
        padding: 0 10px;
    }

    .item-store {
        margin-top: 4px;
        font-size: 12px;
        color: #666;
    }

    .item-price {
        margin-top: 4px;
        font-size: 16px;
        font-weight: bold;
        color: #0069D9;
    }

    /* 空狀態訊息 */
    .empty-message {
        text-align: center;
        padding: 40px 20px;
        color: #999;
        font-size: 16px;
    }

        .empty-message p {
            margin: 0;
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

    /* 右下角購物車圖示 */
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
    }

        .cart-btn svg {
            stroke: white;
            width: 28px;
            height: 28px;
        }

    /* 自訂組合樣式 */
    .combo-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
    }

    .add-combo-btn {
        background-color: #0069D9;
        color: white;
        border: none;
        border-radius: 6px;
        padding: 8px 16px;
        cursor: pointer;
        font-size: 14px;
        font-weight: bold;
    }

    .add-combo-btn:hover {
        background-color: #0056b3;
    }

    .empty-add-btn {
        margin-top: 10px;
    }

    /* 組合卡片樣式 - 與其他卡片保持一致 */
    .combo-card {
        position: relative;
        transition: transform 0.3s;
    }

    .combo-card:hover {
        transform: scale(1.05);
    }

    .combo-preview {
        padding: 15px;
        text-align: center;
    }

    .combo-name-preview {
        font-size: 18px;
        font-weight: bold;
        color: #0069D9;
        margin-bottom: 10px;
    }

    .combo-info-preview {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 14px;
    }

    .combo-count {
        color: #666;
    }

    .combo-price {
        color: #0069D9;
        font-weight: bold;
    }

    .combo-actions-overlay {
        position: absolute;
        top: 10px;
        right: 10px;
        display: flex;
        gap: 5px;
        opacity: 0;
        transition: opacity 0.3s;
    }

    .combo-card:hover .combo-actions-overlay {
        opacity: 1;
    }

    .edit-btn, .delete-btn {
        padding: 4px 8px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
        font-weight: bold;
    }

    .edit-btn {
        background-color: rgba(0, 105, 217, 0.9);
        color: white;
    }

    .edit-btn:hover {
        background-color: #0069D9;
    }

    .delete-btn {
        background-color: rgba(220, 53, 69, 0.9);
        color: white;
    }

    .delete-btn:hover {
        background-color: #dc3545;
    }

    /* 組合詳情 Modal */
    .combo-detail-modal {
        background-color: white;
        border-radius: 15px;
        padding: 30px;
        max-width: 600px;
        width: 90%;
        max-height: 80vh;
        overflow-y: auto;
        position: relative;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
    }

    .combo-detail-modal h3 {
        color: #0069D9;
        margin-bottom: 20px;
        text-align: center;
        font-size: 24px;
    }

    .combo-items-detail {
        margin-bottom: 20px;
    }

    .combo-item-detail {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px solid #eee;
    }

    .combo-item-detail:last-child {
        border-bottom: none;
    }

    .item-detail-info {
        flex: 1;
    }

    .item-detail-info .item-name {
        font-weight: bold;
        display: block;
        margin-bottom: 5px;
    }

    .item-customizations {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;
    }

    .custom-tag {
        background-color: #e9ecef;
        color: #495057;
        padding: 2px 6px;
        border-radius: 3px;
        font-size: 11px;
        border: 1px solid #dee2e6;
    }

    .item-price {
        color: #0069D9;
        font-weight: bold;
    }

    .combo-total-detail {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 20px;
        border-top: 2px solid #eee;
        margin-top: 20px;
    }

    .combo-total-detail span {
        font-size: 18px;
        font-weight: bold;
        color: #0069D9;
    }

    .order-combo-btn {
        background-color: #0069D9;
        color: #fff;
        border: none;
        border-radius: 6px;
        padding: 10px 20px;
        cursor: pointer;
        font-size: 16px;
        font-weight: bold;
    }



    .empty-combo-detail {
        text-align: center;
        padding: 40px 20px;
        color: #999;
    }

    .empty-combo-detail p {
        margin-bottom: 15px;
    }

    .add-item-btn {
        background-color: #17a2b8;
        color: white;
        border: none;
        border-radius: 6px;
        padding: 8px 16px;
        cursor: pointer;
        font-size: 14px;
    }

    .add-item-btn:hover {
        background-color: #138496;
    }

    /* 編輯模式樣式 */
    .combo-item-detail.editing {
        background-color: #f8f9fa;
        border: 2px solid #0069D9;
    }

    .item-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 8px;
    }

    .item-actions {
        display: flex;
        gap: 8px;
    }

    .edit-item-btn, .delete-item-btn {
        padding: 4px 8px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
        font-weight: bold;
    }

    .edit-item-btn {
        background-color: #0069D9;
        color: white;
    }



    .delete-item-btn {
        background-color: #dc3545;
        color: white;
    }


    .item-quantity-info {
        margin-top: 8px;
        font-size: 14px;
        color: #666;
    }

    .item-edit-form {
        width: 100%;
    }

    .item-edit-form h4 {
        margin: 0 0 15px 0;
        color: #0069D9;
    }

    .edit-quantity, .edit-customizations {
        margin-bottom: 15px;
    }

    .edit-quantity label, .edit-customizations label {
        display: block;
        margin-bottom: 5px;
        font-weight: bold;
        color: #333;
    }

    .quantity-control {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .quantity-control button {
        width: 30px;
        height: 30px;
        border: none;
        background-color: #0069D9;
        color: white;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        font-weight: bold;
    }

    .quantity-control button:hover {
        background-color: #0056b3;
    }

    .quantity-control input {
        width: 60px;
        padding: 5px;
        text-align: center;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
    }

    .customization-options {
        padding: 10px;
        background-color: #f8f9fa;
        border-radius: 6px;
        border: 1px solid #dee2e6;
    }

    .note {
        margin: 0;
        color: #666;
        font-style: italic;
        font-size: 14px;
    }

    .edit-actions {
        display: flex;
        gap: 10px;
        justify-content: flex-end;
        margin-top: 15px;
    }

    .save-btn, .cancel-btn {
        padding: 8px 16px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 14px;
        font-weight: bold;
    }

    .save-btn {
        background-color: #0069D9;
        color: white;
    }



    .cancel-btn {
        background-color: #ccc;
        color: white;
    }

    /* 組合 Modal 樣式 - 與 user-modal 保持一致 */
    .combo-modal {
        background-color: #fff;
        padding: 20px 30px;
        border-radius: 12px;
        width: 300px;
        max-width: 90%;
        text-align: left;
        box-shadow: 0 5px 15px rgba(0,0,0,0.3);
    }

    .combo-modal.large-modal {
        width: 600px;
        max-height: 80vh;
        overflow-y: auto;
    }

    .combo-modal h3 {
        color: #0069D9;
        margin-bottom: 15px;
    }


</style>
