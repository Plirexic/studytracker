<template>
  <div class="dashboard-container" v-if="authStore.isLoggedIn">
    <header class="dashboard-header">
      <h1>Welcome, {{ authStore.currentStudentName }}!</h1>
      <button @click="handleLogout" class="logout-button">Logout</button>
    </header>

    <div class="dashboard-main-content">
      <!-- Left Column -->
      <div class="left-column">
        <section class="stats-section">
          <h2>Your Progress</h2>
          <div v-if="tasks.length > 0" class="progress-bar-container">
            <div
              class="progress-bar-fill"
              :style="{ width: progressPercentage + '%' }"
              :class="{ 'full-rounded': progressPercentage === 100 }"
            >
              <span v-if="progressPercentage > 10">{{ Math.round(progressPercentage) }}%</span>
            </div>
          </div>
          <p v-else-if="!listLoading && !statsLoading">No tasks yet to track progress.</p>
          <p v-if="pendingTasksCount !== null && tasks.length > 0">
            Pending Tasks: <strong>{{ pendingTasksCount }}</strong>
             ({{ completedTasksCount }} / {{ totalTasksCount }} completed)
          </p>
          <p v-else-if="statsLoading">Loading stats...</p>
          <p v-if="statsError" class="error-message">{{ statsError }}</p>
        </section>

        <section class="create-task-wrapper">
          <h2>Create New Task</h2>
          <div class="create-task-form">
            <input type="text" v-model="newTask.title" placeholder="Task Title" />
            <textarea v-model="newTask.description" placeholder="Task Description (Optional)"></textarea>
            <input type="date" v-model="newTask.dueDate" />
            <button @click="handleCreateTask" :disabled="createLoading">
              {{ createLoading ? 'Creating...' : 'Add Task' }}
            </button>
            <p v-if="taskFormError" class="error-message">{{ taskFormError }}</p>
          </div>
        </section>
      </div>

      <!-- Right Column -->
      <div class="right-column">
        <section class="tasks-section">
          <h2>Your Tasks</h2>
          <div v-if="listLoading && !tasks.length">Loading tasks...</div>
          <ul v-if="tasks.length" class="task-list">
            <li v-for="task in tasks" :key="task.id" :class="{ completed: task.completed }">
              <!-- Editing State -->
              <div v-if="editingTaskId === task.id" class="task-edit-form">
                <input type="text" v-model="editTaskData.title" placeholder="Task Title" />
                <textarea v-model="editTaskData.description" placeholder="Task Description"></textarea>
                <input type="date" v-model="editTaskData.dueDate" />
                <label class="edit-completed-label">
                  Completed:
                  <input type="checkbox" v-model="editTaskData.completed" />
                </label>
                <div class="edit-actions">
                  <button @click="handleSaveUpdateTask(task.id)" class="save-btn" :disabled="updateLoadingId === task.id">
                    {{ updateLoadingId === task.id ? 'Saving...' : 'Save' }}
                  </button>
                  <button @click="cancelEdit" class="cancel-btn" :disabled="updateLoadingId === task.id">Cancel</button>
                </div>
                <p v-if="taskEditError" class="error-message">{{ taskEditError }}</p>
              </div>

              <!-- Display State -->
              <div v-else class="task-item-display">
                <div class="task-info">
                  <h3>{{ task.title }}</h3>
                  <p v-if="task.description">{{ task.description }}</p>
                  <p v-if="task.dueDate">Due: {{ formatDate(task.dueDate) }}</p>
                </div>
                <div class="task-actions">
                  <button @click="startEditTask(task)" class="edit-btn" :disabled="updateLoadingId || deleteLoadingId">Edit</button>
                  <button @click="toggleComplete(task)" :disabled="updateLoadingId === task.id || deleteLoadingId === task.id">
                    {{ updateLoadingId === task.id ? 'Updating...' : (task.completed ? 'Mark Incomplete' : 'Mark Complete') }}
                  </button>
                  <button @click="handleDeleteTask(task.id)" class="delete-task-btn" :disabled="deleteLoadingId === task.id || updateLoadingId === task.id">
                    {{ deleteLoadingId === task.id ? 'Deleting...' : 'Delete' }}
                  </button>
                </div>
              </div>
            </li>
          </ul>
          <p v-else-if="!listLoading && !listError">No tasks found. Create one!</p>
          <p v-if="listError" class="error-message">{{ listError }}</p>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import apiService from '@/services/apiService';

const router = useRouter();
const authStore = useAuthStore();

const tasks = ref([]);
const pendingTasksCount = ref(null);
const newTask = ref({ title: '', description: '', dueDate: null, completed: false });

