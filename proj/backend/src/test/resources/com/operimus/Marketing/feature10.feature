@Feature10
Feature: Edit existing marketing workflow

  Scenario: Successful edit of an existing workflow by adding a Form block
    Given the manager is on the login page
    When the manager enters valid credentials
    And the manager clicks the login button
    Then the manager should be redirected to the Campaigns page
    And the manager selects the campaign named "Winter Clearance"
    Then the manager should be redirected to the Campaign Details page
    And the manager clicks the View Workflow button
    Then the manager should be redirected to the Workflow Builder page
    When the manager adds a Form block to the workflow
    And the manager connects nodes in the workflow
    And the manager saves the edited workflow
    Then the workflow should be updated successfully and visible in the builder
