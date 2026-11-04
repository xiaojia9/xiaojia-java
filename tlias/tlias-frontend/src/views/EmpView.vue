<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Camera,
  Delete,
  Edit,
  OfficeBuilding,
  Plus,
  Refresh,
  Search,
  UserFilled
} from '@element-plus/icons-vue'
import PageHero from '@/components/PageHero.vue'
import {
  addEmp,
  deleteEmps,
  getEmp,
  pageEmps,
  updateEmp
} from '@/api/emp'
import { getDepts } from '@/api/dept'
import { getToken } from '@/utils/auth'
import { formatDate, formatMoney } from '@/utils/format'
import {
  GENDER_OPTIONS,
  JOB_OPTIONS,
  genderText,
  jobTagType,
  jobText
} from '@/utils/dict'

/* ------------------------------ 列表 ------------------------------ */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selection = ref([])
const tableRef = ref(null)
const depts = ref([])

const query = reactive({
  name: '',
  gender: '',
  dateRange: []
})

const page = ref(1)
const pageSize = ref(10)

function buildParams() {
  const params = { page: page.value, pageSize: pageSize.value }
  if (query.name.trim()) params.name = query.name.trim()
  if (query.gender !== '' && query.gender !== null && query.gender !== undefined) {
    params.gender = query.gender
  }
  const [begin, end] = Array.isArray(query.dateRange) ? query.dateRange : []
  if (begin) params.begin = begin
  if (end) params.end = end
  return params
}

async function loadEmps() {
  loading.value = true
  try {
    const data = await pageEmps(buildParams())
    list.value = Array.isArray(data?.rows) ? data.rows : []
    total.value = Number(data?.total) || 0
    // 当前页被删空时自动回退一页
    if (!list.value.length && total.value > 0 && page.value > 1) {
      page.value -= 1
      await loadEmps()
    }
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadDepts() {
  try {
    const data = await getDepts()
    depts.value = Array.isArray(data) ? data : []
  } catch {
    depts.value = []
  }
}

function handleSearch() {
  page.value = 1
  loadEmps()
}

function handleReset() {
  query.name = ''
  query.gender = ''
  query.dateRange = []
  page.value = 1
  loadEmps()
}

function handlePageChange(value) {
  page.value = value
  loadEmps()
}

function handleSizeChange(value) {
  pageSize.value = value
  page.value = 1
  loadEmps()
}

function handleSelectionChange(rows) {
  selection.value = rows
}

/* ------------------------------ 新增 / 编辑 ------------------------------ */
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formLoading = ref(false)
const saving = ref(false)
const formRef = ref(null)

function createEmptyForm() {
  return {
    id: null,
    username: '',
    name: '',
    gender: 1,
    image: '',
    job: null,
    salary: null,
    entryDate: '',
    deptId: null,
    exprList: []
  }
}

const form = reactive(createEmptyForm())

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增员工' : '编辑员工'))

const uploadHeaders = computed(() => ({ token: getToken() || '' }))

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]{3,20}$/,
      message: '用户名为 3-20 位字母、数字或下划线',
      trigger: 'blur'
    }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度需在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  job: [{ required: true, message: '请选择职位', trigger: 'change' }],
  salary: [{ required: true, message: '请输入薪资', trigger: 'blur' }],
  entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择归属部门', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, createEmptyForm())
}

function openCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function openEdit(row) {
  dialogMode.value = 'edit'
  resetForm()
  form.id = row.id
  form.username = row.username || ''
  form.name = row.name || ''
  form.gender = Number(row.gender) === 2 ? 2 : 1
  form.image = row.image || ''
  form.job = row.job ?? null
  form.salary = row.salary ?? null
  form.entryDate = row.entryDate ? formatDate(row.entryDate, '') : ''
  form.deptId = row.deptId ?? null
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())

  formLoading.value = true
  try {
    const data = await getEmp(row.id)
    if (data) {
      form.id = data.id
      form.username = data.username || ''
      form.name = data.name || ''
      form.gender = Number(data.gender) === 2 ? 2 : 1
      form.image = data.image || ''
      form.job = data.job ?? null
      form.salary = data.salary ?? null
      form.entryDate = data.entryDate ? formatDate(data.entryDate, '') : ''
      form.deptId = data.deptId ?? null
      form.exprList = Array.isArray(data.exprList)
        ? data.exprList.map((item) => ({
            company: item.company || '',
            job: item.job || '',
            begin: item.begin ? formatDate(item.begin, '') : '',
            end: item.end ? formatDate(item.end, '') : ''
          }))
        : []
    }
  } catch {
    // 错误提示已由拦截器统一处理，保留列表行数据供编辑
  } finally {
    formLoading.value = false
  }
}

