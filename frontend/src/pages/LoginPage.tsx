import { useState } from 'react';
import type { FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ShoppingBag, User, Lock, Eye, EyeOff, Loader2 } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { User as UserType } from '../types';

interface LoginResponse {
    token: string;
    user: UserType;
}

type LoginError = { message: string } | null;

interface LocationState {
    from?: { pathname: string };
}

export default function LoginPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const { token, login } = useAuth();

    // Where ProtectedRoute/AdminRoute sent the user from, so a successful
    // login lands them back where they were headed instead of always '/'.
    const from = (location.state as LocationState)?.from?.pathname ?? '/';

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<LoginError>(null);

    // Already-logged-in users never see this form.
    if (token) {
        return <Navigate to={from} replace />;
    }

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (isSubmitting) return;

        setError(null);
        setIsSubmitting(true);

        try {
            const { data } = await api.post<LoginResponse>('/auth/login', {
                username,
                password,
            });
            login(data.token, data.user);
            navigate(from, { replace: true });
        } catch (err: any) {
            if (err?.response?.status === 401) {
                setError({ message: 'Incorrect username or password. Please try again.' });
            } else if (!err?.response) {
                setError({ message: 'Could not connect to the server. Please check your connection.' });
            } else {
                setError({ message: 'Something went wrong. Please try again.' });
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen w-full flex items-center justify-center bg-[#F7F5F0] px-6">
            <div className="w-full max-w-sm">
                {/* Logo + heading */}
                <div className="flex flex-col items-center mb-10">
                    <div className="w-16 h-16 rounded-[22px] bg-gray-900 flex items-center justify-center mb-4">
                        <ShoppingBag size={28} className="text-white" strokeWidth={1.8} />
                    </div>
                    <h1 className="text-2xl font-bold text-gray-900">e-SpazaSethu</h1>
                    <p className="text-sm text-gray-500 mt-1">Sign in to your store</p>
                </div>

                <form onSubmit={handleSubmit} noValidate className="space-y-3">
                    {/* Username */}
                    <div className="relative">
                        <User
                            size={18}
                            className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"
                            strokeWidth={1.8}
                        />
                        <input
                            id="username"
                            name="username"
                            type="text"
                            autoComplete="username"
                            aria-label="Username"
                            required
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            disabled={isSubmitting}
                            className="w-full h-14 pl-12 pr-4 rounded-2xl bg-white border border-gray-200 text-base text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent disabled:bg-gray-100"
                            placeholder="Username"
                        />
                    </div>

                    {/* Password */}
                    <div className="relative">
                        <Lock
                            size={18}
                            className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"
                            strokeWidth={1.8}
                        />
                        <input
                            id="password"
                            name="password"
                            type={showPassword ? 'text' : 'password'}
                            autoComplete="current-password"
                            aria-label="Password"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={isSubmitting}
                            className="w-full h-14 pl-12 pr-12 rounded-2xl bg-white border border-gray-200 text-base text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent disabled:bg-gray-100"
                            placeholder="Password"
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword((v) => !v)}
                            aria-label={showPassword ? 'Hide password' : 'Show password'}
                            className="absolute inset-y-0 right-0 w-12 flex items-center justify-center text-gray-400 active:text-gray-600"
                        >
                            {showPassword ? <EyeOff size={18} strokeWidth={1.8} /> : <Eye size={18} strokeWidth={1.8} />}
                        </button>
                    </div>

                    {/* Submit */}
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full h-14 rounded-2xl bg-gray-900 text-white font-semibold text-base flex items-center justify-center gap-2 transition-colors active:bg-black disabled:opacity-60 mt-5"
                    >
                        {isSubmitting && <Loader2 size={18} className="animate-spin" />}
                        {isSubmitting ? 'Logging in…' : 'Log In'}
                    </button>

                    {error && (
                        <p role="alert" className="text-sm text-red-600 text-center pt-1">
                            {error.message}
                        </p>
                    )}
                </form>
            </div>
        </div>
    );
}
