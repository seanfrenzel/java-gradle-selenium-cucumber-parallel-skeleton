Feature: Look at a neat gif


  ####################
  ## Neat Gif Tests ##
  ####################
  @Web @NeatGifTest
  Scenario: Looks at neat gif
    Given the user navigates to Giphy
    And enters "Neat Keanu Reeves" text into "Search Input" on ExamplePage
    And clicks "Search" on ExamplePage
    And opens a Neat GIF
    Then waits 5s to verify "Neat Gif" is displayed on ExamplePage