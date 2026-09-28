import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Home, ShoppingCart, Package, Menu, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const BottomNav = () => {
    const { currentUser, logout } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const [drawerOpen, setDrawerOpen] = useState(false);
    const isAdmin = currentUser?.role === 'ADMIN';

    if (location.pathname === '/login') return null;

    const tabs = [
        { label: 'Dashboard', path: '/', icon: Home },
        { label: 'POS', path: '/pos', icon: ShoppingCart },
        { label: 'Products', path: '/products', icon: Package },
    ];

    const isActive = (path: string) =>
        path === '/' ? location.pathname === '/' : location.pathname.startsWith(path);

    const drawerLinks = [
        { label: 'Sales History', path: '/sales', adminOnly: false },
        { label: 'Stock Movements', path: '/movements', adminOnly: true },
        { label: 'Reports', path: '/reports', adminOnly: false },
        { label: 'Stocktake', path: '/stocktake', adminOnly: false },
        { label: 'Settings', path: '/settings', adminOnly: false },
    ];

    const goTo = (path: string) => {
        setDrawerOpen(false);
        navigate(path);
    };

    return (
        <>
            {drawerOpen && (
                <div className="fixed inset-0 z-40 flex flex-col justify-end bg-black/40" onClick={() => setDrawerOpen(false)}>
                    <div
                        className="bg-white rounded-t-2xl p-4 pb-8"
                        style={{ paddingBottom: 'calc(env(safe-area-inset-bottom) + 2rem)' }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="font-semibold text-lg">More</h2>
                            <button onClick={() => setDrawerOpen(false)} aria-label="Close">
                                <X size={22} />
                            </button>
                        </div>
                        <ul className="space-y-1">
                            {drawerLinks
                                .filter((link) => !link.adminOnly || isAdmin)
                                .map((link) => (
                                    <li key={link.path}>
                                        <button
                                            onClick={() => goTo(link.path)}
                                            className="w-full text-left py-3 px-2 rounded hover:bg-gray-100"
                                        >
                                            {link.label}
                                        </button>
                                    </li>
                                ))}
                            <li>
                                <button
                                    onClick={logout}
                                    className="w-full text-left py-3 px-2 rounded text-red-600 hover:bg-red-50"
                                >
                                    Logout
                                </button>
                            </li>
                        </ul>
                    </div>
                </div>
            )}

            <nav
                className="fixed bottom-0 left-0 right-0 z-30 flex justify-around bg-white border-t"
                style={{ paddingBottom: 'env(safe-area-inset-bottom)' }}
            >
                {tabs.map(({ label, path, icon: Icon }) => (
                    <button
                        key={path}
                        onClick={() => navigate(path)}
                        className={`flex flex-col items-center gap-1 py-2 px-4 ${
                            isActive(path) ? 'text-blue-600' : 'text-gray-500'
                        }`}
                    >
                        <Icon size={22} />
                        <span className="text-xs">{label}</span>
                    </button>
                ))}
                <button
                    onClick={() => setDrawerOpen(true)}
                    className="flex flex-col items-center gap-1 py-2 px-4 text-gray-500"
                >
                    <Menu size={22} />
                    <span className="text-xs">More</span>
                </button>
            </nav>
        </>
    );
};

export default BottomNav;