function addExpr() {
  form.exprList.push({ company: '', job: '', begin: '', end: '' })
}

function removeExpr(index) {
  form.exprList.splice(index, 1)
}

function validateExprList() {
  for (let i = 0; i < form.exprList.length; i += 1) {
    const item = form.exprList[i]
    const empty = !String(item.company || '').trim() && !String(item.job || '').trim() && !item.begin && !item.end
    if (empty) continue
    if (!String(item.company || '').trim()) {
      ElMessage.warning(`第 ${i + 1} 条工作经历缺少公司名称`)
      return null
    }
    if (!String(item.job || '').trim()) {
      ElMessage.warning(`第 ${i + 1} 条工作经历缺少职位`)
      return null
    }
    if (!item.begin || !item.end) {
      ElMessage.warning(`第 ${i + 1} 条工作经历需填写开始和结束日期`)
      return null
    }
    if (item.begin > item.end) {
      ElMessage.warning(`第 ${i + 1} 条工作经历的结束日期不能早于开始日期`)
      return null
    }
  }
  return form.exprList
    .filter((item) => String(item.company || '').trim() && String(item.job || '').trim())
    .map((item) => ({
      company: item.company.trim(),
      job: item.job.trim(),
      begin: item.begin,
      end: item.end
    }))
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const exprList = validateExprList()
  if (exprList === null) return

  const payload = {
    username: form.username.trim(),
    name: form.name.trim(),
    gender: Number(form.gender),
    image: form.image || '',
    job: Number(form.job),
    salary: Number(form.salary),
    entryDate: form.entryDate,
    deptId: Number(form.deptId),
    exprList
  }
  if (dialogMode.value === 'edit') payload.id = form.id

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await addEmp(payload)
      ElMessage.success('新增员工成功')
    } else {
      await updateEmp(payload)
      ElMessage.success('修改员工成功')
    }
    dialogVisible.value = false
    await loadEmps()
  } catch {
    // 错误提示已由拦截器统一处理
  } finally {
    saving.value = false
  }
}

/* ------------------------------ 删除 ------------------------------ */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工「${row.name}」吗？删除后该员工的账号与工作经历将一并移除。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return
  }

  try {
    await deleteEmps([row.id])
    ElMessage.success(`已删除员工「${row.name}」`)
    tableRef.value?.clearSelection()
    await loadEmps()
  } catch {
    // 错误提示已由拦截器统一处理
  }
}

