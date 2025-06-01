<template>
  <section class="tasks-section">
    <h2>Your Tasks</h2>
    <div v-if="loading && !tasks.length">Loading tasks...</div>
    <ul v-if="tasks.length" class="task-list">
      <TaskItem
        v-for="task in tasks"
        :key="task.id"
        :task="task"
        :update-loading="updateLoadingId === task.id"
        :delete-loading="deleteLoadingId === task.id"
        @update-task="handleUpdateTask"
        @delete-task="handleDeleteTask"
        @toggle-complete="handleToggleComplete"
      />
    </ul>
    <p v-else-if="!loading && !error">No tasks found. Create one!</p>
    <p v-if="error" class="error-message">{{ error }}</p>
  </section>
</template>

<script setup>
import TaskItem from './TaskItem.vue';

defineProps({
  tasks: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  },
  updateLoadingId: {
    type: [Number, String],
    default: null
  },
  deleteLoadingId: {
    type: [Number, String],
    default: null
  }
});

const emit = defineEmits(['update-task', 'delete-task', 'toggle-complete']);

const handleUpdateTask = (taskData) => {
  emit('update-task', taskData);
};

const handleDeleteTask = (taskId) => {
  emit('delete-task', taskId);
};

const handleToggleComplete = (task) => {
  emit('toggle-complete', task);
};
</script>

<style scoped>
.tasks-section {
  padding: 20px;
  background-color: #f8f9fa; 
  border: 1px solid #dee2e6; 
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  overflow: hidden;
  margin-bottom: 0;
}

.tasks-section h2 {
  color: #343a40;
  margin-top: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
  font-size: 1.3em; 
  margin-bottom: 15px;
  flex-shrink: 0;
}

.task-list {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
  flex-grow: 1;
  padding-right: 8px;
}

.error-message { 
  color: #dc3545; 
  margin-top: 6px; 
  font-size: 0.85em; 
  text-align: left; 
}
</style>