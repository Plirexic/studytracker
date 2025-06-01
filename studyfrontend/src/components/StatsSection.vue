<template>
  <section class="stats-section">
    <h2>Your Progress</h2>
    <ProgressBar 
      v-if="totalTasks > 0" 
      :percentage="progressPercentage" 
    />
    <p v-else-if="!loading">No tasks yet to track progress.</p>
    <p v-if="pendingTasks !== null && totalTasks > 0">
      Pending Tasks: <strong>{{ pendingTasks }}</strong>
      ({{ completedTasks }} / {{ totalTasks }} completed)
    </p>
    <p v-else-if="loading">Loading stats...</p>
    <p v-if="error" class="error-message">{{ error }}</p>
  </section>
</template>

<script setup>
import ProgressBar from './ProgressBar.vue';

defineProps({
  totalTasks: {
    type: Number,
    required: true
  },
  completedTasks: {
    type: Number,
    required: true
  },
  pendingTasks: {
    type: Number,
    default: null
  },
  progressPercentage: {
    type: Number,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  }
});
</script>

<style scoped>
.stats-section {
  padding: 20px;
  background-color: #f8f9fa; 
  border: 1px solid #dee2e6; 
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
}

.stats-section h2 {
  color: #343a40;
  margin-top: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
  font-size: 1.3em; 
  margin-bottom: 15px;
  flex-shrink: 0;
}

.error-message { 
  color: #dc3545; 
  margin-top: 6px; 
  font-size: 0.85em; 
  text-align: left; 
}
</style>