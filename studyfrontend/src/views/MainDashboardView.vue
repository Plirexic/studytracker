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
          <h2>Your Stats</h2>
          <p v-if="pendingTasksCount !== null">
            Pending Tasks: <strong>{{ pendingTasksCount }}</strong>
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
            <button @click="handleCreateTask" :disabled="tasksLoading">
              {{ tasksLoading ? 'Creating...' : 'Add Task' }}
            </button>
            <p v-if="taskFormError" class="error-message">{{ taskFormError }}</p>
          </div>
        </section>
      </div>

      <!-- Right Column -->
      <div class="right-column">
        <section class="tasks-section">
          <h2>Your Tasks</h2>
          <div v-if="tasksLoading && !tasks.length">Loading tasks...</div>
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
                   <button @click="handleSaveUpdateTask(task.id)" class="save-btn">Save</button>
                   <button @click="cancelEdit" class="cancel-btn">Cancel</button>
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
                  <button @click="startEditTask(task)" class="edit-btn">Edit</button>
                  <button @click="toggleComplete(task)">
                    {{ task.completed ? 'Mark Incomplete' : 'Mark Complete' }}
                  </button>
                  <button @click="handleDeleteTask(task.id)" class="delete-task-btn">Delete</button>
                </div>
              </div>
            </li>
          </ul>
          <p v-else-if="!tasksLoading && !tasksError">No tasks found. Create one!</p>
          <p v-if="tasksError" class="error-message">{{ tasksError }}</p>
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

const tasksLoading = ref(false);
const tasksError = ref(null);
const statsLoading = ref(false);
const statsError = ref(null);
const taskFormError = ref(null);

const studentId = computed(() => authStore.currentStudentId);

const editingTaskId = ref(null);
const editTaskData = ref({ id: null, title: '', description: '', dueDate: null, completed: false });
const taskEditError = ref(null);

