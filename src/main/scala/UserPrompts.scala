import cats.effect.IO
import cats.syntax.all.*
import org.http4s.client.Client

import MealDbApiAccess.EndpointType
import MealDbApiAccess.EndpointType.*
import MealDbApiAccess.*
import DomainModel.{Ingredient, MealArea, MealId}
import MealDecoders.{MealSummaryResponse, MealsResponse}


object UserInput:

  def prompt(msg: String): IO[String] =
    for
      _        <- IO.println(s"$msg ")
      response <- IO.readLine
    yield response
  
  private def promptAndMap[T](msg: String)(f: String => T): IO[T] =
    for
      input <- prompt(msg)
    yield f(input)

  private def areaFilterInteractive(): IO[MealByArea] =
    promptAndMap("Please enter the area (cuisine) you want to search a recipe for:") { areaName =>
      MealByArea(MealArea(areaName))
    }

  private def ingredientFilterInteractive(): IO[MealBySingleIngredient] =
    promptAndMap("Please enter the ingredient you want to search a recipe for:") { ingredientName =>
      MealBySingleIngredient(Ingredient(ingredientName, ""))
    }

  def searchTypeInteractive(): IO[EndpointType] =
    val message = """
    |Please enter one of the following letters:
    |[r] to find a random recipe
    |[i] to search a recipe by a specific ingredient
    |[a] to search a recipe by a specific area
    """.stripMargin
    for
      input <- prompt(message)
      result <- input match
        case "r" => IO.pure(RandomMeal)
        case "i" => ingredientFilterInteractive()
        case "a" => areaFilterInteractive()
        case _   => IO.println("Please enter a valid input]") *> searchTypeInteractive()
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
      eitherResponse <- mealRecipeFromApi(client, searchType)

      response <- eitherResponse match
        case Right(fullMeals) => IO.pure(fullMeals) // already a full MealsResponse
        case Left(summary) => summary match
          case summary @ MealSummaryResponse(meals) if meals.isEmpty =>
            IO.pure(MealsResponse(Nil)) // no meals found
          case summary =>
            for
              mealById <- pickMealFromSummary(summary)
              fullMealsEither <- mealRecipeFromApi(client, mealById)
              fullMeals <- fullMealsEither match
                case Right(mr) => IO.pure(mr)
                case Left(_)   => IO.raiseError(new Exception("Unexpected summary response"))
            yield fullMeals
    yield response

  def indefiniteArticle(word: String): String =
    val vowels = Set('a', 'e', 'i', 'o', 'u')
    if word.nonEmpty && vowels.contains(word.toLowerCase().head) then "an" else "a"

  def prettyPrintMealRecipe(response: MealsResponse): String =
    response.meals.headOption match
      case None => "Sorry, no meal recipes were found."
      case Some(meal) =>
        val ingredients = meal.ingredients
          .map(ing => s"- ${ing.name}: ${ing.measure}")
          .mkString("\n")
      
        s"""
        |Here is how to make **${meal.name}**,
        |${indefiniteArticle(meal.area.toString())} ${meal.area} ${meal.category.toString().toLowerCase()} dish:
        |
        |These are the required ingredients and their amounts:
        |$ingredients
        |
        |These are the step-by-step instructions:
        |${meal.instructions.trim}
        """.stripMargin
