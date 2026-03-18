@Feature20
Feature: Edit email templates

  Scenario: Successful editing and saving of an existing email template
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    When the content creator navigates to the Email Templates section
    Then the Email Templates page should be displayed
    And the content creator opens the template named "Test Email Template" for editing
    And the content creator edits the name and description to "Email Test"
    And the content creator previews the template
    And the content creator saves the updated email template
    Then a success message should appear confirming the template update