const fetchStudentData = async () => {
  if (!studentId.value) return;

  tasksLoading.value = true;
  tasksError.value = null;
  try {
    const response = await apiService.getTasksForStudent(studentId.value);
    tasks.value = response.data;
  } catch (err) {
    console.error("Failed to fetch tasks:", err);
    tasksError.value = 'Failed to load tasks. ' + (err.response?.data?.message || err.message);
  } finally {
    tasksLoading.value = false;
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
  if (!newTask.value.title.trim() || !studentId.value) {
    taskFormError.value = "Task title is required.";
    return;
  }
  taskFormError.value = null;
  tasksLoading.value = true;
  try {
    await apiService.createTask(studentId.value, {
      title: newTask.value.title,
      description: newTask.value.description,
      dueDate: newTask.value.dueDate,
    });
    newTask.value = { title: '', description: '', dueDate: null, completed: false };
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to create task:", err);
    taskFormError.value = 'Failed to create task. ' + (err.response?.data?.message || err.message);
  } finally {
    tasksLoading.value = false;
  }
};

const startEditTask = (task) => {
  editingTaskId.value = task.id;
  editTaskData.value = { ...task };
  if (editTaskData.value.dueDate) {
    if (typeof editTaskData.value.dueDate === 'string' && editTaskData.value.dueDate.includes('T')) {
        editTaskData.value.dueDate = editTaskData.value.dueDate.split('T')[0];
    } else if (editTaskData.value.dueDate instanceof Date) {
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
  tasksLoading.value = true;
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
    tasksLoading.value = false;
  }
};


const toggleComplete = async (task) => {
  const updatedTaskData = {
      title: task.title,
      description: task.description,
      dueDate: task.dueDate,
      completed: !task.completed 
  };
  tasksLoading.value = true;
  try {
    await apiService.updateTask(task.id, updatedTaskData);
    await fetchStudentData();
  } catch (err) {
    console.error("Failed to update task status:", err);
    tasksError.value = 'Failed to update task. ' + (err.response?.data?.message || err.message);
  } finally {
    tasksLoading.value = false;
  }
};

const handleDeleteTask = async (taskId) => {
  if (!confirm("Are you sure you want to delete this task?")) return;
  tasksLoading.value = true;
  try {
    await apiService.deleteTask(taskId);
    await fetchStudentData();
  } catch (err)
   {
    console.error("Failed to delete task:", err);
    tasksError.value = 'Failed to delete task. ' + (err.response?.data?.message || err.message);
  } finally {
    tasksLoading.value = false;
  }
};

const handleLogout = () => {
  authStore.logout();
  router.push({ name: 'Login' });
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const options = { year: 'numeric', month: 'long', day: 'numeric' };
  return new Date(dateString).toLocaleDateString(undefined, options);
};

// Fetch data when component is mounted or when studentId changes (e.g., after login)
onMounted(() => {
  if (authStore.isLoggedIn) {
    fetchStudentData();
  }
});

watch(studentId, (newId) => {
  if (newId) {
    fetchStudentData();
  } else {
    tasks.value = [];
    pendingTasksCount.value = null;
  }
}, { immediate: true });

</script>

<style scoped>
.dashboard-container {
  width: 100%;
  max-width: 2000px;
  margin: 20px auto;
  padding: 25px; 
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
  background-color: #ffffff; 
  border-radius: 10px; 
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.dashboard-header h1 {
  color: #2c3e50; 
  font-size: 1.8em;
}

.logout-button {
  padding: 10px 18px;
  background-color: #e74c3c; 
  color: white;
  border: none;
  border-radius: 5px;
  font-weight: bold;
  transition: background-color 0.2s ease;
}
.logout-button:hover {
  background-color: #c0392b;
}

.dashboard-main-content {
  display: flex;
  gap: 25px;
  align-items: flex-start;
  width: 100%;
}

.left-column {
  flex-grow: 1;
  flex-shrink: 1;
  flex-basis: 30%; 
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.right-column {
  flex-grow: 1; 
  flex-shrink: 1;
  flex-basis: 70%;
}

.stats-section,
.create-task-wrapper, 
.tasks-section {
  padding: 20px;
  background-color: #fdfdfd;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
}

.stats-section h2,
.create-task-wrapper h2,
.tasks-section h2 {
  color: #34495e;
  margin-top: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 1.4em;
  margin-bottom: 15px; 
}

.stats-section, .tasks-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fdfdfd; 
  border: 1px solid #e8e8e8; 
  border-radius: 8px;
}

.stats-section h2, .tasks-section h2 {
  color: #34495e; 
  margin-top: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 1.4em;
}

.create-task-form {
  margin-bottom: 25px;
  padding: 20px;
  border: 1px solid #dfe4ea;
  border-radius: 6px;
  background-color: #fff; 
}

.create-task-form h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #34495e;
  font-size: 1.2em;
}

.create-task-form input[type="text"],
.create-task-form textarea,
.create-task-form input[type="date"] {
  width: calc(100% - 24px); 
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid #ced4da; 
  border-radius: 4px;
  box-sizing: border-box; 
  font-size: 1em;
}

.create-task-form input[type="text"]:focus,
.create-task-form textarea:focus,
.create-task-form input[type="date"]:focus {
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.create-task-form textarea {
  resize: vertical;
  min-height: 70px;
}

.create-task-form button {
  padding: 12px 20px;
  background-color: #2ecc71;
  color: white;
  border: none;
  border-radius: 5px;
  font-weight: bold;
  transition: background-color 0.2s ease;
}

.create-task-form button:hover {
  background-color: #27ae60;
}

.create-task-form button:disabled {
  background-color: #bdc3c7; 
  cursor: not-allowed;
}

.task-list {
  list-style: none;
  padding: 0;
}

.task-list li {
  display: flex; 
  flex-direction: column; 
  padding: 15px;
  border-bottom: 1px solid #ecf0f1;
  background-color: #fff;
  margin-bottom: 8px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: box-shadow 0.2s ease;
}

.task-list li:hover {
    box-shadow: 0 4px 8px rgba(0,0,0,0.07);
}

.task-list li.completed .task-info h3,
.task-list li.completed .task-info p {
  text-decoration: line-through;
  color: #95a5a6;
}

.task-item-display {
  display: flex;
  justify-content: space-between;
  align-items: center; 
  width: 100%;
}

.task-info {
  flex-grow: 1; 
}

.task-info h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 1.15em;
}

.task-info p {
  margin: 0 0 5px 0;
  font-size: 0.95em;
  color: #555e68; 
}

.task-info p:last-child {
  margin-bottom: 0;
}


/* Task Edit Styles */
.task-edit-form {
  width: 100%;
  padding: 15px;
  border: 1px solid #007bff; 
  border-radius: 6px;
  background-color: #f8f9fa; 
  margin-bottom: 10px; 
}

.task-edit-form input[type="text"],
.task-edit-form textarea,
.task-edit-form input[type="date"] {
  width: calc(100% - 22px);
  padding: 10px;
  margin-bottom: 10px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  box-sizing: border-box;
}

.task-edit-form input[type="text"]:focus,
.task-edit-form textarea:focus,
.task-edit-form input[type="date"]:focus {
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.task-edit-form textarea {
  min-height: 60px;
}

.edit-completed-label {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 0.9em;
  color: #333;
}

.edit-completed-label input[type="checkbox"] {
  margin-left: 8px;
  margin-right: 5px; 
  width: auto;
  vertical-align: middle;
}

.edit-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
}

.edit-actions button { 
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}

.edit-actions .save-btn {
  background-color: #28a745;
  color: white;
}

.edit-actions .save-btn:hover {
  background-color: #218838;
}

.edit-actions .cancel-btn {
  background-color: #6c757d;
  color: white;
}

.edit-actions .cancel-btn:hover {
  background-color: #5a6268;
}


/* Task Actions Buttons (Display Mode) */
.task-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.task-actions button {
  padding: 6px 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  font-size: 0.85em;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.task-actions .edit-btn {
  background-color: #007bff;
  color: white;
}

.task-actions .edit-btn:hover {
  background-color: #0056b3;
}

.task-actions button:not(.edit-btn):not(.delete-task-btn) { 
  background-color: #e9ecef; 
  color: #333;
  border-color: #ced4da;
}

.task-actions button:not(.edit-btn):not(.delete-task-btn):hover {
  background-color: #dae0e5;
}

.task-actions .delete-task-btn {
  background-color: #ffc107; 
  color: #212529; 
  border-color: #ffc107;
}

.task-actions .delete-task-btn:hover {
  background-color: #e0a800;
}

.error-message {
  color: #c0392b;
  margin-top: 8px;
  font-size: 0.9em;
  text-align: left;
}

.create-task-form .error-message {
    margin-bottom: 10px;
}

.task-edit-form .error-message {
    margin-top: 10px;
}

@media (max-width: 768px) { 
  .dashboard-main-content {
    flex-direction: column; 
  }
  .left-column, .right-column {
    flex: none;
    width: 100%; 
  }
}
</style>