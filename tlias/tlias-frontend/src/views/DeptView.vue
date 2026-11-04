<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, OfficeBuilding, Plus, Refresh, Search } from '@element-plus/icons-vue'
import PageHero from '@/components/PageHero.vue'
import { addDept, deleteDept, getDepts, updateDept } from '@/api/dept'
import { formatDateTime } from '@/utils/format'

const depts = ref([])
const loading = ref(false)
const keyword = ref('')

const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, name: '' })

const rules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 20, message: '部门名称长度需在 2 到 20 个字符之间', trigger: 'blur' }
  ]
}

const filteredDepts = computed(() => {
  const kw = keyword.value.trim()
  if (!kw) return depts.value
  return depts.value.filter((item) => String(item.name || '').includes(kw))
})

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增部门' : '编辑部门'))

async function loadDepts() {
  loading.value = true
  try {
    const data = await getDepts()
    depts.value = Array.isArray(data) ? data : []
  } catch {
    depts.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  dialogMode.value = 'create'
  form.id = null
  form.name = ''
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row) {
  dialogMode.value = 'edit'
  form.id = row.id
  form.name = row.name || ''
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const name = form.name.trim()
  const duplicated = depts.value.some((item) => item.name === name && item.id !== form.id)
  if (duplicated) {
    ElMessage.warning('该部门名称已存在，请更换名称')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await addDept({ name })
      ElMessage.success('新增部门成功')
    } else {
      await updateDept({ id: form.id, name })
      ElMessage.success('修改部门成功')
    }
    dialogVisible.value = false
    await loadDepts()
  } catch {
    // 错误提示已由拦截器统一处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门「${row.name}」吗？删除后该部门下的关联数据将无法继续维护。`,
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
    await deleteDept(row.id)
    ElMessage.success(`已删除部门「${row.name}」`)
    await loadDepts()
  } catch {
    // 错误提示已由拦截器统一处理
  }
}

onMounted(loadDepts)
</script>

<template>
  <div class="tf-page">
    <PageHero
      eyebrow="组织架构"
      title="部门管理"
      desc="维护教学部门信息，为员工归属与班级运营提供组织基础。"
    >
      <template #actions>
        <el-button :icon="Refresh" plain :loading="loading" @click="loadDepts">刷新数据</el-button>
      </template>
    </PageHero>

    <section class="tf-card tf-card--flush">
      <header class="tf-card__head dept-head">
        <div class="dept-head__title">
          <span class="tf-card__title">部门列表</span>
          <span class="dept-count">{{ depts.length }}</span>
        </div>
        <div class="dept-head__actions">
          <el-input
            v-model="keyword"
            class="dept-search"
            clearable
            placeholder="搜索部门名称"
            :prefix-icon="Search"
          />
          <el-button type="primary" :icon="Plus" @click="openCreate">新增部门</el-button>
        </div>
      </header>

      <div class="dept-body">
        <el-table
          v-loading="loading"
          :data="filteredDepts"
          row-key="id"
          table-layout="fixed"
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="96" sortable>
            <template #default="{ row }">
              <span class="tf-mono dept-id">#{{ row.id }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="name" label="部门名称" min-width="220" sortable>
            <template #default="{ row }">
              <div class="dept-name">
                <span class="dept-badge">{{ String(row.name || '?').slice(0, 1) }}</span>
                <span class="dept-name__text">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="创建时间" width="190">
            <template #default="{ row }">
              <span class="dept-time">{{ formatDateTime(row.createTime) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="updateTime" label="更新时间" width="190">
            <template #default="{ row }">
              <span class="dept-time">{{ formatDateTime(row.updateTime) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="160" align="right" fixed="right">
            <template #default="{ row }">
              <div class="tf-row-actions dept-actions">
                <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <template #empty>
            <div class="tf-empty">
              <el-icon :size="46"><OfficeBuilding /></el-icon>
              <span>{{ keyword ? '没有匹配的部门，试试其他关键词' : '还没有部门数据，立即创建第一个部门' }}</span>
              <el-button v-if="!keyword" type="primary" plain :icon="Plus" @click="openCreate">
                新增部门
              </el-button>
            </div>
          </template>
        </el-table>
      </div>

      <footer class="tf-pager">
        <div class="tf-pager__hint">
          <template v-if="keyword">
            共 <b>{{ filteredDepts.length }}</b> 个匹配结果 / 全部 <b>{{ depts.length }}</b> 个部门
          </template>
          <template v-else>
            共 <b>{{ depts.length }}</b> 个部门 · 数据来自 Tlias 组织中心
          </template>
        </div>
        <span class="dept-foot-note">部门调整后，员工与班级的归属会同步更新</span>
      </footer>
    </section>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="86px"
        label-position="right"
        @submit.prevent
      >
        <el-form-item label="部门名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="例如：教研部"
            maxlength="20"
            show-word-limit
            clearable
            :prefix-icon="OfficeBuilding"
            @keyup.enter="submitForm"
          />
        </el-form-item>
        <div class="dept-tip">
          <el-icon><OfficeBuilding /></el-icon>
          <span>部门名称需保持唯一，建议使用清晰的组织名称，如「学工部」「教研中心」。</span>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dept-head {
  gap: 18px;
}

.dept-head__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dept-count {
  min-width: 24px;
  height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(58, 155, 220, 0.1);
  border: 1px solid rgba(58, 155, 220, 0.18);
  color: #2277b4;
  font-family: var(--tf-font-display);
  font-size: 12px;
  font-weight: 700;
}

.dept-head__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dept-search {
  width: 220px;
}

.dept-body {
  padding: 8px 20px 0;
}

.dept-id {
  color: var(--tf-ink-3);
}

.dept-name {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.dept-badge {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e4f5ff, #c6e8ff);
  border: 1px solid rgba(58, 155, 220, 0.2);
  color: #1e6fa8;
  font-family: var(--tf-font-display);
  font-size: 13px;
  font-weight: 700;
}

.dept-name__text {
  color: var(--tf-ink);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-time {
  color: var(--tf-ink-2);
  font-family: var(--tf-font-mono);
  font-size: 12.5px;
}

.dept-actions {
  justify-content: flex-end;
}

.dept-foot-note {
  font-size: 12px;
  color: var(--tf-ink-3);
}

.dept-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 4px 0 0 86px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(58, 155, 220, 0.07);
  border: 1px dashed rgba(58, 155, 220, 0.22);
  font-size: 12px;
  line-height: 1.6;
  color: var(--tf-ink-2);
}

.dept-tip .el-icon {
  color: #3a9bdc;
  margin-top: 2px;
  flex-shrink: 0;
}

@media (max-width: 720px) {
  .dept-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .dept-head__actions {
    width: 100%;
  }

  .dept-search {
    flex: 1;
    width: auto;
  }

  .dept-tip {
    margin-left: 0;
  }
}
</style>
