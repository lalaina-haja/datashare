import { Given, When, Then } from "@badeball/cypress-cucumber-preprocessor";

/** Mock API GET files */
Given("I have uploaded files", () => {
  cy.intercept("GET", "**/files/my*", {
    statusCode: 200,
    body: [
      {
        filename: "document.pdf",
        contentType: "application/pdf",
        size: 1250000,
        downloadToken: "ABCDEF",
        createdAt: new Date(Date.now()).toISOString(),
        expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
      },
      {
        filename: "photo.jpg",
        contentType: "image/jpg",
        size: 245000,
        downloadToken: "BCDEFG",
        createdAt: new Date(Date.now() - 4 * 86400000).toISOString(),
        expiresAt: new Date(Date.now() + 3 * 86400000).toISOString(),
      },
      {
        filename: "old-video.mp4",
        contentType: "video/mp4",
        size: 12500000,
        downloadToken: "CDEFGH",
        createdAt: new Date(Date.now() - 8 * 86400000).toISOString(),
        expiresAt: new Date(Date.now() - 86400000).toISOString(),
      },
    ],
  }).as("getFiles");
});

/** Select list filter */
When("I select filter {string}", (filter: string) => {
  cy.get("mat-button-toggle").contains(filter).click();
  cy.get("mat-list").should("be.visible");
});

/** Check the files list is display and contains at least 1 element */
Then("I should see the files list", () => {
  cy.wait("@getFiles");
  cy.get("mat-list").should("be.visible");
  cy.get("mat-list-item.file-item").should("have.length.gte", 1);
});

/** Check the selected list filter */
Then("the selected filter is {string}", (filter: string) => {
  cy.get("mat-button-toggle")
    .contains(filter)
    .should("have.attr", "aria-checked", "true");
});

/** Check the pagination list is visible */
Then("the list pagination is visible", () => {
  cy.get("mat-paginator").should("be.visible");
});

/** Check only active files are displayed */
Then("only active files are displayed", () => {
  cy.get("mat-list-item.file-item").each(($item) => {
    cy.wrap($item)
      .find("mat-icon")
      .filter(':contains("delete")')
      .should("be.visible");
    cy.wrap($item).find("button").contains("Accéder").should("be.visible");
    cy.wrap($item).contains("Ce fichier a expiré").should("not.exist");
  });
});

/** Check only expired files are displayed */
Then("only expired files are displayed", () => {
  cy.get("mat-list-item.file-item").each(($item) => {
    cy.wrap($item).contains("Ce fichier a expiré").should("be.visible");
    cy.wrap($item).find("button.btn-action").should("not.exist");
    cy.wrap($item).find("mat-icon").contains("delete").should("not.exist");
  });
});

/** Check all files are displayed */
Then("all files are displayed", () => {
  cy.get("mat-list-item.file-item").should("have.length", 3);
});
