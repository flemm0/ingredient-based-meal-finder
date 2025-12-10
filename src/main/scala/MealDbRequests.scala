import cats.effect.*
import org.http4s.*
import org.http4s.client.Client
import io.circe.parser.*

import DomainModels.*
import MealDecoders.*

object MealDbApiAccess:
  
  enum EndpointType:
    case RandomMeal
    case MealBySingleIngredient(ingredient: Ingredient)
    case MealById(mealId: MealId)

  import EndpointType.*

  def apiEndpointBuilder(endpoint: EndpointType): IO[Uri] = endpoint match
    case RandomMeal =>
      IO.fromEither(Uri.fromString("https://www.themealdb.com/api/json/v1/1/random.php"))

    case MealBySingleIngredient(ingredient) =>
      IO.fromEither(
        Uri
          .fromString("https://www.themealdb.com/api/json/v1/1/filter.php")
          .map(_.withQueryParam("i", ingredient.name))
      )

    case MealById(mealId) =>
      IO.fromEither(
        Uri
          .fromString("https://www.themealdb.com/api/json/v1/1/lookup.php")
          .map(_.withQueryParam("i", mealId.value))
      )

  def mealsFilteredByIngredient() = ???

  // TODO: Need to re-run query if using filter on single ingredient
  def mealRecipeFromApi(client: Client[IO],
      endpointType: EndpointType): IO[Either[FilteredMealsResponse, MealsResponse]] =
    apiEndpointBuilder(endpointType).flatMap { uri =>
      endpointType match
        case RandomMeal | MealById(_) =>
          client.expect[String](uri)
            .flatMap(body =>
              IO.fromEither(decode[MealsResponse](body).map(Right(_)))
            )
        case MealBySingleIngredient(_) =>
          client.expect[String](uri)
            .flatMap(body =>
              IO.fromEither(decode[FilteredMealsResponse](body).map(Left(_)))
            )
    }
