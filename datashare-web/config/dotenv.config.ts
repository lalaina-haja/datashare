import * as dotenv from 'dotenv';
import * as path from 'path';

const ENV_PATH = path.resolve(__dirname, '../../.env');

dotenv.config({ path: ENV_PATH });

export const env = {
  WEB_PORT: process.env['WEB_PORT'] || '4200',
  API_PORT: process.env['API_PORT'] || '8080',
};