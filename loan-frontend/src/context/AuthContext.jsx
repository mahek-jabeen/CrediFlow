import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Check localStorage on mount to restore session
  useEffect(() => {
    const storedAuth = localStorage.getItem('isAuthenticated');
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');

    if (storedAuth === 'true' && storedUser && storedToken) {
      setIsAuthenticated(true);
      setUser(JSON.parse(storedUser));
    }
    
    setLoading(false);
  }, []);

  const login = async (email, password, selectedRole) => {
    // Real backend authentication
    
    // Simple validation
    if (!email || !password) {
      throw new Error('Email and password are required');
    }

    try {
      const response = await fetch('https://crediflow-vhy5.onrender.com/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password, role: selectedRole }),
      });

      const data = await response.json();
      localStorage.setItem("token", data.message || data.token);

      if (!response.ok) {
        throw new Error(data.error || 'Login failed');
      }

      // Validate role match
      if (data.role && data.role !== selectedRole) {
        throw new Error(`Role mismatch. You selected ${selectedRole} but your account is ${data.role}`);
      }

      const userData = {
        email: data.email,
        name: data.fullName,
        userId: data.userId,
        role: data.role || selectedRole,
        loginTime: new Date().toISOString()
      };

      setIsAuthenticated(true);
      setUser(userData);

      // 🔥 STORE JWT TOKEN
      localStorage.setItem('token', data.token);
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('user', JSON.stringify(userData));

      return userData;
    } catch (error) {
      throw new Error(error.message || 'Login failed');
    }
  };

  const signup = async (name, email, password, confirmPassword, role = 'USER') => {
    // Real backend signup
    
    // Validations
    if (!name || !email || !password || !confirmPassword) {
      throw new Error('All fields are required');
    }

    if (password.length < 8) {
      throw new Error('Password must be at least 8 characters');
    }

    if (password !== confirmPassword) {
      throw new Error('Passwords do not match');
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      throw new Error('Invalid email format');
    }

    try {
      const response = await fetch('https://crediflow-vhy5.onrender.com/api/auth/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          fullName: name,
          email, 
          password,
          role
        }),
      });

      const data = await response.json();
      localStorage.setItem("token", data.token);

      if (!response.ok) {
        throw new Error(data.error || 'Signup failed');
      }

      const userData = {
        email: data.email,
        name: data.fullName,
        userId: data.userId,
        role: data.role || role,
        signupTime: new Date().toISOString()
      };

      setIsAuthenticated(true);
      setUser(userData);

      // 🔥 STORE JWT TOKEN
      localStorage.setItem('token', data.token);
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('user', JSON.stringify(userData));

      return userData;
    } catch (error) {
      throw new Error(error.message || 'Signup failed');
    }
  };

  const logout = () => {
    setIsAuthenticated(false);
    setUser(null);
    
    // Clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('user');
  };

  const value = {
    isAuthenticated,
    user,
    loading,
    login,
    signup,
    logout
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
