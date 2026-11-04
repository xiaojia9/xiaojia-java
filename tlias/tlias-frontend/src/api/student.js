import request from '@/utils/request'

/** 分页查询学员 → { total, rows } */
export function pageStudents(params) {
  return request.get('/students', { params })
}

/** 根据 id 查询学员 */
export function getStudent(id) {
  return request.get(`/students/${id}`)
}

/** 新增学员 */
export function addStudent(data) {
  return request.post('/students', data)
}

/** 修改学员 */
export function updateStudent(data) {
  return request.put('/students', data)
}

/** 批量删除学员 DELETE /students/1,2,3 */
export function deleteStudents(ids) {
  const value = Array.isArray(ids) ? ids.join(',') : ids
  return request.delete(`/students/${value}`)
}

/** 违纪处理 PUT /students/violation/{id}/{score} */
export function updateViolation(id, score) {
  return request.put(`/students/violation/${id}/${score}`)
}
