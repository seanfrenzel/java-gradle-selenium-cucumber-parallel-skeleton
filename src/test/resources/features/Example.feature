Feature: Look at a neat gif


  ####################
  ## Neat Gif Tests ##
  ####################
  @Web @NeatGifTest
  Scenario: Looks at neat gif
    Given the user navigates to the website
    And enters "Neat" text into "Search Input" on "Example Page"
    And clicks "Search" on "Example Page"
    And opens a Neat GIF
    Then waits 5s to verify "Neat Gif" is displayed on "Example Page"