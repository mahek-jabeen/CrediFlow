/**
 * API service for EMI schedule operations
 */

const API_BASE_URL = 'https://crediflow-vhy5.onrender.com';

/**
 * Generates EMI schedule based on the provided payload
 * 
 * @param {Object} payload - The EMI schedule request data
 * @param {number} payload.principalAmount - Principal loan amount
 * @param {number} payload.annualInterestRate - Annual interest rate in percentage
 * @param {number} payload.tenureMonths - Loan tenure in months
 * @returns {Promise<Object>} The EMI schedule response
 */
export const generateEmiSchedule = async (payload) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE_URL}/api/emi/schedule`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
    },
    body: JSON.stringify(payload),
  });

  const data = await response.json();
  return data;
};
