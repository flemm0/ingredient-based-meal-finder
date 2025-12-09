import cats.effect.{IO, IOApp}
import cats.implicits.*
import org.http4s.ember.client.EmberClientBuilder

import UserInput.searchTypeInteractive
import MealDbApiAccess.*

object main extends IOApp.Simple:

  val run: IO[Unit] = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      for
        endpoint <- searchTypeInteractive()
        uri      <- apiEndpointBuilder(endpoint)
        response <- runApiQuery(client, uri)
        _        <- IO.println(response)
      yield ()
    }
