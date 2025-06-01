<template>
  <li :class="{ completed: task.completed }">
    <!-- Editing State -->
    <div v-if="isEditing" class="task-edit-form">
      <input 
        type="text" 
        v-model="editData.title" 
        placeholder="Task Title" 
      />
      <textarea 
        v-model="editData.description" 
        placeholder="Task Description"
      ></textarea>
      <input 
        type="date" 
        v-model="editData.dueDate" 
      />
      <label class="edit-completed-label">
        Completed:
        <input 
          type="checkbox" 
          v-model="editData.completed" 
        />
      </label>
      <div class="edit-actions">
        <button 
          @click="handleSave" 
          class="save-btn" 
          :disabled="updateLoading"
        >
          {{ updateLoading ? 'Saving...' : 'Save' }}
        </button>
        <button 
          @click="handleCancelEdit" 
          class="cancel-btn" 
          :disabled="updateLoading"
        >
          Cancel
        </button>
      </div>
      <p v-if="editError" class="error-message">{{ editError }}</p>
    </div>

    <!-- Display State -->
    <div v-else class="task-item-display">
      <div class="task-info">
        <h3>{{ task.title }}</h3>
        <p v-if="task.description">{{ task.description }}</p>
        <p v-if="task.dueDate">Due: {{ formatDate(task.dueDate) }}</p>
      </div>
      <div class="task-actions">
        <button 
          @click="handleStartEdit" 
          class="edit-btn" 
          :disabled="updateLoading || deleteLoading"
        >
          Edit
        </button>
        <button 
          @click="handleToggleComplete" 
          :disabled="updateLoading || deleteLoading"
        >
          {{ updateLoading ? 'Updating...' : (task.completed ? 'Mark Incomplete' : 'Mark Complete') }}
        </button>
        <button 
          @click="handleDelete" 
          class="delete-task-btn" 
          :disabled="deleteLoading || updateLoading"
        >
          {{ deleteLoading ? 'Deleting...' : 'Delete' }}
        </button>
      </div>
    </div>
  </li>
</template>

<script setup>
import { ref, reactive } from 'vue';

const props = defineProps({
  task: {
    type: Object,
    required: true
  },
  updateLoading: {
    type: Boolean,
    default: false
  },
  deleteLoading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update-task', 'delete-task', 'toggle-complete']);

const isEditing = ref(false);
const editError = ref(null);
const editData = reactive({
  id: null,
  title: '',
  description: '',
  dueDate: null,
  completed: false
});

const handleStartEdit = () => {
  isEditing.value = true;
  Object.assign(editData, { ...props.task });
  
  if (editData.dueDate) {
    if (typeof editData.dueDate === 'string' && editData.dueDate.includes('T')) {
      editData.dueDate = editData.dueDate.split('T')[0];
    } else if (editData.dueDate instanceof Date) {
      editData.dueDate = editData.dueDate.toISOString().split('T')[0];
    }
  }
  editError.value = null;
};

const handleCancelEdit = () => {
  isEditing.value = false;
  editError.value = null;
};

const handleSave = () => {
  if (!editData.title.trim()) {
    editError.value = "Task title cannot be empty.";
    return;
  }
  if (!editData.dueDate) {
    editError.value = "Due date is required.";
    return;
  }
  
  editError.value = null;
  emit('update-task', { ...editData });
  isEditing.value = false;
};

const handleToggleComplete = () => {
  emit('toggle-complete', props.task);
};

const handleDelete = () => {
  if (!confirm("Are you sure you want to delete this task?")) return;
  emit('delete-task', props.task.id);
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
</script>

<style scoped>
li {
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

li:last-child { 
  margin-bottom: 0; 
  border-bottom: none; 
}

li:hover { 
  box-shadow: 0 2px 5px rgba(0,0,0,0.1); 
}

li.completed .task-info h3,
li.completed .task-info p {
  text-decoration: line-through; 
  color: #6c757d; 
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
  border-color: #007bff; 
  outline: 0; 
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}

.task-edit-form textarea { 
  min-height: 50px; 
}

.edit-completed-label {
  display: flex; 
  align-items: center; 
  margin-bottom: 10px; 
  font-size: 0.9em; 
  color: #212529;
}

.edit-completed-label input[type="checkbox"] {
  margin-left: 8px; 
  margin-right: 5px; 
  width: auto; 
  vertical-align: middle;
}

.edit-actions { 
  display: flex; 
  gap: 8px; 
  justify-content: flex-end; 
  margin-top: 8px; 
}

.edit-actions button {
  padding: 7px 14px; 
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

.task-actions { 
  display: flex; 
  gap: 6px; 
  align-items: center; 
  flex-shrink: 0; 
}

.task-actions button {
  padding: 5px 10px; 
  border: 1px solid transparent; 
  border-radius: 4px;
  font-size: 0.8em; 
  white-space: nowrap;
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
  background-color: #f8f9fa; 
  color: #343a40; 
  border-color: #ced4da;
}

.task-actions button:not(.edit-btn):not(.delete-task-btn):hover { 
  background-color: #e2e6ea; 
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
  color: #dc3545; 
  margin-top: 6px; 
  font-size: 0.85em; 
  text-align: left; 
}

.task-edit-form .error-message { 
  margin-top: 8px; 
}
</style>