import { useState } from 'react';
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';
import { benchmarkDataset, profileDataset } from '../services/api';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const MAX_FILE_SIZE = 64 * 1024 * 1024;

function formatNumber(value, digits = 2) {
  if (value === null || value === undefined || !Number.isFinite(Number(value))) {
    return '—';
  }

  return Number(value).toLocaleString('id-ID', {
    maximumFractionDigits: digits,
  });
}

function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${formatNumber(bytes / 1024, 1)} KB`;

  return `${formatNumber(bytes / (1024 * 1024), 2)} MB`;
}

function validateFile(file) {
  if (!file) return 'Pilih file CSV terlebih dahulu.';
  if (!file.name.toLowerCase().endsWith('.csv')) {
    return 'File harus menggunakan ekstensi .csv.';
  }
  if (file.size === 0) return 'File yang dipilih kosong.';
  if (file.size > MAX_FILE_SIZE) {
    return 'Ukuran file melebihi batas 64 MiB. Gunakan dataset yang lebih kecil.';
  }

  return '';
}

function Metric({ label, value, description }) {
  return (
    <article className="profile-stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
      {description && <small>{description}</small>}
    </article>
  );
}

export default function DatasetWorkspace({ onAnalysisComplete }) {
  const [file, setFile] = useState(null);

  const [profile, setProfile] = useState(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState('');

  const [missingRate, setMissingRate] = useState('0.15');
  const [repetitions, setRepetitions] = useState('5');
  const [seed, setSeed] = useState('42');
  const [constantValue, setConstantValue] = useState('0');

  const [benchmarkResult, setBenchmarkResult] = useState(null);
  const [benchmarkLoading, setBenchmarkLoading] = useState(false);
  const [benchmarkError, setBenchmarkError] = useState('');
  const [selectedColumn, setSelectedColumn] = useState('');

  const busy = profileLoading || benchmarkLoading;

  function handleFileChange(event) {
    const selectedFile = event.target.files?.[0] ?? null;

    setFile(selectedFile);
    setProfile(null);
    setProfileError('');
    setBenchmarkResult(null);
    setBenchmarkError('');
    setSelectedColumn('');
  }

  async function handleProfileSubmit(event) {
    event.preventDefault();
    setProfileError('');

    const fileError = validateFile(file);
    if (fileError) {
      setProfileError(fileError);
      return;
    }

    setProfileLoading(true);

    try {
      const result = await profileDataset(file);

      if (!result || !Array.isArray(result.columns)) {
        throw new Error('Format respons profiling tidak sesuai harapan.');
      }

      setProfile(result);
      onAnalysisComplete?.();
    } catch (err) {
      setProfileError(err.message || 'Profiling dataset gagal. Silakan coba kembali.');
    } finally {
      setProfileLoading(false);
    }
  }

  async function handleBenchmark() {
    setBenchmarkError('');

    const fileError = validateFile(file);
    if (fileError) {
      setBenchmarkError(fileError);
      return;
    }

    const rate = Number(missingRate);
    const repeatCount = Number(repetitions);
    const randomSeed = Number(seed);
    const constant = Number(constantValue);

    if (!Number.isFinite(rate) || rate <= 0 || rate >= 1) {
      setBenchmarkError('Missing rate harus lebih besar dari 0 dan lebih kecil dari 1.');
      return;
    }

    if (!Number.isInteger(repeatCount) || repeatCount < 2 || repeatCount > 20) {
      setBenchmarkError('Repetitions harus berupa bilangan bulat antara 2 dan 20.');
      return;
    }

    if (seed.trim() === '' || !Number.isSafeInteger(randomSeed)) {
      setBenchmarkError('Seed harus berupa bilangan bulat yang aman untuk JavaScript.');
      return;
    }

    if (constantValue.trim() === '' || !Number.isFinite(constant)) {
      setBenchmarkError('Constant value harus berupa angka yang valid.');
      return;
    }

    setBenchmarkLoading(true);

    try {
      const result = await benchmarkDataset(file, {
        missingRate: rate,
        repetitions: repeatCount,
        seed: randomSeed,
        constantValue: constant,
      });

      if (!result || !Array.isArray(result.columns)) {
        throw new Error('Format respons benchmark tidak sesuai harapan.');
      }

      setBenchmarkResult(result);
      setSelectedColumn(result.columns[0]?.columnName ?? '');
      onAnalysisComplete?.();
    } catch (err) {
      setBenchmarkError(err.message || 'Benchmark gagal. Silakan coba kembali.');
    } finally {
      setBenchmarkLoading(false);
    }
  }

  const profileColumns = profile?.columns ?? [];
  const totalMissing = profileColumns.reduce(
    (total, column) => total + Number(column.missingCount ?? 0),
    0,
  );
  const columnsWithMissing = profileColumns.filter(
    (column) => Number(column.missingCount ?? 0) > 0,
  ).length;

  const benchmarkColumns = benchmarkResult?.columns ?? [];
  const currentColumn = benchmarkColumns.find((column) => column.columnName === selectedColumn);
  const strategies = currentColumn?.strategies ?? [];

  const chartData = {
    labels: strategies.map((strategy) => strategy.strategy),
    datasets: [
      {
        label: 'MAE',
        data: strategies.map((strategy) => strategy.meanMae),
        backgroundColor: '#0d9488',
        borderRadius: 5,
      },
      {
        label: 'RMSE',
        data: strategies.map((strategy) => strategy.meanRmse),
        backgroundColor: '#6366f1',
        borderRadius: 5,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' },
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

  return (
    <section className="history-section workspace-section" aria-labelledby="workspace-title">
      <div className="section-header">
        <div>
          <p className="eyebrow">DATASET WORKSPACE</p>
          <h2 id="workspace-title">Dataset Workspace</h2>
          <p>Profiling kelengkapan data atau membandingkan strategi imputasi pada file CSV.</p>
        </div>
      </div>

      <form className="upload-form" onSubmit={handleProfileSubmit}>
        <div className="upload-field">
          <label htmlFor="dataset-file">Pilih file CSV</label>
          <input
            id="dataset-file"
            type="file"
            accept=".csv,text/csv"
            onChange={handleFileChange}
            disabled={busy}
          />
          <p className="upload-help">
            File maksimum 64 MiB. Pilih dataset sekali, lalu gunakan untuk profiling maupun
            benchmark.
          </p>

          {file && (
            <div className="selected-file">
              <span className="selected-file-name">{file.name}</span>
              <span>{formatFileSize(file.size)}</span>
            </div>
          )}
        </div>

        <div className="workspace-divider" />

        <div className="benchmark-config">
          <div>
            <h3>Konfigurasi benchmark</h3>
            <p>Atur proses evaluasi sebelum menjalankan benchmark.</p>
          </div>

          <div className="benchmark-form-grid">
            <div className="form-field">
              <label htmlFor="missing-rate">Missing rate</label>
              <input
                id="missing-rate"
                type="number"
                min="0.01"
                max="0.99"
                step="0.01"
                value={missingRate}
                onChange={(event) => setMissingRate(event.target.value)}
                disabled={busy}
              />
              <small>Rentang 0–1; 0,15 berarti 15%.</small>
            </div>

            <div className="form-field">
              <label htmlFor="repetitions">Repetitions</label>
              <input
                id="repetitions"
                type="number"
                min="2"
                max="20"
                step="1"
                value={repetitions}
                onChange={(event) => setRepetitions(event.target.value)}
                disabled={busy}
              />
              <small>Jumlah pengulangan eksperimen, antara 2 dan 20.</small>
            </div>

            <div className="form-field">
              <label htmlFor="seed">Seed</label>
              <input
                id="seed"
                type="number"
                step="1"
                value={seed}
                onChange={(event) => setSeed(event.target.value)}
                disabled={busy}
              />
              <small>Untuk reproduksibilitas eksperimen.</small>
            </div>

            <div className="form-field">
              <label htmlFor="constant-value">Constant value</label>
              <input
                id="constant-value"
                type="number"
                step="any"
                value={constantValue}
                onChange={(event) => setConstantValue(event.target.value)}
                disabled={busy}
              />
              <small>Nilai tetap untuk strategi constant.</small>
            </div>
          </div>
        </div>

        <div className="workspace-actions">
          <button className="workspace-primary-button" type="submit" disabled={busy || !file}>
            {profileLoading ? 'Memproses profiling…' : 'Jalankan Profiling'}
          </button>

          <button
            className="workspace-secondary-button"
            type="button"
            onClick={handleBenchmark}
            disabled={busy || !file}
          >
            {benchmarkLoading ? 'Menjalankan benchmark…' : 'Jalankan Benchmark'}
          </button>
        </div>
      </form>

      {profileError && (
        <div className="error-message workspace-message" role="alert">
          <strong>Profiling belum berhasil.</strong>
          <p>{profileError}</p>
        </div>
      )}

      {benchmarkError && (
        <div className="error-message workspace-message" role="alert">
          <strong>Benchmark belum berhasil.</strong>
          <p>{benchmarkError}</p>
        </div>
      )}

      {profileLoading && (
        <p className="status-message" role="status">
          Sedang membaca dan menganalisis file CSV. Mohon tunggu…
        </p>
      )}

      {benchmarkLoading && (
        <p className="status-message" role="status">
          Benchmark berjalan. Waktu pemrosesan bergantung pada ukuran dataset dan jumlah
          pengulangan.
        </p>
      )}

      {profile && (
        <div className="profile-results" aria-live="polite">
          <div className="profile-result-heading">
            <div>
              <p className="eyebrow">PROFILING SELESAI</p>
              <h3>{profile.fileName || file?.name || 'Dataset'}</h3>
              <p>Ringkasan struktur dan kelengkapan dataset.</p>
            </div>
            <span className="profile-status">Berhasil</span>
          </div>

          <div className="profile-summary-grid">
            <Metric label="Jumlah baris" value={formatNumber(profile.rowCount, 0)} />
            <Metric label="Jumlah kolom" value={formatNumber(profile.columnCount, 0)} />
            <Metric label="Total nilai kosong" value={formatNumber(totalMissing, 0)} />
          </div>

          <div className="profile-table-heading">
            <div>
              <h3>Kelengkapan setiap kolom</h3>
              <p>
                {formatNumber(columnsWithMissing, 0)} dari {formatNumber(profileColumns.length, 0)}{' '}
                kolom memiliki nilai kosong.
              </p>
            </div>
          </div>

          {profileColumns.length > 0 ? (
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
                  {profileColumns.map((column) => (
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
            <p className="status-message">Tidak ada informasi kolom dalam hasil profiling.</p>
          )}

          <p className="profile-footnote">
            Hasil profiling telah disimpan dan tersedia melalui Riwayat Analisis.
          </p>
        </div>
      )}

      {benchmarkResult && (
        <div className="profile-results benchmark-results" aria-live="polite">
          <div className="profile-result-heading">
            <div>
              <p className="eyebrow">BENCHMARK SELESAI</p>
              <h3>{benchmarkResult.fileName || file?.name || 'Dataset'}</h3>
              <p>Perbandingan hasil strategi imputasi pada kolom numerik yang dapat dievaluasi.</p>
            </div>
            <span className="profile-status">Berhasil</span>
          </div>

          <div className="profile-summary-grid">
            <Metric
              label="Missing rate"
              value={`${formatNumber(benchmarkResult.missingRate * 100)}%`}
            />
            <Metric label="Repetitions" value={formatNumber(benchmarkResult.repetitions, 0)} />
            <Metric label="Seed" value={formatNumber(benchmarkResult.seed, 0)} />
            <Metric label="Constant value" value={formatNumber(benchmarkResult.constantValue)} />
          </div>

          {benchmarkResult.skippedColumns?.length > 0 && (
            <details className="skipped-columns-message">
              <summary>
                Kolom dilewati ({benchmarkResult.skippedColumns.length})
              </summary>
              <ul>
                {benchmarkResult.skippedColumns.map((column) => (
                  <li key={column}>{column}</li>
                ))}
              </ul>
            </details>
          )}

          {benchmarkColumns.length === 0 ? (
            <p className="status-message">
              Benchmark selesai, tetapi tidak ada kolom yang menghasilkan metrik. Periksa tipe data
              pada dataset.
            </p>
          ) : (
            <>
              <div className="column-selector">
                <label htmlFor="benchmark-column-select">Kolom dataset</label>
                <select
                  id="benchmark-column-select"
                  value={selectedColumn}
                  onChange={(event) => setSelectedColumn(event.target.value)}
                >
                  {benchmarkColumns.map((column) => (
                    <option key={column.columnName} value={column.columnName}>
                      {column.columnName}
                    </option>
                  ))}
                </select>
                <span>
                  {currentColumn
                    ? `${formatNumber(currentColumn.observedCount, 0)} nilai terobservasi`
                    : ''}
                </span>
              </div>

              {strategies.length > 0 && (() => {
                const best = strategies.reduce((a, b) =>
                  a.meanMae < b.meanMae ? a : b
                );

                return (
                  <div className="best-strategy-card">
                    <strong>Strategi terbaik: {best.strategy}</strong>
                    <p>
                      Mean MAE: {formatNumber(best.meanMae)} ·
                      Mean RMSE: {formatNumber(best.meanRmse)}
                    </p>
                    <small>
                      Dipilih berdasarkan Mean MAE terendah pada kolom {selectedColumn}.
                    </small>
                  </div>
                );
              })()}

              <h3 className="subsection-title">Perbandingan MAE dan RMSE</h3>
              <p className="section-description">
                Nilai lebih rendah menunjukkan kesalahan imputasi yang lebih kecil. MAE dan RMSE
                ditampilkan berdampingan untuk kolom terpilih.
              </p>

              <div className="benchmark-chart-container">
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

          <p className="profile-footnote">
            Hasil benchmark telah disimpan dan dapat dibuka kembali melalui Riwayat Analisis.
          </p>
        </div>
      )}
    </section>
  );
}
