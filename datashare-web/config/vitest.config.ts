import { defineConfig } from 'vitest/config';
import { resolve } from 'path';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
    plugins: [angular()],
    test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: ['config/vitest.setup.ts'],
        include: ['src/test/unit/**/*.ts', 'src/test/integration/**/*.ts'],
        exclude: ['node_modules', 'dist', 'cypress', 'src/test/e2e/**'],
        coverage: {
            provider: 'v8',
            reporter: ['text', 'json', 'html', 'lcov'],
            reportsDirectory: './coverage',
            include: ['src/app/**/*.ts'],
            exclude: [
                'src/app/**/*.spec.ts',
                'src/app/**/*.test.ts',
                'src/test/**',
                '**/*.d.ts',
                '**/*.config.ts',
                '**/index.ts',
                '**/*.module.ts'
            ],
            thresholds: {
                lines: 80,
                functions: 80,
                branches: 80,
                statements: 80
            }
        },
        reporters: ['verbose'],
        pool: 'threads',
    },
    resolve: {
        alias: {
            '@app': resolve(__dirname, '../src/app'),
            '@test': resolve(__dirname, '../src/test'),
            '@config': resolve(__dirname, '../config')
        }
    }
});
