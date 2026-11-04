import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layout/AppLayout.vue'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/depts',
    children: [
      {
        path: 'depts',
        name: 'depts',
        component: () => import('@/views/DeptView.vue'),
        meta: { title: '部门管理', desc: '维护组织架构中的部门信息' }
      },
      {
        path: 'emps',
        name: 'emps',
        component: () => import('@/views/EmpView.vue'),
        meta: { title: '员工管理', desc: '管理员工档案、职位与工作经历' }
      },
      {
        path: 'clazzs',
        name: 'clazzs',
        component: () => import('@/views/ClazzView.vue'),
        meta: { title: '班级管理', desc: '维护班级信息、班主任与学科方向' }
      },
      {
        path: 'students',
        name: 'students',
        component: () => import('@/views/StudentView.vue'),
        meta: { title: '学员管理', desc: '跟进学员信息与违纪情况' }
      },
      {
        path: 'report',
        name: 'report',
        component: () => import('@/views/ReportView.vue'),
        meta: { title: '数据统计', desc: '员工与学员的可视化数据概览' }
      },
      {
        path: 'logs',
        name: 'logs',
        component: () => import('@/views/LogView.vue'),
        meta: { title: '操作日志', desc: '查看系统内的操作记录与执行耗时' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/depts'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const token = getToken()
  if (!token && !to.meta.public) {
    return { path: '/login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : {} }
  }
  if (token && to.path === '/login') {
    return { path: '/depts' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta?.title
    ? `${to.meta.title} · Tlias 智能学习辅助系统`
    : 'Tlias 智能学习辅助系统'
})

export default router
