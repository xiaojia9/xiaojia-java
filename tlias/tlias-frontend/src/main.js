import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import 'element-plus/dist/index.css'
import '@/styles/global.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全局注册 Element Plus 图标，模板中可直接使用 <el-icon><User /></el-icon>
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app.use(ElementPlus, { locale: zhCn })
app.use(router)

app.mount('#app')
