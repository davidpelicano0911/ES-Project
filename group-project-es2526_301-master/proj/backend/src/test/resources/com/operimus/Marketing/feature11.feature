@Feature11
Feature: Create and manage campaigns

    Scenario: Successful creation of a new campaign
        Given the manager is on the login page
        When the manager enters valid credentials
        And the manager clicks the login button
        Then the manager should be redirected to the Campaigns page
        And the manager clicks the Create Campaign button
        And the manager fills in the campaign details with a name, description, and deadline
        And the manager saves the new campaign
        Then the campaign should be created and visible in the campaign list

    Scenario: Successful deletion of a campaign
        Given the manager is on the login page
        When the manager enters valid credentials
        And the manager clicks the login button
        Then the manager should be redirected to the Campaigns page
        And a campaign named "Test Campaign" exists in the campaign list
        When the manager clicks the delete button for the created campaign
        And the manager confirms the campaign deletion
        Then a success message should be displayed indicating the campaign was deleted