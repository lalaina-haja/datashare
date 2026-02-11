import { Given, Then } from "@badeball/cypress-cucumber-preprocessor";

/** Open a page */
Given("I open the {word} page", (pageName: string) => {
  cy.visit(`/${pageName}`);
});

/** Check mat-card-title content */
Then("I should see the title {string}", (title: string) => {
  cy.get("mat-card-title").contains(title).should("be.visible");
});
