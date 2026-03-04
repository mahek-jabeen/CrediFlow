import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const RoleBasedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== requiredRole) {
    if (user?.role === 'USER') {
      return <Navigate to="/dashboard/user" replace />;
    } else if (user?.role === 'ADMIN') {
      return <Navigate to="/dashboard/admin" replace />;
    }
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default RoleBasedRoute;
