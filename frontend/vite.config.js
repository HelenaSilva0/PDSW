import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
export default defineConfig({
    plugins: [react()],
    base: '/',
    server: {
        port: 5173,
        proxy: {
            '/auth': 'http://localhost:8080',
            '/pacientes': 'http://localhost:8080',
            '/medicos': 'http://localhost:8080',
            '/profiles': 'http://localhost:8080',
            '/historico-familiar': 'http://localhost:8080',
            '/exames': 'http://localhost:8080',
            '/chats': 'http://localhost:8080'
        }
    },
    build: { outDir: '../src/main/resources/static', emptyOutDir: false, assetsDir: 'assets' },
    test: { environment: 'jsdom', setupFiles: './src/test/setup.ts' }
});
