@Feature21
Feature: Personalize email templates

  Scenario: Successful update of the email template subject
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    When the content creator navigates to the Email Templates section
    Then the Email Templates page should be displayed
    And the content creator opens the template named "Email Test" for editing
    And the content creator edits the subject to "Campaign {CompanyName}"
    And the content creator previews the template
    And the content creator saves the updated email template
    Then a success message should appear confirming the template update
