/// <reference types="cypress" />
import "./commands";

// Hooks
before(() => {
  cy.log("🚀 Cypress E2E started");
});

beforeEach(() => {
  // Reset database before each test
  cy.task("resetDatabase").then((result) => {
    cy.log(typeof result === "string" ? result : JSON.stringify(result));
  });
});

afterEach(() => {
  // TODO
});

after(() => {
  cy.log("✅ Cypress E2E finished");
});
