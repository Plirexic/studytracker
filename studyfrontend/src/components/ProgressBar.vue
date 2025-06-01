<template>
  <div class="progress-bar-container">
    <div
      class="progress-bar-fill"
      :style="{ width: percentage + '%' }"
      :class="{ 'full-rounded': percentage === 100 }"
    >
      <span v-if="percentage > 10">{{ Math.round(percentage) }}%</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  percentage: {
    type: Number,
    required: true,
    default: 0
  }
});
</script>

<style scoped>
.progress-bar-container {
  width: 100%;
  height: 28px; 
  background-color: #e9ecef; 
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dee2e6;
  margin-bottom: 15px;
}

.progress-bar-fill {
  height: 100%;
  background-color: #007bff;
  border-radius: 6px 0 0 6px; 
  transition: width 0.4s ease-in-out; 
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 500; 
  font-size: 0.85em;
}

.progress-bar-fill.full-rounded { 
  border-radius: 6px; 
}
</style>