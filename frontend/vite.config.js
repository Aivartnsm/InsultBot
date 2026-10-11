import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // Suunab /api päringud Spring Booti backendile - nii ei teki CORS probleemi
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
