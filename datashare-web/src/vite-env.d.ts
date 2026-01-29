/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  readonly VITE_APP_NAME: string;
  readonly VITE_TIMEOUT: string;
  // Ajouter d'autres variables d'environnement ici
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
