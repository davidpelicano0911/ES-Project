@Feature29
Feature: Form Editing

  Scenario: Content Marketer edits a saved form's name and description
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    Given a saved form exists with name "Registration Form" and description "Form to register new users"
    When the Content Marketer edits the form name to "Updated Registration Form" and description to "Updated form for user registration"
    And they confirm saving the form
    Then the form is saved successfully after editing