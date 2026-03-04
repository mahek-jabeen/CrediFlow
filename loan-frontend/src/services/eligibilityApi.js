/**
 * API service for loan eligibility operations
 */

const API_BASE_URL = 'http://localhost:8080';

/**
 * Checks loan eligibility based on the provided payload
 * 
 * @param {Object} payload - The eligibility check request data
 * @param {number} payload.monthlyIncome - Monthly income of the applicant
 * @param {number} payload.monthlyExpenses - Monthly expenses of the applicant
 * @param {number} payload.creditScore - Credit score of the applicant
 * @param {number} payload.requestedLoanAmount - Requested loan amount
 * @param {number} payload.tenureMonths - Loan tenure in months
 * @param {number} payload.annualInterestRate - Annual interest rate
 * @returns {Promise<Object>} The eligibility check response
 */
export const checkEligibility = async (payload) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE_URL}/api/eligibility/check`, {
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
