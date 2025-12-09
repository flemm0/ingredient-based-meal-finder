import cats.effect.IO

import MealDbApiAccess.MealApiEndpoint.*
import RecipeFinder.model.Ingredient
import MealDbApiAccess.MealApiEndpoint


object UserInput:

  def prompt(msg: String): IO[String] =
    for
      _        <- IO.println(s"$msg ")
      response <- IO.readLine
    yield response 

  def ingredientInputInteractive(): IO[MealBySingleIngredient] =
    for
      ingredientName <- prompt("Please enter the name of an ingredient to search a recipe for:")
    yield MealBySingleIngredient(Ingredient(ingredientName, ""))

  def searchTypeInteractive(): IO[MealApiEndpoint] =
    for
      input <- prompt("""
        Please enter [1] to find a random recipe or
        [2] to search a recipe by a specific ingredient
      """)
      result <- input match
        case "1" => IO.pure(RandomMeal)
        case "2" => ingredientInputInteractive()
        case _   => IO.println("Please enter either [1] or [2]") *> searchTypeInteractive()
    yield result
