Feature: Authentication

  Scenario: Register
    Given I open the register page
    When I submit valid register data
    Then I should see a success message

  Scenario: Login and Logout
    Given I open the login page
    When I submit valid credentials
    Then I should be logged in
    When I logout
    Then I should be redirected to login
