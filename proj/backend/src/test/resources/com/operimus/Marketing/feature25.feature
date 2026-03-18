@Feature25
Feature: Create and preview Landing Pages

  Scenario: Successful creation and preview of a new landing page
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page
    When the content marketer navigates to the Landing Pages section
    Then the Landing Pages page should be displayed
    When the content marketer clicks the Create Landing Page button
    Then the Create Landing Page form should open
    And the content marketer fills in the landing page name and description
    And the content marketer clicks the Preview Landing Page button
    Then a preview modal should appear showing the landing page preview
    And the content marketer saves the new landing page
    Then a success message should confirm the landing page creation
    