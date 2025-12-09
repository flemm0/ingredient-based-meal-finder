
object RecipeFinder:

  object model:
    opaque type MealId = Int
    object MealId:
      def apply(id: Int): MealId = id
      extension (mealId: MealId) def value: Int = mealId

    enum MealCategory:
      case Beef, Breakfast, Chicken, Dessert, Goat, Lamb, Miscellaneous,
          Pasta, Pork, Seafood, Side, Starter, Vegan, Vegetarian
    
    enum MealArea:
      case American, British, Canadian, Chinese, Dutch, Egyptian, French,
          Greek, Indian, Irish, Italian, Jamaican, Japanese, Kenyan,
          Malaysian, Mexican, Moroccan, Nigerian, Polish, Portuguese,
          Russian, Spanish, Thai, Tunisian, Turkish, Vietnamese

    opaque type MealName = String
    object MealName:
      def apply(name: String): MealName = name
      extension (mealName: MealName) def value: String = mealName

    /**
      * the following is commented out for now as they require a second API call per ingredient
      * to get the ingredient details

    opaque type IngredientId = Int
    object IngredientId:
      def apply(id: Int): IngredientId = id
      extension (ingredientId: IngredientId) def value: Int = ingredientId

    enum IngredientType:
      case Bean, Bread, Cereal, Cheese, Confectionery, Curd, Dairy, Dressing, Drink,
          Fat, Fish, Fruit, Grain, Juice, Legume, Liqueur, Liquid, Meat, Mushroom,
          Nut, Pasta, Pastry, Preserve, Rice, RootVegetable, Sauce, Seafood,
          Seasoning, Sedge, Side, Spice, Spirit, Stock, Sugar, Vegetable, Vinegar, Wine

      def asString: String = this match
        case RootVegetable => "Root Vegetable"
        case other         => other.toString
      */

    case class Ingredient(name: String, measure: String)

    case class Meal(id: MealId, name: String, category: MealCategory,
                    area: MealArea, instructions: String, ingredients: List[Ingredient])


  import model.*, model.MealCategory.*, model.MealArea.*

