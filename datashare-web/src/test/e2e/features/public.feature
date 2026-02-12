@upload @download @public
Feature: Public Upload and Download
    As an anonymous user 
    I want to upload and download files

  Background:
    Given I open the home page

  Scenario: Successful file upload and download
    Given no user is logged in

    When I click on upload button
    Then I should see the files/upload page

    When I select the file "sample.pdf"
    Then I should see the file name "sample.pdf" in the preview

    When I click on the televerser button
    Then the presigned upload request is successful
    And the s3 file upload request is successful
    And I should see the copy link button

    When I click on the download link
    Then I should see the file "sample.pdf" info
    When I click on the Telecharger button
    Then the file is opened in a new tab 
