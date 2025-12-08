import cats.effect.*
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.client.dsl.io.*
import io.circe.Decoder
import io.circe.generic.semiauto.*
import io.circe.parser.decode

import RecipeFinder.model.*
import MealJson.MealsResponse


object MealDbApiAccess:
  
  enum MealApiEndpoint:
    case RandomMeal
    case MealBySingleIngredient(ingredient: Ingredient)

  import MealApiEndpoint.*

  def apiEndpointBuilder(endpoint: MealApiEndpoint): IO[Uri] = endpoint match
    case RandomMeal =>
      IO.fromEither(Uri.fromString("https://www.themealdb.com/api/json/v1/1/random.php"))
    case MealBySingleIngredient(ingredient) =>
      IO.fromEither(
        Uri
          .fromString("https://www.themealdb.com/api/json/v1/1/filter.php")
          .map(_.withQueryParam("i", ingredient.name))
      )

  def runApiQuery(client: Client[IO], endpoint: Uri): IO[MealsResponse] =
    client.expect[String](endpoint)
      .flatMap(body =>
        IO.fromEither(decode[MealsResponse](body))
      )

  val run: IO[MealsResponse] = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      for
        uri <- apiEndpointBuilder(RandomMeal)
        response <- runApiQuery(client, uri)
      yield response
    }
