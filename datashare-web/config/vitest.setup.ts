import '@angular/compiler';
import 'zone.js';
import 'zone.js/testing';
import { getTestBed } from '@angular/core/testing';
import { beforeAll, afterAll, afterEach } from 'vitest';
import { BrowserTestingModule, platformBrowserTesting } from '@angular/platform-browser/testing';
import { config } from 'dotenv';
import { resolve } from 'path';

getTestBed().initTestEnvironment(
  BrowserTestingModule,
  platformBrowserTesting()
);

config({
  path: resolve(__dirname, '../../.env'),
});

process.env['API_URL'] = `http://localhost:${process.env['API_PORT']}`;
process.env['WEB_URL'] = `http://localhost:${process.env['WEB_PORT']}`;

// Init Angular test environment
declare global {
  // eslint-disable-next-line no-var
  var __ANGULAR_TEST_ENV_INITIALIZED__: boolean | undefined;
}

if (!globalThis.__ANGULAR_TEST_ENV_INITIALIZED__) {
  getTestBed().initTestEnvironment(
    BrowserTestingModule,
    platformBrowserTesting(),
  );

  globalThis.__ANGULAR_TEST_ENV_INITIALIZED__ = true;
}

// Configuration globale pour jsdom
beforeAll(() => {
  // Setup global avant tous les tests
  if (typeof window !== 'undefined') {
    // Configuration du window si nécessaire
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {}, // Deprecated
        removeListener: () => {}, // Deprecated
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => false,
      }),
    });

    // Mock pour localStorage
    Object.defineProperty(window, 'localStorage', {
      value: (() => {
        let store: Record<string, string> = {};
        return {
          getItem: (key: string) => store[key] || null,
          setItem: (key: string, value: string) => {
            store[key] = value.toString();
          },
          removeItem: (key: string) => {
            delete store[key];
          },
          clear: () => {
            store = {};
          },
        };
      })(),
      writable: true,
    });

    // Mock pour sessionStorage
    Object.defineProperty(window, 'sessionStorage', {
      value: (() => {
        let store: Record<string, string> = {};
        return {
          getItem: (key: string) => store[key] || null,
          setItem: (key: string, value: string) => {
            store[key] = value.toString();
          },
          removeItem: (key: string) => {
            delete store[key];
          },
          clear: () => {
            store = {};
          },
        };
      })(),
      writable: true,
    });

    // Mock pour getComputedStyle
    if (!window.getComputedStyle) {
      Object.defineProperty(window, 'getComputedStyle', {
        value: () => ({
          getPropertyValue: () => '',
        }),
      });
    }

    // Mock pour CSS
    if (!window.CSS) {
      Object.defineProperty(window, 'CSS', {
        value: {
          supports: () => false,
        },
      });
    }
  }
});

afterEach(() => {
  // Nettoyage après chaque test
  if (typeof window !== 'undefined') {
    window.localStorage.clear();
    window.sessionStorage.clear();
  }
});

afterAll(() => {
  // Nettoyage après tous les tests
});
