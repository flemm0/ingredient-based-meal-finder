import cats.effect.{IO, IOApp}
import cats.implicits.*
import org.http4s.ember.client.EmberClientBuilder

import UserInput.runInteractive
import MealDbApiAccess.*


object main extends IOApp.Simple:

  val run: IO[Unit] = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      for
        response <- runInteractive(client)
        _        <- IO.println(response.toString())
      yield ()
    }
