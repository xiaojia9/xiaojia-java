<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Delete,
  Edit,
  Plus,
  Reading,
  Refresh,
  Search,
  User,
  Warning
} from '@element-plus/icons-vue'
import PageHero from '@/components/PageHero.vue'
import {
  addStudent,
  deleteStudents,
  getStudent,
  pageStudents,
  updateStudent,
  updateViolation
} from '@/api/student'
import { listClazzs } from '@/api/clazz'
import { formatDate } from '@/utils/format'
import {
  DEGREE_OPTIONS,
  GENDER_OPTIONS,
  degreeTagType,
  degreeText,
  genderText
} from '@/utils/dict'

/* ------------------------------ 列表 ------------------------------ */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selection = ref([])
const tableRef = ref(null)
const clazzs = ref([])

const query = reactive({
  name: '',
  degree: '',
  clazzId: ''
})

const page = ref(1)
const pageSize = ref(10)

function buildParams() {
  const params = { page: page.value, pageSize: pageSize.value }
  if (query.name.trim()) params.name = query.name.trim()
  if (query.degree !== '' && query.degree !== null && query.degree !== undefined) {
    params.degree = query.degree
  }
  if (query.clazzId !== '' && query.clazzId !== null && query.clazzId !== undefined) {
    params.clazzId = query.clazzId
  }
  return params
}

async function loadStudents() {
  loading.value = true
  try {
    const data = await pageStudents(buildParams())
    list.value = Array.isArray(data?.rows) ? data.rows : []
    total.value = Number(data?.total) || 0
    if (!list.value.length && total.value > 0 && page.value > 1) {
      page.value -= 1
      await loadStudents()
    }
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadClazzs() {
  try {
    const data = await listClazzs()
    clazzs.value = Array.isArray(data) ? data : []
  } catch {
    clazzs.value = []
  }
}

function handleSearch() {
  page.value = 1
  loadStudents()
}

function handleReset() {
  query.name = ''
  query.degree = ''
  query.clazzId = ''
  page.value = 1
  loadStudents()
}

function handlePageChange(value) {
  page.value = value
  loadStudents()
}

function handleSizeChange(value) {
  pageSize.value = value
  page.value = 1
  loadStudents()
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
    name: '',
    no: '',
    gender: 1,
    phone: '',
    degree: null,
    clazzId: null,
    idCard: '',
    isCollege: 1,
    address: '',
    graduationDate: ''
  }
}

const form = reactive(createEmptyForm())

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增学员' : '编辑学员'))

const rules = {
  name: [
    { required: true, message: '请输入学员姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度需在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  no: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { max: 30, message: '学号不能超过 30 个字符', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }
  ],
  degree: [{ required: true, message: '请选择学历', trigger: 'change' }],
  clazzId: [{ required: true, message: '请选择所属班级', trigger: 'change' }],
  idCard: [
    {
      pattern: /(^\d{15}$)|(^\d{17}(\d|X|x)$)/,
      message: '请输入正确的 15 或 18 位身份证号',
      trigger: 'blur'
    }
  ]
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
  form.no = row.no || ''
  form.gender = Number(row.gender) === 2 ? 2 : 1
  form.phone = row.phone || ''
  form.degree = row.degree ?? null
  form.clazzId = row.clazzId ?? null
  form.idCard = row.idCard || ''
  form.isCollege = Number(row.isCollege) === 0 ? 0 : 1
  form.address = row.address || ''
  form.graduationDate = row.graduationDate ? formatDate(row.graduationDate, '') : ''
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())

  formLoading.value = true
  try {
    const data = await getStudent(row.id)
    if (data) {
      form.id = data.id
      form.name = data.name || ''
      form.no = data.no || ''
      form.gender = Number(data.gender) === 2 ? 2 : 1
      form.phone = data.phone || ''
      form.degree = data.degree ?? null
      form.clazzId = data.clazzId ?? null
      form.idCard = data.idCard || ''
      form.isCollege = Number(data.isCollege) === 0 ? 0 : 1
      form.address = data.address || ''
      form.graduationDate = data.graduationDate ? formatDate(data.graduationDate, '') : ''
    }
  } catch {
    // 错误提示已由拦截器统一处理
  } finally {
    formLoading.value = false
  }
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    name: form.name.trim(),
    no: form.no.trim(),
    gender: Number(form.gender),
    phone: form.phone.trim(),
    degree: Number(form.degree),
    clazzId: Number(form.clazzId),
    idCard: form.idCard.trim(),
    isCollege: Number(form.isCollege),
    address: form.address.trim(),
    graduationDate: form.graduationDate || null
  }
  if (dialogMode.value === 'edit') payload.id = form.id

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await addStudent(payload)
      ElMessage.success('新增学员成功')
    } else {
      await updateStudent(payload)
      ElMessage.success('修改学员成功')
    }
    dialogVisible.value = false
    await loadStudents()
  } catch {
    // 错误提示已由拦截器统一处理
  } finally {
    saving.value = false
  }
}

