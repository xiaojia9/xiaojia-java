import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, clearAuth } from '@/utils/auth'

/**
 * 统一请求实例
 * - baseURL: /api（由 Vite dev proxy 转发到 http://localhost:8080）
 * - 请求头携带 token: <jwt>
 * - 响应拦截：code === 1 时直接返回 data；code === 0 时提示并 reject
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 20000
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.token = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

let redirecting = false

function toLogin(message) {
  clearAuth()
  if (redirecting) return
  redirecting = true
  if (message) ElMessage.error(message)
  const current = router.currentRoute.value
  if (current.path !== '/login') {
    const redirect = current.fullPath && current.fullPath !== '/' ? current.fullPath : undefined
    router.replace({ path: '/login', query: redirect ? { redirect } : {} })
  }
  window.setTimeout(() => {
    redirecting = false
  }, 1200)
}

request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 非包装结构（例如文件流）直接返回
    if (!body || typeof body !== 'object' || !('code' in body)) {
      return body
    }
    if (body.code === 1) {
      return body.data
    }
    const msg = body.msg || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      toLogin('登录状态已失效，请重新登录')
      return Promise.reject(error)
    }
    const msg =
      error?.response?.data?.msg ||
      (status ? `请求失败（HTTP ${status}）` : '网络异常，请确认后端服务（http://localhost:8080）已启动')
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
