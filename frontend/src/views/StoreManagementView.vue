<template>
    <div class="home">
        <!-- 遮罩層 -->
        <div v-show="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

        <!-- 左側側邊欄 -->
        <div :class="['sidebar', { open: sidebarOpen }]">
            <div class="sidebar-user" v-if="customer">
                <img class="sidebar-avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user">
                <span class="username">{{ customer.nickname }}, 老闆好</span>
            </div>
            <ul>
                <li @click="openUserModal">使用者資訊</li>
                <router-link to="/store-management"><li>菜單設定</li></router-link>
                <router-link to="/merchant-order"><li>訂單管理</li></router-link>
                <li @click="goToEditStore">編輯店家資訊</li>
            </ul>
            <div class="sidebar-logout">
                <button @click="logout">登出</button>
            </div>
        </div>

        <!-- 左上角顧客頭像 -->
        <img v-if="customer" class="avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user" @click="toggleSidebar">

        <!-- LOGO + 系統名稱 -->
        <header class="header">
            <div class="logo-container">
                <h1 class="logo">菜單管理</h1>
            </div>
        </header>

        <!-- 新增分類和餐點按鈕 -->
        <div class="add-item-section">
            <button class="btn-add-category" @click="openAddCategoryModal">
                <span class="add-icon">+</span>
                新增分類
            </button>
            <button class="btn-add-item" @click="openAddItemModal">
                <span class="add-icon">+</span>
                新增餐點
            </button>
        </div>

        <!-- 搜尋區 -->
        <div class="search-section">
            <input
            type="text"
            class="search-bar"
            placeholder="搜尋餐點…"
            v-model="keyword"
            />
        </div>

        <!-- 動態分類顯示 -->
        <div v-if="categories.length === 0" class="no-category-message">
            <p>尚未新增任何分類</p>
        </div>

        <section v-for="(category, index) in categories" :key="category" class="category-section">
            <div class="category-header">
                <h2 class="category-title">{{ category }}</h2>
                <button class="btn-delete-category" @click="deleteCategory(category)">刪除分類</button>
            </div>
            <div class="slider-container">
                <button class="scroll-btn left" @click="scrollLeft(index)">&#8249;</button>
                <div :ref="'slider' + index" class="slider">
                    <div 
                        v-for="item in getItemsByCategory(category)"
                        :key="item.id"
                        class="menu-item-card"
                        @click="openEditItemModal(item)"
                    >
                        <img :src="item.imgUrl || require('@/assets/logo.png')" class="item-img" alt="餐點圖片">
                        <p class="item-name">{{ item.itemName }}</p>
                        <p class="item-price"> {{ item.price }}元</p>
                    </div>
                </div>
                <button class="scroll-btn right" @click="scrollRight(index)">&#8250;</button>
            </div>
            <div v-if="getItemsByCategory(category).length === 0" class="empty-message">
                尚無{{ category }}類的餐點
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

        <!-- 新增分類 Modal -->
        <div v-if="categoryModalOpen" class="modal-overlay" @click.self="closeCategoryModal">
            <div class="category-modal">
                <h2 color="#0069D9">分類名稱:</h2>
                <div class="form-group">
                    <input type="text" v-model="newCategoryName">
                </div>
                <div class="modal-actions">
                    <button @click="addCategory">新增</button>
                    <button type="button" @click="closeCategoryModal">取消</button>
                </div>
            </div>
        </div>

        <!-- 餐點編輯 Modal -->
        <OwnerMenuItem
            :show="menuItemModalOpen"
            :product="selectedProduct"
            :categories="categories"
            @close="closeMenuItemModal"
            @save="saveMenuItem"
            @delete="deleteItem"
        />

    </div>
</template>

<script>
import OwnerMenuItem from '@/components/OwnerMenuItem.vue';

