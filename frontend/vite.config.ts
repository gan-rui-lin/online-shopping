import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  build: {
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }

          if (id.includes('@element-plus/icons-vue')) {
            return 'vendor-element-icons'
          }
          if (id.includes('element-plus/es/') || id.includes('element-plus/lib/')) {
            return 'vendor-element-plus'
          }
          if (id.includes('vue-router')) {
            return 'vendor-vue-router'
          }
          if (id.includes('pinia')) {
            return 'vendor-pinia'
          }
          if (id.includes('vue-i18n')) {
            return 'vendor-i18n'
          }
          if (id.includes('axios')) {
            return 'vendor-axios'
          }
          if (id.includes('dayjs')) {
            return 'vendor-dayjs'
          }
          if (id.includes('/vue/')) {
            return 'vendor-vue-core'
          }

          return 'vendor-misc'
        },
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['legacy-js-api'],
        quietDeps: true,
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
