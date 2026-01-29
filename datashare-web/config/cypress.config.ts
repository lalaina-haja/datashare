import { defineConfig } from 'cypress';
import createBundler from '@bahmutov/cypress-esbuild-preprocessor';
import { addCucumberPreprocessorPlugin } from '@badeball/cypress-cucumber-preprocessor';
import { env } from './dotenv.config';

export default defineConfig({
  e2e: {
    baseUrl: `http://localhost:${env['WEB_PORT']}`,
    env: {
      API_URL: `http://localhost:${env['API_PORT']}`,
    },
    setupNodeEvents(on, config) {

      // default bundler
      const bundler = createBundler({});
      
      // attach bundler to Cypress
      on('file:preprocessor', bundler);

      // add the Cucumber plugin
      return addCucumberPreprocessorPlugin(on, config);
    },
    specPattern: 'src/test/e2e/features/**/*.feature',
    supportFile: false,
  },
});
