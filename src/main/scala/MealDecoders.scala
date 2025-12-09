object MealJson:

  import io.circe.*
  import io.circe.generic.semiauto.*
  import RecipeFinder.model.*

  given Decoder[MealId] = Decoder.decodeInt.map(MealId(_))
  given Decoder[MealName] = Decoder.decodeString.map(MealName(_))
  
  given Decoder[MealCategory] = Decoder.decodeString.emap(s =>
    MealCategory.values.find(_.toString == s)
      .toRight(s"Invalid MealCategory: $s")
  )
  given Decoder[MealArea] = Decoder.decodeString.emap(s =>
    MealArea.values.find(_.toString == s)
      .toRight(s"Invalid MealArea: $s")
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
      id <- cursor.get[Int]("idMeal")
      name <- cursor.get[String]("strMeal")
      category <- cursor.get[MealCategory]("strCategory")
      area <- cursor.get[MealArea]("strArea")
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
  given Decoder[MealsResponse] = deriveDecoder
