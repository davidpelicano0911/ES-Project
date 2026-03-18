@Feature30
Feature: Delete Forms 

  Scenario: Content Marketer deletes an existing form
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    Given a form exists in the library with name "Updated Registration Form"
    When the Content Marketer chooses to delete it
    Then the system confirms the action before removal
    And shows a success message once removed