/* ------------------------------ 违纪处理 ------------------------------ */
async function handleViolation(row) {
  let result
  try {
    result = await ElMessageBox.prompt(
      `学员「${row.name}」当前累计扣 ${Number(row.violationScore) || 0} 分，请输入本次违纪扣分（1-10 分）。`,
      '违纪处理',
      {
        type: 'warning',
        confirmButtonText: '确认扣分',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入 1-10 之间的整数',
        inputPattern: /^([1-9]|10)$/,
        inputErrorMessage: '请输入 1-10 之间的整数',
        closeOnClickModal: false
      }
    )
  } catch {
    return
  }

  const score = Number(result?.value)
  if (!Number.isInteger(score) || score < 1 || score > 10) {
    ElMessage.warning('扣分分值需为 1-10 之间的整数')
    return
  }

  try {
    await updateViolation(row.id, score)
    ElMessage.success(`已为「${row.name}」登记违纪扣分 ${score} 分`)
    await loadStudents()
  } catch {
    // 错误提示已由拦截器统一处理
  }
}

/* ------------------------------ 删除 ------------------------------ */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除学员「${row.name}」吗？删除后学员档案与违纪记录将一并移除。`,
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
    await deleteStudents([row.id])
    ElMessage.success(`已删除学员「${row.name}」`)
    tableRef.value?.clearSelection()
    await loadStudents()
  } catch {
    // 错误提示已由拦截器统一处理
  }
}

async function handleBatchDelete() {
  if (!selection.value.length) return
  const count = selection.value.length
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${count} 位学员吗？该操作不可恢复。`,
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
    await deleteStudents(ids)
    ElMessage.success(`已删除 ${count} 位学员`)
    tableRef.value?.clearSelection()
    await loadStudents()
  } catch {
    // 错误提示已由拦截器统一处理
  }
}

onMounted(() => {
  loadClazzs()
  loadStudents()
})
</script>

