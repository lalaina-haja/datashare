@upload
Feature: File Upload

  Background:
    Given I am logged in
    And I open the files/upload page

  Scenario: Successful PDF file upload
    When I select the file "sample.pdf"
    Then I should see the file name "sample.pdf" in the preview
    When I click on the televerser button
    Then the presigned upload request is successful
    And the s3 file upload request is successful
    And I should see the copy link button

  Scenario: No file selected
    Then the televerser button is not activated

  Scenario Outline: Different file types
    When I select the file "<name>"
    Then I should see the file name "<name>" in the preview
    And  I should see the file icon <icon> in the preview

    Examples:
      | name         | icon              |
      | sample.pdf   | picture_as_pdf    |
      | sample.jpg   | image             |
      | sample.mp4   | movie             |
      | sample.mp3   | audiotrack        |
      | sample.json  | insert_drive_file |