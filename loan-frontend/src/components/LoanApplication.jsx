import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const LoanApplication = () => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    amount: '',
    tenure: '',
    interestRate: ''
  });
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetchLoanStatus();
  }, []);

  const fetchLoanStatus = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/user/loan/status', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        const applications = Array.isArray(data) ? data : (data.data && Array.isArray(data.data) ? data.data : []);
        setApplications(applications);
      }
    } catch (error) {
      console.error('Error fetching loan status:', error);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/user/loan/apply', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          amount: parseFloat(formData.amount),
          tenure: parseInt(formData.tenure),
          interestRate: parseFloat(formData.interestRate)
        })
      });

      if (response.ok) {
        setMessage('Loan application submitted successfully!');
        setFormData({ amount: '', tenure: '', interestRate: '' });
        fetchLoanStatus();
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error submitting application');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h2 className="text-2xl font-bold mb-6">Loan Application</h2>
      
      {/* Application Form */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h3 className="text-lg font-semibold mb-4">Apply for New Loan</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Loan Amount ($)
            </label>
            <input
              type="number"
              name="amount"
              value={formData.amount}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter amount"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Tenure (months)
            </label>
            <input
              type="number"
              name="tenure"
              value={formData.tenure}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter tenure in months"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Interest Rate (%)
            </label>
            <input
              type="number"
              name="interestRate"
              value={formData.interestRate}
              onChange={handleChange}
              required
              step="0.1"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter interest rate"
            />
          </div>
          
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
          >
            {loading ? 'Submitting...' : 'Apply for Loan'}
          </button>
        </form>
        
        {message && (
          <div className={`mt-4 p-3 rounded-md ${message.includes('Error') ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
            {message}
          </div>
        )}
      </div>

      {/* Loan Status */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h3 className="text-lg font-semibold mb-4">Your Loan Applications</h3>
        {applications.length === 0 ? (
          <p className="text-gray-500">No loan applications found.</p>
        ) : (
          <div className="space-y-4">
            {Array.isArray(applications) && applications.map((app) => (
              <div key={app.id} className="border border-gray-200 rounded-md p-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-600">Application Number</p>
                    <p className="font-medium">{app.applicationNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Status</p>
                    <span className={`inline-block px-2 py-1 text-xs font-semibold rounded-full ${
                      app.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                      app.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {app.status}
                    </span>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Amount</p>
                    <p className="font-medium">${app.requestedAmount}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tenure</p>
                    <p className="font-medium">{app.requestedTenureMonths} months</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Interest Rate</p>
                    <p className="font-medium">{app.proposedInterestRate}%</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Applied Date</p>
                    <p className="font-medium">{new Date(app.applicationDate).toLocaleDateString()}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default LoanApplication;
