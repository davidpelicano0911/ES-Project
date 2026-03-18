@Feature02
Feature: User Logout

  Scenario: Successful logout from any page
    Given the user is on the login page
    When the user enters valid credentials
    And the user clicks the login button
    And the user clicks the logout option
    Then the session should end and redirect to the Keycloak login page
