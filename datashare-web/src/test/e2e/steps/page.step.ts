import { Given, Then } from "@badeball/cypress-cucumber-preprocessor";

const shouldSeePage = (pageName: string) => {
  cy.url().should("include", `/${pageName}`);
};

/** Open a page */
Given("I open the {word} page", (pageName: string) => {
  cy.visit(`/${pageName}`);
});

/** Check mat-card-title content */
Then("I should see the title {string}", (title: string) => {
  cy.get("mat-card-title").contains(title).should("be.visible");
});

/** Check if user is on the expected page */
Then("I should stay at {word} page", shouldSeePage);
Then("I should see the {word} page", shouldSeePage);
