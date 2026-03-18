@Feature28
Feature: Form Creation

  Scenario: Content Marketer creates a new form
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    Given the Content Marketer is on the form builder page
    When they fill in the form name as "Registration Form" and the description as "Form to register new users"
    And they click on "Save Form"
    Then the form is saved successfully