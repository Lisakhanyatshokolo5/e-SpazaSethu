import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { Sale } from '../types';

const SaleDetailPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { currentUser } = useAuth();
    const [sale, setSale] = useState<Sale | null>(null);
    const [showConfirm, setShowConfirm] = useState(false);
    const [cancelling, setCancelling] = useState(false);

    const isAdmin = currentUser?.role === 'ADMIN';

    useEffect(() => {
        if (!id) return;
        // eslint-disable-next-line react-hooks/set-state-in-effect -- initial data fetch requires setting sale on load
        void api.get<Sale>(`/sales/${id}`).then((res) => setSale(res.data));
    }, [id]);

    const handleCancel = async () => {
        if (!id) return;
        setCancelling(true);
        try {
            await api.patch(`/sales/${id}/cancel`);
            setSale((prev) => (prev ? { ...prev, status: 'Cancelled' } : prev));
            setShowConfirm(false);
        } finally {
            setCancelling(false);
        }
    };

    if (!sale) return <p className="p-4 text-sm text-gray-500">Loading...</p>;

    return (
        <div className="p-4 pb-24">
            <button onClick={() => navigate(-1)} className="text-sm text-blue-600 mb-4">
                ← Back
            </button>

            <p className="text-xs text-gray-400">Sale #{sale.saleId}</p>
            <div className="flex justify-between items-center mb-1">
                <h1 className="text-xl font-semibold">
                    {new Date(sale.saleDateTime).toLocaleString('en-ZA')}
                </h1>
                <span
                    className={`text-xs px-2 py-1 rounded-full ${
                        sale.status === 'Completed' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                    }`}
                >
          {sale.status}
        </span>
            </div>
            <p className="text-sm text-gray-500 mb-4">
                {sale.cashierName} · {sale.paymentMethod}
            </p>

            <table className="w-full text-sm mb-4">
                <thead>
                <tr className="text-left text-gray-500 border-b">
                    <th className="py-2">Product</th>
                    <th className="py-2 text-right">Qty</th>
                    <th className="py-2 text-right">Unit Price</th>
                    <th className="py-2 text-right">Subtotal</th>
                </tr>
                </thead>
                <tbody>
                {sale.items.map((item) => (
                    <tr key={item.productId} className="border-b">
                        <td className="py-2">{item.productName}</td>
                        <td className="py-2 text-right">{item.quantity}</td>
                        <td className="py-2 text-right">R{item.unitPrice.toFixed(2)}</td>
                        <td className="py-2 text-right">R{item.subtotal.toFixed(2)}</td>
                    </tr>
                ))}
                </tbody>
            </table>

            <p className="text-lg font-bold text-right mb-6">
                Total: R{sale.totalAmount.toFixed(2)}
            </p>

            {isAdmin && sale.status !== 'Cancelled' && (
                <button
                    onClick={() => setShowConfirm(true)}
                    className="w-full border border-red-500 text-red-600 rounded py-2 text-sm font-medium"
                >
                    Cancel Sale
                </button>
            )}

            {showConfirm && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg p-6 max-w-sm w-full">
                        <p className="mb-4">Are you sure you want to cancel this sale? This can't be undone.</p>
                        <div className="flex gap-3">
                            <button
                                onClick={() => setShowConfirm(false)}
                                className="flex-1 border rounded py-2 text-sm"
                            >
                                Back
                            </button>
                            <button
                                onClick={() => void handleCancel()}
                                disabled={cancelling}
                                className="flex-1 bg-red-600 text-white rounded py-2 text-sm disabled:opacity-50"
                            >
                                {cancelling ? 'Cancelling...' : 'Confirm'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SaleDetailPage;