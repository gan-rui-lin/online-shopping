import dayjs from 'dayjs'

export function formatPrice(price: number | undefined | null): string {
  if (price == null) return '¥0.00'
  return `¥${Number(price).toFixed(2)}`
}

export function formatDate(date: string | undefined | null, template = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return ''
  return dayjs(date).format(template)
}

export function formatDateShort(date: string | undefined | null): string {
  return formatDate(date, 'YYYY-MM-DD')
}

export function parseSpecJson(specJson: string | undefined | null): Record<string, string> {
  if (!specJson) return {}
  try {
    return JSON.parse(specJson)
  } catch {
    return {}
  }
}

export function formatSpec(specJson: string | undefined | null): string {
  const specs = parseSpecJson(specJson)
  return Object.entries(specs)
    .map(([k, v]) => `${k}: ${v}`)
    .join(', ')
}
