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
import nologinorder from '@/views/nologinorder.vue';
import OrderView from '@/views/OrderView.vue';
import StoreSetting from'@/views/StoreSetting.vue';
import StoreManagementView from'@/views/StoreManagementView.vue';
import MerchantOrderView from'@/views/MerchantOrderView.vue';
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
    { path: '/nologinorder', name: 'nologinorder', component: nologinorder },
    { path: '/nologinshop/:id', name: 'nologinshop', component: nologinshop, props: true },
    { path: '/order', name: 'OrderView', component: OrderView },
    { path: '/store-setting', name: 'StoreSetting', component: StoreSetting },
    { path: '/store-setting/:id', name: 'StoreSettingEdit', component: StoreSetting, props: true },
    { path: '/store-management', name: 'StoreManagementView', component: StoreManagementView },
    { path: '/merchant-order', name: 'MerchantOrderView', component: MerchantOrderView },
];

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 導航守衛：未登入就導向 login
import store from '@/store'

// router.beforeEach((to, from, next) => {
//     const isLoggedIn = store.state.user.isLoggedIn || !!sessionStorage.getItem('user')
//     const user = JSON.parse(sessionStorage.getItem('user'))
    
//     if (to.meta.requiresAuth && !isLoggedIn) {
//         next('/login')  // 需要登入但未登入 → 導向 /login
//     } else if (to.path === '/login' && isLoggedIn&&user.role === 'buyer') {
//         next('/home')   
//     } else if (to.path === '/login' && isLoggedIn&&user.role === 'owner') {
//         next('/store-management')   
//     } else {
//         next()
//     }
// })

router.beforeEach(async (to, from, next) => {
    // 1. 取得目前的登入狀態 (優先看 Vuex，沒有才看 SessionStorage)
    let isLoggedIn = store.state.user.isLoggedIn || !!sessionStorage.getItem('user')
    let user = store.state.user.customer || JSON.parse(sessionStorage.getItem('user'))

    // 2. 針對「需要權限」的頁面 (Requires Auth)
    if (to.meta.requiresAuth) {
        // 如果本地看起來沒登入 (例如剛從 Google 跳回來，Vuex 是空的)
        if (!isLoggedIn) {
            console.log('本地無登入資料，嘗試向後端檢查 Session...')
            
            // 🔥 關鍵步驟：呼叫 user.js 裡的 checkLoginStatus 去問後端
            const isValidSession = await store.dispatch('user/checkLoginStatus')
            
            if (isValidSession) {
                // 後端說：Session 有效！(Vuex 資料已經在 action 裡補上了)
                next() // 放行
                return
            } else {
                // 後端說：沒登入或過期
                next('/login') // 踢回登入頁
                return
            }
        }
    }

    // 3. 針對「已登入」卻想去「登入頁」的情況 (防止重複登入)
    if (to.path === '/login' && isLoggedIn) {
        // 重新抓一次 user (因為剛剛 checkLoginStatus 可能更新了它)
        user = store.state.user.customer || JSON.parse(sessionStorage.getItem('user'))
        
        if (user && user.role === 'owner') {
            next('/store-management')
        } else {
            next('/home')
        }
        return
    }

    // 4. 其他情況 (公開頁面，或是已登入且有權限) -> 直接放行
    next()
})

export default router