const listLoading = ref(false);
const listError = ref(null);
const createLoading = ref(false);
const updateLoadingId = ref(null);
const deleteLoadingId = ref(null);
const statsLoading = ref(false);
const statsError = ref(null);
const taskFormError = ref(null);
const editingTaskId = ref(null);
const editTaskData = ref({ id: null, title: '', description: '', dueDate: null, completed: false });
const taskEditError = ref(null);

const studentId = computed(() => authStore.currentStudentId);

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

const handleCreateTask = async () => {
  if (!newTask.value.title.trim()) { 
    taskFormError.value = "Task title is required.";
    return;
  }
  if (!newTask.value.dueDate) {
    taskFormError.value = "Due date is required.";
    return;
  }
  taskFormError.value = null;
  createLoading.value = true;
  try {
    const taskPayload = {
      title: newTask.value.title,
      description: newTask.value.description,
      dueDate: newTask.value.dueDate,
      completed: newTask.value.completed 
    };
    await apiService.createTask(studentId.value, taskPayload);
    newTask.value = { title: '', description: '', dueDate: null, completed: false };
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to create task:", err);
    taskFormError.value = 'Failed to create task. ' + (err.response?.data?.message || err.message);
  } finally {
    createLoading.value = false;
  }
};

const startEditTask = (task) => {
  editingTaskId.value = task.id;
  editTaskData.value = { ...task };
  if (editTaskData.value.dueDate) {
    if (typeof editTaskData.value.dueDate === 'string' && editTaskData.value.dueDate.includes('T')) {
        editTaskData.value.dueDate = editTaskData.value.dueDate.split('T')[0];
    } else if (editTaskData.value.dueDate instanceof Date) { // Should not happen if data comes from JSON
        editTaskData.value.dueDate = editTaskData.value.dueDate.toISOString().split('T')[0];
    }
  }
  taskEditError.value = null;
};

const cancelEdit = () => {
  editingTaskId.value = null;
  taskEditError.value = null;
};

const handleSaveUpdateTask = async (taskId) => {
  if (!editTaskData.value.title.trim()) {
    taskEditError.value = "Task title cannot be empty.";
    return;
  }
   if (!editTaskData.value.dueDate) {
    taskEditError.value = "Due date is required.";
    return;
  }
  updateLoadingId.value = taskId;
  taskEditError.value = null;
  try {
    const payload = {
        title: editTaskData.value.title,
        description: editTaskData.value.description,
        dueDate: editTaskData.value.dueDate,
        completed: editTaskData.value.completed
    };
    await apiService.updateTask(taskId, payload);
    editingTaskId.value = null;
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to update task:", err);
    taskEditError.value = 'Failed to update task. ' + (err.response?.data?.message || err.message);
  } finally {
    updateLoadingId.value = null;
  }
};

const totalTasksCount = computed(() => tasks.value.length);
const completedTasksCount = computed(() => {
  return tasks.value.filter(task => task.completed).length;
});
const progressPercentage = computed(() => {
  if (totalTasksCount.value === 0) return 0;
  return (completedTasksCount.value / totalTasksCount.value) * 100;
});

