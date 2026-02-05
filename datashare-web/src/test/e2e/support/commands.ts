/// <reference types="cypress" />

Cypress.Commands.add("getAppConfig", () => {
  return cy.window().its("environment");
});

Cypress.Commands.add("getAngularEnv", () => {
  return cy
    .window()
    .its("environment")
    .should("exist")
    .should("have.property", "apiUrl");
});
