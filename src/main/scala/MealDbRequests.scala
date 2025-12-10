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

  private def decodeResponse(
    endpoint: EndpointType,
    body: String
  ): Either[Throwable, Either[MealSummaryResponse, MealsResponse]] =
    endpoint match
      case RandomMeal | MealById(_) =>
        decode[MealsResponse](body).map(Right(_))
      case MealBySingleIngredient(_) =>
        deocde[MealSummaryResponse](body).map(Left(_))
    
  def mealRecipeFromApi(
    client: Client[IO],
    endpoint: EndpointType
  ): IO[Either[MealSummaryResponse, MealsResponse]] =
    for
      uri  <- apiEndpointBuilder(endpoint)
      body <- client.expect[String](uri)
      resp <- IO.fromEither(decodeResponse(endpoint, body))
    yield resp
