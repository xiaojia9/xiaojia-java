<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import ParticleBackground from '@/components/ParticleBackground.vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { login } from '@/api/auth'
import { setAuth } from '@/utils/auth'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

function fillDemo() {
  form.username = 'songjiang'
  form.password = '123456'
  formRef.value?.clearValidate()
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await login({
      username: form.username.trim(),
      password: form.password
    })
    setAuth(
      {
        id: data?.id,
        username: data?.username,
        name: data?.name
      },
      data?.token
    )
    ElMessage.success(`欢迎回来，${data?.name || data?.username || '管理员'}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.replace(redirect && redirect.startsWith('/') ? redirect : '/depts')
  } catch {
    // 错误提示已由 axios 响应拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <ParticleBackground />

    <div class="orb orb--1" aria-hidden="true"></div>
    <div class="orb orb--2" aria-hidden="true"></div>
    <div class="orb orb--3" aria-hidden="true"></div>

    <div class="orbit" aria-hidden="true">
      <span class="orbit__ring orbit__ring--1"></span>
      <span class="orbit__ring orbit__ring--2"></span>
      <span class="orbit__ring orbit__ring--3"></span>
      <span class="orbit__dot"></span>
    </div>

    <div class="login-shell">
      <section class="login-card">
        <div class="login-card__shine" aria-hidden="true"></div>

        <div class="login-head">
          <div class="login-badge">TLIAS · ADMIN CONSOLE</div>
          <BrandLogo :size="62" glow class="login-logo" />
          <h1 class="login-title">Tlias 智能学习辅助系统</h1>
          <p class="login-subtitle">登录管理后台，开启高效教学管理</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          size="large"
          hide-required-asterisk
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              aria-label="用户名"
              autocomplete="username"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              aria-label="密码"
              autocomplete="current-password"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item class="login-submit-item">
            <el-button
              type="primary"
              class="login-submit"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading" class="login-submit__label">登 录</span>
              <span v-if="!loading" class="login-submit__arrow" aria-hidden="true">→</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="demo-hint">
          <div class="demo-hint__text">
            演示账号：<b>songjiang</b> / <b>123456</b>
          </div>
          <button class="demo-hint__fill" type="button" @click="fillDemo">一键填充</button>
        </div>
      </section>

      <p class="login-foot">© 2026 Tlias · 让每一次教学决策都有数据可依</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px 20px;
  overflow: hidden;
  background: linear-gradient(155deg, #f5fcff 0%, #e2f4ff 34%, #c6e6ff 68%, #a9d8fb 100%);
}

.login-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(30, 111, 168, 0.1) 1px, transparent 1px);
  background-size: 26px 26px;
  -webkit-mask-image: radial-gradient(circle at 50% 45%, rgba(0, 0, 0, 0.65), transparent 72%);
  mask-image: radial-gradient(circle at 50% 45%, rgba(0, 0, 0, 0.65), transparent 72%);
  pointer-events: none;
}

/* 背景光斑 */
.orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(2px);
}

.orb--1 {
  top: -190px;
  left: -160px;
  width: 560px;
  height: 560px;
  background: radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0) 66%);
  animation: tf-drift-a 22s ease-in-out infinite alternate;
}

.orb--2 {
  right: -200px;
  bottom: -220px;
  width: 640px;
  height: 640px;
  background: radial-gradient(circle at 50% 50%, rgba(58, 155, 220, 0.32), rgba(58, 155, 220, 0) 68%);
  animation: tf-drift-b 26s ease-in-out infinite alternate;
}

.orb--3 {
  top: 8%;
  right: 12%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle at 50% 50%, rgba(126, 205, 250, 0.45), rgba(126, 205, 250, 0) 70%);
  animation: tf-drift-a 18s ease-in-out infinite alternate-reverse;
}

/* 装饰轨道 */
.orbit {
  position: absolute;
  width: min(880px, 118vw);
  height: min(880px, 118vw);
  border-radius: 50%;
  pointer-events: none;
  opacity: 0.55;
}

.orbit__ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.72);
}

.orbit__ring--2 {
  inset: 9%;
  border-color: rgba(255, 255, 255, 0.5);
  border-style: dashed;
  animation: tf-spin 68s linear infinite;
}

.orbit__ring--3 {
  inset: 21%;
  border-color: rgba(58, 155, 220, 0.18);
}

.orbit__dot {
  position: absolute;
  top: 11.5%;
  left: 50%;
  width: 9px;
  height: 9px;
  margin-left: -4.5px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 0 6px rgba(255, 255, 255, 0.28), 0 0 22px rgba(255, 255, 255, 0.9);
  animation: tf-float 7s ease-in-out infinite;
}

/* 登录容器 */
.login-shell {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
  max-width: 440px;
}

.login-card {
  position: relative;
  width: 100%;
  padding: 38px 38px 26px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.88);
  box-shadow:
    0 30px 80px rgba(19, 82, 120, 0.24),
    0 2px 0 rgba(255, 255, 255, 0.85) inset;
  -webkit-backdrop-filter: blur(24px) saturate(150%);
  backdrop-filter: blur(24px) saturate(150%);
  animation: tf-card-in 0.72s cubic-bezier(0.22, 0.61, 0.36, 1) both;
  --el-component-size-large: 46px;
}

.login-card__shine {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.85) 0%, rgba(255, 255, 255, 0) 42%);
  opacity: 0.55;
}

.login-head {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 26px;
}

.login-badge {
  margin-bottom: 18px;
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid rgba(58, 155, 220, 0.22);
  background: rgba(255, 255, 255, 0.6);
  font-family: var(--tf-font-display);
  font-size: 9.5px;
  font-weight: 700;
  letter-spacing: 0.22em;
  color: #2b7fb8;
}

.login-logo {
  animation: tf-float 6.5s ease-in-out infinite;
}

.login-title {
  margin: 16px 0 0;
  font-family: var(--tf-font-display);
  font-size: 23px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #0f3c59;
}

.login-subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  color: #4f7c96;
}

.login-form {
  position: relative;
  padding-top: 4px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 4px 14px;
  background: rgba(255, 255, 255, 0.78);
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(58, 155, 220, 0.18) inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(58, 155, 220, 0.36) inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 1px #3a9bdc inset, 0 8px 20px rgba(58, 155, 220, 0.16);
}

.login-form :deep(.el-input__prefix) {
  color: #5d90b2;
  font-size: 16px;
}

.login-form :deep(.el-input__inner) {
  font-size: 14px;
  color: #103b57;
}

.login-submit-item {
  margin-top: 6px;
  margin-bottom: 16px;
}

.login-submit {
  position: relative;
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 15px;
  letter-spacing: 0.18em;
  font-weight: 700;
  overflow: hidden;
  background-image: linear-gradient(120deg, #4fb3ec 0%, #3a9bdc 45%, #2277b4 100%);
  box-shadow: 0 14px 28px rgba(35, 124, 180, 0.34);
}

.login-submit::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  width: 55%;
  left: -60%;
  background: linear-gradient(100deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.42) 50%, rgba(255, 255, 255, 0) 100%);
  transform: skewX(-14deg);
  transition: left 0.65s ease;
}

.login-submit:hover::after {
  left: 110%;
}

.login-submit__label {
  position: relative;
  z-index: 1;
}

.login-submit__arrow {
  position: relative;
  z-index: 1;
  margin-left: 10px;
  letter-spacing: 0;
  font-family: var(--tf-font-display);
}

.demo-hint {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.48);
  border: 1px dashed rgba(58, 155, 220, 0.28);
}

.demo-hint__text {
  font-size: 12px;
  color: #4f7c96;
}

.demo-hint__text b {
  font-family: var(--tf-font-mono);
  font-size: 12px;
  color: #1e6fa8;
  letter-spacing: 0.02em;
}

.demo-hint__fill {
  flex-shrink: 0;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  font-family: inherit;
  font-size: 12px;
  font-weight: 700;
  color: #2b8cc9;
  text-decoration: underline;
  text-decoration-style: dotted;
  text-underline-offset: 3px;
  transition: color 0.2s ease;
}

.demo-hint__fill:hover {
  color: #1e6fa8;
}

.login-foot {
  margin: 0;
  font-size: 11.5px;
  letter-spacing: 0.06em;
  color: rgba(38, 93, 125, 0.66);
}

/* 动画 */
@keyframes tf-card-in {
  from {
    opacity: 0;
    transform: translateY(26px) scale(0.985);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes tf-drift-a {
  from {
    transform: translate3d(0, 0, 0) scale(1);
  }
  to {
    transform: translate3d(40px, 28px, 0) scale(1.08);
  }
}

@keyframes tf-drift-b {
  from {
    transform: translate3d(0, 0, 0) scale(1);
  }
  to {
    transform: translate3d(-46px, -30px, 0) scale(1.06);
  }
}

@keyframes tf-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 520px) {
  .login-card {
    padding: 30px 22px 22px;
    border-radius: 22px;
  }

  .login-title {
    font-size: 20px;
  }

  .orbit {
    opacity: 0.32;
  }
}
</style>
