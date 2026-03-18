@Feature22
Feature: Delete email templates

  Scenario: Successful deletion of an existing email template
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    When the content creator navigates to the Email Templates section
    Then the Email Templates page should be displayed
    And the content creator deletes the email template named "Email Test"
    Then a success message should appear confirming the template deletion
