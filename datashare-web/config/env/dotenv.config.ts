import * as dotenv from "dotenv";
import * as path from "path";

const ENV_PATH = path.resolve(__dirname, "../../.env");

dotenv.config({ path: ENV_PATH });

export const env = {
  APP_NAME: process.env["APP_NAME"] || "DataShare",
  APP_HOST: process.env["APP_HOST"] || "http://localhost",
  WEB_PORT: process.env["WEB_PORT"] || "4200",
  API_PORT: process.env["API_PORT"] || "8080",
  BASE_URL: `${process.env["APP_HOST"] || "http://localhost"}:${process.env["WEB_PORT"] || "4200"}`,
  API_URL: `${process.env["APP_HOST"] || "http://localhost"}:${process.env["API_PORT"] || "8080"}`,
  DB_HOST: process.env["DB_HOST"] || "localhost",
  DB_PORT: process.env["DB_PORT"] || "5432",
  DB_NAME: process.env["DB_NAME"] || "datashare_db",
  DB_USER: process.env["DB_USER"] || "datashare_usr",
  DB_PASSWORD: process.env["DB_PASSWORD"] || "datashare_pwd",
  TEST_EMAIL: process.env["TEST_EMAIL"] || "test@example.com",
  TEST_PASS: process.env["TEST_PASS"] || "Passw0rd!",
};
