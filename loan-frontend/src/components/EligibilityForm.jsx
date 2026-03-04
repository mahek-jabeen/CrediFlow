import { useState } from 'react';
import { checkEligibility } from '../services/eligibilityApi';
import Alert from '../components/Alert';
import LoadingSpinner from '../components/LoadingSpinner';
import SkeletonLoader from '../components/SkeletonLoader';

const EligibilityForm = () => {
  const [formData, setFormData] = useState({
    monthlyIncome: '',
    monthlyExpenses: '',
    creditScore: '',
    requestedLoanAmount: '',
    tenureMonths: '',
    annualInterestRate: ''
  });

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevData => ({
      ...prevData,
      [name]: value
    }));
    
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };
  
  const validateForm = () => {
    const newErrors = {};
    
    // Monthly Income validation
    if (!formData.monthlyIncome || parseFloat(formData.monthlyIncome) <= 0) {
      newErrors.monthlyIncome = 'Monthly income must be greater than 0';
    }
    
    // Monthly Expenses validation
    if (!formData.monthlyExpenses || parseFloat(formData.monthlyExpenses) < 0) {
      newErrors.monthlyExpenses = 'Monthly expenses cannot be negative';
    }
    
    // Credit Score validation
    if (!formData.creditScore || parseInt(formData.creditScore) < 300 || parseInt(formData.creditScore) > 850) {
      newErrors.creditScore = 'Credit score must be between 300 and 850';
    }
    
    // Loan Amount validation
    if (!formData.requestedLoanAmount || parseFloat(formData.requestedLoanAmount) <= 0) {
      newErrors.requestedLoanAmount = 'Loan amount must be greater than 0';
    }
    
    // Tenure validation
    if (!formData.tenureMonths || parseInt(formData.tenureMonths) < 6 || parseInt(formData.tenureMonths) > 360) {
      newErrors.tenureMonths = 'Tenure must be between 6 and 360 months';
    }
    
    // Interest Rate validation
    if (!formData.annualInterestRate || parseFloat(formData.annualInterestRate) <= 0 || parseFloat(formData.annualInterestRate) > 20) {
      newErrors.annualInterestRate = 'Interest rate must be between 0% and 20%';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Prevent multiple submissions
    if (loading) {
      return;
    }
    
    if (!validateForm()) {
      return;
    }
    
    // Reset previous result and errors
    setResult(null);
    setErrors({});
    setLoading(true);

    try {
      // Convert form data to numbers for API
      const payload = {
        monthlyIncome: parseFloat(formData.monthlyIncome),
        monthlyExpenses: parseFloat(formData.monthlyExpenses),
        creditScore: parseInt(formData.creditScore),
        requestedLoanAmount: parseFloat(formData.requestedLoanAmount),
        tenureMonths: parseInt(formData.tenureMonths),
        annualInterestRate: parseFloat(formData.annualInterestRate)
      };

      // Call API and get response
      const response = await checkEligibility(payload);
      
      // Handle response based on eligible field
      if (response.eligible === true) {
        setResult({
          success: true,
          message: response.reason
        });
      } else {
        setResult({
          success: false,
          message: response.reason
        });
      }
    } catch (error) {
      // Catch network/API errors and display user-friendly message
      const userFriendlyMessage = error.message.includes('connect') 
        ? 'Unable to connect to server. Please check your internet connection and try again.'
        : 'Failed to check eligibility. Please try again later.';
      setResult({
        success: false,
        message: userFriendlyMessage
      });
    } finally {
      // Always re-enable button
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="bg-white rounded-lg shadow-md p-8">
          <h2 className="text-3xl font-bold text-gray-900 mb-2 text-center">
          Loan Eligibility Check
        </h2>
        <p className="text-gray-600 text-center mb-8">
          Fill in your details to check loan eligibility
        </p>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Monthly Income */}
            <div>
              <label 
                htmlFor="monthlyIncome" 
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Monthly Income
              </label>
              <input
                type="number"
                id="monthlyIncome"
                name="monthlyIncome"
                value={formData.monthlyIncome}
                onChange={handleChange}
                disabled={loading}
                className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 ${
                  errors.monthlyIncome 
                    ? 'border-red-500 focus:ring-red-500 bg-red-50' 
                    : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 hover:border-gray-400'
                } ${loading ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}`}
                placeholder="Enter your monthly income"
                step="0.01"
              />
            </div>

            {/* Monthly Expenses */}
            <div>
              <label 
                htmlFor="monthlyExpenses" 
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Monthly Expenses
              </label>
              <input
                type="number"
                id="monthlyExpenses"
                name="monthlyExpenses"
                value={formData.monthlyExpenses}
                onChange={handleChange}
                disabled={loading}
                className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 ${
                  errors.monthlyExpenses 
                    ? 'border-red-500 focus:ring-red-500 bg-red-50' 
                    : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 hover:border-gray-400'
                } ${loading ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}`}
                placeholder="Enter your monthly expenses"
                step="0.01"
              />
            </div>

            {/* Credit Score */}
            <div>
              <label 
                htmlFor="creditScore" 
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Credit Score
              </label>
              <input
                type="number"
                id="creditScore"
                name="creditScore"
                value={formData.creditScore}
                onChange={handleChange}
                disabled={loading}
                className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 ${
                  errors.creditScore 
                    ? 'border-red-500 focus:ring-red-500 bg-red-50' 
                    : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 hover:border-gray-400'
                } ${loading ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}`}
                placeholder="Enter your credit score"
                min="300"
                max="850"
              />
            </div>

            {/* Requested Loan Amount */}
            <div>
              <label 
                htmlFor="requestedLoanAmount" 
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Requested Loan Amount
              </label>
              <input
                type="number"
                id="requestedLoanAmount"
                name="requestedLoanAmount"
                value={formData.requestedLoanAmount}
                onChange={handleChange}
                disabled={loading}
                className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 ${
                  errors.requestedLoanAmount 
                    ? 'border-red-500 focus:ring-red-500 bg-red-50' 
                    : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 hover:border-gray-400'
                } ${loading ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}`}
                placeholder="Enter requested loan amount"
                step="0.01"
              />
            </div>

            {/* Tenure (months) */}
            <div>
              <label 
                htmlFor="tenureMonths" 
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Tenure (months)
              </label>
              <input
                type="number"
                id="tenureMonths"
                name="tenureMonths"
                value={formData.tenureMonths}
                onChange={handleChange}
                disabled={loading}
                className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 ${
                  errors.tenureMonths 
                    ? 'border-red-500 focus:ring-red-500 bg-red-50' 
                    : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 hover:border-gray-400'
                } ${loading ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}`}
                placeholder="Enter loan tenure in months"
                min="1"
                max="360"
              />
            </div>

            {/* Annual Interest Rate */}
            <div>
              <label 
                htmlFor="annualInterestRate" 
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Annual Interest Rate (%)
              </label>
              <input
                type="number"
                id="annualInterestRate"
                name="annualInterestRate"
                value={formData.annualInterestRate}
                onChange={handleChange}
                disabled={loading}
                className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 ${
                  errors.annualInterestRate 
                    ? 'border-red-500 focus:ring-red-500 bg-red-50' 
                    : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 hover:border-gray-400'
                } ${loading ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}`}
                placeholder="Enter annual interest rate"
                step="0.01"
                min="0"
                max="20"
              />
            </div>

            {/* Submit Button */}
            <div className="pt-4">
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 disabled:bg-blue-400 disabled:cursor-not-allowed flex items-center justify-center shadow-sm hover:shadow-md transform hover:scale-[1.02]"
              >
                {loading ? (
                  <>
                    <LoadingSpinner size="sm" className="mr-2" />
                    <span>Checking eligibility...</span>
                  </>
                ) : (
                  <span>Check Eligibility</span>
                )}
              </button>
            </div>
          </form>

          {/* Result Display */}
          {result && (
            <Alert 
              type={result.success ? 'success' : 'error'}
              message={result.message}
              onDismiss={() => setResult(null)}
              className="mt-6"
            />
          )}
      </div>
    </div>
  );
};

export default EligibilityForm;
