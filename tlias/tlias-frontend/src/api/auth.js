import request from '@/utils/request'

/** 登录 → data: { id, username, name, token } */
export function login(data) {
  return request.post('/login', data)
}
