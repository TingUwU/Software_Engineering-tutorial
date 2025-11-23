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
                <li @click="openUserModal">使用者資訊</li>
                <router-link to="/cart"><li>購物車</li></router-link>
                <li>訂單管理</li>
                <li>歷史</li>
                <li>收藏</li>
            </ul>
        </div>

        <!-- 左上角顧客頭像 -->
        <img class="avatar" :src="customer.photo || require('@/assets/logo.png')" alt="user" @click="toggleSidebar">

        <!-- LOGO + 系統名稱 -->
        <header class="header">
            <div class="logo-container">
                <h1 class="logo">店家一覽</h1>
            </div>
        </header>

        <!-- 搜尋區 -->
        <div class="search-section">
            <input type="text" class="search-bar" placeholder="搜尋餐廳…" v-model="keyword">
            <button class="search-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
            </button>
        </div>

        <!-- 中式店家 -->
        <section class="category-section">
            <h2 class="category-title">中式</h2>
            <div class="slider-container">
                <button class="scroll-btn left" @click="scrollLeft('chinese')">&#8249;</button>
                <div ref="chineseSlider" class="slider">
                    <router-link v-for="shop in chineseShops"
                                 :key="shop.id"
                                 :to="{ name: 'ShopView', params: { id: shop.id } }"
                                 class="shop-card">
                        <img :src="shop.menu[0]?.imgUrl || require('@/assets/logo.png')" class="shop-img" alt="店家圖片">
                        <p class="shop-name">{{ shop.name }}</p>
                    </router-link>
                </div>
                <button class="scroll-btn right" @click="scrollRight('chinese')">&#8250;</button>
            </div>
        </section>

        <!-- 西式店家 -->
        <section class="category-section">
            <h2 class="category-title">西式</h2>
            <div class="slider-container">
                <button class="scroll-btn left" @click="scrollLeft('western')">&#8249;</button>
                <div ref="westernSlider" class="slider">
                    <router-link v-for="shop in westernShops"
                                 :key="shop.id"
                                 :to="{ name: 'ShopView', params: { id: shop.id } }"
                                 class="shop-card">
                        <img :src="shop.menu[0]?.imgUrl || require('@/assets/logo.png')" class="shop-img" alt="店家圖片">
                        <p class="shop-name">{{ shop.name }}</p>
                    </router-link>
                </div>
                <button class="scroll-btn right" @click="scrollRight('western')">&#8250;</button>
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

    </div>
</template>

<script>
    export default {
        data() {
            return {
                sidebarOpen: false,
                userModalOpen: false,
                menuItemModalOpen: false,
                selectedProduct: {
                    id: '',
                    itemName: '',
                    price: 0
                },
                keyword: "",
                editCustomer: {
                    photo: ""
                }
            };
        },
        computed: {
            //獲取用戶資料
            customer() {
                return this.$store.getters['user/customer']
            },
            // 獲取所有店家資料
            allShops() {
                return this.$store.getters['shops/allShops']
            },
            // 中式店家（根據 ID 過濾）
            chineseShops() {
                const chineseIds = ['store001', 'store002', 'c1', 'c2'];
                return this.allShops.filter(shop => chineseIds.includes(shop.id));
            },
            // 西式店家（根據 ID 過濾）
            westernShops() {
                const westernIds = ['store003', 'store004', 'w1', 'w2'];
                return this.allShops.filter(shop => westernIds.includes(shop.id));
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
            updateUser() {
                this.$store.dispatch('user/updateCustomer', this.editCustomer);
                this.closeUserModal();
                alert('使用者資訊已更新！');
            },
            scrollLeft(type) {
                const slider = type === 'chinese' ? this.$refs.chineseSlider : this.$refs.westernSlider;
                slider.scrollBy({ left: -200, behavior: 'smooth' });
            },
            scrollRight(type) {
                const slider = type === 'chinese' ? this.$refs.chineseSlider : this.$refs.westernSlider;
                slider.scrollBy({ left: 200, behavior: 'smooth' });
            },
            goToCart(){
                this.$router.push('/cart');
            },
           
            closeMenuItemModal() {
                this.menuItemModalOpen = false;
            },
            handleAddToCart(cartItem) {
                console.log('加入購物車:', cartItem);
                this.$store.dispatch('cart/addItem', cartItem);
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
</style>
