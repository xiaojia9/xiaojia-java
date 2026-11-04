import request from '@/utils/request'

/** 员工性别分布 → [{ name, value }] */
export function getEmpGenderData() {
  return request.get('/report/empGenderData')
}

/** 员工职位分布 → { jobList, dataList } */
export function getEmpJobData() {
  return request.get('/report/empJobData')
}

/** 学员学历分布 → [{ name, value }] */
export function getStudentDegreeData() {
  return request.get('/report/studentDegreeData')
}

/** 班级人数统计 → { clazzList, dataList } */
export function getStudentCountData() {
  return request.get('/report/studentCountData')
}
