// src/stores/auth.js
import { defineStore } from 'pinia';
import apiService from '@/services/apiService';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    student: null,
    error: null,
    loading: false,
  }),
  getters: {
    isLoggedIn: (state) => !!state.student,
    currentStudentId: (state) => state.student?.id,
    currentStudentName: (state) => state.student?.name,
  },
  actions: {
    async login(name, email) {
      this.loading = true;
      this.error = null;
      try {
        // Replace by real login in the future TODO
        const response = await apiService.getStudents();
        const foundStudent = response.data.find(
          (s) => s.name === name && s.email === email
        );

        if (foundStudent) {
          this.student = foundStudent;
          localStorage.setItem('loggedInStudent', JSON.stringify(foundStudent));
          return true;
        } else {
          this.error = 'Invalid name or email.';
          return false;
        }
      } catch (err) {
        console.error('Login error:', err);
        this.error = 'An error occurred during login. ' + (err.response?.data?.message || err.message);
        return false;
      } finally {
        this.loading = false;
      }
    },
    logout() {
      this.student = null;
      localStorage.removeItem('loggedInStudent');
    },
    checkLoginStatus() {
      const storedStudent = localStorage.getItem('loggedInStudent');
      if (storedStudent) {
        this.student = JSON.parse(storedStudent);
      }
    },
  },
});