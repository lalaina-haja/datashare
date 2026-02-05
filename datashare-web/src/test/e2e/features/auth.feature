Feature: Authentication

  Background:
    Given the existing user "test@example.com" with password "Passw0rd!"
    And the inexisting user "new@example.com"

  Scenario: Register success
    Given I open the register page
    When I register "new@example.com" with password "Passw0rd!"
    Then I should be redirected to login

  Scenario: Register failed
    Given I open the register page
    When I register "test@example.com" with password "Passw0rd!"
    Then I should stay at register page
    And I should see the error message "Email is already in use: test@example.com"

  Scenario: Login success
    Given I open the login page
    When I login "test@example.com" with password "Passw0rd!"
    Then I should be logged in

  Scenario: Login failed
    Given I open the login page
    When I login "test@example.com" with password "WrongPassw0rd!"
    Then I should stay at login page
    And I should see the error message "Invalid password"

  Scenario: Logout
    Given I am logged in
    When I logout
    Then I should be logged out
