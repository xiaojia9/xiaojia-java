import request from '@/utils/request'

/** 分页查询操作日志 → { total, rows } */
export function pageLogs(params) {
  return request.get('/log/page', { params })
}
