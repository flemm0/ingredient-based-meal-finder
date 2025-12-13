## “What Can I Cook?” — Console-Based Meal Recipe Finder

Lightweight CLI that queries TheMealDB to fetch recipes. The current code implements an interactive command-line flow allowing users to either fetch a random recipe or search for meals by a single ingredient, then view the full recipe and ingredients for a selected meal.

**Current Features**
- **Random recipe**: fetch a random meal and display its ingredients and instructions.
- **Search by single ingredient**: search the filter endpoint for meals containing an ingredient, pick one, and view the full recipe.
- **Search by meal area (region)**
- **Interactive prompts**: simple CLI prompts drive the flow.

**How to run**
- Run interactively with sbt:

    `sbt run`

- Run tests with:

    `sbt test`

**APIs used**
- Uses TheMealDB public API endpoints:
    - `https://www.themealdb.com/api/json/v1/1/random.php` (random meal)
    - `https://www.themealdb.com/api/json/v1/1/filter.php?i=<ingredient>` (search by ingredient)
    - `https://www.themealdb.com/api/json/v1/1/lookup.php?i=<id>` (lookup by id) a

**Dependencies**
- Built with Scala 3, using `cats-effect`, `http4s-ember-client`, and `circe` for JSON decoding.
