@download
Feature: File Download

  Background:
    Given I am logged in
    And I upload the file "sample.pdf"
    And I save the download link

  Scenario: Authenticated user Download
    When I open the download link
    Then I should see the file "sample.pdf" info
    When I click on the Telecharger button
    Then the file is opened in a new tab

  Scenario: Anonymous user Download
    Given I am logged out
    When I open the download link
    Then I should see the file "sample.pdf" info
    When I click on the Telecharger button
    Then the file is opened in a new tab