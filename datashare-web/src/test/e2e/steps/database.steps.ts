import { Given } from "@badeball/cypress-cucumber-preprocessor";
import bcrypt from "bcryptjs";

/**
 * Check the existing user in the database with correct password
 */
Given(
  "the existing user {string} with password {string}",
  (email: string, password: string) => {
    // Check user existence in the database
    cy.task<{ rows: any[] }>("connectDB", {
      query: "SELECT * FROM users WHERE email = $1",
      params: [email],
    }).then((result) => {
      expect(result.rows).to.have.length(1, `User ${email} not found`);
    });
    // Verify password
    cy.task<{ rows: any[] }>("connectDB", {
      query: "SELECT password FROM users WHERE email = $1",
      params: [email],
    }).then(async (result) => {
      const hashedPassword = result.rows[0].password;
      const isValid = await bcrypt.compare(password, hashedPassword);
      expect(isValid).to.be.true;
    });
  },
);

/**
 * Check the inexisting user in the database
 */
Given("the inexisting user {string}", (email: string) => {
  cy.task<{ rows: any[] }>("connectDB", {
    query: "SELECT password FROM users WHERE email = $1",
    params: [email],
  }).then((result) => {
    expect(result.rows).to.have.length(
      0,
      `User ${email} exists but should not`,
    );
  });
});
