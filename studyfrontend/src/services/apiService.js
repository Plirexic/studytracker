import axios from 'axios';
import { API_BASE_URL } from '../config/apiConfig.js';

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export default {

    // --- Student Endpoints ---
    getStudents() {
        return apiClient.get('students');
    },
    createStudent(studentData) {
        return apiClient.post('students', studentData);
    },

    // --- Task Endpoints ---
    getTasksForStudent(studentId) {
        return apiClient.get(`tasks/students/${studentId}/tasks`);
    },
    getTaskByID(taskId) {
        return apiClient.get(`tasks/${taskId}`);
    },
    createTask(studentId, taskData) {
        return apiClient.post(`tasks/students/${studentId}/tasks`, taskData);
    },
    updateTask(taskId, taskData) {
        return apiClient.put(`tasks/update/${taskId}`, taskData);
    },
    deleteTask(taskId) {
        return apiClient.delete(`tasks/delete/${taskId}`);
    },
    getPendingTaskCount(studentId) {
        return apiClient.get(`tasks/students/${studentId}/tasks/pending-count`);
    }
};