import request from '@/utils/request'

/** 分页查询班级 → { total, rows } */
export function pageClazzs(params) {
  return request.get('/clazzs', { params })
}

/** 查询全部班级（下拉框） */
export function listClazzs() {
  return request.get('/clazzs/list')
}

/** 根据 id 查询班级 */
export function getClazz(id) {
  return request.get(`/clazzs/${id}`)
}

/** 新增班级 */
export function addClazz(data) {
  return request.post('/clazzs', data)
}

/** 修改班级 */
export function updateClazz(data) {
  return request.put('/clazzs', data)
}

/** 删除班级 DELETE /clazzs/{id} */
export function deleteClazz(id) {
  return request.delete(`/clazzs/${id}`)
}
