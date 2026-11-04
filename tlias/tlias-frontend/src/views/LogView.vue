<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Clock,
  CopyDocument,
  Document,
  Refresh,
  View
} from '@element-plus/icons-vue'
import PageHero from '@/components/PageHero.vue'
import { pageLogs } from '@/api/log'
import { formatCost, formatDateTime, prettyText } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const detail = ref({})

async function loadLogs() {
  loading.value = true
  try {
    const data = await pageLogs({ page: page.value, pageSize: pageSize.value })
    list.value = Array.isArray(data?.rows) ? data.rows : []
    total.value = Number(data?.total) || 0
    if (!list.value.length && total.value > 0 && page.value > 1) {
      page.value -= 1
      await loadLogs()
    }
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(value) {
  page.value = value
  loadLogs()
}

function handleSizeChange(value) {
  pageSize.value = value
  page.value = 1
  loadLogs()
}

function operatorName(row) {
  return row.operateEmpName || (row.operateEmpId ? `操作员 #${row.operateEmpId}` : '系统')
}

function previewText(value) {
  if (value === null || value === undefined || value === '') return '（空）'
  const text = String(value).replace(/\s+/g, ' ').trim()
  return text.length > 72 ? `${text.slice(0, 72)}…` : text
}

function isSlow(row) {
  return Number(row.costTime) > 1000
}

function openDetail(row) {
  detail.value = row || {}
  detailVisible.value = true
}

async function copyText(text, label) {
  const content = text === null || text === undefined ? '' : String(text)
  if (!content) {
    ElMessage.warning(`${label}为空，无需复制`)
    return
  }
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success(`${label}已复制到剪贴板`)
  } catch {
    ElMessage.warning('当前浏览器不支持自动复制，请手动选择文本后复制')
  }
}

onMounted(loadLogs)
</script>

<template>
  <div class="tf-page">
    <PageHero
      eyebrow="系统审计"
      title="操作日志"
      desc="记录系统关键操作与调用耗时，便于追溯变更、定位异常。"
    >
      <template #actions>
        <el-button :icon="Refresh" plain :loading="loading" @click="loadLogs">刷新日志</el-button>
      </template>
    </PageHero>

    <section class="tf-card tf-card--flush">
      <header class="tf-card__head">
        <div class="log-head__title">
          <span class="tf-card__title">日志列表</span>
          <span class="log-total">共 {{ total }} 条记录</span>
        </div>
        <span class="log-head__note">
          <el-icon><Clock /></el-icon>
          日志按操作时间倒序排列，仅支持查看
        </span>
      </header>

      <div class="log-table">
        <el-table
          v-loading="loading"
          :data="list"
          row-key="id"
          table-layout="fixed"
          style="width: 100%"
          @row-dblclick="openDetail"
        >
          <el-table-column prop="operateEmpName" label="操作人" width="120">
            <template #default="{ row }">
              <div class="log-operator">
                <span class="log-operator__avatar">{{ operatorName(row).slice(0, 1) }}</span>
                <div class="log-operator__body">
                  <span class="log-operator__name">{{ operatorName(row) }}</span>
                  <span v-if="row.operateEmpId" class="log-operator__id tf-mono">
                    #{{ row.operateEmpId }}
                  </span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="operateTime" label="操作时间" width="168">
            <template #default="{ row }">
              <span class="log-time">{{ formatDateTime(row.operateTime) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="className" label="类名" min-width="190">
            <template #default="{ row }">
              <span class="log-class tf-ellipsis" :title="row.className">{{ row.className || '—' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="methodName" label="方法名" width="104">
            <template #default="{ row }">
              <span class="log-method">{{ row.methodName || '—' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="costTime" label="耗时（ms）" width="104" align="right">
            <template #default="{ row }">
              <span class="log-cost" :class="{ 'is-slow': isSlow(row) }">
                {{ Number(row.costTime) || 0 }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="methodParams" label="方法参数" min-width="190">
            <template #default="{ row }">
              <button type="button" class="log-snippet" @click="openDetail(row)">
                <span class="log-snippet__text">{{ previewText(row.methodParams) }}</span>
              </button>
            </template>
          </el-table-column>

          <el-table-column prop="returnValue" label="返回值" min-width="136">
            <template #default="{ row }">
              <button type="button" class="log-snippet" @click="openDetail(row)">
                <span class="log-snippet__text">{{ previewText(row.returnValue) }}</span>
              </button>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="96" align="right" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="View" @click="openDetail(row)">查看</el-button>
            </template>
          </el-table-column>

          <template #empty>
            <div class="tf-empty">
              <el-icon :size="46"><Document /></el-icon>
              <span>暂无操作日志</span>
            </div>
          </template>
        </el-table>
      </div>

      <footer class="tf-pager">
        <div class="tf-pager__hint">
          共 <b>{{ total }}</b> 条日志 · 双击任意行可查看完整调用信息
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
      v-model="detailVisible"
      title="日志详情"
      width="760px"
      top="6vh"
      append-to-body
      :close-on-click-modal="false"
      class="log-dialog"
    >
      <div class="log-detail">
        <div class="log-detail__summary">
          <div class="log-detail__avatar">{{ operatorName(detail).slice(0, 1) }}</div>
          <div class="log-detail__meta">
            <span class="log-detail__name">{{ operatorName(detail) }}</span>
            <span class="log-detail__time">{{ formatDateTime(detail.operateTime) }}</span>
          </div>
          <span class="log-detail__cost" :class="{ 'is-slow': isSlow(detail) }">
            耗时 {{ formatCost(detail.costTime) }}
          </span>
        </div>

        <el-descriptions :column="2" border class="log-detail__desc">
          <el-descriptions-item label="日志编号">
            <span class="tf-mono">#{{ detail.id ?? '—' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ operatorName(detail) }}
          </el-descriptions-item>
          <el-descriptions-item label="类名" :span="2">
            <span class="log-class">{{ detail.className || '—' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="方法名">
            <span class="log-method">{{ detail.methodName || '—' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            {{ Number(detail.costTime) || 0 }} ms
          </el-descriptions-item>
        </el-descriptions>

        <section class="log-block">
          <header class="log-block__head">
            <span class="log-block__title">方法参数</span>
            <el-button
              link
              type="primary"
              :icon="CopyDocument"
              @click="copyText(detail.methodParams, '方法参数')"
            >
              复制
            </el-button>
          </header>
          <pre class="log-pre">{{ prettyText(detail.methodParams) }}</pre>
        </section>

        <section class="log-block">
          <header class="log-block__head">
            <span class="log-block__title">返回值</span>
            <el-button
              link
              type="primary"
              :icon="CopyDocument"
              @click="copyText(detail.returnValue, '返回值')"
            >
              复制
            </el-button>
          </header>
          <pre class="log-pre">{{ prettyText(detail.returnValue) }}</pre>
        </section>
      </div>

      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.log-head__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-total {
  font-size: 12px;
  color: var(--tf-ink-3);
}

.log-head__note {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--tf-ink-3);
}

.log-table {
  padding: 6px 20px 0;
}

.log-operator {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 0;
}

.log-operator__avatar {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--tf-font-display);
  font-size: 12px;
  font-weight: 700;
  color: #1e6fa8;
  background: linear-gradient(135deg, #e6f6ff, #c9e9ff);
  border: 1px solid rgba(58, 155, 220, 0.18);
}

.log-operator__body {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.log-operator__name {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--tf-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-operator__id {
  font-size: 10.5px;
  color: var(--tf-ink-3);
}

.log-time {
  font-family: var(--tf-font-mono);
  font-size: 12px;
  color: var(--tf-ink-2);
}

.log-class {
  font-family: var(--tf-font-mono);
  font-size: 11.5px;
  color: #3c6079;
}

.log-method {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(58, 155, 220, 0.08);
  border: 1px solid rgba(58, 155, 220, 0.14);
  font-family: var(--tf-font-mono);
  font-size: 11.5px;
  color: #1e6fa8;
}

.log-cost {
  font-family: var(--tf-font-mono);
  font-size: 12px;
  color: var(--tf-ink-2);
}

.log-cost.is-slow {
  color: #b45309;
  font-weight: 600;
}

.log-snippet {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 4px 8px;
  border: 1px solid rgba(58, 155, 220, 0.14);
  border-radius: 7px;
  background: rgba(245, 251, 255, 0.8);
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.log-snippet:hover {
  border-color: rgba(58, 155, 220, 0.42);
  background: rgba(232, 246, 255, 0.9);
}

.log-snippet__text {
  font-family: var(--tf-font-mono);
  font-size: 11px;
  line-height: 1.5;
  color: var(--tf-ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 详情弹窗 */
.log-detail__summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: linear-gradient(120deg, rgba(235, 247, 255, 0.95), rgba(255, 255, 255, 0.6));
  border: 1px solid rgba(58, 155, 220, 0.16);
}

.log-detail__avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--tf-font-display);
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #5cb8ef, #2f8ed2);
  box-shadow: 0 8px 18px rgba(58, 155, 220, 0.28);
}

.log-detail__meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.log-detail__name {
  font-size: 14px;
  font-weight: 700;
  color: var(--tf-ink);
}

.log-detail__time {
  font-family: var(--tf-font-mono);
  font-size: 11.5px;
  color: var(--tf-ink-3);
}

.log-detail__cost {
  margin-left: auto;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(58, 155, 220, 0.08);
  font-size: 11.5px;
  color: #2b7fb8;
  white-space: nowrap;
}

.log-detail__cost.is-slow {
  background: rgba(232, 163, 61, 0.14);
  color: #b45309;
}

.log-detail__desc {
  margin-bottom: 16px;
}

.log-block + .log-block {
  margin-top: 14px;
}

.log-block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.log-block__title {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-family: var(--tf-font-display);
  font-size: 12.5px;
  font-weight: 700;
  color: var(--tf-ink);
}

.log-block__title::before {
  content: '';
  width: 3px;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(180deg, #6fc2f2, #2f8ed2);
}

.log-pre {
  margin: 0;
  max-height: 240px;
  overflow: auto;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f4fbff;
  border: 1px solid rgba(58, 155, 220, 0.14);
  font-family: var(--tf-font-mono);
  font-size: 11.5px;
  line-height: 1.7;
  color: #2c546c;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 720px) {
  .log-head__note {
    display: none;
  }
}
</style>
