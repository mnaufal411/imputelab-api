import { useEffect, useState } from 'react';
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';
import {
  deleteHistory,
  getHistory,
  getHistoryDetail,
} from './services/api';
import DatasetWorkspace from './components/DatasetWorkspace';
import './App.css';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

function formatDate(value) {
  if (!value) return '—';

  return new Date(value).toLocaleString('id-ID', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function formatNumber(value, digits = 2) {
  if (value === null || value === undefined) return '—';

  return Number(value).toLocaleString('id-ID', {
    maximumFractionDigits: digits,
  });
}

function MetricCard({ label, value, description }) {
  return (
    <article className="stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{description}</small>
    </article>
  );
}

function App() {
  const [history, setHistory] = useState([]);
  const [historyRefreshKey, setHistoryRefreshKey] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedId, setSelectedId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [selectedColumn, setSelectedColumn] = useState('');
  const [historyType, setHistoryType] = useState('PROFILE');
  const [deletingId, setDeletingId] = useState(null);
  const [deleteError, setDeleteError] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);

  useEffect(() => {
    let active = true;

    async function loadHistory() {
      try {
        const data = await getHistory();

        if (active) {
          setHistory(data);
          setError('');
        }
      } catch (err) {
        if (active) {
          setError(err.message || 'Gagal mengambil riwayat analisis.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadHistory();

    return () => {
      active = false;
    };
  }, [historyRefreshKey]);

  useEffect(() => {
    if (selectedId === null) {
      return undefined;
    }

    let active = true;

    async function loadDetail() {
      setDetailLoading(true);
      setDetail(null);
      setDetailError('');

      try {
        const data = await getHistoryDetail(selectedId);

        if (active) {
          setDetail(data);
          setSelectedColumn(data.columns?.[0]?.columnName ?? '');
        }
      } catch (err) {
        if (active) {
          setDetailError(err.message || 'Gagal mengambil detail analisis.');
        }
      } finally {
        if (active) {
          setDetailLoading(false);
        }
      }
    }

    loadDetail();

    return () => {
      active = false;
    };
  }, [selectedId]);

  const benchmarkCount = history.filter((item) => item.analysisType === 'BENCHMARK').length;

  const profileCount = history.filter((item) => item.analysisType === 'PROFILE').length;
  const filteredHistory = history.filter((item) => item.analysisType === historyType);
  const columns = detail?.columns ?? [];
  const currentColumn = columns.find((column) => column.columnName === selectedColumn);
  const strategies = currentColumn?.strategies ?? [];

  const chartData = {
    labels: strategies.map((item) => item.strategy),
    datasets: [
      {
        label: 'MAE',
        data: strategies.map((item) => item.meanMae),
        backgroundColor: '#0d9488',
        borderRadius: 5,
      },
      {
        label: 'RMSE',
        data: strategies.map((item) => item.meanRmse),
        backgroundColor: '#6366f1',
        borderRadius: 5,
      },
    ],
  };


  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
      },
      tooltip: {
        callbacks: {
          label: (context) => `${context.dataset.label}: ${formatNumber(context.parsed.y)}`,
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value) => formatNumber(value),
        },
      },
    },
  };

  function handleDeleteHistory(item) {
    setDeleteError('');
    setDeleteTarget(item);
  }



  async function confirmDeleteHistory() {
    if (!deleteTarget) return;

    const item = deleteTarget;
    setDeletingId(item.id);
    setDeleteError('');

    try {
      await deleteHistory(item.id);

      setHistory((current) =>
        current.filter((entry) => entry.id !== item.id)
      );

      if (String(selectedId) === String(item.id)) {
        setSelectedId(null);
        setDetail(null);
      }

      setDeleteTarget(null);
    } catch (err) {
      setDeleteError(err.message || 'Gagal menghapus riwayat.');
    } finally {
      setDeletingId(null);
    }
  }

  return (

    <main className="dashboard">
      <header className="page-header">
        <div>
          <p className="eyebrow">DATA QUALITY WORKSPACE</p>
          <h1>ImputeLab</h1>
          <p className="subtitle">Explore missing data and evaluate imputation strategies.</p>
        </div>

        <button className="refresh-button" onClick={() => window.location.reload()}>
          Refresh
        </button>
      </header>

      <section className="stats-grid" aria-label="Ringkasan analisis">
        <MetricCard
          label="Total Analisis"
          value={loading ? '…' : history.length}
          description="Seluruh riwayat tersimpan"
        />
        <MetricCard
          label="Benchmark"
          value={loading ? '…' : benchmarkCount}
          description="Evaluasi strategi imputasi"
        />
        <MetricCard
          label="Profil Dataset"
          value={loading ? '…' : profileCount}
          description="Analisis struktur CSV"
        />
      </section>

      <DatasetWorkspace onAnalysisComplete={() => setHistoryRefreshKey((value) => value + 1)} />

      <section className="history-section">
        <div className="section-header">
          <div>
            <h2>Riwayat Analisis</h2>
            <p>Pilih jenis analisis untuk melihat rekaman dan detailnya.</p>
          </div>
          <span className="record-count">
            {loading ? 'Memuat…' : `${filteredHistory.length} rekaman`}
          </span>
        </div>

        <div className="history-switch" role="group" aria-label="Jenis riwayat analisis">
          <button
            type="button"
            className={`history-switch-button ${historyType === 'PROFILE' ? 'active' : ''}`}
            aria-pressed={historyType === 'PROFILE'}
            onClick={() => setHistoryType('PROFILE')}
          >
            Profiling <span>{profileCount}</span>
          </button>
          <button
            type="button"
            className={`history-switch-button ${historyType === 'BENCHMARK' ? 'active' : ''}`}
            aria-pressed={historyType === 'BENCHMARK'}
            onClick={() => setHistoryType('BENCHMARK')}
          >
            Benchmark <span>{benchmarkCount}</span>
          </button>
        </div>

        {error && (
          <div className="error-message" role="alert">
            <strong>Gagal memuat data.</strong>
            <p>{error}</p>
            <p>Pastikan Spring Boot berjalan di port 8080.</p>
          </div>
        )}

        {loading && <p className="status-message">Memuat riwayat analisis…</p>}

        {!loading && !error && filteredHistory.length === 0 && (
          <p className="status-message">
            Belum ada riwayat {historyType === 'PROFILE' ? 'profiling' : 'benchmark'}.
          </p>
        )}

        {!loading && !error && filteredHistory.length > 0 && (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nama File</th>
                  <th>Baris</th>
                  <th>Kolom</th>
                  {historyType === 'BENCHMARK' && <th>Missing Rate</th>}
                  <th>Tanggal</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {filteredHistory.map((item) => (
                  <tr key={item.id}>
                    <td>#{item.id}</td>
                    <td className="file-name">{item.fileName || '—'}</td>
                    <td>{formatNumber(item.rowCount, 0)}</td>
                    <td>{formatNumber(item.columnCount, 0)}</td>

                    {historyType === 'BENCHMARK' && (
                      <td>
                        {item.missingRate == null
                          ? '—'
                          : `${formatNumber(item.missingRate * 100)}%`}
                      </td>
                    )}

                    <td>{formatDate(item.createdAt)}</td>

                    <td>
                      <button
                        className="detail-button"
                        onClick={() => setSelectedId(item.id)}
                      >
                        Lihat detail
                      </button>

                      <button
                        className="delete-button"
                        onClick={() => handleDeleteHistory(item)}
                        disabled={deletingId === item.id}
                      >
                        {deletingId === item.id ? 'Menghapus…' : 'Hapus'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {deleteTarget && (
        <div
          className="modal-overlay"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget && !deletingId) {
              setDeleteTarget(null);
            }
          }}
        >
          <section
            className="delete-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-modal-title"
          >
            <div className="delete-modal-icon" aria-hidden="true">
              <span>!</span>
            </div>

            <h2 id="delete-modal-title">Hapus riwayat analisis?</h2>

            <p>
              Riwayat berikut akan dihapus secara permanen beserta hasil analisisnya.
            </p>

            <div className="delete-file-preview">
              <strong>{deleteTarget.fileName}</strong>
              <span>
                ID #{deleteTarget.id} ·{' '}
                {deleteTarget.analysisType === 'PROFILE' ? 'Profiling' : 'Benchmark'}
              </span>
            </div>

            {deleteError && (
              <div className="error-message" role="alert">
                {deleteError}
              </div>
            )}

            <div className="delete-modal-actions">
              <button
                className="cancel-delete-button"
                disabled={deletingId !== null}
                onClick={() => setDeleteTarget(null)}
              >
                Batal
              </button>

              <button
                className="confirm-delete-button"
                disabled={deletingId !== null}
                onClick={confirmDeleteHistory}
              >
                {deletingId !== null ? 'Menghapus…' : 'Ya, hapus'}
              </button>
            </div>
          </section>
        </div>
      )}

      {selectedId !== null && (
        <section className="history-section detail-section">
          <div className="section-header">
            <div>
              <p className="eyebrow">ANALYSIS DETAIL · #{selectedId}</p>
              <h2>{detail?.fileName ?? 'Detail Analisis'}</h2>
              <p>Metadata dan hasil evaluasi strategi imputasi.</p>
            </div>
            <button
              className="refresh-button"
              onClick={() => {
                setSelectedId(null);
                setDetail(null);
              }}
            >
              Tutup
            </button>
          </div>

          {detailLoading && <p className="status-message">Memuat detail analisis…</p>}

          {detailError && (
            <div className="error-message" role="alert">
              {detailError}
            </div>
          )}

          {detail && (
            <>
              <div className="detail-meta">
                <div>
                  <span>Tipe analisis</span>
                  <strong>{detail.analysisType}</strong>
                </div>
                <div>
                  <span>Jumlah baris</span>
                  <strong>{formatNumber(detail.rowCount, 0)}</strong>
                </div>
                <div>
                  <span>Jumlah kolom</span>
                  <strong>{formatNumber(detail.columnCount, 0)}</strong>
                </div>

                {detail.analysisType === 'BENCHMARK' && (
                  <>
                    <div>
                      <span>Missing rate</span>
                      <strong>
                        {detail.missingRate == null
                          ? '—'
                          : `${formatNumber(detail.missingRate * 100)}%`}
                      </strong>
                    </div>
                    <div>
                      <span>Repetitions</span>
                      <strong>{formatNumber(detail.repetitions, 0)}</strong>
                    </div>
                    <div>
                      <span>Seed</span>
                      <strong>{formatNumber(detail.seed, 0)}</strong>
                    </div>
                    <div>
                      <span>Constant value</span>
                      <strong>{formatNumber(detail.constantValue)}</strong>
                    </div>
                  </>
                )}

                <div>
                  <span>Waktu analisis</span>
                  <strong>{formatDate(detail.createdAt)}</strong>
                </div>
              </div>

              {detail.analysisType === 'PROFILE' ? (
                <div className="profile-history-results">
                  <h3 className="subsection-title">Kelengkapan setiap kolom</h3>
                  <p className="section-description">
                    Ringkasan jumlah nilai kosong dan nilai terisi yang tersimpan saat profiling.
                  </p>

                  {detail.profileColumns?.length > 0 ? (
                    <div className="table-wrapper">
                      <table>
                        <thead>
                          <tr>
                            <th>Nama kolom</th>
                            <th>Nilai kosong</th>
                            <th>Nilai terisi</th>
                            <th>Persentase kosong</th>
                          </tr>
                        </thead>
                        <tbody>
                          {detail.profileColumns.map((column) => (
                            <tr key={column.columnName}>
                              <td className="file-name">{column.columnName}</td>
                              <td>{formatNumber(column.missingCount, 0)}</td>
                              <td>{formatNumber(column.nonMissingCount, 0)}</td>
                              <td>
                                <span
                                  className={
                                    Number(column.missingPercentage) > 0
                                      ? 'missing-percentage has-missing'
                                      : 'missing-percentage'
                                  }
                                >
                                  {formatNumber(column.missingPercentage)}%
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <p className="status-message">
                      Riwayat ini belum menyimpan rincian profil setiap kolom. Jalankan profiling
                      ulang untuk menyimpan hasil lengkap.
                    </p>
                  )}
                </div>
              ) : columns.length === 0 ? (
                <p className="status-message">
                  Tidak ada hasil kolom yang tersimpan untuk analisis ini.
                </p>
              ) : (
                <>
                  <div className="column-selector">
                    <label htmlFor="column-select">Kolom dataset</label>
                    <select
                      id="column-select"
                      value={selectedColumn}
                      onChange={(event) => setSelectedColumn(event.target.value)}
                    >
                      {columns.map((column) => (
                        <option key={column.columnName} value={column.columnName}>
                          {column.columnName}
                        </option>
                      ))}
                    </select>
                    <span>
                      {currentColumn ? `${currentColumn.observedCount} nilai terobservasi` : ''}
                    </span>
                  </div>

                  <h3 className="subsection-title">Perbandingan MAE dan RMSE</h3>
                  <p className="section-description">
                    Nilai lebih rendah menunjukkan kesalahan imputasi yang lebih kecil pada kolom
                    terpilih.
                  </p>

                  <div className="chart-container">
                    {strategies.length > 0 ? (
                      <Bar data={chartData} options={chartOptions} />
                    ) : (
                      <p className="status-message">Tidak ada metrik strategi untuk kolom ini.</p>
                    )}
                  </div>

                  <h3 className="subsection-title">Rincian strategi</h3>
                  <div className="table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Strategi</th>
                          <th>Evaluasi</th>
                          <th>Mean MAE</th>
                          <th>Mean RMSE</th>
                          <th>Std. Dev. RMSE</th>
                        </tr>
                      </thead>
                      <tbody>
                        {strategies.map((strategy) => (
                          <tr key={strategy.strategy}>
                            <td className="file-name">{strategy.strategy}</td>
                            <td>{formatNumber(strategy.evaluatedCount, 0)}</td>
                            <td>{formatNumber(strategy.meanMae)}</td>
                            <td>{formatNumber(strategy.meanRmse)}</td>
                            <td>{formatNumber(strategy.rmseStdDev)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </>
          )}
        </section>
      )}

      <footer className="page-footer">
        ImputeLab · Dataset profiling and imputation benchmarking
      </footer>
    </main>
  );
}

export default App;
