<script setup lang="ts">
withDefaults(defineProps<{
  description?: string
  image?: string
}>(), {
  description: 'No data available',
})
</script>

<template>
  <div class="empty-state">
    <el-empty :description="description" :image="image">
      <template #default>
        <slot />
      </template>
    </el-empty>
  </div>
</template>

<style lang="scss" scoped>
.empty-state {
  padding: 60px 20px;
  display: flex;
  justify-content: center;
}
</style>
