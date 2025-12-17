// router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import ShopView from '@/views/ShopView.vue';
import CartView from '@/views/CartView.vue';
import LoginView from '@/views/LoginView.vue';
import RegisterView from '@/views/RegisterView.vue';
import FavoriteView from '@/views/FavoriteView.vue';
import nologinhome from '@/views/nologinhome.vue';
import nologincart from '@/views/nologincart.vue';
import nologinshop from '@/views/nologinshop.vue';
import OrderView from '@/views/OrderView.vue';
import StoreSetting from '@/views/StoreSetting.vue';
const routes = [
    { path: '/', name: 'Root', component: LoginView },
    { path: '/login', component: LoginView },
    { path: '/home', component: HomeView, meta: { requiresAuth: true, role: 'buyer' } },
    { path: '/cart', name: 'Cart', component: CartView },
    { path: '/shop/:id', name: 'ShopView', component: ShopView, props: true },
    { path: '/register', name: 'Register', component: RegisterView },
    { path: '/favorite', name: 'Favorite', component: FavoriteView },
    { path: '/nologinhome', name: 'nologinhome', component: nologinhome },
    { path: '/nologincart', name: 'nologincart', component: nologincart },
    { path: '/nologinshop/:id', name: 'nologinshop', component: nologinshop, props: true },
    { path: '/order', name: 'OrderView', component: OrderView },
    { path: '/store-setting', name: 'StoreSetting', component: StoreSetting, meta: { requiresAuth: true, role: 'owner' } },
];

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 導航守衛：未登入就導向 login
import store from '@/store'

router.beforeEach((to, from, next) => {
    const isLoggedIn = store.state.user.customer.isLoggedIn || !!localStorage.getItem('user')
    const userRole = store.state.user.customer.role || (localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).role : null)

    // 根據角色決定首頁
    const getHomePage = (role) => {
        if (role === 'owner') {
            return '/store-setting'
        } else if (role === 'buyer') {
            return '/home'
        } else {
            return '/home'  // 預設為顧客首頁
        }
    }

    if (to.meta.requiresAuth && !isLoggedIn) {
        next('/login')  // 需要登入但未登入 → 導向 /login
    } else if ((to.path === '/login' || to.path === '/') && isLoggedIn) {
        // 已登入 → 登入角色對應首頁
        next(getHomePage(userRole))
    } else {//其他所有情況
        next()
    }
})


export default router