async function handleBatchDelete() {
  if (!selection.value.length) return
  const count = selection.value.length
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${count} 位员工吗？该操作不可恢复。`,
      '批量删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return
  }

  try {
    const ids = selection.value.map((item) => item.id)
    await deleteEmps(ids)
    ElMessage.success(`已删除 ${count} 位员工`)
    tableRef.value?.clearSelection()
    await loadEmps()
  } catch {
    // 错误提示已由拦截器统一处理
  }
}

/* ------------------------------ 头像上传 ------------------------------ */
function beforeUpload(file) {
  const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
  if (!allowed.includes(file.type)) {
    ElMessage.error('仅支持 JPG / PNG / WEBP / GIF 格式的图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 5MB')
    return false
  }
  return true
}

function handleUploadSuccess(response) {
  if (response && response.code === 1 && response.data) {
    form.image = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response?.msg || '头像上传失败')
  }
}

function handleUploadError() {
  ElMessage.error('头像上传失败，请确认后端服务已启动')
}

function removeImage() {
  form.image = ''
}

onMounted(() => {
  loadDepts()
  loadEmps()
})
</script>

<template>
  <div class="tf-page">
    <PageHero
      eyebrow="师资团队"
      title="员工管理"
      desc="管理讲师、班主任与咨询团队，维护员工档案、职位与工作经历。"
    >
      <template #actions>
        <el-button :icon="Refresh" plain :loading="loading" @click="loadEmps">刷新数据</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增员工</el-button>
      </template>
    </PageHero>

    <section class="tf-card tf-card--flush">
      <header class="tf-card__head">
        <div class="emp-head__title">
          <span class="tf-card__title">员工列表</span>
          <span class="emp-total">共 {{ total }} 人</span>
        </div>
        <div class="emp-head__actions">
          <el-button
            type="danger"
            plain
            :icon="Delete"
            :disabled="!selection.length"
            @click="handleBatchDelete"
          >
            批量删除<template v-if="selection.length">（{{ selection.length }}）</template>
          </el-button>
        </div>
      </header>

      <el-form class="tf-toolbar" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="姓名">
          <el-input
            v-model="query.name"
            placeholder="搜索员工姓名"
            clearable
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="query.gender" placeholder="全部性别" clearable @change="handleSearch">
            <el-option
              v-for="item in GENDER_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入职日期">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            unlink-panels
            class="emp-daterange"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item class="emp-toolbar__btns">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="emp-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          row-key="id"
          table-layout="fixed"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="48" align="center" fixed="left" />

          <el-table-column label="头像" width="78" align="center">
            <template #default="{ row }">
              <el-image
                v-if="row.image"
                class="emp-avatar"
                :src="row.image"
                fit="cover"
                :preview-src-list="[row.image]"
                preview-teleported
                hide-on-click-modal
              >
                <template #error>
                  <div class="emp-avatar emp-avatar--empty">
                    <el-icon :size="18"><UserFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="emp-avatar emp-avatar--empty">
                <el-icon :size="18"><UserFilled /></el-icon>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="name" label="姓名" min-width="150">
            <template #default="{ row }">
              <div class="emp-name">
                <span class="emp-name__text">{{ row.name || '—' }}</span>
                <span class="emp-name__account">@{{ row.username || '未设置账号' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="gender" label="性别" width="80" align="center">
            <template #default="{ row }">
              <span class="emp-gender">{{ genderText(row.gender) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="job" label="职位" width="112">
            <template #default="{ row }">
              <el-tag :type="jobTagType(row.job)" effect="light" round>{{ jobText(row.job) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="salary" label="薪资" width="122" align="right">
            <template #default="{ row }">
              <span class="emp-salary">¥ {{ formatMoney(row.salary) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="deptName" label="部门" min-width="140">
            <template #default="{ row }">
              <span class="emp-dept">
                <el-icon :size="13"><OfficeBuilding /></el-icon>
                {{ row.deptName || '未分配' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="entryDate" label="入职日期" width="124">
            <template #default="{ row }">
              <span class="emp-date">{{ formatDate(row.entryDate) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="148" align="right" fixed="right">
            <template #default="{ row }">
              <div class="tf-row-actions emp-actions">
                <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <template #empty>
            <div class="tf-empty">
              <el-icon :size="46"><UserFilled /></el-icon>
              <span>暂无符合条件的员工，试试调整筛选条件</span>
              <el-button type="primary" plain :icon="Plus" @click="openCreate">新增员工</el-button>
            </div>
          </template>
        </el-table>
      </div>

      <footer class="tf-pager">
        <div class="tf-pager__hint">
          共 <b>{{ total }}</b> 位员工
          <template v-if="selection.length"> · 已选 <b>{{ selection.length }}</b> 位</template>
        </div>
        <el-pagination
          background
          :current-page="page"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </footer>
    </section>

    <!-- 新增 / 编辑员工 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      top="5vh"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      class="emp-dialog"
    >
      <div v-loading="formLoading" class="emp-dialog__body">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="92px"
          label-position="right"
          @submit.prevent
        >
          <div class="form-section">
            <div class="form-section__head">
              <span class="form-dot"></span>
              <span>基础信息</span>
            </div>

            <div class="avatar-field">
              <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                accept="image/jpeg,image/png,image/webp,image/gif"
              >
                <div class="avatar-box" :class="{ 'is-filled': !!form.image }">
                  <el-image v-if="form.image" :src="form.image" fit="cover" class="avatar-img" />
                  <div v-else class="avatar-empty">
                    <el-icon :size="20"><Camera /></el-icon>
                    <span>上传头像</span>
                  </div>
                  <div v-if="form.image" class="avatar-mask">
                    <el-icon :size="18"><Camera /></el-icon>
                    <span>更换</span>
                  </div>
                </div>
              </el-upload>
              <div class="avatar-tips">
                <p>员工头像</p>
                <span>支持 JPG / PNG / WEBP / GIF，建议使用 200×200 的正方形图片，大小不超过 5MB。</span>
                <el-button v-if="form.image" link type="danger" @click="removeImage">移除头像</el-button>
              </div>
            </div>

            <div class="form-grid">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" placeholder="登录账号，例如 zhangsan" maxlength="20" clearable />
              </el-form-item>
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入员工姓名" maxlength="20" clearable />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender">
                  <el-radio v-for="item in GENDER_OPTIONS" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="职位" prop="job">
                <el-select v-model="form.job" placeholder="请选择职位" style="width: 100%">
                  <el-option
                    v-for="item in JOB_OPTIONS"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="薪资" prop="salary">
                <div class="salary-wrap">
                  <el-input-number
                    v-model="form.salary"
                    :min="0"
                    :max="1000000"
                    :step="1000"
                    :precision="0"
                    controls-position="right"
                    placeholder="请输入薪资"
                    style="width: 100%"
                  />
                  <span class="salary-unit">元/月</span>
                </div>
              </el-form-item>
              <el-form-item label="入职日期" prop="entryDate">
                <el-date-picker
                  v-model="form.entryDate"
                  type="date"
                  placeholder="请选择入职日期"
                  value-format="YYYY-MM-DD"
                  format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="归属部门" prop="deptId" class="form-grid__full">
                <el-select
                  v-model="form.deptId"
                  placeholder="请选择归属部门"
                  filterable
                  style="width: 100%"
                >
                  <el-option
                    v-for="dept in depts"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                  />
                </el-select>
              </el-form-item>
            </div>
          </div>

          <div class="form-section">
            <div class="form-section__head">
              <span class="form-dot"></span>
              <span>工作经历</span>
              <span class="form-section__hint">按时间倒序展示员工职业履历</span>
              <el-button class="form-section__add" type="primary" link :icon="Plus" @click="addExpr">
                添加经历
              </el-button>
            </div>

            <div v-if="!form.exprList.length" class="expr-empty">
              <span>暂无工作经历记录</span>
              <el-button type="primary" link :icon="Plus" @click="addExpr">添加第一条经历</el-button>
            </div>

            <div v-for="(item, index) in form.exprList" :key="index" class="expr-row">
              <span class="expr-index">{{ index + 1 }}</span>
              <div class="expr-grid">
                <div class="expr-field expr-field--wide">
                  <span class="expr-label">公司名称</span>
                  <el-input v-model="item.company" placeholder="例如：阿里巴巴" maxlength="40" />
                </div>
                <div class="expr-field">
                  <span class="expr-label">职位</span>
                  <el-input v-model="item.job" placeholder="例如：Java 开发工程师" maxlength="30" />
                </div>
                <div class="expr-field">
                  <span class="expr-label">开始日期</span>
                  <el-date-picker
                    v-model="item.begin"
                    type="date"
                    placeholder="开始日期"
                    value-format="YYYY-MM-DD"
                    format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </div>
                <div class="expr-field">
                  <span class="expr-label">结束日期</span>
                  <el-date-picker
                    v-model="item.end"
                    type="date"
                    placeholder="结束日期"
                    value-format="YYYY-MM-DD"
                    format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </div>
              </div>
              <el-button
                class="expr-del"
                link
                type="danger"
                :icon="Delete"
                title="删除该条经历"
                @click="removeExpr(index)"
              />
            </div>
          </div>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.emp-head__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.emp-total {
  font-size: 12px;
  color: var(--tf-ink-3);
}

.emp-head__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.emp-toolbar__btns {
  margin-left: 2px;
}

.emp-daterange {
  width: 268px !important;
}

.emp-table {
  padding: 6px 20px 0;
}

.emp-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(58, 155, 220, 0.16);
  box-shadow: 0 4px 12px rgba(32, 112, 164, 0.1);
  background: #eef8ff;
}

.emp-avatar--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8fb6cf;
}

.emp-name {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.emp-name__text {
  font-weight: 600;
  color: var(--tf-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.emp-name__account {
  font-family: var(--tf-font-mono);
  font-size: 11.5px;
  color: var(--tf-ink-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.emp-gender {
  color: var(--tf-ink-2);
}

.emp-salary {
  font-family: var(--tf-font-mono);
  font-size: 12.5px;
  font-weight: 600;
  color: #1e6fa8;
}

.emp-dept {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--tf-ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.emp-dept .el-icon {
  color: #7fb1cf;
}

.emp-date {
  font-family: var(--tf-font-mono);
  font-size: 12.5px;
  color: var(--tf-ink-2);
}

.emp-actions {
  justify-content: flex-end;
}

/* ---------- 对话框 ---------- */
.emp-dialog__body {
  max-height: calc(88vh - 138px);
  overflow-y: auto;
  padding: 2px 4px 2px 0;
  margin-right: -4px;
}

.form-section + .form-section {
  margin-top: 6px;
  padding-top: 18px;
  border-top: 1px dashed var(--tf-line);
}

.form-section__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-family: var(--tf-font-display);
  font-size: 13.5px;
  font-weight: 700;
  color: var(--tf-ink);
  letter-spacing: 0.02em;
}

.form-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3a9bdc;
  box-shadow: 0 0 0 3px rgba(58, 155, 220, 0.16);
}

.form-section__hint {
  font-family: var(--tf-font);
  font-size: 11.5px;
  font-weight: 400;
  color: var(--tf-ink-3);
}

.form-section__add {
  margin-left: auto;
}

.avatar-field {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0 0 16px 92px;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(120deg, rgba(236, 248, 255, 0.9), rgba(255, 255, 255, 0.5));
  border: 1px dashed rgba(58, 155, 220, 0.24);
}

.avatar-uploader {
  flex-shrink: 0;
}

.avatar-box {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eaf6ff;
  border: 1px solid rgba(58, 155, 220, 0.22);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.avatar-box:hover {
  border-color: #3a9bdc;
  box-shadow: 0 8px 20px rgba(58, 155, 220, 0.18);
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #5d90b2;
  font-size: 11px;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  background: rgba(20, 68, 98, 0.55);
  color: #fff;
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-box:hover .avatar-mask {
  opacity: 1;
}

.avatar-tips {
  min-width: 0;
}

.avatar-tips p {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--tf-ink);
}

.avatar-tips span {
  display: block;
  font-size: 11.5px;
  line-height: 1.6;
  color: var(--tf-ink-3);
}

.avatar-tips .el-button {
  margin-top: 2px;
  height: auto;
  padding: 0;
  font-size: 11.5px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 20px;
}

.form-grid .el-form-item {
  margin-bottom: 18px;
}

.form-grid__full {
  grid-column: 1 / -1;
}

.salary-wrap {
  position: relative;
  width: 100%;
}

.salary-unit {
  position: absolute;
  right: 40px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--tf-ink-3);
  pointer-events: none;
}

.salary-wrap :deep(.el-input__inner) {
  padding-right: 46px;
  text-align: left;
}

.expr-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 18px;
  border-radius: 12px;
  border: 1px dashed rgba(58, 155, 220, 0.28);
  background: rgba(240, 250, 255, 0.6);
  color: var(--tf-ink-3);
  font-size: 12.5px;
}

.expr-row {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 12px 12px;
  border-radius: 12px;
  border: 1px solid rgba(58, 155, 220, 0.14);
  background: rgba(247, 252, 255, 0.86);
}

.expr-row + .expr-row {
  margin-top: 10px;
}

.expr-index {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  margin-top: 20px;
  border-radius: 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(58, 155, 220, 0.12);
  color: #2277b4;
  font-family: var(--tf-font-display);
  font-size: 11.5px;
  font-weight: 700;
}

.expr-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1.35fr 1fr 150px 150px;
  gap: 10px 10px;
  min-width: 0;
}

.expr-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.expr-label {
  font-size: 11.5px;
  color: var(--tf-ink-3);
}

.expr-del {
  flex-shrink: 0;
  margin-top: 20px;
}

@media (max-width: 900px) {
  .expr-grid {
    grid-template-columns: 1fr 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .avatar-field {
    margin-left: 0;
  }

  .emp-daterange {
    width: 230px !important;
  }
}

@media (max-width: 560px) {
  .expr-grid {
    grid-template-columns: 1fr;
  }

  .expr-index,
  .expr-del {
    margin-top: 2px;
  }

  .avatar-field {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
