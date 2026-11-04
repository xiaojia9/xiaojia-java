import request from '@/utils/request'

/** 查询全部部门 → [{ id, name, createTime, updateTime }] */
export function getDepts() {
  return request.get('/depts')
}

/** 新增部门 body: { name } */
export function addDept(data) {
  return request.post('/depts', data)
}

/** 修改部门 body: { id, name } */
export function updateDept(data) {
  return request.put('/depts', data)
}

/** 根据 id 查询部门 */
export function getDept(id) {
  return request.get(`/depts/${id}`)
}

/** 删除部门 DELETE /depts?id=1 */
export function deleteDept(id) {
  return request.delete('/depts', { params: { id } })
}
