<template>
  <div id="app-container">
    <RouterView />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { onMounted } from 'vue';

const authStore = useAuthStore();

onMounted(() => {
  authStore.checkLoginStatus();
});
</script>

<style>
*, *::before, *::after {
  box-sizing: border-box;
}

body, html {
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  font-family: Arial, sans-serif;
  background-color: #f4f7f6;
  color: #333;
  overflow-x: hidden;
}

#app-container {
  width: 50vw;
  min-height: 100vh;
  justify-content: center;
}

button {
  cursor: pointer;
  font-family: inherit;
}
</style>