import { defineConfig, mergeConfig } from 'vitest/config';
import baseConfig from './vitest.config';

export default mergeConfig(
  baseConfig,
  defineConfig({
    test: {
      name: 'unit',
      include: ['src/test/unit/**/*.{test,spec}.ts'],
      exclude: ['node_modules', 'dist', 'src/test/integration/**', 'src/test/e2e/**'],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html'],
        reportsDirectory: './coverage/unit',
        include: ['src/app/**/*.ts'],
        exclude: [
          'src/app/**/*.spec.ts',
          'src/app/**/*.test.ts',
          'src/test/**',
          '**/*.d.ts',
          '**/*.config.ts',
          '**/index.ts',
          '**/*.module.ts'
        ]
      }
    }
  })
);
