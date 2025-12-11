import cats.effect.{IO, IOApp}
import cats.implicits.*
import org.http4s.ember.client.EmberClientBuilder

import UserInput.{runInteractive, prettyPrintMealRecipe}
import MealDbApiAccess.*


object main extends IOApp.Simple:

  val run: IO[Unit] = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      for
        response <- runInteractive(client)
        _        <- IO.println(prettyPrintMealRecipe(response))
      yield ()
    }
