// src/utils/dateUtils.js
export function getTodayDateString() {
  const today = new Date();
  const year = today.getFullYear();
  const month = (today.getMonth() + 1).toString().padStart(2, '0');
  const day = today.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function isValidFutureDate(dateString) {
  if (!dateString || !/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
      // Basic check for YYYY-MM-DD format
      return false;
  }

  // 1. Get today's date, normalized to the beginning of the day (midnight) IN LOCAL TIMEZONE.
  const todayNormalized = new Date();
  todayNormalized.setHours(0, 0, 0, 0);

  // 2. Parse the input dateString.
  const parts = dateString.split('-');
  const year = parseInt(parts[0], 10);
  const month = parseInt(parts[1], 10) - 1; 
  const day = parseInt(parts[2], 10);
  const selectedDate = new Date(year, month, day); 

  // Now both todayNormalized and selectedDate represent midnight in the local timezone
  // for their respective dates
  return selectedDate.getTime() >= todayNormalized.getTime()
}