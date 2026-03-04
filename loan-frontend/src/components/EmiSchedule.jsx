import { useState } from 'react';
import { generateEmiSchedule } from '../services/emiApi';
import Alert from '../components/Alert';
import LoadingSpinner from '../components/LoadingSpinner';
import SkeletonLoader from '../components/SkeletonLoader';

const EmiSchedule = () => {
  const [formData, setFormData] = useState({
    principalAmount: '',
    annualInterestRate: '',
    tenureMonths: ''
  });

  const [loading, setLoading] = useState(false);
  const [scheduleData, setScheduleData] = useState(null);
  const [error, setError] = useState(null);
  const [summary, setSummary] = useState(null);
  const [errors, setErrors] = useState({});

  const validateForm = () => {
    const newErrors = {};
    
    // Principal Amount validation
    if (!formData.principalAmount || parseFloat(formData.principalAmount) <= 0) {
      newErrors.principalAmount = 'Principal amount must be greater than 0';
    }
    
    // Interest Rate validation
    if (!formData.annualInterestRate || parseFloat(formData.annualInterestRate) <= 0 || parseFloat(formData.annualInterestRate) > 20) {
      newErrors.annualInterestRate = 'Interest rate must be between 0% and 20%';
    }
    
    // Tenure validation
    if (!formData.tenureMonths || parseInt(formData.tenureMonths) < 6 || parseInt(formData.tenureMonths) > 360) {
      newErrors.tenureMonths = 'Tenure must be between 6 and 360 months';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Prevent multiple submissions
    if (loading) {
      return;
    }
    
    if (!validateForm()) {
      return;
    }
    
    // Clear previous results, summary, and errors
    setScheduleData(null);
    setSummary(null);
    setError(null);
    setErrors({});
    setLoading(true);

    try {
      // Convert form data to numbers for API
      const payload = {
        principalAmount: parseFloat(formData.principalAmount),
        annualInterestRate: parseFloat(formData.annualInterestRate),
        tenureMonths: parseInt(formData.tenureMonths)
      };

      // Call API and get response
      const response = await generateEmiSchedule(payload);
      
      // Handle response - API returns array directly
      if (Array.isArray(response) && response.length > 0) {
        setScheduleData(response);
        
        // Calculate summary from EMI schedule data
        const calculatedSummary = calculateSummary(response);
        setSummary(calculatedSummary);
        
        setError(null);
      } else {
        const userFriendlyMessage = error.message.includes('connect') 
          ? 'Unable to connect to server. Please check your internet connection and try again.'
          : 'Failed to generate EMI schedule. Please try again later.';
        setError(userFriendlyMessage);
        setScheduleData(null);
        setSummary(null);
      }
    } catch (err) {
      const userFriendlyMessage = err.message.includes('connect') 
        ? 'Unable to connect to server. Please check your internet connection and try again.'
        : 'Failed to generate EMI schedule. Please try again later.';
      setError(userFriendlyMessage);
      setScheduleData(null);
      setSummary(null);
    } finally {
      // Always re-enable button
      setLoading(false);
    }
  };

  /**
   * Calculate EMI summary totals from schedule data
   * @param {Array} scheduleData - Array of EMI schedule items
   * @returns {Object} Summary object with total principal, interest, and amount
   */
  const calculateSummary = (scheduleData) => {
    if (!scheduleData || scheduleData.length === 0) {
      return null;
    }

    // Calculate total principal paid (sum of all principal components)
    const totalPrincipalPaid = scheduleData.reduce((sum, item) => {
      return sum + (item.principalComponent || 0);
    }, 0);

    // Calculate total interest paid (sum of all interest components)
    const totalInterestPaid = scheduleData.reduce((sum, item) => {
      return sum + (item.interestComponent || 0);
    }, 0);

    // Calculate total amount payable (sum of all EMI amounts)
    const totalAmountPayable = scheduleData.reduce((sum, item) => {
      return sum + (item.emiAmount || 0);
    }, 0);

    return {
      totalPrincipalPaid,
      totalInterestPaid,
      totalAmountPayable,
      numberOfEmis: scheduleData.length
    };
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  /**
   * Format date for CSV export (YYYY-MM-DD format)
   * @param {string} dateString - Date string to format
   * @returns {string} Formatted date string
   */
  const formatDateForCSV = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  /**
   * Format number for CSV export (remove currency symbols and formatting)
   * @param {number} amount - Number to format
   * @returns {string} Formatted number string
   */
  const formatNumberForCSV = (amount) => {
    if (amount === null || amount === undefined) return '0.00';
    return amount.toFixed(2);
  };

  /**
   * Download EMI schedule as CSV file
   * Uses browser APIs (Blob, URL.createObjectURL) without external libraries
   */
  const handleDownloadCSV = () => {
    if (!scheduleData || scheduleData.length === 0) {
      return;
    }

    // CSV Headers
    const headers = [
      'EMI No',
      'EMI Amount',
      'Principal Component',
      'Interest Component',
      'Remaining Balance',
      'Due Date'
    ];

    // Generate CSV rows from schedule data
    const rows = scheduleData.map((item) => [
      item.emiNumber || '-',
      formatNumberForCSV(item.emiAmount),
      formatNumberForCSV(item.principalComponent),
      formatNumberForCSV(item.interestComponent),
      formatNumberForCSV(item.remainingPrincipal),
      formatDateForCSV(item.dueDate)
    ]);

    // Combine headers and rows into CSV string
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');

    // Create Blob from CSV content
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });

    // Create download link using browser APIs
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    
    // Generate filename with timestamp
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
    const filename = `emi_schedule_${timestamp}.csv`;
    
    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    link.style.display = 'none';
    
    // Trigger download
    document.body.appendChild(link);
    link.click();
    
    // Cleanup
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="max-w-6xl mx-auto">
      <div className="bg-white rounded-lg shadow-md p-8">
        <h2 className="text-3xl font-bold text-gray-900 mb-2 text-center">
          EMI Schedule Calculator
        </h2>
        <p className="text-gray-600 text-center mb-8">
          Generate detailed EMI payment schedule
        </p>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-6 mb-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Principal Amount */}
              <div>
                <label 
                  htmlFor="principalAmount" 
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Principal Amount (₹)
                </label>
              <input
                type="number"
                id="principalAmount"
                name="principalAmount"
                value={formData.principalAmount}
                onChange={handleChange}
                disabled={loading}
                className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-all duration-200 ${
                  errors.principalAmount 
                    ? 'border-red-500 focus:ring-red-500 bg-red-50' 
                    : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 hover:border-gray-400'
                } ${loading ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}`}
                placeholder="Enter principal amount"
                step="0.01"
                required
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
                placeholder="Enter interest rate"
                step="0.01"
                min="0"
                max="20"
                required
              />
            </div>

            {/* Tenure */}
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
                placeholder="Enter tenure"
                min="6"
                max="360"
                required
              />
            </div>
          </div>

          {/* Submit Button */}
          <div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 disabled:bg-blue-400 disabled:cursor-not-allowed flex items-center justify-center shadow-sm hover:shadow-md transform hover:scale-[1.02]"
            >
              {loading ? (
                <>
                  <LoadingSpinner size="sm" className="mr-2" />
                  <span>Generating schedule...</span>
                </>
              ) : (
                <span>Generate Schedule</span>
              )}
            </button>
          </div>
        </form>

        {/* Error Display */}
        {error && (
          <Alert 
            type="error" 
            message={error}
            onDismiss={() => setError(null)}
            className="mb-6"
          />
        )}

        {/* EMI Schedule Table */}
        {scheduleData && scheduleData.length > 0 && (
          <div className="mt-8">
            <div className="bg-white rounded-lg shadow-md overflow-hidden">
              <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-gray-900">EMI Schedule</h3>
                  <button
                    onClick={handleDownloadCSV}
                    className="text-blue-600 hover:text-blue-700 font-medium text-sm flex items-center transition-colors"
                  >
                    <svg className="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m-6 0H6" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4H4" />
                    </svg>
                    Download CSV
                  </button>
                </div>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">EMI No</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">EMI Amount</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Principal Component</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Interest Component</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Remaining Balance</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Due Date</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {scheduleData.map((item, index) => (
                      <tr key={index} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.emiNumber || '-'}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatCurrency(item.emiAmount)}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatCurrency(item.principalComponent)}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatCurrency(item.interestComponent)}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatCurrency(item.remainingPrincipal)}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatDate(item.dueDate)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default EmiSchedule;
