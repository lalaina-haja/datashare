/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      /** Reset the database */
      resetDatabase(): Chainable<void>;

      /**
       * Get the angular environment configuration from window
       * @example cy.getAppConfig().then(env => cy.type(env.testUserEmail))
       */
      getAppConfig(): Chainable<any>;

      /**
       * Strictly typed version of getAppConfig, returning the environment configuration
       */
      getAngularEnv(): Chainable<any>;
    }
  }
}

export {};
