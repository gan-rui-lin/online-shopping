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
        meta: { titleKey: 'routeTitle.home' },
      },
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/public/LoginView.vue'),
        meta: { titleKey: 'routeTitle.login', guest: true },
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/public/RegisterView.vue'),
        meta: { titleKey: 'routeTitle.register', guest: true },
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/public/ProductListView.vue'),
        meta: { titleKey: 'routeTitle.productList' },
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/public/ProductDetailView.vue'),
        meta: { titleKey: 'routeTitle.productDetail' },
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
        meta: { titleKey: 'routeTitle.buyerProfile' },
      },
      {
        path: 'addresses',
        name: 'BuyerAddresses',
        component: () => import('@/views/buyer/AddressView.vue'),
        meta: { titleKey: 'routeTitle.buyerAddresses' },
      },
      {
        path: 'cart',
        name: 'BuyerCart',
        component: () => import('@/views/buyer/CartView.vue'),
        meta: { titleKey: 'routeTitle.buyerCart' },
      },
      {
        path: 'orders',
        name: 'BuyerOrders',
        component: () => import('@/views/buyer/OrderListView.vue'),
        meta: { titleKey: 'routeTitle.buyerOrders' },
      },
      {
        path: 'orders/:orderNo',
        name: 'BuyerOrderDetail',
        component: () => import('@/views/buyer/OrderDetailView.vue'),
        meta: { titleKey: 'routeTitle.buyerOrderDetail' },
      },
      {
        path: 'review/:orderItemId',
        name: 'BuyerReviewCreate',
        component: () => import('@/views/buyer/ReviewCreateView.vue'),
        meta: { titleKey: 'routeTitle.buyerReviewCreate' },
      },
      {
        path: 'merchant-apply',
        name: 'MerchantApply',
        component: () => import('@/views/buyer/MerchantApplyView.vue'),
        meta: { titleKey: 'routeTitle.merchantApply' },
      },
      {
        path: 'intelligence',
        name: 'BuyerIntelligenceHub',
        component: () => import('@/views/buyer/IntelligenceHubView.vue'),
        meta: { titleKey: 'routeTitle.intelligenceHub' },
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
        meta: { titleKey: 'routeTitle.merchantDashboard' },
      },
      {
        path: 'shop',
        name: 'MerchantShopInfo',
        component: () => import('@/views/merchant/ShopInfoView.vue'),
        meta: { titleKey: 'routeTitle.merchantShopInfo' },
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/ProductListView.vue'),
        meta: { titleKey: 'routeTitle.merchantProducts' },
      },
      {
        path: 'products/create',
        name: 'MerchantProductCreate',
        component: () => import('@/views/merchant/ProductCreateView.vue'),
        meta: { titleKey: 'routeTitle.merchantProductCreate' },
      },
      {
        path: 'products/edit/:id',
        name: 'MerchantProductEdit',
        component: () => import('@/views/merchant/ProductCreateView.vue'),
        meta: { titleKey: 'routeTitle.merchantProductEdit' },
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/OrderListView.vue'),
        meta: { titleKey: 'routeTitle.merchantOrders' },
      },
      {
        path: 'intelligence',
        name: 'MerchantIntelligence',
        component: () => import('@/views/merchant/IntelligenceView.vue'),
        meta: { titleKey: 'routeTitle.merchantIntelligence' },
      },
      {
        path: 'reviews',
        name: 'MerchantReviews',
        component: () => import('@/views/merchant/ReviewManageView.vue'),
        meta: { titleKey: 'routeTitle.merchantReviews' },
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
        meta: { titleKey: 'routeTitle.adminDashboard' },
      },
      {
        path: 'members',
        name: 'AdminMembers',
        component: () => import('@/views/admin/MemberManageView.vue'),
        meta: { titleKey: 'routeTitle.adminMembers' },
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/OrderInterventionView.vue'),
        meta: { titleKey: 'routeTitle.adminOrders' },
      },
      {
        path: 'merchants',
        name: 'AdminMerchantAudit',
        component: () => import('@/views/admin/MerchantAuditView.vue'),
        meta: { titleKey: 'routeTitle.adminMerchantAudit' },
      },
      {
        path: 'products',
        name: 'AdminProductAudit',
        component: () => import('@/views/admin/ProductAuditView.vue'),
        meta: { titleKey: 'routeTitle.adminProductAudit' },
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/CategoryManageView.vue'),
        meta: { titleKey: 'routeTitle.adminCategories' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/public/NotFoundView.vue'),
    meta: { titleKey: 'routeTitle.notFound' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

setupGuards(router)

export default router
