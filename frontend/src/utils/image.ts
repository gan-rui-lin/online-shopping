const API_BASE = import.meta.env.VITE_API_BASE_URL || ''
const FILE_BASE = import.meta.env.VITE_FILE_BASE_URL || ''

function normalizeBaseUrl(baseUrl: string): string {
  if (!baseUrl) return ''
  return baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
}

function pickBase(url: string): string {
  if (url.startsWith('/cdn/images')) {
    return normalizeBaseUrl(FILE_BASE) || normalizeBaseUrl(API_BASE)
  }
  return normalizeBaseUrl(API_BASE)
}

function joinUrl(base: string, path: string): string {
  if (!base) return path
  if (path.startsWith('/')) return `${base}${path}`
  return `${base}/${path}`
}

function normalizeImagePath(rawUrl: string): string {
  const normalized = rawUrl
    .replace(/\\/g, '/')
    .trim()
    .replace(/^['"]+|['"]+$/g, '')
    .trim()
  if (!normalized) return ''

  if (normalized.startsWith('/cdn/images') || normalized.startsWith('cdn/images/')) {
    return normalized.startsWith('/') ? normalized : `/${normalized}`
  }

  // Backward compatibility for DB values like uploads/images/xx.jpg
  if (normalized.startsWith('/uploads/images/') || normalized.startsWith('uploads/images/')) {
    const suffix = normalized.replace(/^\/?uploads\/images\//, '')
    return `/cdn/images/${suffix}`
  }

  // Bare filename fallback, e.g. AirPods Pro 2.jpg
  if (!normalized.startsWith('/')) {
    return `/cdn/images/${normalized}`
  }

  return normalized
}

export function resolveImageUrl(url?: string): string {
  if (!url) return ''

  // ✅ 已经是完整 URL（http/https）
  if (/^https?:\/\//i.test(url)) {
    try {
      const parsed = new URL(url)
      parsed.pathname = encodeURI(parsed.pathname)
      return parsed.toString()
    } catch {
      return url
    }
  }

  const normalizedPath = normalizeImagePath(url)
  const base = pickBase(normalizedPath)
  return joinUrl(base, encodeURI(normalizedPath))
}

export function resolveImageUrls(urls?: string[]): string[] {
  if (!urls || !urls.length) return []
  return urls.map((url) => resolveImageUrl(url))
}