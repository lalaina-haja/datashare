import { defineConfig } from "cypress";
import createBundler from "@bahmutov/cypress-esbuild-preprocessor";
import { addCucumberPreprocessorPlugin } from "@badeball/cypress-cucumber-preprocessor";
import { createEsbuildPlugin } from "@badeball/cypress-cucumber-preprocessor/esbuild";
import { Client } from "pg";
import bcrypt from "bcryptjs";
import { env } from "../env/dotenv.config";

const TEST_EMAIL = env["TEST_EMAIL"];
const TEST_PASS = env["TEST_PASS"];
const config_db = {
  host: env["DB_HOST"],
  port: parseInt(env["DB_PORT"]),
  database: env["DB_NAME"],
  user: env["DB_USER"],
  password: env["DB_PASSWORD"],
};

export default defineConfig({
  e2e: {
    // Disable Cypress's own environment variable support
    //allowCypressEnv: true,
    // Base URL and environment variables
    baseUrl: env["BASE_URL"],
    env: {
      API_URL: env["API_URL"],
      TEST_EMAIL: TEST_EMAIL,
      TEST_PASS: TEST_PASS,
    },

    // Cucumber feature files location
    specPattern: "**/test/e2e/features/**/*.feature",
    supportFile: "src/test/e2e/support/e2e.ts",

    // Setup node events for Cucumber preprocessor
    async setupNodeEvents(on, config) {
      await addCucumberPreprocessorPlugin(on, config);

      // Task to connect to the database
      on("task", {
        async connectDB({
          query,
          params,
        }: {
          query: string;
          params?: any[];
        }): Promise<{ rows: any[] }> {
          const client = new Client(config_db);
          try {
            await client.connect();
            const res = await client.query(query, params);
            return { rows: res.rows };
          } finally {
            await client.end();
          }
        },
      });

      // Task to reset the database
      on("task", {
        async resetDatabase() {
          const client = new Client(config_db);

          await client.connect();

          // Clear existing data and insert default test user
          await client.query("TRUNCATE users CASCADE");
          const hashedPassword = await bcrypt.hash(TEST_PASS, 10);

          await client.query(
            `
            INSERT INTO users (email, password, created_at) 
            VALUES ($1, $2, NOW())
          `,
            [TEST_EMAIL, hashedPassword],
          );

          await client.end();
          return "Database reset complete";
        },
      });

      // Configure the bundler for Cucumber
      on(
        "file:preprocessor",
        createBundler({
          plugins: [createEsbuildPlugin(config)],
        }),
      );

      return config;
    },
  },
});
