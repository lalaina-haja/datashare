import type { Config } from '@jest/types';

const config: Config.InitialOptions = {
  preset: 'ts-jest',
  //testEnvironment: 'jsdom', // Browser-like for Angular
  moduleFileExtensions: ['ts', 'js', 'html', 'json'],

  testMatch: ['**/*.spec.ts'], // Unit test files

  //setupFilesAfterEnv: ['<rootDir>/src/test-setup.ts'], // Angular test setup

  collectCoverage: true,
  coverageDirectory: '<rootDir>/coverage/unit',
  coverageReporters: ['text', 'lcov'],

  globals: {
    'ts-jest': {
      tsconfig: 'tsconfig.spec.json'
    }
  },

  verbose: true
};

export default config;
