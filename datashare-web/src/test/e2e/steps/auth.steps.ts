import { Given, When, Then } from '@badeball/cypress-cucumber-preprocessor';

const API_URL = Cypress.env('API_URL');

Given('I open the register page', () => {
  cy.visit('/register');
});

When('I submit valid register data', () => {
  cy.intercept('POST', `${API_URL}/register`).as('register');

  cy.get('input[name=email]').type('e2e@test.com');
  cy.get('input[name=password]').type('password');
  cy.get('button[type=submit]').click();

  cy.wait('@register');
});

Then('I should see a success message', () => {
  cy.contains('Registration successful');
});

Given('I open the login page', () => {
  cy.visit('/login');
});

When('I submit valid credentials', () => {
  cy.intercept('POST', `${API_URL}/login`).as('login');

  cy.get('input[name=email]').type('e2e@test.com');
  cy.get('input[name=password]').type('password');
  cy.get('button[type=submit]').click();

  cy.wait('@login');
});

Then('I should be logged in', () => {
  cy.url().should('include', '/dashboard');
});

When('I logout', () => {
  cy.intercept('POST', `${API_URL}/logout`).as('logout');
  cy.get('[data-testid=logout]').click();
  cy.wait('@logout');
});

Then('I should be redirected to login', () => {
  cy.url().should('include', '/login');
});
