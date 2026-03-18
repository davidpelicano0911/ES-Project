@Feature26
Feature: Edit Landing Pages

  Scenario: Successful edit of an existing landing page
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    When the content marketer navigates to the Landing Pages section
    Then the Landing Pages page should be displayed
    Given the content marketer is on the Landing Pages list with at least one item
    When the content marketer selects an existing landing page to edit
    Then the landing page editor should open
    When the content marketer modifies the name and description
    And saves the edited landing page
    Then a confirmation message should appear before overwriting
    