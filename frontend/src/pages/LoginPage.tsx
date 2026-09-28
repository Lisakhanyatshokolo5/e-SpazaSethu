import { useState, type FormEvent } from 'react';
import { useNavigate, useLocation, Navigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { LoginResponse } from '../types';

const LoginPage = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { token, login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    if (token) {
        const from = (location.state as { from?: Location })?.from?.pathname || '/';
        return <Navigate to={from} replace />;
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const { data } = await api.post<LoginResponse>('/auth/login', { username, password });
            login(data.token, data.user);
            const from = (location.state as { from?: Location })?.from?.pathname || '/';
            navigate(from, { replace: true });
        } catch (err: any) {
            if (err.response?.status === 401) {
                setError('Incorrect username or password. Please try again.');
            } else {
                setError('Could not connect to the server. Please check your connection.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <form onSubmit={handleSubmit} className="w-full max-w-sm">
                <h1 className="text-2xl font-bold text-center mb-8">e-SpazaSethu</h1>

                {error && (
                    <p className="text-red-600 text-sm mb-4 text-center">{error}</p>
                )}

                <label className="block text-sm font-medium mb-1">Username</label>
                <input
                    className="w-full border rounded p-3 mb-4"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />

                <label className="block text-sm font-medium mb-1">Password</label>
                <div className="relative mb-6">
                    <input
                        className="w-full border rounded p-3 pr-10"
                        type={showPassword ? 'text' : 'password'}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <button
                        type="button"
                        onClick={() => setShowPassword((s) => !s)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500"
                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                        {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                    </button>
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 text-white font-semibold py-3 rounded disabled:opacity-50"
                >
                    {loading ? 'Logging in...' : 'Login'}
                </button>
            </form>
        </div>
    );
};

export default LoginPage;