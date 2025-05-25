// src/router/index.js 
import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import MainDashboardView from '../views/MainDashboardView.vue';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      // Redirect logic: if logged in, go to dashboard, else go to login
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore();
        authStore.checkLoginStatus();
        if (authStore.isLoggedIn) {
          next({ name: 'Dashboard' });
        } else {
          next({ name: 'Login' });
        }
      },
    },
    {
      path: '/login',
      name: 'Login',
      component: LoginView,
      meta: { requiresGuest: true }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: MainDashboardView,
      meta: { requiresAuth: true }
    }
  ],
});

// Navigation Guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  if (!authStore.student && localStorage.getItem('loggedInStudent')) {
      authStore.checkLoginStatus();
  }


  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'Login' });
  } else if (to.meta.requiresGuest && authStore.isLoggedIn) {
    next({ name: 'Dashboard' });
  } else {
    next();
  }
});

export default router;