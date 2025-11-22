// router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import CartView from '@/views/CartView.vue';
import ShopView from '@/views/ShopView.vue';

const routes = [
    { path: '/', name: 'Home', component: HomeView },
    { path: '/cart', name: 'Cart', component: CartView },
    { path: '/shop/:id', name: 'ShopView', component: ShopView, props: true }
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
});

export default router;
