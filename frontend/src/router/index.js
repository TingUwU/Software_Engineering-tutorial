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
const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    { path: '/home', component: HomeView, meta: { requiresAuth: true } },
    { path: '/cart', name: 'Cart', component: CartView },
    { path: '/shop/:id', name: 'ShopView', component: ShopView, props: true },
    { path: '/register', name: 'Register', component: RegisterView },
    { path: '/favorite', name: 'Favorite', component: FavoriteView },
    { path: '/nologinhome', name: 'nologinhome', component: nologinhome },
    { path: '/nologincart', name: 'nologincart', component: nologincart },
    { path: '/nologinshop/:id', name: 'nologinshop', component: nologinshop, props: true },
    { path: '/order', name: 'OrderView', component: OrderView },
];

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 導航守衛：未登入就導向 login
import store from '@/store'

router.beforeEach((to, from, next) => {
    const isLoggedIn = store.state.user.isLoggedIn || !!localStorage.getItem('user')

    if (to.meta.requiresAuth && !isLoggedIn) {
        next('/nologinhome')  // 需要登入但未登入 → 導向 /login
    } else if (to.path === '/login' && isLoggedIn) {
        next('/home')   // 已登入 → 導向 /home
    } else {
        next()
    }
})


export default router
