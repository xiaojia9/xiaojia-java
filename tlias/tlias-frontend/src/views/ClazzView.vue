<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar,
  Delete,
  Edit,
  Plus,
  Reading,
  Refresh,
  Search,
  UserFilled
} from '@element-plus/icons-vue'
import PageHero from '@/components/PageHero.vue'
import {
  addClazz,
  deleteClazz,
  getClazz,
  pageClazzs,
  updateClazz
} from '@/api/clazz'
import { listEmps } from '@/api/emp'
import { formatDate } from '@/utils/format'
import {
  SUBJECT_OPTIONS,
  jobText,
  statusTagType,
  statusText,
  subjectText
} from '@/utils/dict'

/* ------------------------------ 列表 ------------------------------ */
const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  name: '',
  dateRange: []
})

const page = ref(1)
const pageSize = ref(10)

function buildParams() {
  const params = { page: page.value, pageSize: pageSize.value }
  if (query.name.trim()) params.name = query.name.trim()
  const [begin, end] = Array.isArray(query.dateRange) ? query.dateRange : []
  if (begin) params.begin = begin
  if (end) params.end = end
  return params
}

async function loadClazzs() {
  loading.value = true
  try {
    const data = await pageClazzs(buildParams())
    list.value = Array.isArray(data?.rows) ? data.rows : []
    total.value = Number(data?.total) || 0
    if (!list.value.length && total.value > 0 && page.value > 1) {
      page.value -= 1
      await loadClazzs()
    }
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const masters = ref([])

async function loadMasters() {
  try {
    const data = await listEmps()
    masters.value = Array.isArray(data) ? data : []
  } catch {
    masters.value = []
  }
}

function masterNameOf(row) {
  if (row.masterName) return row.masterName
  const matched = masters.value.find((item) => item.id === row.masterId)
  return matched ? matched.name : ''
}

function handleSearch() {
  page.value = 1
  loadClazzs()
}

function handleReset() {
  query.name = ''
  query.dateRange = []
  page.value = 1
  loadClazzs()
}

function handlePageChange(value) {
  page.value = value
  loadClazzs()
}

function handleSizeChange(value) {
  pageSize.value = value
  page.value = 1
  loadClazzs()
}

function subjectTagType(subject) {
  switch (String(subject)) {
    case '1':
      return 'primary'
    case '2':
      return 'success'
    case '3':
      return 'warning'
    case '4':
      return 'danger'
    case '5':
      return 'info'
    case '6':
      return 'primary'
    default:
      return 'info'
  }
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
    name: '',
    room: '',
    beginDate: '',
    endDate: '',
    masterId: null,
    subject: null
  }
}

const form = reactive(createEmptyForm())

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增班级' : '编辑班级'))

function validateEndDate(rule, value, callback) {
  if (!value) {
    callback(new Error('请选择结课时间'))
    return
  }
  if (form.beginDate && value < form.beginDate) {
    callback(new Error('结课时间不能早于开班时间'))
    return
  }
  callback()
}

const rules = {
  name: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 30, message: '班级名称长度需在 2 到 30 个字符之间', trigger: 'blur' }
  ],
  room: [{ max: 20, message: '教室名称不能超过 20 个字符', trigger: 'blur' }],
  beginDate: [{ required: true, message: '请选择开班时间', trigger: 'change' }],
  endDate: [{ required: true, validator: validateEndDate, trigger: 'change' }],
  masterId: [{ required: true, message: '请选择班主任', trigger: 'change' }],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }]
}

