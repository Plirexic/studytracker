<template>
  <section class="create-task-wrapper">
    <h2>Create New Task</h2>
    <div class="create-task-form">
      <input 
        type="text" 
        v-model="taskData.title" 
        placeholder="Task Title" 
      />
      <textarea 
        v-model="taskData.description" 
        placeholder="Task Description (Optional)"
      ></textarea>
      <input 
        type="date" 
        v-model="taskData.dueDate"
        :min="todayDate"
        @change="validateDueDate" 
      />
      <p v-if="dueDateError" class="error-message">{{ dueDateError }}</p>
      <button 
        @click="handleSubmit" 
        :disabled="loading || !!dueDateError"
      >
        {{ loading ? 'Creating...' : 'Add Task' }}
      </button>
      <p v-if="error" class="error-message">{{ error }}</p>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { getTodayDateString, isValidFutureDate } from '@/utils/dateUtils';

const emit = defineEmits(['create-task']);

defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  }
});

const taskData = reactive({
  title: '',
  description: '',
  dueDate: null,
  completed: false
});

const todayDate = ref(getTodayDateString());
const dueDateError = ref(null);

const validateDueDate = () => {
  if (taskData.dueDate && !isValidFutureDate(taskData.dueDate)) {
    dueDateError.value = 'Due date cannot be in the past.';
  } else if (!taskData.dueDate) { // Check if it was cleared
    dueDateError.value = 'Due date is required.';
  }
  else {
    dueDateError.value = null;
  }
};

const handleSubmit = () => {
  if (!taskData.title.trim()) {
    alert('Task title is required.');
    return;
  }
  if (!taskData.dueDate) {
    dueDateError.value = 'Due date is required.';
    return;
  }
  validateDueDate();
  if (dueDateError.value) {
    return;
  }
  
  emit('create-task', { ...taskData });
  
  // Reset form
  taskData.title = '';
  taskData.description = '';
  taskData.dueDate = null;
};
</script>

<style scoped>
.create-task-wrapper {
  padding: 20px;
  background-color: #f8f9fa; 
  border: 1px solid #dee2e6; 
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
}

.create-task-wrapper h2 {
  color: #343a40;
  margin-top: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
  font-size: 1.3em; 
  margin-bottom: 15px;
  flex-shrink: 0;
}

.create-task-form {
  padding: 15px;
  background-color: #fff;
  border: 1px solid #ced4da;
  border-radius: 6px;
}

.create-task-form input[type="text"],
.create-task-form textarea,
.create-task-form input[type="date"] {
  width: 100%; 
  padding: 10px; 
  margin-bottom: 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 0.95em;
}

.create-task-form input[type="text"]:focus,
.create-task-form textarea:focus,
.create-task-form input[type="date"]:focus {
  border-color: #80bdff; 
  outline: 0; 
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}

.create-task-form textarea { 
  resize: vertical; 
  min-height: 60px; 
}

.create-task-form button {
  width: 100%;
  padding: 10px 15px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  font-size: 1em;
  transition: background-color 0.2s ease;
}

.create-task-form button:hover { 
  background-color: #218838; 
}

.create-task-form button:disabled { 
  background-color: #adb5bd; 
  cursor: not-allowed; 
}

.error-message { 
  color: #dc3545; 
  margin-top: 6px; 
  font-size: 0.85em; 
  text-align: left; 
}

.create-task-form .error-message { 
  margin-bottom: 8px; 
}
</style>