import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AdminRoute = () => {
    const { token, currentUser } = useAuth();
    if (!token) return <Navigate to="/login" replace />;
    if (currentUser?.role !== 'ADMIN') return <Navigate to="/" replace />;
    return <Outlet />;
};

export default AdminRoute;