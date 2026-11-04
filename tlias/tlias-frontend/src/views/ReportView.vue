<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import {
  DataAnalysis,
  PieChart,
  Reading,
  Refresh,
  TrendCharts,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import PageHero from '@/components/PageHero.vue'
import {
  getEmpGenderData,
  getEmpJobData,
  getStudentCountData,
  getStudentDegreeData
} from '@/api/report'
import { jobText } from '@/utils/dict'

const loading = ref(false)
const chartReady = ref(false)

const genderData = ref([])
const jobData = ref({ jobList: [], dataList: [] })
const degreeData = ref([])
const countData = ref({ clazzList: [], dataList: [] })

const GENDER_COLORS = ['#3a9bdc', '#f0a6c0', '#2eb88a', '#e8a33d']
const CHART_COLORS = ['#4aa9e0', '#2eb88a', '#e8a33d', '#8b9ff0', '#5cc6d6', '#e8686a']
const DEGREE_COLORS = ['#9ad4f2', '#4aa9e0', '#2eb88a', '#e8a33d', '#8b9ff0', '#e8686a']
const JOB_COLORS = ['#4aa9e0', '#5cc6d6', '#2eb88a', '#e8a33d', '#8b9ff0']

const DONUT_R = 46
const DONUT_C = 2 * Math.PI * DONUT_R

function normalizePairs(data) {
  if (!Array.isArray(data)) return []
  return data
    .map((item, index) => ({
      name: item?.name === null || item?.name === undefined || item?.name === ''
        ? `未分类 ${index + 1}`
        : String(item.name),
      value: Number(item?.value) || 0
    }))
    .filter((item) => item.value > 0)
}

const genderRows = computed(() =>
  normalizePairs(genderData.value).map((item, index) => ({
    ...item,
    color: item.name.includes('女')
      ? GENDER_COLORS[1]
      : item.name.includes('男')
        ? GENDER_COLORS[0]
        : GENDER_COLORS[index % GENDER_COLORS.length]
  }))
)

const genderTotal = computed(() => genderRows.value.reduce((sum, item) => sum + item.value, 0))

const genderSegments = computed(() => {
  let offset = 0
  return genderRows.value.map((item) => {
    const ratio = genderTotal.value ? item.value / genderTotal.value : 0
    const length = Math.max(ratio * DONUT_C - 3, 0)
    const segment = {
      ...item,
      ratio,
      length,
      offset
    }
    offset += ratio * DONUT_C
    return segment
  })
})

const jobRows = computed(() => {
  const names = Array.isArray(jobData.value?.jobList) ? jobData.value.jobList : []
  const values = Array.isArray(jobData.value?.dataList) ? jobData.value.dataList : []
  return names
    .map((name, index) => ({
      name: jobText(name),
      value: Number(values[index]) || 0,
      color: JOB_COLORS[index % JOB_COLORS.length]
    }))
    .filter((item) => item.value > 0)
})

const jobTotal = computed(() => jobRows.value.reduce((sum, item) => sum + item.value, 0))
const jobMax = computed(() => Math.max(1, ...jobRows.value.map((item) => item.value)))

function percentOf(value, total) {
  if (!total) return 0
  return Math.round((value / total) * 1000) / 10
}

const degreeBase = computed(() => normalizePairs(degreeData.value))

const degreeTotal = computed(() => degreeBase.value.reduce((sum, item) => sum + item.value, 0))

const degreeRows = computed(() =>
  degreeBase.value.map((item, index) => ({
    ...item,
    color: DEGREE_COLORS[index % DEGREE_COLORS.length],
    percent: percentOf(item.value, degreeTotal.value)
  }))
)

const degreeGradient = computed(() => {
  if (!degreeTotal.value) return 'conic-gradient(#eaf6fd 0% 100%)'
  let cursor = 0
  const stops = degreeRows.value.map((item) => {
    const start = cursor
    cursor += (item.value / degreeTotal.value) * 100
    return `${item.color} ${start.toFixed(2)}% ${cursor.toFixed(2)}%`
  })
  return `conic-gradient(from -90deg, ${stops.join(', ')})`
})

const classRows = computed(() => {
  const names = Array.isArray(countData.value?.clazzList) ? countData.value.clazzList : []
  const values = Array.isArray(countData.value?.dataList) ? countData.value.dataList : []
  return names.map((name, index) => ({
    name: String(name || `班级 ${index + 1}`),
    value: Number(values[index]) || 0
  }))
})

const classTotal = computed(() => classRows.value.reduce((sum, item) => sum + item.value, 0))
const classMax = computed(() => Math.max(1, ...classRows.value.map((item) => item.value)))

function barWidth(value, max) {
  if (!chartReady.value) return '0%'
  return `${Math.max((value / max) * 100, 2)}%`
}

function columnHeight(value, max) {
  if (!chartReady.value) return '0%'
  return `${Math.max((value / max) * 100, 2)}%`
}

async function loadReport() {
  loading.value = true
  chartReady.value = false
  try {
    const [gender, job, degree, count] = await Promise.all([
      getEmpGenderData().catch(() => []),
      getEmpJobData().catch(() => ({ jobList: [], dataList: [] })),
      getStudentDegreeData().catch(() => []),
      getStudentCountData().catch(() => ({ clazzList: [], dataList: [] }))
    ])
    genderData.value = Array.isArray(gender) ? gender : []
    jobData.value = job && typeof job === 'object' ? job : { jobList: [], dataList: [] }
    degreeData.value = Array.isArray(degree) ? degree : []
    countData.value = count && typeof count === 'object' ? count : { clazzList: [], dataList: [] }
  } finally {
    loading.value = false
    await nextTick()
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        chartReady.value = true
      })
    })
  }
}

