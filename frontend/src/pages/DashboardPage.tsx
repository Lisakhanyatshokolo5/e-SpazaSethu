import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { DashboardData, SaleSummary, Product } from '../types';

const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
};

const DashboardPage = () => {
    const { currentUser } = useAuth();
    const [dashboard, setDashboard] = useState<DashboardData | null>(null);
    const [lowStock, setLowStock] = useState<Product[]>([]);
    const [recentSales, setRecentSales] = useState<SaleSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

    const loadData = useCallback(async () => {
        setLoading(true);
        setError(false);
        try {
            const [dashRes, lowStockRes, recentRes] = await Promise.all([
                api.get<DashboardData>('/reports/dashboard'),
                api.get<Product[]>('/products/low-stock'),
                api.get<SaleSummary[]>('/sales?from=TODAY&to=TODAY&limit=5'),
            ]);
            setDashboard(dashRes.data);
            setLowStock(lowStockRes.data);
            setRecentSales(recentRes.data);
        } catch {
            setError(true);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- initial data fetch requires a loading flag
        void loadData();
    }, [loadData]);

    if (loading) {
        return (
            <div className="p-4 space-y-4 animate-pulse">
                <div className="h-6 bg-gray-200 rounded w-1/2" />
                <div className="h-24 bg-gray-200 rounded" />
                <div className="h-24 bg-gray-200 rounded" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-4 text-center">
                <p className="mb-4">Something went wrong loading the dashboard.</p>
                <button onClick={() => void loadData()} className="bg-blue-600 text-white px-4 py-2 rounded">
                    Retry
                </button>
            </div>
        );
    }

    return (
        <div className="p-4 pb-24">
            <h1 className="text-xl font-semibold">
                {getGreeting()}, {currentUser?.username}
            </h1>
            <p className="text-sm text-gray-500 mb-4">
                {new Date().toLocaleDateString('en-ZA', { weekday: 'long', day: 'numeric', month: 'long' })}
            </p>

            <div className="grid grid-cols-2 gap-3 mb-4">
                <div className="bg-blue-50 rounded-lg p-4">
                    <p className="text-xs text-gray-600">Today's Sales</p>
                    <p className="text-2xl font-bold">R{dashboard?.totalSales.toFixed(2)}</p>
                    <p className="text-xs text-gray-500 mt-1">{dashboard?.transactionCount} transactions</p>
                </div>
                <div className="bg-green-50 rounded-lg p-4">
                    <p className="text-xs text-gray-600">Today's Profit</p>
                    <p className="text-2xl font-bold">R{dashboard?.totalProfit.toFixed(2)}</p>
                </div>
            </div>

            {lowStock.length > 0 ? (
                <div className="bg-amber-50 border border-amber-300 rounded-lg p-4 mb-4">
                    <p className="font-semibold text-amber-800 mb-2">Low stock alert</p>
                    <ul className="space-y-1">
                        {lowStock.map((p) => (
                            <li key={p.productId}>
                                <Link to={`/products/${p.productId}/edit`} className="flex justify-between text-sm">
                                    <span>{p.name}</span>
                                    <span className="font-medium">{p.stockQuantity} left</span>
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            ) : (
                <div className="bg-green-50 border border-green-300 rounded-lg p-4 mb-4 text-sm text-green-800">
                    All stock levels are healthy.
                </div>
            )}

            <div className="grid grid-cols-3 gap-3 mb-4">
                <Link to="/pos" className="bg-blue-600 text-white text-center rounded-lg py-4 text-sm font-medium">
                    New Sale
                </Link>
                <Link to="/products/new" className="bg-gray-800 text-white text-center rounded-lg py-4 text-sm font-medium">
                    Add Product
                </Link>
                <Link to="/stocktake" className="bg-gray-600 text-white text-center rounded-lg py-4 text-sm font-medium">
                    Start Stocktake
                </Link>
            </div>

            <div>
                <h2 className="font-semibold mb-2">Recent sales</h2>
                {recentSales.length === 0 ? (
                    <p className="text-sm text-gray-500">No sales recorded today yet.</p>
                ) : (
                    <ul className="divide-y">
                        {recentSales.map((sale) => (
                            <li key={sale.saleId}>
                                <Link to={`/sales/${sale.saleId}`} className="flex justify-between py-2 text-sm">
                                    <span>{new Date(sale.saleDateTime).toLocaleTimeString('en-ZA', { hour: '2-digit', minute: '2-digit' })}</span>
                                    <span>{sale.paymentMethod}</span>
                                    <span className="font-medium">R{sale.totalAmount.toFixed(2)}</span>
                                </Link>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
};

export default DashboardPage;