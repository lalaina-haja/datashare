import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200', // Angular dev server
    supportFile: '../e2e-cypress/support/e2e.ts',
    specPattern: '../e2e-cypress/**/*.spec.ts',
    setupNodeEvents(on, config) {
      // implement node event listeners here if needed
      return config;
    }
  },
  env: {
    apiUrl: 'http://localhost:8080', // API endpoint
  },
  video: false
});
