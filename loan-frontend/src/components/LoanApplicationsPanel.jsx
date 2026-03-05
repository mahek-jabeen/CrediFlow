import { useState, useEffect } from 'react';
import Alert from './Alert';
import LoadingSpinner from './LoadingSpinner';
import { useAuth } from '../context/AuthContext';

const LoanApplicationsPanel = () => {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useAuth();

  useEffect(() => {
    fetchApplications();
  }, []);

  const fetchApplications = async () => {
    try {
      setLoading(true);
      
      const response = await fetch('https://crediflow-vhy5.onrender.com/api/admin/loan-applications', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch loan applications');
      }

      const data = await response.json();
      setApplications(data);
      
    } catch (err) {
      setError(err.message || 'Failed to fetch loan applications');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (applicationNumber) => {
    try {
      const response = await fetch('https://crediflow-vhy5.onrender.com/api/admin/loan-approval', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        },
        body: JSON.stringify({
          applicationNumber: applicationNumber,
          action: 'APPROVE'
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to approve application');
      }

      const updatedApplication = await response.json();
      
      // Update local state
      setApplications(prev => 
        prev.map(app => 
          app.applicationNumber === applicationNumber 
            ? updatedApplication
            : app
        )
      );
      
    } catch (err) {
      setError(err.message || 'Failed to approve application');
    }
  };

  const handleReject = async (applicationNumber, reason) => {
    try {
      const response = await fetch('https://crediflow-vhy5.onrender.com/api/admin/loan-approval', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        },
        body: JSON.stringify({
          applicationNumber: applicationNumber,
          action: 'REJECT',
          rejectionReason: reason
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to reject application');
      }

      const updatedApplication = await response.json();
      
      // Update local state
      setApplications(prev => 
        prev.map(app => 
          app.applicationNumber === applicationNumber 
            ? updatedApplication
            : app
        )
      );
      
    } catch (err) {
      setError(err.message || 'Failed to reject application');
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <Alert 
        type="error" 
        message={error}
        onDismiss={() => setError('')}
      />
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden">
      <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900">Loan Applications</h3>
        <p className="text-sm text-gray-600 mt-1">Review and process loan applications</p>
      </div>
      
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Applicant
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Loan Details
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Eligibility
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {applications.map((application) => (
              <tr key={application.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div className="text-sm font-medium text-gray-900">{application.userName}</div>
                    <div className="text-sm text-gray-500">{application.userEmail}</div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">
                    <div>{formatCurrency(application.requestedAmount)}</div>
                    <div className="text-xs text-gray-500">{application.requestedTenureMonths} months</div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                    application.eligible 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {application.eligible ? 'Eligible' : 'Not Eligible'}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                    application.status === 'APPROVED' 
                      ? 'bg-green-100 text-green-800'
                      : application.status === 'REJECTED'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-yellow-100 text-yellow-800'
                  }`}>
                    {application.status}
                  </span>
                  {application.remarks && (
                    <div className="text-xs text-red-600 mt-1">{application.remarks}</div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  {application.status === 'PENDING' && (
                    <div className="flex space-x-2">
                      {application.eligible && (
                        <button
                          onClick={() => handleApprove(application.applicationNumber)}
                          className="text-green-600 hover:text-green-900 font-medium"
                        >
                          Approve
                        </button>
                      )}
                      <button
                        onClick={() => {
                          const reason = prompt('Enter rejection reason:');
                          if (reason) {
                            handleReject(application.applicationNumber, reason);
                          }
                        }}
                        className="text-red-600 hover:text-red-900 font-medium"
                      >
                        Reject
                      </button>
                    </div>
                  )}
                  {application.status === 'APPROVED' && (
                    <span className="text-green-600">Approved</span>
                  )}
                  {application.status === 'REJECTED' && (
                    <span className="text-red-600">Rejected</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default LoanApplicationsPanel;
