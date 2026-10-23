const API_BASE_URL = '/api';

async function request(url, options = {}) {
  const response = await fetch(url, options);

  if (!response.ok) {
    let message = `HTTP ${response.status}`;

    try {
      const body = await response.json();
      message = body.message || body.detail || body.error || message;
    } catch {
      // Gunakan pesan HTTP jika respons bukan JSON.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export function getHistory() {
  return request(`${API_BASE_URL}/history`);
}

export function getHistoryDetail(id) {
  return request(`${API_BASE_URL}/history/${id}`);
}

export function profileDataset(file) {
  const formData = new FormData();
  formData.append('file', file);

  return request(`${API_BASE_URL}/datasets/profile`, {
    method: 'POST',
    body: formData,
  });
}

export function deleteHistory(id) {
  return request(`${API_BASE_URL}/history/${id}`, {
    method: 'DELETE',
  });
}

export function benchmarkDataset(
  file,
  { missingRate = 0.15, repetitions = 5, seed = 42, constantValue = 0 } = {},
) {
  const formData = new FormData();
  formData.append('file', file);

  const params = new URLSearchParams({
    missingRate: String(missingRate),
    repetitions: String(repetitions),
    seed: String(seed),
    constantValue: String(constantValue),
  });

  return request(`${API_BASE_URL}/datasets/benchmark?${params}`, {
    method: 'POST',
    body: formData,
  });

}
