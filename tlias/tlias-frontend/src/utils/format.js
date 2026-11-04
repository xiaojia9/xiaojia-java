function toDate(value) {
  if (value === null || value === undefined || value === '') return null
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value
  if (typeof value === 'number') {
    const d = new Date(value)
    return Number.isNaN(d.getTime()) ? null : d
  }
  const text = String(value).trim()
  if (!text) return null
  // "2024-01-01 08:00:00" 在部分浏览器中无法直接解析，统一为 "/" 分隔
  const normalized = text.includes('T') ? text : text.replace(/-/g, '/')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

const pad = (n) => String(n).padStart(2, '0')

/**
 * 格式化为 YYYY-MM-DD
 * @param {string|number|Date} value
 * @param {string} fallback 空值占位符
 */
export function formatDate(value, fallback = '—') {
  const date = toDate(value)
  if (!date) return fallback
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

/**
 * 格式化为 YYYY-MM-DD HH:mm:ss
 * @param {string|number|Date} value
 * @param {string} fallback 空值占位符
 */
export function formatDateTime(value, fallback = '—') {
  const date = toDate(value)
  if (!date) return fallback
  return `${formatDate(date)} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/** 仅取时间部分 HH:mm:ss */
export function formatTime(value, fallback = '—') {
  const date = toDate(value)
  if (!date) return fallback
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/** 耗时展示：<1s 显示毫秒，否则显示秒 */
export function formatCost(ms) {
  const value = Number(ms)
  if (!Number.isFinite(value) || value < 0) return '—'
  if (value < 1000) return `${Math.round(value)} ms`
  return `${(value / 1000).toFixed(2)} s`
}

/** 千分位金额 */
export function formatMoney(value, fallback = '—') {
  const num = Number(value)
  if (!Number.isFinite(num)) return fallback
  return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

/** 尝试把 JSON 字符串美化输出，失败则原样返回 */
export function prettyText(value) {
  if (value === null || value === undefined || value === '') return '（空）'
  if (typeof value !== 'string') {
    try {
      return JSON.stringify(value, null, 2)
    } catch {
      return String(value)
    }
  }
  const text = value.trim()
  if (!text) return '（空）'
  try {
    return JSON.stringify(JSON.parse(text), null, 2)
  } catch {
    return value
  }
}
