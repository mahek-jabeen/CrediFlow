import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import RoleBasedRoute from './components/RoleBasedRoute';
import UserDashboard from './components/UserDashboard';
import AdminDashboard from './components/AdminDashboard';
import Login from './pages/Login';
import Signup from './pages/Signup';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          
          {/* Protected Role-Based Routes */}
          <Route 
            path="/dashboard/user" 
            element={
              <ProtectedRoute>
                <RoleBasedRoute requiredRole="USER">
                  <UserDashboard />
                </RoleBasedRoute>
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/dashboard/admin" 
            element={
              <ProtectedRoute>
                <RoleBasedRoute requiredRole="ADMIN">
                  <AdminDashboard />
                </RoleBasedRoute>
              </ProtectedRoute>
            } 
          />
          
          {/* Default Route - Redirect to login */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          
          {/* Legacy dashboard route - redirect based on role */}
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <RoleBasedRouter />
              </ProtectedRoute>
            } 
          />
          
          {/* Catch all - redirect to login */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

// Component to redirect based on user role
const RoleBasedRouter = () => {
  const { user } = useAuth();
  const userRole = user?.role || 'USER';
  
  if (userRole === 'ADMIN') {
    return <Navigate to="/dashboard/admin" replace />;
  }
  
  return <Navigate to="/dashboard/user" replace />;
};

export default App
