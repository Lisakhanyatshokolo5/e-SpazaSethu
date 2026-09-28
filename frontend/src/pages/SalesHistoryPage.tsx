import { useState, useCallback, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import type { Sale, SalesReportSummary, PaginatedResponse } from '../types';

type Preset = 'today' | 'yesterday' | 'last7' | 'custom';

const toDateStr = (d: Date) => d.toISOString().split('T')[0];

const getRangeForPreset = (preset: Preset): { from: string; to: string } => {
    const today = new Date();
    if (preset === 'today') return { from: toDateStr(today), to: toDateStr(today) };
    if (preset === 'yesterday') {
        const y = new Date(today);
        y.setDate(y.getDate() - 1);
        return { from: toDateStr(y), to: toDateStr(y) };
    }
    const weekAgo = new Date(today);
    weekAgo.setDate(weekAgo.getDate() - 6);
    return { from: toDateStr(weekAgo), to: toDateStr(today) };
};

const statusBadgeClass = (status: string) =>
    status === 'Completed' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700';

const SalesHistoryPage = () => {
    const [preset, setPreset] = useState<Preset>('today');
    const [from, setFrom] = useState(() => getRangeForPreset('today').from);
    const [to, setTo] = useState(() => getRangeForPreset('today').to);
    const [sales, setSales] = useState<Sale[]>([]);
    const [summary, setSummary] = useState<SalesReportSummary | null>(null);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [loading, setLoading] = useState(true);

    const applyPreset = (p: Preset) => {
        setPreset(p);
        if (p !== 'custom') {
            const range = getRangeForPreset(p);
            setFrom(range.from);
            setTo(range.to);
        }
        setPage(1);
    };

    const loadData = useCallback(async (pageNum: number, append: boolean) => {
        setLoading(true);
        try {
            const [salesRes, summaryRes] = await Promise.all([
                api.get<PaginatedResponse<Sale>>(`/sales?from=${from}&to=${to}&page=${pageNum}`),
                api.get<SalesReportSummary>(`/reports/summary?from=${from}&to=${to}`),
            ]);
            setSales((prev) => (append ? [...prev, ...salesRes.data.data] : salesRes.data.data));
            setTotalPages(salesRes.data.totalPages);
            setSummary(summaryRes.data);
        } finally {
            setLoading(false);
        }
    }, [from, to]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- initial data fetch requires a loading flag
        void loadData(1, false);
    }, [loadData]);

    const handleLoadMore = () => {
        const next = page + 1;
        setPage(next);
        void loadData(next, true);
    };

    return (
        <div className="p-4 pb-24">
            <h1 className="text-xl font-semibold mb-4">Sales History</h1>

            <div className="flex gap-2 mb-3 flex-wrap">
                {(['today', 'yesterday', 'last7'] as Preset[]).map((p) => (
                    <button
                        key={p}
                        onClick={() => applyPreset(p)}
                        className={`px-3 py-1.5 rounded-full text-sm ${
                            preset === p ? 'bg-blue-600 text-white' : 'bg-gray-100'
                        }`}
                    >
                        {p === 'today' ? 'Today' : p === 'yesterday' ? 'Yesterday' : 'Last 7 Days'}
                    </button>
                ))}
            </div>

            <div className="flex gap-2 items-center mb-4 text-sm">
                <input
                    type="date"
                    value={from}
                    onChange={(e) => { setFrom(e.target.value); setPreset('custom'); setPage(1); }}
                    className="border rounded p-2"
                />
                <span>to</span>
                <input
                    type="date"
                    value={to}
                    onChange={(e) => { setTo(e.target.value); setPreset('custom'); setPage(1); }}
                    className="border rounded p-2"
                />
            </div>

            {summary && (
                <div className="flex gap-4 bg-gray-50 rounded-lg p-3 mb-4 text-sm">
                    <div>
                        <p className="text-gray-500">Total sales</p>
                        <p className="font-bold">R{summary.totalAmount.toFixed(2)}</p>
                    </div>
                    <div>
                        <p className="text-gray-500">Total profit</p>
                        <p className="font-bold">R{summary.totalProfit.toFixed(2)}</p>
                    </div>
                </div>
            )}

            {sales.length === 0 && !loading ? (
                <p className="text-sm text-gray-500">No sales found for this period.</p>
            ) : (
                <ul className="divide-y">
                    {sales.map((sale) => (
                        <li key={sale.saleId}>
                            <Link to={`/sales/${sale.saleId}`} className="block py-3">
                                <div className="flex justify-between text-sm mb-1">
                  <span>
                    {new Date(sale.saleDateTime).toLocaleDateString('en-ZA', {
                        weekday: 'short', day: 'numeric', month: 'short',
                    })}, {new Date(sale.saleDateTime).toLocaleTimeString('en-ZA', { hour: '2-digit', minute: '2-digit' })}
                  </span>
                                    <span className="font-semibold">R{sale.totalAmount.toFixed(2)}</span>
                                </div>
                                <div className="flex justify-between text-xs text-gray-500">
                                    <span>{sale.cashierName} · {sale.paymentMethod}</span>
                                    <span className={`px-2 py-0.5 rounded-full ${statusBadgeClass(sale.status)}`}>
                    {sale.status}
                  </span>
                                </div>
                            </Link>
                        </li>
                    ))}
                </ul>
            )}

            {page < totalPages && (
                <button
                    onClick={handleLoadMore}
                    disabled={loading}
                    className="w-full mt-4 py-2 rounded border text-sm"
                >
                    {loading ? 'Loading...' : 'Load more'}
                </button>
            )}
        </div>
    );
};

export default SalesHistoryPage;