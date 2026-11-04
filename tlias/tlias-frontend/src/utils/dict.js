/**
 * 枚举字典：后端可能返回数字或字符串，统一按字符串匹配
 */
export const GENDER_OPTIONS = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

export const JOB_OPTIONS = [
  { label: '班主任', value: 1 },
  { label: '讲师', value: 2 },
  { label: '学工主管', value: 3 },
  { label: '教研主管', value: 4 },
  { label: '咨询师', value: 5 }
]

export const SUBJECT_OPTIONS = [
  { label: 'Java', value: 1 },
  { label: '前端', value: 2 },
  { label: '大数据', value: 3 },
  { label: 'Python', value: 4 },
  { label: 'Go', value: 5 },
  { label: '嵌入式', value: 6 }
]

export const STATUS_OPTIONS = [
  { label: '未开班', value: '未开班' },
  { label: '已开班', value: '已开班' },
  { label: '已结课', value: '已结课' }
]

export const DEGREE_OPTIONS = [
  { label: '初中', value: 1 },
  { label: '高中', value: 2 },
  { label: '大专', value: 3 },
  { label: '本科', value: 4 },
  { label: '硕士', value: 5 },
  { label: '博士', value: 6 }
]

export const IS_COLLEGE_OPTIONS = [
  { label: '是', value: 1 },
  { label: '否', value: 0 }
]

function makeTextMap(options) {
  const map = {}
  options.forEach((item) => {
    map[String(item.value)] = item.label
  })
  return map
}

const GENDER_MAP = makeTextMap(GENDER_OPTIONS)
const JOB_MAP = makeTextMap(JOB_OPTIONS)
const SUBJECT_MAP = makeTextMap(SUBJECT_OPTIONS)
const DEGREE_MAP = makeTextMap(DEGREE_OPTIONS)

function textOf(map, value, fallback = '—') {
  if (value === null || value === undefined || value === '') return fallback
  return map[String(value)] || String(value)
}

export const genderText = (v) => textOf(GENDER_MAP, v)
export const jobText = (v) => textOf(JOB_MAP, v)
export const subjectText = (v) => textOf(SUBJECT_MAP, v)
export const degreeText = (v) => textOf(DEGREE_MAP, v)
export const isCollegeText = (v) => (String(v) === '1' ? '是' : '否')

export function statusText(value) {
  if (!value) return '未开班'
  return String(value)
}

/** 班级状态对应的 el-tag 类型：未开班 info / 已开班 success / 已结课 warning */
export function statusTagType(status) {
  switch (statusText(status)) {
    case '已开班':
      return 'success'
    case '已结课':
      return 'warning'
    default:
      return 'info'
  }
}

export function jobTagType(job) {
  switch (String(job)) {
    case '1':
      return 'warning'
    case '2':
      return 'primary'
    case '3':
      return 'success'
    case '4':
      return 'danger'
    case '5':
      return 'info'
    default:
      return 'info'
  }
}

export function genderTagType(gender) {
  return String(gender) === '2' ? 'danger' : 'primary'
}

/** 学历标签配色（由低到高） */
export function degreeTagType(degree) {
  switch (String(degree)) {
    case '1':
    case '2':
      return 'info'
    case '3':
      return 'primary'
    case '4':
      return 'success'
    case '5':
      return 'warning'
    case '6':
      return 'danger'
    default:
      return 'info'
  }
}
