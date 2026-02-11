@files @list
Feature: User's files

  Background:
    Given I am logged in
    And I have uploaded files
    And I open the files page

  Scenario: Default list view
    Then I should see the title "Mes fichiers"
    And I should see the files list
    And the selected filter is "Actifs"
    And only active files are displayed
    And the list pagination is visible

  Scenario: Filter on all files
    When I select filter "Tous"
    Then all files are displayed

  Scenario: Filter on expired files
    When I select filter "Expirés"
    Then only expired files are displayed

