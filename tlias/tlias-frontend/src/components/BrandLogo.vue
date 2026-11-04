<script setup>
const props = defineProps({
  size: { type: Number, default: 44 },
  glow: { type: Boolean, default: false }
})

// 每个实例使用独立的渐变 id，避免多处复用时冲突
const uid = Math.random().toString(36).slice(2, 9)
const gradientId = `tf-logo-g-${uid}`
</script>

<template>
  <div
    class="brand-logo"
    :class="{ 'brand-logo--glow': props.glow }"
    :style="{ width: `${props.size}px`, height: `${props.size}px` }"
  >
    <svg viewBox="0 0 48 48" role="img" aria-label="Tlias">
      <defs>
        <linearGradient :id="gradientId" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#5cb8ef" />
          <stop offset="55%" stop-color="#3a9bdc" />
          <stop offset="100%" stop-color="#1a669c" />
        </linearGradient>
      </defs>
      <rect x="0" y="0" width="48" height="48" rx="13" :fill="`url(#${gradientId})`" />
      <ellipse cx="15" cy="12" rx="12" ry="8" fill="#ffffff" opacity="0.14" />
      <rect
        x="0.75"
        y="0.75"
        width="46.5"
        height="46.5"
        rx="12.4"
        fill="none"
        stroke="#ffffff"
        stroke-opacity="0.32"
        stroke-width="1.1"
      />
      <!-- 学士帽 -->
      <path d="M24 11 L37.6 17.1 L24 23.2 L10.4 17.1 Z" fill="#ffffff" opacity="0.97" />
      <path
        d="M17.4 20.5 V26 C17.4 28.5 20.3 30.5 24 30.5 C27.7 30.5 30.6 28.5 30.6 26 V20.5"
        fill="none"
        stroke="#ffffff"
        stroke-width="2.5"
        stroke-linecap="round"
        opacity="0.92"
      />
      <!-- 帽穗 -->
      <path
        d="M37.6 17.1 V21.6"
        fill="none"
        stroke="#bfe8ff"
        stroke-width="1.5"
        stroke-linecap="round"
        opacity="0.9"
      />
      <circle cx="37.6" cy="22.6" r="1.7" fill="#bfe8ff" />
      <!-- 底部书本线 -->
      <rect x="17.6" y="34.2" width="12.8" height="2.4" rx="1.2" fill="#ffffff" opacity="0.55" />
    </svg>
  </div>
</template>

<style scoped>
.brand-logo {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 30%;
}

.brand-logo svg {
  display: block;
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 6px 14px rgba(11, 59, 90, 0.28));
}

.brand-logo--glow svg {
  filter: drop-shadow(0 10px 26px rgba(58, 155, 220, 0.5));
}
</style>
