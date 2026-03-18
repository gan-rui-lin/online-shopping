import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { setupGuards } from './guards'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/public/HomeView.vue'),
        meta: { title: 'Home' },
      },
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/public/LoginView.vue'),
        meta: { title: 'Login', guest: true },
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/public/RegisterView.vue'),
        meta: { title: 'Register', guest: true },
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/public/ProductListView.vue'),
        meta: { title: 'Products' },
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/public/ProductDetailView.vue'),
        meta: { title: 'Product Detail' },
      },
    ],
  },
  {
    path: '/buyer',
    component: () => import('@/layout/BuyerLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'profile',
        name: 'BuyerProfile',
        component: () => import('@/views/buyer/ProfileView.vue'),
        meta: { title: 'My Profile' },
      },
      {
        path: 'addresses',
        name: 'BuyerAddresses',
        component: () => import('@/views/buyer/AddressView.vue'),
        meta: { title: 'My Addresses' },
      },
      {
        path: 'cart',
        name: 'BuyerCart',
        component: () => import('@/views/buyer/CartView.vue'),
        meta: { title: 'Shopping Cart' },
      },
      {
        path: 'orders',
        name: 'BuyerOrders',
        component: () => import('@/views/buyer/OrderListView.vue'),
        meta: { title: 'My Orders' },
      },
      {
        path: 'orders/:orderNo',
        name: 'BuyerOrderDetail',
        component: () => import('@/views/buyer/OrderDetailView.vue'),
        meta: { title: 'Order Detail' },
      },
      {
        path: 'review/:orderItemId',
        name: 'BuyerReviewCreate',
        component: () => import('@/views/buyer/ReviewCreateView.vue'),
        meta: { title: 'Write Review' },
      },
      {
        path: 'merchant-apply',
        name: 'MerchantApply',
        component: () => import('@/views/buyer/MerchantApplyView.vue'),
        meta: { title: 'Apply as Merchant' },
      },
    ],
  },
  {
    path: '/merchant',
    component: () => import('@/layout/MerchantLayout.vue'),
    meta: { requiresAuth: true, requiredRole: 'ROLE_MERCHANT' },
    children: [
      {
        path: '',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/DashboardView.vue'),
        meta: { title: 'Merchant Dashboard' },
      },
      {
        path: 'shop',
        name: 'MerchantShopInfo',
        component: () => import('@/views/merchant/ShopInfoView.vue'),
        meta: { title: 'Shop Info' },
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/ProductListView.vue'),
        meta: { title: 'Product Management' },
      },
      {
        path: 'products/create',
        name: 'MerchantProductCreate',
        component: () => import('@/views/merchant/ProductCreateView.vue'),
        meta: { title: 'Create Product' },
      },
      {
        path: 'products/edit/:id',
        name: 'MerchantProductEdit',
        component: () => import('@/views/merchant/ProductCreateView.vue'),
        meta: { title: 'Edit Product' },
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/OrderListView.vue'),
        meta: { title: 'Merchant Orders' },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiredRole: 'ROLE_ADMIN' },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: 'Admin Dashboard' },
      },
      {
        path: 'merchants',
        name: 'AdminMerchantAudit',
        component: () => import('@/views/admin/MerchantAuditView.vue'),
        meta: { title: 'Merchant Audit' },
      },
      {
        path: 'products',
        name: 'AdminProductAudit',
        component: () => import('@/views/admin/ProductAuditView.vue'),
        meta: { title: 'Product Audit' },
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/CategoryManageView.vue'),
        meta: { title: 'Category Management' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/public/NotFoundView.vue'),
    meta: { title: '404 Not Found' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

setupGuards(router)

export default router
