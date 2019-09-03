Feature: Look at a neat gif


  ####################
  ## Neat Gif Tests ##
  ####################
  @Web @NeatGifTest
  Scenario: Looks at neat gif
    Given the user navigates to the website
    And searches for a neat gif
    Then verifies "Neat Gif" is displayed