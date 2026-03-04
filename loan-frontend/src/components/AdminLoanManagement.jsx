import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const AdminLoanManagement = () => {
  const { user } = useAuth();
  const [loans, setLoans] = useState([]);
  const [filteredLoans, setFilteredLoans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  // Filter states
  const [searchEmail, setSearchEmail] = useState('');
  const [searchApplicationNumber, setSearchApplicationNumber] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [loanTypeFilter, setLoanTypeFilter] = useState('');
  const [dateFromFilter, setDateFromFilter] = useState('');
  const [dateToFilter, setDateToFilter] = useState('');
  const [amountMinFilter, setAmountMinFilter] = useState('');
  const [amountMaxFilter, setAmountMaxFilter] = useState('');

  // Modal states
  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [showAuditModal, setShowAuditModal] = useState(false);
  const [showUnderReviewModal, setShowUnderReviewModal] = useState(false);
  const [selectedLoan, setSelectedLoan] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [approveRemarks, setApproveRemarks] = useState('');
  const [underReviewRemarks, setUnderReviewRemarks] = useState('');
  const [auditLogs, setAuditLogs] = useState([]);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [sortBy, setSortBy] = useState('applicationDate');
  const [sortDir, setSortDir] = useState('desc');
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Metrics state
  const [metrics, setMetrics] = useState({
    totalApplications: 0,
    pendingLoans: 0,
    approvedLoans: 0,
    rejectedLoans: 0,
    totalLoanAmount: 0
  });

  useEffect(() => {
    fetchAllLoans();
    fetchMetrics();
  }, [
    currentPage,
    pageSize,
    sortBy,
    sortDir,
    searchEmail,
    searchApplicationNumber,
    statusFilter,
    loanTypeFilter,
    dateFromFilter,
    dateToFilter,
    amountMinFilter,
    amountMaxFilter
  ]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  const handleSortChange = (newSortBy) => {
    setSortBy(newSortBy);
    setCurrentPage(0); // Reset to first page when sorting changes
  };

  const handleSortDirChange = (newSortDir) => {
    setSortDir(newSortDir);
    setCurrentPage(0); // Reset to first page when sort direction changes
  };

  const fetchMetrics = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/admin/loans/metrics', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setMetrics(data);
      } else {
        console.error('Failed to fetch metrics');
      }
    } catch (error) {
      console.error('Error fetching metrics:', error);
    }
  };

  const exportCsv = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/admin/loans/export/csv', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'loan_applications.csv';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        setMessage('CSV Exported Successfully');
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error exporting CSV');
    }
  };

  const exportPdf = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/admin/loans/export/pdf', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'loan_applications.txt';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        setMessage('PDF Exported Successfully');
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error exporting PDF');
    }
  };

  const fetchAllLoans = async () => {
    try {
      const token = localStorage.getItem('token');
      
      // Build query parameters
      const params = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        sortBy: sortBy,
        sortDir: sortDir
      });

      // Add filter parameters if they exist
      if (searchEmail) params.append('email', searchEmail);
      if (searchApplicationNumber) params.append('applicationNumber', searchApplicationNumber);
      if (statusFilter) params.append('status', statusFilter);
      if (loanTypeFilter) params.append('loanType', loanTypeFilter);
      if (dateFromFilter) params.append('dateFrom', dateFromFilter);
      if (dateToFilter) params.append('dateTo', dateToFilter);
      if (amountMinFilter) params.append('minAmount', amountMinFilter);
      if (amountMaxFilter) params.append('maxAmount', amountMaxFilter);

      const response = await fetch(`http://localhost:8080/api/admin/loans?${params}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        
        // Handle paginated response
        if (data.content !== undefined) {
          setLoans(data.content);
          setTotalPages(data.totalPages);
          setTotalElements(data.totalElements);
        } else {
          // Fallback for non-paginated response
          setLoans(data);
          setTotalPages(1);
          setTotalElements(data.length);
        }
      }
    } catch (error) {
      console.error('Error fetching loans:', error);
    }
  };

  const handleViewAuditLogs = async (loanId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/admin/loans/${loanId}/audit-logs`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const logs = await response.json();
        setAuditLogs(logs);
        setShowAuditModal(true);
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error fetching audit logs');
    }
  };

  const handleUnderReview = async (loanId) => {
    const loan = filteredLoans.find(l => l.id === loanId);
    setSelectedLoan(loan);
    setShowUnderReviewModal(true);
    setUnderReviewRemarks('');
  };

  const confirmUnderReview = async () => {
    if (!selectedLoan) return;
    
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/admin/loans/${selectedLoan.id}/under-review`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ remarks: underReviewRemarks })
      });

      if (response.ok) {
        setMessage('Loan marked under review successfully!');
        setShowUnderReviewModal(false);
        setUnderReviewRemarks('');
        setSelectedLoan(null);
        fetchAllLoans();
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error marking loan under review');
    } finally {
      setLoading(false);
    }
  };

  const handleView = async (loanId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/admin/loans/${loanId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const loanDetails = await response.json();
        setSelectedLoan(loanDetails);
        setShowViewModal(true);
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error fetching loan details');
    }
  };

  const handleApprove = async (loanId) => {
    const loan = filteredLoans.find(l => l.id === loanId);
    setSelectedLoan(loan);
    setShowApproveModal(true);
  };

  const handleReject = async (loanId) => {
    const loan = filteredLoans.find(l => l.id === loanId);
    setSelectedLoan(loan);
    setShowRejectModal(true);
    setRejectReason('');
  };

  const confirmApprove = async () => {
    if (!selectedLoan) return;
    
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/admin/loans/${selectedLoan.id}/approve`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ remarks: approveRemarks })
      });

      if (response.ok) {
        setMessage('Loan approved successfully!');
        setShowApproveModal(false);
        setApproveRemarks('');
        setSelectedLoan(null);
        fetchAllLoans();
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error approving loan');
    } finally {
      setLoading(false);
    }
  };

  const confirmReject = async () => {
    if (!selectedLoan || rejectReason.trim().length === 0) {
      setMessage('Rejection reason is required');
      return;
    }
    
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/admin/loans/${selectedLoan.id}/reject`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ remarks: rejectReason })
      });

      if (response.ok) {
        setMessage('Loan rejected successfully!');
        setShowRejectModal(false);
        setRejectReason('');
        setSelectedLoan(null);
        fetchAllLoans();
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      setMessage('Error rejecting loan');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto p-6">
      <h2 className="text-2xl font-bold mb-6">Admin Loan Management</h2>
      
      {message && (
        <div className={`mb-4 p-3 rounded-md ${message.includes('Error') ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
          {message}
        </div>
      )}

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow-md p-4">
          <h3 className="text-sm font-medium text-gray-500">Total Applications</h3>
          <p className="text-2xl font-bold text-gray-900">{metrics.totalApplications}</p>
        </div>
        <div className="bg-white rounded-lg shadow-md p-4">
          <h3 className="text-sm font-medium text-gray-500">Pending Loans</h3>
          <p className="text-2xl font-bold text-yellow-600">{metrics.pendingLoans}</p>
        </div>
        <div className="bg-white rounded-lg shadow-md p-4">
          <h3 className="text-sm font-medium text-gray-500">Approved Loans</h3>
          <p className="text-2xl font-bold text-green-600">{metrics.approvedLoans}</p>
        </div>
        <div className="bg-white rounded-lg shadow-md p-4">
          <h3 className="text-sm font-medium text-gray-500">Rejected Loans</h3>
          <p className="text-2xl font-bold text-red-600">{metrics.rejectedLoans}</p>
        </div>
        <div className="bg-white rounded-lg shadow-md p-4">
          <h3 className="text-sm font-medium text-gray-500">Total Loan Amount</h3>
          <p className="text-2xl font-bold text-gray-900">${metrics.totalLoanAmount.toLocaleString()}</p>
        </div>
      </div>

      {/* Export Buttons */}
      <div className="flex justify-end space-x-4 mb-6">
        <button
          onClick={exportCsv}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 flex items-center"
        >
          📥 Export CSV
        </button>
        <button
          onClick={exportPdf}
          className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 flex items-center"
        >
          📥 Export PDF
        </button>
      </div>

      {/* Filters Section */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h3 className="text-lg font-semibold mb-4">Filters & Search</h3>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {/* Search Fields */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Search by Email</label>
            <input
              type="email"
              value={searchEmail}
              onChange={(e) => setSearchEmail(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter email..."
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Search by Application Number</label>
            <input
              type="text"
              value={searchApplicationNumber}
              onChange={(e) => setSearchApplicationNumber(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="APP-123456..."
            />
          </div>
          
          {/* Status Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="UNDER_REVIEW">Under Review</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
          
          {/* Loan Type Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Loan Type</label>
            <select
              value={loanTypeFilter}
              onChange={(e) => setLoanTypeFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Types</option>
              <option value="HOME_LOAN">Home Loan</option>
              <option value="PERSONAL_LOAN">Personal Loan</option>
              <option value="CAR_LOAN">Car Loan</option>
              <option value="EDUCATION_LOAN">Education Loan</option>
              <option value="BUSINESS_LOAN">Business Loan</option>
              <option value="GOLD_LOAN">Gold Loan</option>
            </select>
          </div>
          
          {/* Date Range Filters */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Date From</label>
            <input
              type="date"
              value={dateFromFilter}
              onChange={(e) => setDateFromFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Date To</label>
            <input
              type="date"
              value={dateToFilter}
              onChange={(e) => setDateToFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          {/* Amount Range Filters */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Min Amount</label>
            <input
              type="number"
              value={amountMinFilter}
              onChange={(e) => setAmountMinFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Max Amount</label>
            <input
              type="number"
              value={amountMaxFilter}
              onChange={(e) => setAmountMaxFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="1000000"
            />
          </div>
        </div>
        
        <div className="mt-4">
          <button
            onClick={() => {
              setSearchEmail('');
              setSearchApplicationNumber('');
              setStatusFilter('');
              setLoanTypeFilter('');
              setDateFromFilter('');
              setDateToFilter('');
              setAmountMinFilter('');
              setAmountMaxFilter('');
            }}
            className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
          >
            Clear Filters
          </button>
        </div>
      </div>

      {/* Sorting Controls */}
      <div className="flex justify-end space-x-4 mb-6">
        <div className="flex items-center space-x-2">
          <label className="text-sm font-medium text-gray-700">Sort by:</label>
          <select
            value={sortBy}
            onChange={(e) => handleSortChange(e.target.value)}
            className="px-3 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="applicationDate">Applied Date</option>
            <option value="requestedAmount">Requested Amount</option>
            <option value="applicationNumber">Application Number</option>
          </select>
        </div>
        <div className="flex items-center space-x-2">
          <label className="text-sm font-medium text-gray-700">Order:</label>
          <select
            value={sortDir}
            onChange={(e) => handleSortDirChange(e.target.value)}
            className="px-3 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="desc">Newest First</option>
            <option value="asc">Oldest First</option>
          </select>
        </div>
      </div>

      {/* Results Summary */}
      <div className="bg-white rounded-lg shadow-md p-4 mb-6">
        <p className="text-sm text-gray-600">
          Showing {loans.length} of {totalElements} loan applications
        </p>
      </div>

      {/* Loan Applications Table */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h3 className="text-lg font-semibold mb-4">Loan Applications</h3>
        {loans.length === 0 ? (
          <p className="text-gray-500">No loan applications found matching your criteria.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Application Number
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    User Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    User Email
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Loan Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tenure
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Interest Rate
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Applied Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {loans.map((loan) => (
                  <tr key={loan.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {loan.applicationNumber}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {loan.userName || 'Unknown'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {loan.userEmail || 'Unknown'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {loan.loanType}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      ${loan.requestedAmount?.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {loan.requestedTenureMonths} months
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {loan.proposedInterestRate}%
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        loan.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                        loan.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {loan.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(loan.applicationDate).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button
                        onClick={() => handleView(loan.id)}
                        className="bg-blue-600 text-white px-3 py-1 rounded text-xs hover:bg-blue-700 mr-2"
                      >
                        View
                      </button>
                      <button
                        onClick={() => handleViewAuditLogs(loan.id)}
                        className="bg-purple-600 text-white px-3 py-1 rounded text-xs hover:bg-purple-700 mr-2"
                      >
                        Audit Logs
                      </button>
                      {loan.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => handleUnderReview(loan.id)}
                            disabled={loading}
                            className="bg-yellow-600 text-white px-3 py-1 rounded text-xs hover:bg-yellow-700 disabled:bg-gray-400 mr-2"
                          >
                            Under Review
                          </button>
                          <button
                            onClick={() => handleApprove(loan.id)}
                            disabled={loading}
                            className="bg-green-600 text-white px-3 py-1 rounded text-xs hover:bg-green-700 disabled:bg-gray-400 mr-2"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => handleReject(loan.id)}
                            disabled={loading}
                            className="bg-red-600 text-white px-3 py-1 rounded text-xs hover:bg-red-700 disabled:bg-gray-400"
                          >
                            Reject
                          </button>
                        </>
                      )}
                      {loan.status !== 'PENDING' && (
                        <span className="text-gray-400 text-xs">No actions</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="bg-white rounded-lg shadow-md p-4 mt-6">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-700">
              Page {currentPage + 1} of {totalPages} ({totalElements} total results)
            </div>
            <div className="flex space-x-2">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 disabled:bg-gray-200 disabled:text-gray-400"
              >
                Previous
              </button>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 disabled:bg-gray-200 disabled:text-gray-400"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Audit Logs Modal */}
      {showAuditModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900">Audit Logs</h3>
              <div className="mt-4 max-h-64 overflow-y-auto">
                {auditLogs.length === 0 ? (
                  <p className="text-sm text-gray-500">No logs yet</p>
                ) : (
                  <div className="space-y-3">
                    {auditLogs.map((log, index) => (
                      <div key={index} className="border-b pb-2">
                        <p className="text-sm font-medium">Action: {log.action}</p>
                        <p className="text-xs text-gray-600">Old Status: {log.oldStatus || 'N/A'}</p>
                        <p className="text-xs text-gray-600">New Status: {log.newStatus}</p>
                        <p className="text-xs text-gray-600">Admin: {log.adminEmail}</p>
                        {log.remarks && <p className="text-xs text-gray-600">Remarks: {log.remarks}</p>}
                        <p className="text-xs text-gray-500">Date: {new Date(log.timestamp).toLocaleString()}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <div className="mt-4 flex justify-end">
                <button
                  onClick={() => {
                    setShowAuditModal(false);
                    setAuditLogs([]);
                  }}
                  className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Under Review Modal */}
      {showUnderReviewModal && selectedLoan && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900">Mark Loan Under Review</h3>
              <div className="mt-2">
                <p className="text-sm text-gray-600">
                  Application Number: <strong>{selectedLoan.applicationNumber}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Amount: <strong>${selectedLoan.requestedAmount}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Applicant: <strong>{selectedLoan.user?.fullName}</strong>
                </p>
              </div>
              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Remarks (optional)
                </label>
                <textarea
                  value={underReviewRemarks}
                  onChange={(e) => setUnderReviewRemarks(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows="3"
                  placeholder="Enter any remarks for marking under review..."
                />
              </div>
              <div className="mt-4 flex justify-end space-x-2">
                <button
                  onClick={() => {
                    setShowUnderReviewModal(false);
                    setUnderReviewRemarks('');
                    setSelectedLoan(null);
                  }}
                  className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmUnderReview}
                  disabled={loading}
                  className="px-4 py-2 bg-yellow-600 text-white rounded-md hover:bg-yellow-700 disabled:bg-gray-400"
                >
                  {loading ? 'Processing...' : 'Mark Under Review'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* View Modal */}
      {showViewModal && selectedLoan && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900">Loan Application Details</h3>
              <div className="mt-4 space-y-2">
                <p className="text-sm text-gray-600">
                  Application Number: <strong>{selectedLoan.applicationNumber}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  User Name: <strong>{selectedLoan.userName}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  User Email: <strong>{selectedLoan.userEmail}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Loan Type: <strong>{selectedLoan.loanType}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Amount: <strong>${selectedLoan.requestedAmount}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Tenure: <strong>{selectedLoan.requestedTenureMonths} months</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Interest Rate: <strong>{selectedLoan.proposedInterestRate}%</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Credit Score: <strong>{selectedLoan.creditScore}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Existing EMI: <strong>${selectedLoan.existingEmiAmount}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Status: <strong>{selectedLoan.status}</strong>
                </p>
                {selectedLoan.remarks && (
                  <p className="text-sm text-gray-600">
                    Remarks: <strong>{selectedLoan.remarks}</strong>
                  </p>
                )}
                <p className="text-sm text-gray-600">
                  Applied Date: <strong>{new Date(selectedLoan.applicationDate).toLocaleDateString()}</strong>
                </p>
                {selectedLoan.reviewedDate && (
                  <p className="text-sm text-gray-600">
                    Reviewed Date: <strong>{new Date(selectedLoan.reviewedDate).toLocaleDateString()}</strong>
                  </p>
                )}
                {selectedLoan.reviewedBy && (
                  <p className="text-sm text-gray-600">
                    Reviewed By: <strong>{selectedLoan.reviewedBy}</strong>
                  </p>
                )}
              </div>
              <div className="mt-4 flex justify-end">
                <button
                  onClick={() => {
                    setShowViewModal(false);
                    setSelectedLoan(null);
                  }}
                  className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Approve Modal */}
      {showApproveModal && selectedLoan && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900">Approve Loan Application</h3>
              <div className="mt-2">
                <p className="text-sm text-gray-600">
                  Application Number: <strong>{selectedLoan.applicationNumber}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Amount: <strong>${selectedLoan.requestedAmount}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Applicant: <strong>{selectedLoan.user?.fullName}</strong>
                </p>
              </div>
              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Remarks (optional)
                </label>
                <textarea
                  value={approveRemarks}
                  onChange={(e) => setApproveRemarks(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows="3"
                  placeholder="Enter any remarks for this approval..."
                />
              </div>
              <div className="mt-4 flex justify-end space-x-2">
                <button
                  onClick={() => {
                    setShowApproveModal(false);
                    setApproveRemarks('');
                    setSelectedLoan(null);
                  }}
                  className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmApprove}
                  disabled={loading}
                  className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:bg-gray-400"
                >
                  {loading ? 'Approving...' : 'Approve Loan'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {showRejectModal && selectedLoan && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900">Reject Loan Application</h3>
              <div className="mt-2">
                <p className="text-sm text-gray-600">
                  Application Number: <strong>{selectedLoan.applicationNumber}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Amount: <strong>${selectedLoan.requestedAmount}</strong>
                </p>
                <p className="text-sm text-gray-600">
                  Applicant: <strong>{selectedLoan.user?.fullName}</strong>
                </p>
              </div>
              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Rejection Reason <span className="text-red-500">*</span>
                </label>
                <textarea
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows="3"
                  placeholder="Enter the reason for rejection..."
                  required
                />
              </div>
              <div className="mt-4 flex justify-end space-x-2">
                <button
                  onClick={() => {
                    setShowRejectModal(false);
                    setRejectReason('');
                    setSelectedLoan(null);
                  }}
                  className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmReject}
                  disabled={loading}
                  className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:bg-gray-400"
                >
                  {loading ? 'Rejecting...' : 'Reject Loan'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminLoanManagement;