const toggleComplete = async (task) => {
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
  if (!confirm("Are you sure you want to delete this task?")) return;
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

const formatDate = (dateString) => {
  if (!dateString) return '';
  // Check if dateString is already just YYYY-MM-DD
  if (typeof dateString === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
    const [year, month, day] = dateString.split('-');
    return new Date(Number(year), Number(month) - 1, Number(day)).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
  }
  const options = { year: 'numeric', month: 'long', day: 'numeric' };
  return new Date(dateString).toLocaleDateString(undefined, options);
};

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
.dashboard-container {
  width: 100%;
  max-width: 1200px; 
  min-height: calc(100vh - 40px); 
  margin: 20px auto;
  padding: 25px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
  flex-shrink: 0;
}
.dashboard-header h1 { color: #2c3e50; font-size: 1.8em; }

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
.progress-bar-fill.full-rounded { border-radius: 6px; }

.logout-button {
  padding: 9px 16px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 5px;
  font-weight: 500;
  transition: background-color 0.2s ease;
}
.logout-button:hover { background-color: #c82333; }

.dashboard-main-content {
  display: flex;
  gap: 25px;
  align-items: flex-start;
  width: 100%;
  flex-grow: 1;
  overflow: hidden;
}

.left-column {
  flex: 1 1 350px; 
  min-width: 320px; 
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.right-column {
  flex: 2 1 60%; 
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.stats-section,
.create-task-wrapper,
.tasks-section {
  padding: 20px;
  background-color: #f8f9fa; 
  border: 1px solid #dee2e6; 
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
}

.stats-section h2,
.create-task-wrapper h2,
.tasks-section h2 {
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
  border-color: #80bdff; outline: 0; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}
.create-task-form textarea { resize: vertical; min-height: 60px; }
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
.create-task-form button:hover { background-color: #218838; }
.create-task-form button:disabled { background-color: #adb5bd; cursor: not-allowed; }


.tasks-section {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  overflow: hidden;
  margin-bottom: 0;
}

.task-list {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
  flex-grow: 1;
  padding-right: 8px;
}

.task-list li {
  display: flex;
  flex-direction: column;
  padding: 12px 15px; 
  border-bottom: 1px solid #e9ecef;
  background-color: #fff;
  margin-bottom: 8px;
  border-radius: 5px; 
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: box-shadow 0.2s ease;
}
.task-list li:last-child { margin-bottom: 0; border-bottom: none; }
.task-list li:hover { box-shadow: 0 2px 5px rgba(0,0,0,0.1); }

.task-list li.completed .task-info h3,
.task-list li.completed .task-info p {
  text-decoration: line-through; color: #6c757d; 
}

.task-item-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.task-info {
  flex-grow: 1;
  margin-right: 15px;
  word-break: break-word;
  overflow-wrap: break-word; 
  word-wrap: break-word; 
  min-width: 0; 
  hyphens: auto; 
  -webkit-hyphens: auto;
  -ms-hyphens: auto;
}
.task-info h3 {
  margin: 0 0 6px 0; 
  color: #212529; 
  font-size: 1.1em;
  overflow-wrap: break-word;
  word-wrap: break-word;
  hyphens: auto;
  -webkit-hyphens: auto;
  -ms-hyphens: auto;
}
.task-info p { 
  margin: 0 0 4px 0; 
  font-size: 0.9em; 
  color: #495057; 
  overflow-wrap: break-word;
  word-wrap: break-word;
  hyphens: auto;
  -webkit-hyphens: auto;
  -ms-hyphens: auto;
}
.task-info p:last-child { 
  margin-bottom: 0; 
}


.task-edit-form {
  width: 100%;
  padding: 15px;
  border: 1px dashed #007bff;
  border-radius: 5px;
  background-color: #f0f6ff;
  margin-bottom: 10px;
  box-sizing: border-box;
}
.task-edit-form input[type="text"],
.task-edit-form textarea,
.task-edit-form input[type="date"] {
  width: 100%;
  padding: 9px;
  margin-bottom: 9px;
  border: 1px solid #b8cce0;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 0.95em;
}
.task-edit-form input[type="text"]:focus,
.task-edit-form textarea:focus,
.task-edit-form input[type="date"]:focus {
  border-color: #007bff; outline: 0; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}
.task-edit-form textarea { min-height: 50px; }

.edit-completed-label {
  display: flex; align-items: center; margin-bottom: 10px; font-size: 0.9em; color: #212529;
}
.edit-completed-label input[type="checkbox"] {
  margin-left: 8px; margin-right: 5px; width: auto; vertical-align: middle;
}

.edit-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
.edit-actions button {
  padding: 7px 14px; border: none; border-radius: 4px; font-weight: 500;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}
.edit-actions .save-btn { background-color: #28a745; color: white; }
.edit-actions .save-btn:hover { background-color: #218838; }
.edit-actions .cancel-btn { background-color: #6c757d; color: white; }
.edit-actions .cancel-btn:hover { background-color: #5a6268; }


.task-actions { display: flex; gap: 6px; align-items: center; flex-shrink: 0; }
.task-actions button {
  padding: 5px 10px; border: 1px solid transparent; border-radius: 4px;
  font-size: 0.8em; white-space: nowrap;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}
.task-actions .edit-btn { background-color: #007bff; color: white; }
.task-actions .edit-btn:hover { background-color: #0056b3; }
.task-actions button:not(.edit-btn):not(.delete-task-btn) {
  background-color: #f8f9fa; color: #343a40; border-color: #ced4da;
}
.task-actions button:not(.edit-btn):not(.delete-task-btn):hover { background-color: #e2e6ea; }
.task-actions .delete-task-btn { background-color: #ffc107; color: #212529; border-color: #ffc107; }
.task-actions .delete-task-btn:hover { background-color: #e0a800; }


.error-message { color: #dc3545; margin-top: 6px; font-size: 0.85em; text-align: left; }
.create-task-form .error-message { margin-bottom: 8px; }
.task-edit-form .error-message { margin-top: 8px; }

@media (max-width: 860px) { 
  .dashboard-main-content { flex-direction: column; }
  .left-column, .right-column {
    flex-basis: auto; width: 100%; min-width: auto;
  }
  .tasks-section { max-height: 55vh; }
}
</style>