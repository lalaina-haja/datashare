import { Given, When, Then } from "@badeball/cypress-cucumber-preprocessor";

/** Log in the test user */
Given("I am logged in", () => {
  cy.visit(`/login`);
  cy.getAppConfig().then((env) => {
    cy.intercept("POST", `${env.apiUrl}/auth/login`).as("login");

    cy.get("input[formControlName=email]").type(env.testEmail);
    cy.get("input[formControlName=password]").type(env.testPass);
    cy.get("button[type=submit]").click();

    cy.wait("@login");

    cy.contains("button", `${env.testEmail}`).should("exist").and("be.visible");
  });
});

/** Log out the test user */
Given("I am logged out", () => {
  cy.get('[data-testid="user-button"]').click();
  cy.get('[data-testid="connection-button"]').click();
  cy.contains("button", "Se connecter").should("exist").and("be.visible");
});

/** Submit registration with email and password */
When(
  "I register {string} with password {string}",
  (email: string, password: string) => {
    cy.getAppConfig().then((env) => {
      cy.intercept("POST", `${env.apiUrl}/auth/register`).as("register");

      cy.get("input[formControlName=email]").type(email);
      cy.get("input[formControlName=password]").type(password);
      cy.get("input[formControlName=confirmPassword]").type(password);
      cy.get("button[type=submit]").click();

      cy.wait("@register");
    });
  },
);

/** Submit login with email and password */
When(
  "I login {string} with password {string}",
  (email: string, password: string) => {
    cy.getAppConfig().then((env) => {
      cy.intercept("POST", `${env.apiUrl}/auth/login`).as("login");

      cy.get("input[formControlName=email]").type(email);
      cy.get("input[formControlName=password]").type(password);
      cy.get("button[type=submit]").click();

      cy.wait("@login");
    });
  },
);

/** Logout */
When("I log out", () => {
  cy.get('[data-testid="user-button"]').click();
  cy.get('[data-testid="connection-button"]').click();
});

/** Logged in = Connexion button shows "Se deconnecter" */
Then("I should be logged in", () => {
  cy.getAppConfig().then((env) => {
    cy.contains("button", `${env.testEmail}`).should("exist").and("be.visible");
  });
});

/** Logged out = Connexion button shows "Se connecter" */
Then("I should be logged out", () => {
  cy.contains("button", "Se connecter").should("exist").and("be.visible");
});

/** Check if user is redirected to login page */
Then("I should be redirected to login", () => {
  cy.url().should("include", "/login");
});

/** Check error message */
Then("I should see the error message {string}", (message: string) => {
  cy.get(".error-message").should("contain", message);
});

/** Check if user is on the expected page */
Then("I should stay at {word} page", (pageName: string) => {
  cy.url().should("include", `/${pageName}`);
});