function openCreate() {
  dialogMode.value = 'create'
  Object.assign(form, createEmptyForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function openEdit(row) {
  dialogMode.value = 'edit'
  Object.assign(form, createEmptyForm())
  form.id = row.id
  form.name = row.name || ''
  form.room = row.room || ''
  form.beginDate = row.beginDate ? formatDate(row.beginDate, '') : ''
  form.endDate = row.endDate ? formatDate(row.endDate, '') : ''
  form.masterId = row.masterId ?? null
  form.subject = row.subject ?? null
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())

  formLoading.value = true
  try {
    const data = await getClazz(row.id)
    if (data) {
      form.id = data.id
      form.name = data.name || ''
      form.room = data.room || ''
      form.beginDate = data.beginDate ? formatDate(data.beginDate, '') : ''
      form.endDate = data.endDate ? formatDate(data.endDate, '') : ''
      form.masterId = data.masterId ?? null
      form.subject = data.subject ?? null
    }
  } catch {
    // 错误提示已由拦截器统一处理
  } finally {
    formLoading.value = false
  }
}

function handleBeginChange() {
  if (form.endDate) formRef.value?.validateField('endDate').catch(() => {})
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    name: form.name.trim(),
    room: form.room.trim(),
    beginDate: form.beginDate,
    endDate: form.endDate,
    masterId: Number(form.masterId),
    subject: Number(form.subject)
  }
  if (dialogMode.value === 'edit') payload.id = form.id

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await addClazz(payload)
      ElMessage.success('新增班级成功')
    } else {
      await updateClazz(payload)
      ElMessage.success('修改班级成功')
    }
    dialogVisible.value = false
    await loadClazzs()
  } catch {
    // 错误提示已由拦截器统一处理
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除班级「${row.name}」吗？删除后班级下的学员关联将失效。`,
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
    await deleteClazz(row.id)
    ElMessage.success(`已删除班级「${row.name}」`)
    await loadClazzs()
  } catch {
    // 错误提示已由拦截器统一处理
  }
}

onMounted(() => {
  loadMasters()
  loadClazzs()
})
</script>

<template>
  <div class="tf-page">
    <PageHero
      eyebrow="教学运营"
      title="班级管理"
      desc="维护开班计划、教室与班主任配置，掌握每个班级的开课状态。"
    >
      <template #actions>
        <el-button :icon="Refresh" plain :loading="loading" @click="loadClazzs">刷新数据</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增班级</el-button>
      </template>
    </PageHero>

    <section class="tf-card tf-card--flush">
      <header class="tf-card__head">
        <div class="clazz-head__title">
          <span class="tf-card__title">班级列表</span>
          <span class="clazz-total">共 {{ total }} 个班级</span>
        </div>
        <span class="clazz-head__note">结课时间范围用于筛选已排课的班级</span>
      </header>

      <el-form class="tf-toolbar" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="班级名称">
          <el-input
            v-model="query.name"
            placeholder="搜索班级名称"
            clearable
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="结课时间">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            unlink-panels
            class="clazz-daterange"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="clazz-table">
        <el-table
          v-loading="loading"
          :data="list"
          row-key="id"
          table-layout="fixed"
          style="width: 100%"
        >
          <el-table-column prop="name" label="班级名称" min-width="190">
            <template #default="{ row }">
              <div class="clazz-name">
                <span class="clazz-badge"><el-icon :size="15"><Reading /></el-icon></span>
                <div class="clazz-name__body">
                  <span class="clazz-name__text">{{ row.name || '—' }}</span>
                  <span class="clazz-name__id tf-mono">#{{ row.id }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="room" label="教室" width="120">
            <template #default="{ row }">
              <span class="clazz-room">{{ row.room || '待分配' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="beginDate" label="开班时间" width="124">
            <template #default="{ row }">
              <span class="clazz-date">{{ formatDate(row.beginDate) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="endDate" label="结课时间" width="124">
            <template #default="{ row }">
              <span class="clazz-date">{{ formatDate(row.endDate) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="masterName" label="班主任" width="132">
            <template #default="{ row }">
              <span class="clazz-master">
                <el-icon :size="13"><UserFilled /></el-icon>
                {{ masterNameOf(row) || '未分配' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="subject" label="学科" width="110">
            <template #default="{ row }">
              <el-tag :type="subjectTagType(row.subject)" effect="plain" round>
                {{ subjectText(row.subject) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="108">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" effect="light" round>
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="148" align="right" fixed="right">
            <template #default="{ row }">
              <div class="tf-row-actions clazz-actions">
                <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <template #empty>
            <div class="tf-empty">
              <el-icon :size="46"><Reading /></el-icon>
              <span>暂无符合条件的班级，试试调整筛选条件</span>
              <el-button type="primary" plain :icon="Plus" @click="openCreate">新增班级</el-button>
            </div>
          </template>
        </el-table>
      </div>

      <footer class="tf-pager">
        <div class="tf-pager__hint">
          共 <b>{{ total }}</b> 个班级 · 状态由系统根据开班与结课时间自动维护
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="640px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div v-loading="formLoading">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="96px"
          label-position="right"
          @submit.prevent
        >
          <div class="clazz-form-grid">
            <el-form-item label="班级名称" prop="name">
              <el-input v-model="form.name" placeholder="例如：Java 就业班 2026-01" maxlength="30" clearable />
            </el-form-item>
            <el-form-item label="教室" prop="room">
              <el-input v-model="form.room" placeholder="例如：A-301" maxlength="20" clearable />
            </el-form-item>
            <el-form-item label="开班时间" prop="beginDate">
              <el-date-picker
                v-model="form.beginDate"
                type="date"
                placeholder="请选择开班时间"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 100%"
                @change="handleBeginChange"
              />
            </el-form-item>
            <el-form-item label="结课时间" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="请选择结课时间"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="班主任" prop="masterId">
              <el-select
                v-model="form.masterId"
                placeholder="请选择班主任"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="emp in masters"
                  :key="emp.id"
                  :label="`${emp.name}（${jobText(emp.job)}）`"
                  :value="emp.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="学科" prop="subject">
              <el-select v-model="form.subject" placeholder="请选择学科" style="width: 100%">
                <el-option
                  v-for="item in SUBJECT_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>

          <div class="clazz-tip">
            <el-icon><Calendar /></el-icon>
            <span>班级状态由系统根据开班 / 结课时间自动生成，无需手动设置；保存后将同步更新班级列表。</span>
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
.clazz-head__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clazz-total {
  font-size: 12px;
  color: var(--tf-ink-3);
}

.clazz-head__note {
  font-size: 11.5px;
  color: var(--tf-ink-3);
}

.clazz-daterange {
  width: 268px !important;
}

.clazz-table {
  padding: 6px 20px 0;
}

.clazz-name {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.clazz-badge {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #1e6fa8;
  background: linear-gradient(135deg, #e8f7ff, #c9e9ff);
  border: 1px solid rgba(58, 155, 220, 0.18);
}

.clazz-name__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.clazz-name__text {
  font-weight: 600;
  color: var(--tf-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clazz-name__id {
  color: var(--tf-ink-3);
  font-size: 11px;
}

.clazz-room {
  color: var(--tf-ink-2);
}

.clazz-date {
  font-family: var(--tf-font-mono);
  font-size: 12.5px;
  color: var(--tf-ink-2);
}

.clazz-master {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--tf-ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clazz-master .el-icon {
  color: #7fb1cf;
}

.clazz-actions {
  justify-content: flex-end;
}

.clazz-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 18px;
}

.clazz-form-grid .el-form-item {
  margin-bottom: 18px;
}

.clazz-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 2px 0 0 96px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(58, 155, 220, 0.07);
  border: 1px dashed rgba(58, 155, 220, 0.22);
  font-size: 12px;
  line-height: 1.6;
  color: var(--tf-ink-2);
}

.clazz-tip .el-icon {
  color: #3a9bdc;
  margin-top: 2px;
  flex-shrink: 0;
}

@media (max-width: 720px) {
  .clazz-form-grid {
    grid-template-columns: 1fr;
  }

  .clazz-tip {
    margin-left: 0;
  }

  .clazz-head__note {
    display: none;
  }

  .clazz-daterange {
    width: 230px !important;
  }
}
</style>
