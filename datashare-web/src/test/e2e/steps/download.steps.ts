import { Then, When } from "@badeball/cypress-cucumber-preprocessor";

/** Check the file info is displayed */
Then("I should see the file {string} info", (filename: string) => {
  cy.contains(filename).should("be.visible");
  cy.intercept("GET", `**/**${filename}`).as("download");
});

/** Click download button */
When("I click on the Telecharger button", () => {
  cy.window().then((win) => {
    cy.stub(win, "open").as("windowOpen");
  });
  cy.get('[data-testid="btn-telecharger"]').click();
});

Then("the file is opened in a new tab", () => {
  cy.get("@windowOpen").should("have.been.called");
});
