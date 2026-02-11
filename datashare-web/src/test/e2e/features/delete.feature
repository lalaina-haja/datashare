@files @delete
Feature: Delete user's file
    As a user 
    I want to delete a file 
    So that it no longer appears in the

  Background:
    Given I am logged in
    And I have uploaded files
    And I open the files page

  Scenario: Delete a file successfully
    When I click on the delete button of the file "document.pdf"
    And I confirm the deletion
    Then I should see a dialog "Fichier supprimé avec succès"
    Then the file "sample.pdf" should not appear in the list

  Scenario: Cancel delete and file is not removed 
    When I click on the delete button of the file "document.pdf" 
    And I cancel the deletion 
    Then the file "document.pdf" should still appear in the list 
    And no alert dialog should be displayed
