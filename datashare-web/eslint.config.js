import { resolve } from 'path';
import tseslint from 'typescript-eslint';
import angular from 'angular-eslint';
import templateParser from '@angular-eslint/template-parser';
import templatePlugin from '@angular-eslint/eslint-plugin-template';
import prettierConfig from 'eslint-config-prettier';
import eslint from '@eslint/js';

/**
 * ESLint Flat Config for Angular + TS + Vitest + Cypress + Prettier
 */
export default [
  // ===============================
  // Ignore build, node_modules and types
  // ===============================
  {
    ignores: [
      'dist/',
      'node_modules/',
      'coverage/',
      '.angular/',
      'out-tsc/',
      '**/*.d.ts',
      '*.js',
      '!eslint.config.js',
    ],
  },

  // ====================================
  // JavaScript & TypeScript Rules
  // ====================================
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  ...tseslint.configs.stylistic,

  // ===============================
  // Angular TS (component/directive selectors)
  // ===============================
  ...angular.configs.tsRecommended,

  // ===============================
  // Application TS
  // ===============================
  {
    files: ['src/**/*.ts', '!src/**/*.html', '!src/test/**/*.ts'],
    languageOptions: {
      parser: tseslint.parser,
      parserOptions: {
        project: resolve('./tsconfig.app.json'),
        tsconfigRootDir: process.cwd(),
        ecmaVersion: 2022,
        sourceType: 'module',
      },
    },
    processor: angular.processInlineTemplates,
    rules: {
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
      '@typescript-eslint/ban-ts-comment': 'error',
      '@typescript-eslint/no-explicit-any': 'warn',
    },
  },

  // ===============================
  // Unit / Integration tests
  // ===============================
  {
    files: ['src/test/**/*.ts', 'src/test/unit/**/*.ts', 'src/test/integration/**/*.ts'],
    languageOptions: {
      parser: tseslint.parser,
      parserOptions: {
        project: resolve('./tsconfig.spec.json'),
        tsconfigRootDir: process.cwd(),
      },
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/ban-ts-comment': 'off',
    },
  },

  // ====================================
  // E2E tests (Cypress)
  // ====================================
  {
    files: ['src/test/e2e/**/*.ts'],
    languageOptions: {
      parser: tseslint.parser,
      parserOptions: {
        project: resolve('./tsconfig.e2e.json'),
        tsconfigRootDir: process.cwd(),
      },
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/ban-ts-comment': 'off',
    },
  },

  // ====================================
  // Config files
  // ====================================
  {
    files: ['config/**/*.ts'],
    languageOptions: {
      parser: tseslint.parser,
      parserOptions: {
        project: resolve('./tsconfig.app.json'),
        tsconfigRootDir: process.cwd(),
      },
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
    },
  },

  // ===============================
  // Prettier
  // ===============================
  prettierConfig,
];
