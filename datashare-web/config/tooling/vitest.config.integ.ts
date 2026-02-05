// config/tooling/vitest.config.integ.ts
/// <reference types="vitest" />

import { defineConfig } from "vitest/config";
import angular from "@analogjs/vite-plugin-angular";

export default defineConfig({
  plugins: [angular()],
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: ["config/tooling/vitest.setup.ts"],
    include: ["**/test/integ/**/*.spec.ts"],
    coverage: {
      provider: "v8",
      reporter: ["text", "html"],
      reportsDirectory: "../../coverage/integ",
      include: ["src/app/**/*.ts"],
      exclude: [
        "src/app/**/*.spec.ts",
        "src/app/**/*.test.ts",
        "src/test/**",
        "**/*.d.ts",
        "**/*.config.ts",
        "**/index.ts",
        "**/*.module.ts",
      ],
    },
  },
});
