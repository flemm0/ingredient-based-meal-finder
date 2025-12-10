import cats.effect.IO
import cats.syntax.all.*
import org.http4s.client.Client

import MealDbApiAccess.EndpointType
import MealDbApiAccess.EndpointType.*
import MealDbApiAccess.*
import DomainModels.Ingredient
import MealDecoders.{MealSummaryResponse, MealsResponse}


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

  def searchTypeInteractive(): IO[EndpointType] =
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

  def pickMealFromSummary(summary: MealSummaryResponse): IO[MealById] =
    for
      _ <- IO.println("Found the following meals:")
      _ <- summary.meals.zipWithIndex.traverse_ {
        case (meal, idx) => IO.println(s"[${idx + 1}] ${meal.name} ${meal.id}")
      }
      input <- prompt("Enter the number of the meal you want to see the full recipe for:")
      idx   <- IO.fromEither(
        Either
          .catchOnly[NumberFormatException](input.toInt - 1)
          .leftMap(_ => new Exception("Please enter a valid number"))
      )
      selected <- IO.fromEither(
        summary.meals.lift(idx).toRight(new Exception("Number out of range"))
      )
    yield MealById(selected.id)

  def runInteractive(client: Client[IO]): IO[MealsResponse] =
    for
      searchType <- searchTypeInteractive()
      eitherResponse <- MealDbApiAccess.mealRecipeFromApi(client, searchType)

      response <- eitherResponse match
        case Right(fullMeals) => IO.pure(fullMeals) // already a full MealsResponse
        case Left(summary) => 
          for
            mealById <- pickMealFromSummary(summary)
            fullMealsEither <- MealDbApiAccess.mealRecipeFromApi(client, mealById)
            fullMeals <- fullMealsEither match
              case Right(mr) => IO.pure(mr)
              case Left(_)   => IO.raiseError(new Exception("Unexpected summary response"))
          yield fullMeals
    yield response

