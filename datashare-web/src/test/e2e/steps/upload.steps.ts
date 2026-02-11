import { When, Then } from "@badeball/cypress-cucumber-preprocessor";

/** Select file from src/test/e2e/fixtures */
When("I select the file {string}", (filename: string) => {
  cy.get('[data-testid="btn-charger"]').click();
  cy.get('[data-testid="file-input"]').selectFile(
    `src/test/e2e/fixtures/${filename}`,
    { force: true },
  );
});

/** Click on Televerser button */
When("I click on the televerser button", () => {
  // prepare upload intercepts
  cy.getAppConfig().then((env) => {
    cy.intercept("POST", "/files/upload").as("presigned");
    cy.intercept("PUT", new RegExp(`${env.bucketName}/uploads`)).as("upload");
  });

  // click button
  cy.get('[data-testid="btn-televerser"]').should("not.be.disabled").click();
});

/** Upload a file */
When("I upload the file {string}", (filename: string) => {
  // open files/upload page
  cy.visit("files/upload");
  // select file
  cy.get('[data-testid="btn-charger"]').click();
  cy.get('[data-testid="file-input"]').selectFile(
    `src/test/e2e/fixtures/${filename}`,
    { force: true },
  );
  // intercept requests
  cy.getAppConfig().then((env) => {
    cy.intercept("POST", "/files/upload").as("presigned");
    cy.intercept("PUT", new RegExp(`${env.bucketName}/uploads`)).as("upload");
  });
  // click televerser button
  cy.get('[data-testid="btn-televerser"]').should("not.be.disabled").click();
  // wait requests to be ok
  cy.wait("@presigned").its("response.statusCode").should("eq", 200);
  cy.wait("@upload").its("response.statusCode").should("eq", 200);
});

/** Save the download link */
When("I save the download link", () => {
  cy.get("div.link a").then(($a) => {
    const downloadUrl = $a.attr("href")!;
    cy.wrap(downloadUrl).as("downloadLink");
  });
});

/** Open the download link */
When("I open the download link", () => {
  cy.get<string>("@downloadLink").then((url: string) => {
    cy.visit(url).as("download");
  });
});

/** Check the file icon in the preview */
Then("I should see the file icon {word} in the preview", (icon: string) => {
  cy.get('[data-testid="file-icon"]').should("be.visible").and("contain", icon);
});

/** Check file name in the preview */
Then(
  "I should see the file name {string} in the preview",
  (filename: string) => {
    cy.get('[data-testid="file-preview"]').should("be.visible");
    cy.get('[data-testid="file-preview"] .file-name')
      .should("be.visible")
      .and("contain", filename);
  },
);

/** Check the presigned upload request */
Then("the presigned upload request is successful", () => {
  cy.wait("@presigned").its("response.statusCode").should("eq", 200);
  cy.get("@presigned")
    .its("response.body")
    .should("have.property", "tokenString")
    .and("be.a", "string")
    .and("not.be.empty");
});

/** Check the s3 file upload request */
Then("the s3 file upload request is successful", () => {
  cy.wait("@upload").its("response.statusCode").should("eq", 200);
});

/** Check the Copy link button is displayed */
Then("I should see the copy link button", () => {
  cy.get('[data-testid="btn-copier"]')
    .should("be.visible")
    .should("not.be.disabled");
});

/** Check the televerser button is not activated */
Then("the televerser button is not activated", () => {
  cy.get('[data-testid="btn-televerser"]')
    .should("be.visible")
    .should("be.disabled");
});
