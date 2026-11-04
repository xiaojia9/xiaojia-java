<script setup>
</script>

<template>
  <router-view v-slot="{ Component }">
    <transition name="tf-fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>
