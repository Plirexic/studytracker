<template>
  <div class="login-container">
    <h2>Studytracker LOGIN</h2>
    <form @submit.prevent="handleLogin" class="login-form">
      <div class="form-group">
        <label for="name">Name:</label>
        <input type="text" id="name" v-model="name" required />
      </div>
      <div class="form-group">
        <label for="email">Email:</label>
        <input type="email" id="email" v-model="email" required />
      </div>
      <button type="submit" :disabled="authStore.loading">
        {{ authStore.loading ? 'Logging in...' : 'Login' }}
      </button>
      <p v-if="authStore.error" class="error-message">{{ authStore.error }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const name = ref('');
const email = ref('');
const router = useRouter();
const authStore = useAuthStore();

const handleLogin = async () => {
  const success = await authStore.login(name.value, email.value);
  if (success) {
    router.push({ name: 'Dashboard' });
  }
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
h2 {
  text-align: center;
  margin-bottom: 20px;
}
.login-form .form-group {
  margin-bottom: 15px;
}
.login-form label {
  display: block;
  margin-bottom: 5px;
}
.login-form input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}
.login-form button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
.login-form button:disabled {
  background-color: #ccc;
}
.error-message {
  color: red;
  text-align: center;
  margin-top: 10px;
}
</style>