// Backendi API kutsed. /api suunatakse vite.config.js proxy kaudu Spring Bootile (port 8080).

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.detail || body?.error || `Request failed with status ${status}`)
    this.status = status
    this.body = body
  }

  get guardrail() {
    return this.body?.guardrail ?? null
  }
}

async function request(url, options) {
  const res = await fetch(url, options)
  const body = await res.json().catch(() => null)
  if (!res.ok) {
    throw new ApiError(res.status, body)
  }
  return body
}

export function sendMessage(message) {
  return request('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message })
  })
}

export function fetchJokes() {
  return request('/api/jokes')
}

// Tagastab null, kui backend jõusaali ei leidnud (204 No Content)
export function fetchRandomGym() {
  return request('/api/gyms/random')
}
