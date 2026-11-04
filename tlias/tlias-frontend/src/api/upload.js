import request from '@/utils/request'
import { getToken } from '@/utils/auth'

/** el-upload 需要的请求头（携带 token） */
export function getUploadHeaders() {
  const token = getToken()
  return token ? { token } : {}
}

/** 独立上传方法（不依赖 el-upload 时使用）→ data: 图片 URL */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
