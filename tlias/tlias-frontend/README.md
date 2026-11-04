# Tlias 智能学习辅助系统 · 前端

基于 **Vue 3 + Vite 5 + Element Plus** 的天蓝主题后台管理前端，覆盖部门、员工、班级、学员、数据报表与操作日志六大模块。

## 技术栈

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| Vue | ^3.5.13 | 全部使用 `<script setup>` 单文件组件 |
| Vite | ^5.4.11 | 开发服务器与生产构建 |
| Element Plus | ^2.9.1 | 组件库（中文语言包、全量图标注册） |
| vue-router | ^4.5.0 | History 路由 + 登录守卫 |
| axios | ^1.7.9 | 统一请求/响应拦截 |
| @element-plus/icons-vue | ^2.3.1 | 图标 |

无 TypeScript、Pinia、Tailwind、ECharts；图表全部由内联 SVG / CSS 绘制，样式为原生 CSS（CSS 变量主题）。

## 环境要求

- Node.js 18+（本项目在 Node 24 下开发构建）
- npm 9+（本项目在 npm 11 下开发构建）
- 后端服务运行在 `http://localhost:8080`（无 context path）

如内网访问 npm 较慢，可切换镜像：

```bash
npm config set registry https://registry.npmmirror.com
```

## 快速开始

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器（默认 http://127.0.0.1:5173）
npm run dev

# 3. 生产构建，产物输出到 dist/
npm run build

# 4. 本地预览构建产物
npm run preview
```

> 前端所有请求都走 `/api` 前缀，由 Vite 代理转发并去掉前缀：
> `/api/depts` → `http://localhost:8080/depts`。
> 因此开发时请先启动后端服务，否则页面会提示「网络异常，请确认后端服务已启动」。
> 部署到 Nginx 等服务器时，请自行配置等价的 `/api` 反向代理。

### 演示账号

| 用户名 | 密码 |
| --- | --- |
| `songjiang` | `123456` |

登录页提供「一键填充」按钮，可直接填入演示账号。

## 功能模块

| 路由 | 页面 | 主要能力 |
| --- | --- | --- |
| `/login` | 登录 | 粒子网格动态背景、玻璃拟态登录卡、表单校验 |
| `/depts` | 部门管理 | 列表、关键字过滤、新增/编辑、删除确认 |
| `/emps` | 员工管理 | 条件查询、分页、头像上传、职位标签、工作经历动态增删、批量删除 |
| `/clazzs` | 班级管理 | 名称/结课时间查询、班主任与学科选择、状态标签 |
| `/students` | 学员管理 | 条件查询、学历/班级筛选、违纪扣分处理、批量删除 |
| `/report` | 数据报表 | 员工性别环形图、职位横向条形图、学员学历环形图、班级人数柱状图（入场动画） |
| `/logs` | 操作日志 | 调用信息列表、参数/返回值详情弹窗与复制、分页只读 |

## 目录结构

```
tlias-frontend/
├── index.html
├── package.json
├── vite.config.js
├── README.md
└── src/
    ├── main.js                 # 应用入口：Element Plus 中文包 + 全量图标
    ├── App.vue                 # 路由出口与页面过渡
    ├── api/                    # 接口层（与后端一一对应）
    │   ├── auth.js
    │   ├── dept.js
    │   ├── emp.js
    │   ├── clazz.js
    │   ├── student.js
    │   ├── report.js
    │   ├── log.js
    │   └── upload.js
    ├── components/
    │   ├── ParticleBackground.vue   # 登录页粒子网格 Canvas
    │   ├── BrandLogo.vue            # Tlias 品牌 Logo
    │   └── PageHero.vue             # 页面标题区
    ├── layout/
    │   └── AppLayout.vue       # 固定侧边栏 + 顶栏 + 内容区
    ├── router/
    │   └── index.js            # 路由表与登录守卫
    ├── styles/
    │   └── global.css          # 主题变量、Element Plus 覆盖、通用样式
    ├── utils/
    │   ├── auth.js             # token / 用户信息存取
    │   ├── request.js          # axios 实例与拦截器
    │   ├── format.js           # 日期、金额、耗时等格式化
    │   └── dict.js             # 性别/职位/学科/学历等枚举映射
    └── views/
        ├── Login.vue
        ├── DeptView.vue
        ├── EmpView.vue
        ├── ClazzView.vue
        ├── StudentView.vue
        ├── ReportView.vue
        └── LogView.vue
```

## 约定说明

### 接口响应

后端统一返回 `{ code, msg, data }`，`code === 1` 表示成功。`src/utils/request.js` 的响应拦截器会：

- 成功时直接返回 `data`；
- `code === 0` 时 `ElMessage.error(msg)` 并 reject；
- HTTP 401 时清除本地登录信息并跳转 `/login`；
- 请求自动携带 `token` 请求头。

### 主题

全局主题色定义在 `src/styles/global.css`：

```css
:root {
  --tf-brand: #3a9bdc;
  --tf-brand-dark: #1e6fa8;
  --el-color-primary: var(--tf-brand);
}
```

页面背景为天蓝分层渐变 + 固定径向光晕，卡片使用 16px 圆角、半透明白底与柔和投影。

### 构建产物

`npm run build` 输出到 `dist/`，可直接由任意静态服务器托管；请确保 `/api` 反向代理指向后端。
