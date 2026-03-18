@Feature19
Feature: Create and save Email Templates

  Scenario: Successful creation and saving of a new email template
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    When the content creator navigates to the Email Templates section
    Then the Email Templates page should be displayed
    When the content creator clicks the Create Email Template button
    Then the Create Email Template page should open
    And the content creator fills in the template name, description, and subject
    And the content creator previews the template
    And the content creator saves the new email template
    Then a success message should appear confirming the template creation
