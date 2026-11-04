import request from '@/utils/request'

/** 分页查询员工 → { total, rows } */
export function pageEmps(params) {
  return request.get('/emps', { params })
}

/** 查询全部员工（下拉框） */
export function listEmps() {
  return request.get('/emps/list')
}

/** 根据 id 查询员工（含 exprList 工作经历） */
export function getEmp(id) {
  return request.get(`/emps/${id}`)
}

/** 新增员工 body: emp + exprList */
export function addEmp(data) {
  return request.post('/emps', data)
}

/** 修改员工 body: emp + exprList */
export function updateEmp(data) {
  return request.put('/emps', data)
}

/** 批量删除员工 DELETE /emps?ids=1,2,3 */
export function deleteEmps(ids) {
  const value = Array.isArray(ids) ? ids.join(',') : ids
  return request.delete('/emps', { params: { ids: value } })
}