onMounted(loadReport)
</script>

<template>
  <div class="tf-page">
    <PageHero
      eyebrow="数据中心"
      title="数据统计"
      desc="从部门、职位、学历与班级四个维度，实时洞察师资结构与学员分布。"
    >
      <template #actions>
        <el-button :icon="Refresh" plain :loading="loading" @click="loadReport">刷新数据</el-button>
      </template>
    </PageHero>

    <section class="report-stats">
      <article class="report-stat">
        <span class="report-stat__icon"><el-icon><UserFilled /></el-icon></span>
        <div class="report-stat__body">
          <span class="report-stat__label">员工总数</span>
          <strong class="report-stat__value">{{ genderTotal }}</strong>
        </div>
        <span class="report-stat__unit">人</span>
      </article>
      <article class="report-stat">
        <span class="report-stat__icon is-green"><el-icon><DataAnalysis /></el-icon></span>
        <div class="report-stat__body">
          <span class="report-stat__label">岗位种类</span>
          <strong class="report-stat__value">{{ jobRows.length }}</strong>
        </div>
        <span class="report-stat__unit">类</span>
      </article>
      <article class="report-stat">
        <span class="report-stat__icon is-amber"><el-icon><User /></el-icon></span>
        <div class="report-stat__body">
          <span class="report-stat__label">学员总数</span>
          <strong class="report-stat__value">{{ degreeTotal }}</strong>
        </div>
        <span class="report-stat__unit">人</span>
      </article>
      <article class="report-stat">
        <span class="report-stat__icon is-violet"><el-icon><Reading /></el-icon></span>
        <div class="report-stat__body">
          <span class="report-stat__label">班级人数</span>
          <strong class="report-stat__value">{{ classTotal }}</strong>
        </div>
        <span class="report-stat__unit">人</span>
      </article>
    </section>

    <section class="report-grid">
      <!-- 员工性别 -->
      <article v-loading="loading" class="tf-card report-card">
        <header class="tf-card__head">
          <div class="report-card__title">
            <span class="tf-card__title">员工性别分布</span>
            <span class="report-card__sub">EMPLOYEE GENDER</span>
          </div>
          <span class="report-chip">共 {{ genderTotal }} 人</span>
        </header>
        <div class="tf-card__body report-body">
          <div v-if="genderRows.length" class="donut-wrap">
            <svg viewBox="0 0 120 120" class="donut" role="img" aria-label="员工性别分布环形图">
              <circle cx="60" cy="60" :r="DONUT_R" fill="none" stroke="#eaf6fd" stroke-width="14" />
              <circle
                v-for="(item, index) in genderSegments"
                :key="item.name"
                cx="60"
                cy="60"
                :r="DONUT_R"
                fill="none"
                :stroke="item.color"
                stroke-width="14"
                stroke-linecap="round"
                :stroke-dasharray="chartReady ? `${item.length} ${DONUT_C - item.length}` : `0 ${DONUT_C}`"
                :stroke-dashoffset="-item.offset"
                transform="rotate(-90 60 60)"
                class="donut__seg"
                :style="{ transitionDelay: `${index * 0.14}s` }"
              />
              <text x="60" y="58" text-anchor="middle" class="donut__total">{{ genderTotal }}</text>
              <text x="60" y="74" text-anchor="middle" class="donut__unit">员工总数</text>
            </svg>

            <ul class="legend">
              <li v-for="item in genderRows" :key="item.name" class="legend__item">
                <span class="legend__dot" :style="{ background: item.color }"></span>
                <span class="legend__name">{{ item.name }}</span>
                <span class="legend__value">{{ item.value }} 人</span>
                <span class="legend__percent">{{ percentOf(item.value, genderTotal) }}%</span>
              </li>
            </ul>
          </div>
          <div v-else class="chart-empty">
            <el-icon :size="34"><PieChart /></el-icon>
            <span>暂无员工性别数据</span>
          </div>
        </div>
      </article>

      <!-- 员工职位 -->
      <article v-loading="loading" class="tf-card report-card">
        <header class="tf-card__head">
          <div class="report-card__title">
            <span class="tf-card__title">员工职位人数</span>
            <span class="report-card__sub">POSITION DISTRIBUTION</span>
          </div>
          <span class="report-chip">{{ jobRows.length }} 个岗位</span>
        </header>
        <div class="tf-card__body report-body">
          <div v-if="jobRows.length" class="hbars">
            <div v-for="item in jobRows" :key="item.name" class="hbar">
              <div class="hbar__top">
                <span class="hbar__name">{{ item.name }}</span>
                <span class="hbar__value">{{ item.value }} 人</span>
              </div>
              <div class="hbar__track">
                <div
                  class="hbar__fill"
                  :style="{
                    width: barWidth(item.value, jobMax),
                    background: `linear-gradient(90deg, ${item.color}, ${item.color}99)`
                  }"
                ></div>
              </div>
            </div>
          </div>
          <div v-else class="chart-empty">
            <el-icon :size="34"><TrendCharts /></el-icon>
            <span>暂无员工职位数据</span>
          </div>
        </div>
      </article>

      <!-- 学员学历 -->
      <article v-loading="loading" class="tf-card report-card">
        <header class="tf-card__head">
          <div class="report-card__title">
            <span class="tf-card__title">学员学历分布</span>
            <span class="report-card__sub">EDUCATION BACKGROUND</span>
          </div>
          <span class="report-chip">共 {{ degreeTotal }} 人</span>
        </header>
        <div class="tf-card__body report-body">
          <div v-if="degreeRows.length" class="degree-wrap">
            <div class="conic" :class="{ 'is-ready': chartReady }">
              <div class="conic__ring" :style="{ background: degreeGradient }"></div>
              <div class="conic__hole">
                <strong>{{ degreeRows.length }}</strong>
                <span>学历层次</span>
              </div>
            </div>
            <ul class="legend legend--degree">
              <li v-for="item in degreeRows" :key="item.name" class="legend__item">
                <span class="legend__dot" :style="{ background: item.color }"></span>
                <span class="legend__name">{{ item.name }}</span>
                <span class="legend__value">{{ item.value }} 人</span>
                <span class="legend__percent">{{ item.percent }}%</span>
              </li>
            </ul>
          </div>
          <div v-else class="chart-empty">
            <el-icon :size="34"><PieChart /></el-icon>
            <span>暂无学员学历数据</span>
          </div>
        </div>
      </article>

      <!-- 班级人数 -->
      <article v-loading="loading" class="tf-card report-card">
        <header class="tf-card__head">
          <div class="report-card__title">
            <span class="tf-card__title">班级人数</span>
            <span class="report-card__sub">STUDENTS PER CLASS</span>
          </div>
          <span class="report-chip">{{ classRows.length }} 个班级</span>
        </header>
        <div class="tf-card__body report-body">
          <div v-if="classRows.length" class="vbars">
            <div v-for="item in classRows" :key="item.name" class="vbar">
              <span class="vbar__value">{{ item.value }}</span>
              <div class="vbar__track">
                <div
                  class="vbar__fill"
                  :style="{ height: columnHeight(item.value, classMax) }"
                  :title="`${item.name}：${item.value} 人`"
                ></div>
              </div>
              <span class="vbar__name" :title="item.name">{{ item.name }}</span>
            </div>
          </div>
          <div v-else class="chart-empty">
            <el-icon :size="34"><Reading /></el-icon>
            <span>暂无班级人数数据</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
