<template>
    <div class="cart">
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

        <!-- 標題 -->
        <header class="header">
            <h1 class="logo">購物車</h1>
        </header>

        <!-- 購物車橫向滑動卡片 -->
        <div class="slider-container">
            <button class="scroll-btn left" @click="scrollLeft">&#8249;</button>
            <div ref="cartSlider" class="slider">
                <div v-for="(item,index) in cartItems" :key="item._id" class="cart-item-card">
                    <img :src="item.imgUrl || require('@/assets/logo.png')" class="cart-img" alt="餐點">
                    <p class="cart-name">{{ item.itemName }}</p>
                    <p class="cart-custom" v-if="item.customDesc">{{ item.customDesc }}</p>
                    <div class="cart-qty">
                        <button @click="decreaseQty(index)">-</button>
                        <span>{{ item.quantity }}</span>
                        <button @click="increaseQty(index)">+</button>
                    </div>
                    <p class="cart-subtotal">{{ item.quantity * item.price }} 元</p>
                    <button class="cart-delete" @click="removeItem(index)">🗑</button>
                </div>
            </div>
            <button class="scroll-btn right" @click="scrollRight">&#8250;</button>
        </div>

        <!-- 備註 -->
        <div class="cart-remark">
            <label>備註:</label>
            <input type="text" v-model="remark" placeholder="輸入備註…">
        </div>

        <!-- 總金額 -->
        <div class="cart-total">
            <span>總金額: </span>
            <span>{{ totalAmount }} 元</span>
        </div>

        <!-- 用餐方式 & 支付 -->
        <div class="cart-options">
            <details>
                <summary>用餐方式</summary>
                <div class="eat-method">
                    <label><input type="radio" value="dinein" v-model="eatMethod"> 內用</label>
                    <input v-if="eatMethod==='dinein'" type="text" v-model="tableNumber" placeholder="桌號">
                    <label><input type="radio" value="takeout" v-model="eatMethod"> 外帶</label>
                    <input v-if="eatMethod==='takeout'" type="time" v-model="pickupTime">
                </div>
            </details>

            <div class="pay-method">
                <label>支付方式:</label>
                <select v-model="payMethod">
                    <option value="cash">現金</option>
                    <option value="credit">信用卡</option>
                    <option value="linepay">LINE Pay</option>
                </select>
            </div>
        </div>

        <!-- 操作按鈕 -->
        <div class="cart-actions">
            <router-link to="/home" class="btn">繼續點餐</router-link>
            <router-link to="/checkout" class="btn primary">前往結帳</router-link>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                sidebarOpen: false,
                customer: { nickname: "使用者名稱", photo: "" },
                cartItems: [
                    { _id: '1', itemName: "豆漿", quantity: 1, price: 25, imgUrl: "", customDesc: "少糖" },
                    { _id: '2', itemName: "油條", quantity: 2, price: 15, imgUrl: "", customDesc: "" },
                    { _id: '3', itemName: "飯糰", quantity: 1, price: 30, imgUrl: "", customDesc: "多辣" }
                ],
                remark: "",
                eatMethod: "dinein",
                tableNumber: "",
                pickupTime: "",
                payMethod: "cash"
            };
        },
        computed: {
            totalAmount() {
                return this.cartItems.reduce((sum, item) => sum + item.quantity * item.price, 0);
            }
        },
        methods: {
            toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; },
            increaseQty(index) { this.cartItems[index].quantity++; },
            decreaseQty(index) { if (this.cartItems[index].quantity > 1) this.cartItems[index].quantity--; },
            removeItem(index) { this.cartItems.splice(index, 1); },
            scrollLeft() {
                const slider = this.$refs.cartSlider;
                slider.scrollBy({ left: -200, behavior: 'smooth' });
            },
            scrollRight() {
                const slider = this.$refs.cartSlider;
                slider.scrollBy({ left: 200, behavior: 'smooth' });
            }
        }
    };
</script>

<style scoped>
    .cart {
        padding: 20px;
        font-family: "Microsoft JhengHei",sans-serif;
        position: relative;
    }

    .slider-container {
        position: relative;
        display: flex;
        align-items: center;
        margin: 20px 0;
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

    .cart-item-card {
        min-width: 180px;
        flex-shrink: 0;
        border-radius: 12px;
        background: #fff;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        text-align: center;
        padding: 10px;
        position: relative;
    }

    .cart-img {
        width: 100%;
        height: 110px;
        border-radius: 12px 12px 0 0;
        object-fit: cover;
    }

    .cart-name {
        margin-top: 8px;
        font-weight: bold;
        color: #000;
    }

    .cart-custom {
        font-size: 14px;
        color: #666;
    }

    .cart-qty {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 5px;
        margin-top: 5px;
    }

        .cart-qty button {
            width: 28px;
            height: 28px;
        }

    .cart-subtotal {
        font-weight: bold;
        margin-top: 5px;
    }

    .cart-delete {
        position: absolute;
        top: 5px;
        right: 5px;
        background: none;
        border: none;
        font-size: 20px;
        color: #999;
        cursor: pointer;
    }

    .cart-remark {
        margin: 15px 0;
    }

        .cart-remark input {
            width: 100%;
            padding: 6px;
            border-radius: 6px;
            border: 1px solid #ccc;
        }

    .cart-total {
        font-weight: bold;
        font-size: 18px;
        margin-bottom: 10px;
    }

    .cart-actions {
        display: flex;
        gap: 10px;
        margin-top: 10px;
    }

    .btn {
        padding: 10px 20px;
        border-radius: 6px;
        background-color: #ccc;
        color: #333;
        text-align: center;
        text-decoration: none;
    }

        .btn.primary {
            background-color: #0069D9;
            color: #fff;
        }

    .cart-options {
        margin: 15px 0;
    }

    .eat-method {
        display: flex;
        gap: 15px;
        align-items: center;
        margin-top: 5px;
    }

    .pay-method {
        margin-top: 10px;
    }
</style>