export default {
    components: {
        OwnerMenuItem
    },
    data() {
        return {
            sidebarOpen: false,
            userModalOpen: false,
            menuItemModalOpen: false,
            categoryModalOpen: false,
            selectedProduct: {
                id: '',
                itemName: '',
                price: 0,
                description: '',
                tag: '',
                isAvailable: true,
                imgUrl: ''
            },
            keyword: "",
            editCustomer: {
                photo: ""
            },
            newCategoryName: "",
            // 餐點資料 (從 API 或 Vuex 取得)
            menuItems: [],
            // 分類資料
            categories: [],
            storeId: null,
            currentStore: null,
        };
    },
    computed: {
        // 使用者資料
        customer() {
            return this.$store.getters['user/customer']
        }
    },
    async mounted() {
        // 載入餐點資料
        await this.loadOwnerStore();
    },
    methods: {

        async loadOwnerStore() {
            try {
                const userId = this.customer.id;
                if (!userId) {
                    alert('請先登入');
                    this.$router.push('/login');
                    return;
                }

                console.log('正在載入店家資料，用戶ID:', userId);
                
                const res = await fetch('http://localhost:8088/api/stores');
                if (!res.ok) {
                    throw new Error(`HTTP ${res.status}: 無法取得店家列表`);
                }
                
                const stores = await res.json();
                console.log('取得店家列表:', stores.length, '家店');

                const myStore = stores.find(store => store.ownerId === userId);
                
                if (!myStore) {
                    console.log('找不到屬於該用戶的店家，可能尚未建立店家');
                    this.menuItems = [];
                    this.categories = [];
                    alert('尚未建立店家，請先到「店家設定」頁面建立店家');
                    return;
                }

                this.storeId = myStore.id;
                this.currentStore = myStore;
                // 保存當前店家資訊到 localStorage，供其他頁面使用
                localStorage.setItem('currentStore', JSON.stringify(myStore));
                this.menuItems = myStore.menu || [];

                // 從菜單項目的 tag 提取分類
                const tags = [...new Set(this.menuItems.map(item => item.tag))];
                this.categories = tags.filter(tag => tag); // 過濾空值

                console.log('載入店家成功:', myStore.name);
                console.log('  - 店家ID:', this.storeId);
                console.log('  - 菜單項目:', this.menuItems.length, '個');
                console.log('  - 分類:', this.categories);
            } catch (err) {
                console.error(' 載入店家失敗:', err);
                alert('載入店家失敗: ' + err.message);
            }
        },

        getItemsByCategory(category) {
            const key = this.keyword.trim().toLowerCase();
            return this.menuItems.filter(item => 
                item.tag === category && 
                (key === '' || item.itemName.toLowerCase().includes(key))
            );
        },

        async loadMenuItems() {
            await this.loadOwnerStore();
        },

        // 分類管理
        openAddCategoryModal() {
            this.newCategoryName = "";
            this.categoryModalOpen = true;
        },

        closeCategoryModal() {
            this.categoryModalOpen = false;
            this.newCategoryName = "";
        },

        addCategory() {
            const categoryName = this.newCategoryName.trim();
            if (!categoryName) {
                alert('請輸入分類名稱');
                return;
            }
            if (this.categories.includes(categoryName)) {
                alert('此分類已存在');
                return;
            }
            // 新增分類到前端列表（分類會在創建餐點時才真正保存到後端）
            this.categories.push(categoryName);
            alert(`分類「${categoryName}」已新增！請創建餐點以保存此分類。`);
            this.closeCategoryModal();
        },

        deleteCategory(category) {
            const itemsInCategory = this.menuItems.filter(item => item.tag === category);
            if (itemsInCategory.length > 0) {
                alert(`無法刪除分類「${category}」：請先刪除該分類下的所有 ${itemsInCategory.length} 個餐點`);
                return;
            }
            if (confirm(`確定要刪除「${category}」分類嗎？`)) {
                const index = this.categories.indexOf(category);
                if (index !== -1) {
                    this.categories.splice(index, 1);
                    alert(`分類「${category}」已刪除`);
                }
            }
        },

        // 餐點管理
        openAddItemModal() {
            if (this.categories.length === 0) {
                alert('請先新增至少一個分類');
                return;
            }
            this.selectedProduct = {
                itemName: '',
                description: '',
                isAvailable: true,
                price: 0,
                tag: this.categories[0],
                imgUrl: '',
                customOptions: []
            };
            this.menuItemModalOpen = true;
        },

        openEditItemModal(item) {
            this.selectedProduct = { ...item };
            this.menuItemModalOpen = true;
        },

        closeMenuItemModal() {
            this.menuItemModalOpen = false;
        },

        async saveMenuItem(updatedItem) {
            try {
                if(!this.storeId){
                    alert('找不到店家');
                    return;
                }
                let isNewItem = !updatedItem.id || updatedItem.id === '';

                if (updatedItem.id && updatedItem.id !== '') {
                    // 編輯現有餐點
                    const index = this.menuItems.findIndex(item => item.id === updatedItem.id);
                    if (index !== -1) {
                        this.menuItems.splice(index, 1, updatedItem);
                    }
                } else {
                    // 新增餐點 - 移除空的 id 字段
                    // eslint-disable-next-line no-unused-vars
                    const { id, ...itemWithoutId } = updatedItem;
                    this.menuItems.push(itemWithoutId);
                }
                
                // 清理菜單項目：移除空的 id 字段
                const cleanedMenuItems = this.menuItems.map(item => {
                    if (!item.id || item.id === '') {
                        // eslint-disable-next-line no-unused-vars
                        const { id, ...rest } = item;
                        return rest;
                    }
                    return item;
                });
                
                // 發送到後端
                const res = await fetch(`http://localhost:8088/api/stores/${this.storeId}/menu`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${this.customer.id}`
                    },
                    body: JSON.stringify(cleanedMenuItems)
                });
                
                if (!res.ok) {
                    const errorText = await res.text();
                    throw new Error(errorText || '儲存失敗');
                }

                const updatedStore = await res.json();
                this.menuItems = updatedStore.menu || [];
                this.currentStore = updatedStore;

                // 重新從菜單項目提取分類（確保分類與餐點同步）
                const tags = [...new Set(this.menuItems.map(item => item.tag))];
                this.categories = tags.filter(tag => tag);

                alert(isNewItem ? '餐點已新增！' : '餐點已更新！');
                console.log('更新後的分類:', this.categories);

            } catch (err) {
                console.error('儲存失敗:', err);
                alert('儲存失敗，請稍後再試: ' + err.message);
            }
        },

        async deleteItem(itemId) {
            try {
                if (!this.storeId) {
                    alert('找不到店家');
                    return;
                }
                
                // 本地刪除
                const index = this.menuItems.findIndex(item => item.id === itemId);
                if (index !== -1) {
                    this.menuItems.splice(index, 1);
                }

                // 發送到後端
                const res = await fetch(`http://localhost:8088/api/stores/${this.storeId}/menu`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${this.customer.id}`
                    },
                    body: JSON.stringify(this.menuItems)
                });

                if (!res.ok) {
                    const errorText = await res.text();
                    throw new Error(errorText || '刪除失敗');
                }

                const updatedStore = await res.json();
                this.menuItems = updatedStore.menu || [];
                
                // 重新從菜單項目提取分類（刪除餐點後，如果某分類沒有餐點了，該分類會自動消失）
                const tags = [...new Set(this.menuItems.map(item => item.tag))];
                this.categories = tags.filter(tag => tag);

                alert('餐點已刪除！');
                console.log('更新後的分類:', this.categories);
                
            } catch (err) {
                console.error('刪除失敗:', err);
                alert('刪除失敗: ' + err.message);
            } 
        },

        toggleSidebar() {
            this.sidebarOpen = !this.sidebarOpen;
        },

        openUserModal() {
            if (this.customer) {
                this.editCustomer = { ...this.customer };
                this.userModalOpen = true;
            }
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

        scrollLeft(index) {
            const slider = this.$refs['slider' + index];
            if (slider && slider[0]) {
                slider[0].scrollBy({ left: -200, behavior: 'smooth' });
            }
        },

        scrollRight(index) {
            const slider = this.$refs['slider' + index];
            if (slider && slider[0]) {
                slider[0].scrollBy({ left: 200, behavior: 'smooth' });
            }
        },

        onAvatarChange(event) {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = e => {
                    this.editCustomer.photo = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        },

        goToEditStore() {
            if (this.storeId) {
                this.$router.push(`/store-setting/${this.storeId}`);
            }
        },

        logout() {
            this.$store.dispatch('user/logout');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            this.$router.push('/login');
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
        min-height: 100vh;
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

    .logo {
        color: #0069D9;
        font-size: 32px;
        font-weight: bold;
    }

    /* 新增餐點按鈕區域 */
    .add-item-section {
        display: flex;
        justify-content: center;
        gap: 15px;
        margin-top: 20px;
    }

    .btn-add-category,
    .btn-add-item {
        background-color: #0069D9;
        color: white;
        border: none;
        padding: 12px 30px;
        border-radius: 10px;
        font-size: 18px;
        font-weight: 600;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 10px;
        transition: background-color 0.3s, transform 0.2s;
    }
    
    .add-icon {
        font-size: 24px;
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
    }

    /* 分類區塊 */
    .category-section {
        margin-top: 30px;
    }

    .category-header {
        display: flex;
        position: relative;
        align-items: center;
        margin-bottom: 10px;
        padding: 0 20px;
    }

    .category-title {
        color: #0069D9;
        font-size: 22px;
        font-weight: bold;
        position: absolute; left: 50%; transform: translateX(-50%)
    }

    .btn-delete-category {
        background-color: #dc3545;
        color: white;
        border: none;
        padding: 6px 14px;
        border-radius: 6px;
        cursor: pointer;
        font-size: 16px;
        transition: background-color 0.3s;
        width: 100px;
        height: 40px;
        position: absolute;
        right: 20px;
        top: 50%;
        transform: translateY(-50%);
    }



    .no-category-message {
        text-align: center;
        padding: 60px 20px;
        color: #666;
        font-size: 18px;
    }

    .no-category-message p {
        margin: 0;
    }

    /* 左右滑動區塊 */
    .slider-container {
        position: relative;
        width: 100%;
        display: flex;
        align-items: center;
        min-height: 150px;
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
        font-size: 24px;
    }

    .scroll-btn.left {
        left: -18px;
    }

    .scroll-btn.right {
        right: -18px;
    }

    /* 菜單項目卡片 */
    .menu-item-card {
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

    .menu-item-card:hover {
        transform: scale(1.05);
    }

    .item-img {
        width: 100%;
        height: 110px;
        border-radius: 12px 12px 0 0;
        object-fit: cover;
        display: block;
    }

    .item-image-placeholder {
        width: 100%;
        height: 110px;
        border-radius: 12px 12px 0 0;
        background-color: #f5f5f5;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #aea5a5;
        font-size: 14px;
        border-bottom: 2px solid #e0e0e0;
    }

    .item-name {
        margin-top: 8px;
        font-weight: bold;
        color: #000;
        font-size: 14px;
        padding: 0 8px;
    }

    .item-price {
        margin-top: 4px;
        font-size: 14px;
        color: #0069D9;
        font-weight: 600;
        padding: 0 8px;
    }

    .empty-message {
        text-align: center;
        padding: 40px 20px;
        color: #999;
        font-size: 16px;
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
            width: 100px;
            height: 40px;
            font-size: 14px;
        }

            .modal-actions button:first-child {
                background-color: #0069D9;
                color: #fff;
            }

            .modal-actions button:last-child {
                background-color: #ccc;
                color: #333;
            }

    .sidebar-logout {
        margin-top: auto;
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

    /* 響應式設計 */
    @media (max-width: 768px) {
        .scroll-btn {
            width: 30px;
            height: 30px;
            font-size: 20px;
        }

        .scroll-btn.left {
            left: -15px;
        }

        .scroll-btn.right {
            right: -15px;
        }

        .menu-item-card {
            min-width: 140px;
        }

        .item-img,
        .item-image-placeholder {
            height: 90px;
        }
    }
</style>
