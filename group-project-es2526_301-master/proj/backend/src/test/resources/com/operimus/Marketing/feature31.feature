@Feature31
Feature: Create and Delete Social Post

  Scenario: Create and delete a social media post successfully
    Given the content creator is on the login page
    When the content creator enters valid credentials
    And the content creator clicks the login button
    Then the content creator should be redirected to the Campaigns page

    Given the content creator navigates to the Social Posts page
    When the content creator clicks the "Create Post" button
    And fills in the post form with:
      | name        | PostNameTest                     |
      | description | Automated test post description   |
      | platform    | facebook                          |
    And uploads an image for the post
    And selects "Publish Now"
    And submits the social post form

    When the content creator deletes the newly created post