<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  OfficeBuilding,
  UserFilled,
  User,
  Reading,
  DataAnalysis,
  Tickets,
  Clock,
  SwitchButton
} from '@element-plus/icons-vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { clearAuth, getUser } from '@/utils/auth'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const menus = [
  { path: '/depts', label: '部门管理', icon: OfficeBuilding },
  { path: '/emps', label: '员工管理', icon: UserFilled },
  { path: '/clazzs', label: '班级管理', icon: Reading },
  { path: '/students', label: '学员管理', icon: User },
  { path: '/report', label: '数据统计', icon: DataAnalysis },
  { path: '/logs', label: '操作日志', icon: Tickets }
]

const user = ref(getUser() || {})
const displayName = computed(() => user.value.name || user.value.username || '管理员')
const initial = computed(() => (displayName.value || 'T').slice(0, 1).toUpperCase())
const pageTitle = computed(() => route.meta?.title || '管理后台')
const currentPath = computed(() => route.path)

const profileVisible = ref(false)
const now = ref(new Date())
let clockTimer = null

const WEEK_DAYS = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

const clock = computed(() => {
  const d = now.value
  const pad = (v) => String(v).padStart(2, '0')
  return {
    date: formatDate(d, ''),
    week: WEEK_DAYS[d.getDay()],
    time: `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
})

function handleCommand(command) {
  if (command === 'profile') {
    profileVisible.value = true
  } else if (command === 'logout') {
    handleLogout()
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('退出后需要重新登录才能访问系统，确定继续吗？', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '再想想',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return
  }
  clearAuth()
  ElMessage.success('已安全退出')
  router.replace('/login')
}

onMounted(() => {
  clockTimer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar__glow" aria-hidden="true"></div>

      <div class="brand">
        <BrandLogo :size="42" />
        <div class="brand__text">
          <strong class="brand__name">TLIAS</strong>
          <span class="brand__sub">智能学习辅助系统</span>
        </div>
      </div>

      <div class="nav-label">系统导航</div>

      <nav class="nav">
        <router-link
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'is-active': currentPath === item.path }"
          :title="item.label"
        >
          <el-icon class="nav-item__icon"><component :is="item.icon" /></el-icon>
          <span class="nav-item__text">{{ item.label }}</span>
          <span class="nav-item__indicator" aria-hidden="true"></span>
        </router-link>
      </nav>

      <div class="sidebar__foot">
        <div class="side-note">
          <span class="side-note__tag">TLIAS SUITE</span>
          <p class="side-note__title">教学管理控制台</p>
          <p class="side-note__version">v1.0.0 · 内部使用</p>
        </div>
      </div>
    </aside>

    <div class="app-main">
      <header class="topbar">
        <div class="crumbs">
          <span class="crumbs__root">Tlias</span>
          <span class="crumbs__sep">/</span>
          <span class="crumbs__cur">{{ pageTitle }}</span>
        </div>

        <div class="topbar__right">
          <div class="clock" title="当前时间">
            <el-icon><Clock /></el-icon>
            <span class="clock__date">{{ clock.date }} {{ clock.week }}</span>
            <span class="clock__time">{{ clock.time }}</span>
          </div>

          <el-dropdown trigger="click" @command="handleCommand">
            <button class="user-chip" type="button">
              <span class="user-chip__avatar">{{ initial }}</span>
              <span class="user-chip__meta">
                <span class="user-chip__name">{{ displayName }}</span>
                <span class="user-chip__role">系统管理员</span>
              </span>
              <span class="user-chip__caret" aria-hidden="true"></span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="app-content">
        <div class="app-content__inner">
          <router-view v-slot="{ Component }">
            <transition name="tf-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
        <footer class="app-footer">
          Tlias 智能学习辅助系统 · 教学管理控制台
        </footer>
      </main>
    </div>

    <el-dialog v-model="profileVisible" title="个人信息" width="420px" append-to-body>
      <div class="profile">
        <div class="profile__avatar">{{ initial }}</div>
        <div class="profile__name">{{ displayName }}</div>
        <el-tag type="primary" effect="light">已登录</el-tag>
      </div>
      <el-descriptions :column="1" border class="profile__desc">
        <el-descriptions-item label="用户名">{{ user.username || '—' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ user.name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">正常</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="profileVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

/* ============ 侧边栏 ============ */
.sidebar {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 40;
  display: flex;
  flex-direction: column;
  width: var(--tf-sidebar-w);
  padding: 18px 14px 16px;
  color: #fff;
  background: linear-gradient(180deg, #1e6fa8 0%, #3a9bdc 100%);
  box-shadow: 8px 0 30px rgba(16, 63, 94, 0.18);
  overflow: hidden;
  transition: width 0.24s ease;
}

.sidebar__glow {
  position: absolute;
  top: -80px;
  left: -60px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0) 68%);
  pointer-events: none;
}

.sidebar::after {
  content: '';
  position: absolute;
  right: -70px;
  bottom: -90px;
  width: 230px;
  height: 230px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 0 0 26px rgba(255, 255, 255, 0.05), 0 0 0 58px rgba(255, 255, 255, 0.035);
  pointer-events: none;
}

.brand {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 6px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.16);
}

.brand__text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.brand__name {
  font-family: var(--tf-font-display);
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.16em;
  line-height: 1.2;
  color: #fff;
}

.brand__sub {
  font-size: 10px;
  letter-spacing: 0.04em;
  color: rgba(255, 255, 255, 0.72);
  white-space: nowrap;
  transform: scale(0.96);
  transform-origin: left center;
}

.nav-label {
  margin: 18px 8px 8px;
  font-family: var(--tf-font-display);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.22em;
  color: rgba(255, 255, 255, 0.52);
  text-transform: uppercase;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 6px;
}

.nav::-webkit-scrollbar {
  width: 4px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  height: 42px;
  padding: 0 12px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 13.5px;
  font-weight: 500;
  letter-spacing: 0.02em;
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  transform: translateX(1px);
}

.nav-item.is-active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 700;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.34), 0 8px 18px rgba(9, 52, 80, 0.18);
  -webkit-backdrop-filter: blur(6px);
  backdrop-filter: blur(6px);
}

.nav-item__icon {
  font-size: 16px;
  flex-shrink: 0;
}

.nav-item__text {
  white-space: nowrap;
  overflow: hidden;
}

.nav-item__indicator {
  width: 3px;
  height: 0;
  margin-left: auto;
  border-radius: 999px;
  background: #bfe8ff;
  box-shadow: 0 0 10px rgba(191, 232, 255, 0.9);
  transition: height 0.22s ease;
}

.nav-item.is-active .nav-item__indicator {
  height: 16px;
}

.sidebar__foot {
  margin-top: auto;
  padding-top: 14px;
}

.side-note {
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.side-note__tag {
  font-family: var(--tf-font-display);
  font-size: 9.5px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: rgba(255, 255, 255, 0.62);
}

.side-note__title {
  margin: 6px 0 2px;
  font-size: 12.5px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.94);
}

.side-note__version {
  margin: 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

/* ============ 主区域 ============ */
.app-main {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  margin-left: var(--tf-sidebar-w);
  transition: margin-left 0.24s ease;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 64px;
  padding: 0 26px;
  background: rgba(255, 255, 255, 0.66);
  border-bottom: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 6px 24px rgba(58, 155, 220, 0.08);
  -webkit-backdrop-filter: blur(16px) saturate(150%);
  backdrop-filter: blur(16px) saturate(150%);
}

.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  min-width: 0;
}

.crumbs__root {
  font-family: var(--tf-font-display);
  font-weight: 700;
  letter-spacing: 0.14em;
  font-size: 12px;
  color: var(--tf-brand-dark);
  text-transform: uppercase;
}

.crumbs__sep {
  color: rgba(58, 155, 220, 0.4);
}

.crumbs__cur {
  color: var(--tf-ink-2);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.clock {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(58, 155, 220, 0.09);
  color: #2f6f97;
  font-size: 12px;
}

.clock__date {
  color: #4d7e9c;
}

.clock__time {
  font-family: var(--tf-font-display);
  font-weight: 600;
  letter-spacing: 0.06em;
  color: var(--tf-brand-dark);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  height: 42px;
  padding: 0 12px 0 6px;
  border: 1px solid rgba(58, 155, 220, 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-family: inherit;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.user-chip:hover {
  border-color: rgba(58, 155, 220, 0.34);
  box-shadow: 0 8px 18px rgba(58, 155, 220, 0.16);
}

.user-chip__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5cb8ef, #1e6fa8);
  color: #fff;
  font-family: var(--tf-font-display);
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 4px 10px rgba(30, 111, 168, 0.32);
}

.user-chip__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.15;
}

.user-chip__name {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--tf-ink);
}

.user-chip__role {
  font-size: 10.5px;
  color: var(--tf-muted);
}

.user-chip__caret {
  width: 7px;
  height: 7px;
  margin-top: -3px;
  border-right: 1.6px solid #6f95ad;
  border-bottom: 1.6px solid #6f95ad;
  transform: rotate(45deg);
}

.app-content {
  flex: 1;
  padding: 22px 26px 0;
}

.app-content__inner {
  max-width: 1600px;
  margin: 0 auto;
}

.app-footer {
  padding: 22px 0 18px;
  text-align: center;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: rgba(83, 122, 146, 0.6);
}

/* ============ 个人信息 ============ */
.profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 6px 0 18px;
}

.profile__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5cb8ef, #1e6fa8);
  color: #fff;
  font-family: var(--tf-font-display);
  font-size: 24px;
  font-weight: 700;
  box-shadow: 0 10px 24px rgba(30, 111, 168, 0.3);
}

.profile__name {
  font-family: var(--tf-font-display);
  font-size: 17px;
  font-weight: 700;
  color: var(--tf-ink);
}

.profile__desc {
  margin-top: 4px;
}

/* ============ 响应式 ============ */
@media (max-width: 1100px) {
  .sidebar {
    padding: 16px 10px 14px;
    align-items: center;
  }

  .brand {
    justify-content: center;
    padding: 4px 0 16px;
    width: 100%;
  }

  .brand__text,
  .nav-label,
  .side-note,
  .nav-item__text {
    display: none;
  }

  .nav {
    width: 100%;
    align-items: center;
  }

  .nav-item {
    justify-content: center;
    padding: 0;
    width: 46px;
    height: 44px;
  }

  .nav-item__indicator {
    position: absolute;
    right: 5px;
  }

  .topbar {
    padding: 0 18px;
  }

  .app-content {
    padding: 18px 18px 0;
  }

  .clock__date {
    display: none;
  }
}

@media (max-width: 720px) {
  .clock {
    display: none;
  }

  .user-chip__meta {
    display: none;
  }

  .topbar {
    height: 58px;
  }
}
</style>
