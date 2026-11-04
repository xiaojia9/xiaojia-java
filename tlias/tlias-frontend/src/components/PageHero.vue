<script setup>
const props = defineProps({
  eyebrow: { type: String, default: 'Tlias 管理后台' },
  title: { type: String, required: true },
  desc: { type: String, default: '' }
})
</script>

<template>
  <div class="tf-hero">
    <div class="tf-hero__main">
      <span class="tf-hero__eyebrow">
        <i class="tf-hero__dot" aria-hidden="true"></i>
        {{ props.eyebrow }}
      </span>
      <h1 class="tf-hero__title">{{ props.title }}</h1>
      <p v-if="props.desc" class="tf-hero__desc">{{ props.desc }}</p>
    </div>
    <div class="tf-hero__actions">
      <slot name="actions" />
    </div>
    <div class="tf-hero__deco" aria-hidden="true">
      <span class="tf-hero__ring"></span>
      <span class="tf-hero__ring tf-hero__ring--2"></span>
      <span class="tf-hero__ring tf-hero__ring--3"></span>
    </div>
  </div>
</template>

<style scoped>
.tf-hero {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 6px 4px 2px;
  overflow: hidden;
}

.tf-hero__main {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.tf-hero__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-family: var(--tf-font-display);
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #3a9bdc;
}

.tf-hero__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3a9bdc;
  box-shadow: 0 0 0 4px rgba(58, 155, 220, 0.16);
}

.tf-hero__title {
  margin: 8px 0 0;
  font-family: var(--tf-font-display);
  font-size: 27px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.01em;
  color: #103b57;
}

.tf-hero__desc {
  margin: 7px 0 0;
  font-size: 13.5px;
  color: #5c8199;
}

.tf-hero__actions {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  padding-bottom: 4px;
}

.tf-hero__deco {
  position: absolute;
  right: 6%;
  top: -58px;
  width: 260px;
  height: 160px;
  pointer-events: none;
  opacity: 0.75;
}

.tf-hero__ring {
  position: absolute;
  right: 0;
  top: 10px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 1px solid rgba(58, 155, 220, 0.18);
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0) 70%);
}

.tf-hero__ring--2 {
  right: 56px;
  top: 44px;
  width: 92px;
  height: 92px;
  border-color: rgba(58, 155, 220, 0.14);
}

.tf-hero__ring--3 {
  right: 24px;
  top: 34px;
  width: 34px;
  height: 34px;
  border-color: rgba(58, 155, 220, 0.26);
  background: none;
}

@media (max-width: 900px) {
  .tf-hero__deco {
    display: none;
  }

  .tf-hero__title {
    font-size: 23px;
  }
}

@media (max-width: 720px) {
  .tf-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
