<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const rootRef = ref(null)
const canvasRef = ref(null)

let ctx = null
let frameId = 0
let running = false
let width = 0
let height = 0
let dpr = 1
let observer = null
let nodes = []
let pointerBound = false

const pointer = { x: -9999, y: -9999, active: false }

const canAnimate = typeof window !== 'undefined' && !window.matchMedia?.('(prefers-reduced-motion: reduce)').matches

const rand = (min, max) => Math.random() * (max - min) + min

function buildNodes() {
  const count = Math.max(28, Math.min(88, Math.round((width * height) / 18500)))
  nodes = Array.from({ length: count }, () => ({
    x: rand(0, width),
    y: rand(0, height),
    vx: rand(-0.24, 0.24),
    vy: rand(-0.24, 0.24),
    r: rand(1.3, 3.2),
    alpha: rand(0.32, 0.78),
    accent: Math.random() > 0.78,
    phase: rand(0, Math.PI * 2)
  }))
}

function resize() {
  const root = rootRef.value
  const canvas = canvasRef.value
  if (!root || !canvas) return

  const rect = root.getBoundingClientRect()
  width = Math.max(1, rect.width)
  height = Math.max(1, rect.height)
  dpr = Math.min(window.devicePixelRatio || 1, 2)

  canvas.width = Math.round(width * dpr)
  canvas.height = Math.round(height * dpr)
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`

  ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  if (!nodes.length) {
    buildNodes()
  } else {
    nodes.forEach((n) => {
      n.x = Math.min(Math.max(n.x, 0), width)
      n.y = Math.min(Math.max(n.y, 0), height)
    })
  }

  if (!canAnimate) draw(0)
}

function updateNodes() {
  const maxDist = 158

  for (const n of nodes) {
    if (pointer.active) {
      const dx = n.x - pointer.x
      const dy = n.y - pointer.y
      const dist = Math.hypot(dx, dy)
      if (dist < maxDist && dist > 0.001) {
        // 鼠标轻微排斥，保持柔和
        const force = (1 - dist / maxDist) * 0.9
        n.vx += (dx / dist) * force * 0.16
        n.vy += (dy / dist) * force * 0.16
      }
    }

    const speed = Math.hypot(n.vx, n.vy)
    if (speed > 1.05) {
      n.vx *= 0.9
      n.vy *= 0.9
    } else if (speed < 0.09) {
      n.vx += rand(-0.02, 0.02)
      n.vy += rand(-0.02, 0.02)
    }

    n.x += n.vx
    n.y += n.vy

    if (n.x < 0) {
      n.x = 0
      n.vx = Math.abs(n.vx)
    } else if (n.x > width) {
      n.x = width
      n.vx = -Math.abs(n.vx)
    }
    if (n.y < 0) {
      n.y = 0
      n.vy = Math.abs(n.vy)
    } else if (n.y > height) {
      n.y = height
      n.vy = -Math.abs(n.vy)
    }
  }
}

function draw(time) {
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)

  const linkDist = 134
  const maxLinks = linkDist * linkDist

  // 节点之间的连线
  for (let i = 0; i < nodes.length; i += 1) {
    const a = nodes[i]
    for (let j = i + 1; j < nodes.length; j += 1) {
      const b = nodes[j]
      const dx = a.x - b.x
      const dy = a.y - b.y
      const d2 = dx * dx + dy * dy
      if (d2 > maxLinks) continue
      const alpha = (1 - Math.sqrt(d2) / linkDist) * 0.2
      ctx.strokeStyle = `rgba(26, 102, 156, ${alpha.toFixed(3)})`
      ctx.lineWidth = 0.85
      ctx.beginPath()
      ctx.moveTo(a.x, a.y)
      ctx.lineTo(b.x, b.y)
      ctx.stroke()
    }
  }

  // 鼠标附近的线与光晕
  if (pointer.active) {
    const glowRadius = 170
    const gradient = ctx.createRadialGradient(pointer.x, pointer.y, 0, pointer.x, pointer.y, glowRadius)
    gradient.addColorStop(0, 'rgba(120, 200, 245, 0.16)')
    gradient.addColorStop(1, 'rgba(120, 200, 245, 0)')
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(pointer.x, pointer.y, glowRadius, 0, Math.PI * 2)
    ctx.fill()

    for (const n of nodes) {
      const dist = Math.hypot(n.x - pointer.x, n.y - pointer.y)
      if (dist > 170) continue
      const alpha = (1 - dist / 170) * 0.34
      ctx.strokeStyle = `rgba(58, 155, 220, ${alpha.toFixed(3)})`
      ctx.lineWidth = 0.9
      ctx.beginPath()
      ctx.moveTo(n.x, n.y)
      ctx.lineTo(pointer.x, pointer.y)
      ctx.stroke()
    }
  }

  // 节点
  for (const n of nodes) {
    const pulse = canAnimate ? Math.sin(time * 0.0011 + n.phase) * 0.34 : 0
    const radius = Math.max(0.7, n.r + pulse)

    if (n.accent) {
      ctx.beginPath()
      ctx.arc(n.x, n.y, radius * 3.4, 0, Math.PI * 2)
      ctx.fillStyle = 'rgba(120, 200, 245, 0.1)'
      ctx.fill()
    }

    ctx.beginPath()
    ctx.arc(n.x, n.y, radius, 0, Math.PI * 2)
    ctx.fillStyle = n.accent
      ? `rgba(24, 104, 158, ${Math.min(0.9, n.alpha + 0.14)})`
      : `rgba(58, 155, 220, ${n.alpha})`
    ctx.fill()
  }
}

function tick(time) {
  frameId = window.requestAnimationFrame(tick)
  updateNodes()
  draw(time)
}

function start() {
  if (!canAnimate || running || !ctx) return
  running = true
  frameId = window.requestAnimationFrame(tick)
}

function stop() {
  running = false
  window.cancelAnimationFrame(frameId)
}

function onPointerMove(event) {
  const canvas = canvasRef.value
  if (!canvas) return
  const rect = canvas.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  if (x >= 0 && y >= 0 && x <= rect.width && y <= rect.height) {
    pointer.x = x
    pointer.y = y
    pointer.active = true
  } else {
    pointer.active = false
  }
}

function onPointerLeave() {
  pointer.active = false
}

function onVisibilityChange() {
  if (document.hidden) stop()
  else start()
}

onMounted(() => {
  resize()
  start()

  if (typeof ResizeObserver !== 'undefined') {
    observer = new ResizeObserver(() => resize())
    if (rootRef.value) observer.observe(rootRef.value)
  } else {
    window.addEventListener('resize', resize)
  }

  window.addEventListener('pointermove', onPointerMove, { passive: true })
  window.addEventListener('pointerleave', onPointerLeave)
  document.addEventListener('visibilitychange', onVisibilityChange)
  pointerBound = true
})

onBeforeUnmount(() => {
  stop()
  if (observer) {
    observer.disconnect()
    observer = null
  } else {
    window.removeEventListener('resize', resize)
  }
  if (pointerBound) {
    window.removeEventListener('pointermove', onPointerMove)
    window.removeEventListener('pointerleave', onPointerLeave)
    document.removeEventListener('visibilitychange', onVisibilityChange)
  }
  nodes = []
})
</script>

<template>
  <div ref="rootRef" class="particle-bg" aria-hidden="true">
    <canvas ref="canvasRef" class="particle-bg__canvas"></canvas>
    <div class="particle-bg__vignette"></div>
  </div>
</template>

<style scoped>
.particle-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.particle-bg__canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.particle-bg__vignette {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(60% 50% at 50% 40%, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.35) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.15), rgba(207, 234, 255, 0.25));
  mix-blend-mode: soft-light;
}
</style>