<template>
  <div class="tf-page">
    <PageHero
      eyebrow="学员档案"
      title="学员管理"
      desc="维护在读学员的基础资料、学历背景与班级归属，跟踪违纪情况。"
    >
      <template #actions>
        <el-button :icon="Refresh" plain :loading="loading" @click="loadStudents">刷新数据</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增学员</el-button>
      </template>
    </PageHero>

    <section class="tf-card tf-card--flush">
      <header class="tf-card__head">
        <div class="stu-head__title">
          <span class="tf-card__title">学员列表</span>
          <span class="stu-total">共 {{ total }} 位学员</span>
        </div>
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="!selection.length"
          @click="handleBatchDelete"
        >
          批量删除<template v-if="selection.length">（{{ selection.length }}）</template>
        </el-button>
      </header>

      <el-form class="tf-toolbar" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="姓名">
          <el-input
            v-model="query.name"
            placeholder="搜索学员姓名"
            clearable
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="学历">
          <el-select v-model="query.degree" placeholder="全部学历" clearable @change="handleSearch">
            <el-option
              v-for="item in DEGREE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select
            v-model="query.clazzId"
            placeholder="全部班级"
            clearable
            filterable
            class="stu-clazz-select"
            @change="handleSearch"
          >
            <el-option
              v-for="item in clazzs"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="stu-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          row-key="id"
          table-layout="fixed"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="44" align="center" fixed="left" />

          <el-table-column prop="name" label="姓名" min-width="130">
            <template #default="{ row }">
              <div class="stu-name">
                <span class="stu-avatar" :class="{ 'is-female': Number(row.gender) === 2 }">
                  {{ String(row.name || '?').slice(0, 1) }}
                </span>
                <div class="stu-name__body">
                  <span class="stu-name__text">{{ row.name || '—' }}</span>
                  <span class="stu-name__id tf-mono">#{{ row.id }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="no" label="学号" width="120">
            <template #default="{ row }">
              <span class="tf-mono stu-no">{{ row.no || '—' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="gender" label="性别" width="60" align="center">
            <template #default="{ row }">
              <span class="stu-gender">{{ genderText(row.gender) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="phone" label="手机号" width="118">
            <template #default="{ row }">
              <span class="tf-mono stu-phone">{{ row.phone || '—' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="degree" label="学历" width="92">
            <template #default="{ row }">
              <div class="stu-degree">
                <el-tag :type="degreeTagType(row.degree)" effect="light" round>
                  {{ degreeText(row.degree) }}
                </el-tag>
                <span class="stu-college">{{ Number(row.isCollege) === 1 ? '统招' : '非统招' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="clazzName" label="班级" min-width="170">
            <template #default="{ row }">
              <span class="stu-clazz">
                <el-icon :size="13"><Reading /></el-icon>
                {{ row.clazzName || '未分班' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="violationCount" label="违纪次数" width="84" align="center">
            <template #default="{ row }">
              <span
                class="stu-violation"
                :class="{ 'is-warn': Number(row.violationCount) > 0 }"
              >
                {{ Number(row.violationCount) || 0 }} 次
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="violationScore" label="违纪扣分" width="84" align="center">
            <template #default="{ row }">
              <span class="stu-score" :class="{ 'is-warn': Number(row.violationScore) > 0 }">
                {{ Number(row.violationScore) || 0 }} 分
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" align="right" fixed="right">
            <template #default="{ row }">
              <div class="tf-row-actions stu-actions">
                <el-button link type="warning" :icon="Warning" title="违纪处理" @click="handleViolation(row)">
                  违纪
                </el-button>
                <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <template #empty>
            <div class="tf-empty">
              <el-icon :size="46"><User /></el-icon>
              <span>暂无符合条件的学员，试试调整筛选条件</span>
              <el-button type="primary" plain :icon="Plus" @click="openCreate">新增学员</el-button>
            </div>
          </template>
        </el-table>
      </div>

      <footer class="tf-pager">
        <div class="tf-pager__hint">
          共 <b>{{ total }}</b> 位学员
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="760px"
      top="6vh"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div v-loading="formLoading">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="92px"
          label-position="right"
          @submit.prevent
        >
          <div class="stu-form-grid">
            <el-form-item label="学员姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入学员姓名" maxlength="20" clearable />
            </el-form-item>
            <el-form-item label="学号" prop="no">
              <el-input v-model="form.no" placeholder="例如：202601001" maxlength="30" clearable />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio v-for="item in GENDER_OPTIONS" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入 11 位手机号" maxlength="11" clearable />
            </el-form-item>
            <el-form-item label="学历" prop="degree">
              <el-select v-model="form.degree" placeholder="请选择学历" style="width: 100%">
                <el-option
                  v-for="item in DEGREE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="所属班级" prop="clazzId">
              <el-select
                v-model="form.clazzId"
                placeholder="请选择所属班级"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="item in clazzs"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" clearable />
            </el-form-item>
            <el-form-item label="是否统招">
              <div class="stu-switch">
                <el-switch
                  v-model="form.isCollege"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="是"
                  inactive-text="否"
                  inline-prompt
                />
                <span class="stu-switch__text">{{ Number(form.isCollege) === 1 ? '统招学员' : '非统招学员' }}</span>
              </div>
            </el-form-item>
            <el-form-item label="毕业时间">
              <el-date-picker
                v-model="form.graduationDate"
                type="date"
                placeholder="请选择毕业时间"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="联系地址" class="stu-form__full">
              <el-input
                v-model="form.address"
                type="textarea"
                :rows="2"
                resize="none"
                maxlength="100"
                show-word-limit
                placeholder="请输入学员的联系地址"
              />
            </el-form-item>
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
.stu-head__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stu-total {
  font-size: 12px;
  color: var(--tf-ink-3);
}

.stu-clazz-select {
  width: 200px !important;
}

.stu-table {
  padding: 6px 20px 0;
}

.stu-name {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.stu-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--tf-font-display);
  font-size: 13px;
  font-weight: 700;
  color: #1e6fa8;
  background: linear-gradient(135deg, #e6f6ff, #c5e8ff);
  border: 1px solid rgba(58, 155, 220, 0.2);
}

.stu-avatar.is-female {
  color: #b15c7e;
  background: linear-gradient(135deg, #fff0f5, #ffdce9);
  border-color: rgba(214, 128, 160, 0.25);
}

.stu-name__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.stu-name__text {
  font-weight: 600;
  color: var(--tf-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stu-name__id {
  color: var(--tf-ink-3);
  font-size: 11px;
}

.stu-no {
  color: var(--tf-ink-2);
}

.stu-gender {
  color: var(--tf-ink-2);
}

.stu-phone {
  color: var(--tf-ink-2);
}

.stu-degree {
  display: flex;
  flex-direction: column;
  gap: 3px;
  align-items: flex-start;
}

.stu-college {
  font-size: 10.5px;
  color: var(--tf-ink-3);
  padding-left: 2px;
}

.stu-clazz {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--tf-ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stu-clazz .el-icon {
  color: #7fb1cf;
}

.stu-violation,
.stu-score {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  padding: 2px 8px;
  border-radius: 999px;
  font-family: var(--tf-font-mono);
  font-size: 12px;
  color: var(--tf-ink-2);
  background: rgba(138, 166, 184, 0.1);
}

.stu-violation.is-warn,
.stu-score.is-warn {
  color: #c2410c;
  background: rgba(232, 163, 61, 0.16);
  border: 1px solid rgba(232, 163, 61, 0.3);
}

.stu-actions {
  justify-content: flex-end;
}

.stu-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 18px;
}

.stu-form-grid .el-form-item {
  margin-bottom: 18px;
}

.stu-form__full {
  grid-column: 1 / -1;
}

.stu-switch {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stu-switch__text {
  font-size: 12px;
  color: var(--tf-ink-3);
}

@media (max-width: 760px) {
  .stu-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
