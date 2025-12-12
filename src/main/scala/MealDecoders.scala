object MealDecoders:

  import io.circe.*
  import io.circe.generic.semiauto.*
  import DomainModel.*

  given Decoder[MealId] = Decoder.decodeInt.map(MealId(_))
  given Decoder[MealName] = Decoder.decodeString.map(MealName(_))
  given Decoder[MealArea] = Decoder.decodeString.map(MealArea(_))
  given Decoder[MealCategory] = Decoder.decodeString.emap(s =>
    MealCategory.values.find(_.toString == s)
      .toRight(s"Invalid MealCategory: $s")
  )
  given Decoder[Ingredient] = deriveDecoder

  given Decoder[Meal] = Decoder.instance { cursor =>
    def readIngredients: List[Ingredient] =
      1.to(20).toList.flatMap { i =>

        val nameField = s"strIngredient$i"
        val measureField = s"strMeasure$i"

        val nameOpt = cursor.get[String](nameField).toOption.map(_.trim).filter(_.nonEmpty)
        val measureOpt = cursor.get[String](measureField).toOption.map(_.trim).filter(_.nonEmpty)

        (nameOpt, measureOpt) match
          case (Some(name), Some(measure)) => Some(Ingredient(name, measure))
          case (Some(name), None)          => Some(Ingredient(name, ""))  
          case _                           => None
      }
    for
      id           <- cursor.get[Int]("idMeal")
      name         <- cursor.get[String]("strMeal")
      category     <- cursor.get[MealCategory]("strCategory")
      area         <- cursor.get[MealArea]("strArea")
      instructions <- cursor.downField("strInstructions").as[String]
    yield Meal(
            MealId(id),
            name,
            category,
            area,
            instructions,
            readIngredients
          )
  }

  // wrapper for full API response
  case class MealsResponse(meals: List[Meal])
  given Decoder[MealsResponse] = Decoder.instance { cursor =>
    for
      mealsOpt <- cursor.get[Option[List[Meal]]]("meals")
    yield MealsResponse(mealsOpt.getOrElse(Nil))
  }

  // decoder for response for filter endpoint
  given Decoder[MealSummary] = Decoder.instance { cursor =>
    for name <- cursor.get[MealName]("strMeal")
        id   <- cursor.get[MealId]("idMeal")
    yield MealSummary(name, id)  
  }

  case class MealSummaryResponse(meals: List[MealSummary])
  given Decoder[MealSummaryResponse] = Decoder.instance { cursor =>
    for
      mealsOpt <- cursor.get[Option[List[MealSummary]]]("meals")
    yield MealSummaryResponse(mealsOpt.getOrElse(Nil))
  }
