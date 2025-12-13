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
      def loop: IO[Unit] =
        runInteractive(client).flatMap { response =>
          IO.println(prettyPrintMealRecipe(response)) *>
          IO.print("Press Enter to continue or type 'exit' to quit: ") *>
          IO.readLine.flatMap { input =>
            if (input.trim.equalsIgnoreCase("exit")) IO.println("Goodbye 👋")
            else loop
          }
        }
        
      loop
    }
