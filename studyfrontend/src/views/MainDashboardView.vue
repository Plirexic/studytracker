<template>
  <div class="dashboard-container" v-if="authStore.isLoggedIn">
    <header class="dashboard-header">
      <h1>Welcome, {{ authStore.currentStudentName }}!</h1>
      <button @click="handleLogout" class="logout-button">Logout</button>
    </header>

    <div class="dashboard-main-content">
      <!-- Left Column -->
      <div class="left-column">
        <StatsSection
          :total-tasks="totalTasksCount"
          :completed-tasks="completedTasksCount"
          :pending-tasks="pendingTasksCount"
          :progress-percentage="progressPercentage"
          :loading="statsLoading"
          :error="statsError"
        />

        <TaskForm
          :loading="createLoading"
          :error="taskFormError"
          @create-task="handleCreateTask"
        />
      </div>

      <!-- Right Column -->
      <div class="right-column">
        <TaskList
          :tasks="tasks"
          :loading="listLoading"
          :error="listError"
          :update-loading-id="updateLoadingId"
          :delete-loading-id="deleteLoadingId"
          @update-task="handleUpdateTask"
          @delete-task="handleDeleteTask"
          @toggle-complete="handleToggleComplete"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import apiService from '@/services/apiService';

// Component imports
import StatsSection from '@/components/StatsSection.vue';
import TaskForm from '@/components/TaskForm.vue';
import TaskList from '@/components/TaskList.vue';

const router = useRouter();
const authStore = useAuthStore();

// State
const tasks = ref([]);
const pendingTasksCount = ref(null);

// Loading states
const listLoading = ref(false);
const createLoading = ref(false);
const updateLoadingId = ref(null);
const deleteLoadingId = ref(null);
const statsLoading = ref(false);

// Error states
const listError = ref(null);
const statsError = ref(null);
const taskFormError = ref(null);

// Computed properties
const studentId = computed(() => authStore.currentStudentId);
const totalTasksCount = computed(() => tasks.value.length);
const completedTasksCount = computed(() => {
  return tasks.value.filter(task => task.completed).length;
});
const progressPercentage = computed(() => {
  if (totalTasksCount.value === 0) return 0;
  return (completedTasksCount.value / totalTasksCount.value) * 100;
});

// Methods
const fetchStudentData = async () => {
  if (!studentId.value) return;

  listLoading.value = true;
  listError.value = null;
  try {
    const response = await apiService.getTasksForStudent(studentId.value);
    tasks.value = response.data;
  } catch (err) {
    console.error("Failed to fetch tasks:", err);
    listError.value = 'Failed to load tasks. ' + (err.response?.data?.message || err.message);
  } finally {
    listLoading.value = false;
  }

  statsLoading.value = true;
  statsError.value = null;
  try {
    const response = await apiService.getPendingTaskCount(studentId.value);
    pendingTasksCount.value = response.data;
  } catch (err) {
    console.error("Failed to fetch pending task count:", err);
    statsError.value = 'Failed to load task stats. ' + (err.response?.data?.message || err.message);
  } finally {
    statsLoading.value = false;
  }
};

const handleCreateTask = async (taskData) => {
  if (!taskData.title.trim()) { 
    taskFormError.value = "Task title is required.";
    return;
  }
  if (!taskData.dueDate) {
    taskFormError.value = "Due date is required.";
    return;
  }
  
  taskFormError.value = null;
  createLoading.value = true;
  
  try {
    const taskPayload = {
      title: taskData.title,
      description: taskData.description,
      dueDate: taskData.dueDate,
      completed: taskData.completed 
    };
    await apiService.createTask(studentId.value, taskPayload);
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to create task:", err);
    taskFormError.value = 'Failed to create task. ' + (err.response?.data?.message || err.message);
  } finally {
    createLoading.value = false;
  }
};

const handleUpdateTask = async (taskData) => {
  updateLoadingId.value = taskData.id;
  
  try {
    const payload = {
      title: taskData.title,
      description: taskData.description,
      dueDate: taskData.dueDate,
      completed: taskData.completed
    };
    await apiService.updateTask(taskData.id, payload);
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to update task:", err);
    listError.value = 'Failed to update task. ' + (err.response?.data?.message || err.message);
  } finally {
    updateLoadingId.value = null;
  }
};

const handleToggleComplete = async (task) => {
  const updatedTaskData = {
    title: task.title,
    description: task.description,
    dueDate: task.dueDate,
    completed: !task.completed
  };
  
  updateLoadingId.value = task.id;
  listError.value = null;
  
  try {
    await apiService.updateTask(task.id, updatedTaskData);
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to update task status:", err);
    listError.value = 'Failed to update task. ' + (err.response?.data?.message || err.message);
  } finally {
    updateLoadingId.value = null;
  }
};

const handleDeleteTask = async (taskId) => {
  deleteLoadingId.value = taskId;
  listError.value = null;
  
  try {
    await apiService.deleteTask(taskId);
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to delete task:", err);
    listError.value = 'Failed to delete task. ' + (err.response?.data?.message || err.message);
  } finally {
    deleteLoadingId.value = null;
  }
};

const handleLogout = () => {
  authStore.logout();
  router.push({ name: 'Login' });
};

// Lifecycle hooks
onMounted(() => {
  if (authStore.isLoggedIn && studentId.value) {
    fetchStudentData();
  }
});

watch(studentId, (newId, oldId) => {
  if (newId && newId !== oldId) { 
    fetchStudentData();
  } else if (!newId) {
    tasks.value = [];
    pendingTasksCount.value = null;
  }
}, { immediate: true });
</script>

<style scoped>
@import '@/assets/dashboard.css';
</style>