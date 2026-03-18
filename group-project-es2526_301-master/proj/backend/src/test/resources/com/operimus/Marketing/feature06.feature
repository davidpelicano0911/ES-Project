@Feature06
Feature: Build automated marketing workflows

    Scenario: Successful creation and configuration of a new workflow
        Given the manager is on the login page
        When the manager enters valid credentials
        And the manager clicks the login button
        Then the manager should be redirected to the Campaigns page
        And the manager selects the campaign named "Winter Clearance"
        Then the manager should be redirected to the Campaign Details page
        And the manager clicks the Create New Workflow button
        Then the manager should be redirected to the Create Workflow page
        And the manager fills in the workflow details with a name and description
        And the manager confirms the workflow creation
        Then the workflow should be successfully created
        And the manager should be redirected back to the Campaign Details page
        And the manager clicks the View Workflow button
        Then the manager should be redirected to the Workflow Builder page
        And the manager adds nodes to the workflow
        And the manager connects nodes in the workflow
        And the manager clicks the Save button
        Then the workflow should be saved successfully and visible in the builder