/* 概览统计 */
.report-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.report-stat {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: var(--tf-radius);
  background: var(--tf-card-bg);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 10px 30px rgba(58, 155, 220, 0.1);
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
}

.report-stat__icon {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  color: #1e6fa8;
  background: linear-gradient(135deg, #e6f6ff, #c9e9ff);
  border: 1px solid rgba(58, 155, 220, 0.18);
}

.report-stat__icon.is-green {
  color: #1f8a68;
  background: linear-gradient(135deg, #e6faf3, #c8f1e2);
  border-color: rgba(46, 184, 138, 0.2);
}

.report-stat__icon.is-amber {
  color: #a8691b;
  background: linear-gradient(135deg, #fff6e6, #ffe9c4);
  border-color: rgba(232, 163, 61, 0.22);
}

.report-stat__icon.is-violet {
  color: #5b62b8;
  background: linear-gradient(135deg, #f0f1ff, #dfe2ff);
  border-color: rgba(139, 159, 240, 0.25);
}

.report-stat__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.report-stat__label {
  font-size: 12px;
  color: var(--tf-ink-3);
}

.report-stat__value {
  font-family: var(--tf-font-display);
  font-size: 24px;
  font-weight: 700;
  line-height: 1.1;
  color: var(--tf-ink);
}

.report-stat__unit {
  margin-left: auto;
  align-self: flex-end;
  font-size: 11.5px;
  color: var(--tf-ink-3);
}

/* 图表网格 */
.report-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.report-card {
  min-height: 328px;
  display: flex;
  flex-direction: column;
}

.report-card__title {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.report-card__sub {
  padding-left: 14px;
  font-family: var(--tf-font-display);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: #9db9ca;
}

.report-chip {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(58, 155, 220, 0.08);
  border: 1px solid rgba(58, 155, 220, 0.16);
  font-size: 11.5px;
  color: #2b7fb8;
  white-space: nowrap;
}

.report-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 220px;
}

/* 环形图 */
.donut-wrap {
  display: flex;
  align-items: center;
  gap: 30px;
  width: 100%;
}

.donut {
  flex-shrink: 0;
  width: 196px;
  height: 196px;
  filter: drop-shadow(0 8px 18px rgba(58, 155, 220, 0.14));
}

.donut__seg {
  transition: stroke-dasharray 1s cubic-bezier(0.22, 0.61, 0.36, 1);
}

.donut__total {
  font-family: var(--tf-font-display);
  font-size: 24px;
  font-weight: 700;
  fill: #123b56;
}

.donut__unit {
  font-size: 8px;
  letter-spacing: 0.08em;
  fill: #6f8da1;
}

/* 图例 */
.legend {
  flex: 1;
  min-width: 0;
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend__item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  min-width: 0;
}

.legend__dot {
  flex-shrink: 0;
  width: 9px;
  height: 9px;
  border-radius: 3px;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.55);
}

.legend__name {
  color: var(--tf-ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.legend__value {
  margin-left: auto;
  font-family: var(--tf-font-mono);
  font-size: 12px;
  color: var(--tf-ink);
}

.legend__percent {
  width: 52px;
  text-align: right;
  font-family: var(--tf-font-display);
  font-size: 12px;
  font-weight: 700;
  color: #2b7fb8;
}

/* 横向柱状 */
.hbars {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 2px;
}

.hbar__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 7px;
}

.hbar__name {
  font-size: 12.5px;
  color: var(--tf-ink-2);
}

.hbar__value {
  font-family: var(--tf-font-mono);
  font-size: 12px;
  color: var(--tf-ink);
}

.hbar__track {
  height: 12px;
  border-radius: 999px;
  background: rgba(58, 155, 220, 0.09);
  overflow: hidden;
}

.hbar__fill {
  height: 100%;
  border-radius: 999px;
  transition: width 1s cubic-bezier(0.22, 0.61, 0.36, 1);
  box-shadow: 0 2px 8px rgba(58, 155, 220, 0.22);
}

/* 学历 conic */
.degree-wrap {
  display: flex;
  align-items: center;
  gap: 30px;
  width: 100%;
}

.conic {
  position: relative;
  flex-shrink: 0;
  width: 178px;
  height: 178px;
  opacity: 0;
  transform: scale(0.94) rotate(-8deg);
  transition: opacity 0.9s ease, transform 0.9s cubic-bezier(0.22, 0.61, 0.36, 1);
}

.conic.is-ready {
  opacity: 1;
  transform: scale(1) rotate(0deg);
}

.conic__ring {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.6), 0 10px 24px rgba(58, 155, 220, 0.14);
}

.conic__hole {
  position: absolute;
  inset: 32px;
  border-radius: 50%;
  background: linear-gradient(160deg, #ffffff, #f4fbff);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  box-shadow: 0 0 0 1px rgba(58, 155, 220, 0.08) inset;
}

.conic__hole strong {
  font-family: var(--tf-font-display);
  font-size: 24px;
  color: var(--tf-ink);
}

.conic__hole span {
  font-size: 10.5px;
  letter-spacing: 0.06em;
  color: var(--tf-ink-3);
}

/* 纵向柱状 */
.vbars {
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  gap: 14px;
  padding: 4px 2px 0;
  overflow-x: auto;
}

.vbar {
  flex: 1 1 0;
  min-width: 54px;
  max-width: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.vbar__value {
  font-family: var(--tf-font-display);
  font-size: 13px;
  font-weight: 700;
  color: #1e6fa8;
}

.vbar__track {
  width: 100%;
  height: 146px;
  display: flex;
  align-items: flex-end;
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(58, 155, 220, 0.04), rgba(58, 155, 220, 0.09));
  padding: 0 7px;
}

.vbar__fill {
  width: 100%;
  border-radius: 8px 8px 4px 4px;
  background: linear-gradient(180deg, #6fc2f2 0%, #3a9bdc 62%, #267fb8 100%);
  box-shadow: 0 -2px 10px rgba(58, 155, 220, 0.28);
  transition: height 1s cubic-bezier(0.22, 0.61, 0.36, 1);
}

.vbar__name {
  max-width: 100%;
  font-size: 11.5px;
  color: var(--tf-ink-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 空状态 */
.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 30px 10px;
  color: var(--tf-ink-3);
  font-size: 12.5px;
}

.chart-empty .el-icon {
  color: #b6d3e4;
}

@media (max-width: 1200px) {
  .report-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .report-grid {
    grid-template-columns: 1fr;
  }

  .report-card {
    min-height: 300px;
  }
}

@media (max-width: 560px) {
  .report-stats {
    grid-template-columns: 1fr;
  }

  .donut-wrap,
  .degree-wrap {
    flex-direction: column;
    gap: 18px;
  }

  .legend {
    width: 100%;
  }
}
</style>
