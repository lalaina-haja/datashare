import type { Config } from '@jest/types';

const config: Config.InitialOptions = {
  preset: 'ts-jest',
  testEnvironment: 'node', // Node.js for API/service integration
  moduleFileExtensions: ['ts', 'js', 'json'],

  testMatch: ['**/*.it.spec.ts'], // Integration test files

  //setupFilesAfterEnv: ['<rootDir>/src/test-setup-it.ts'], // Optional integration setup

  collectCoverage: false, // usually no coverage for integration

  globals: {
    'ts-jest': {
      tsconfig: 'tsconfig.integration.json'
    }
  },

  verbose: true
};

export default config;
