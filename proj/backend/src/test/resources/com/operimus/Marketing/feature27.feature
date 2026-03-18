@Feature27
Feature: Delete Landing Pages

  Scenario: Successful deletion of an existing landing page
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    When the content marketer navigates to the Landing Pages section
    Then the Landing Pages page should be displayed
    Given the content marketer is on the Landing Pages list with at least one item
    When the content marketer selects it for deletion
    Then the system asks for confirmation
    And confirms the deletion
    Then a success message should appear confirming the landing page deletion