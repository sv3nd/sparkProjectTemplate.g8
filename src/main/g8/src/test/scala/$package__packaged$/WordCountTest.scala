package $organization$.$name$

import org.scalatest.{FlatSpec, Matchers}

/**
  * Basic example of ad-hoc unit test, providing some input to a function and checking post condition.
  *
  * See matchers: http://www.scalatest.org/user_guide/using_matchers#checkingEqualityWithMatchers
  */
class WordCountTest extends FlatSpec with Matchers {


  "a basic string " should "be tokenized into 6 words " in  {

    val tokens = WordCount.tokenizeLc("unit tests for fun and profit!")

    tokens should have size 6
    tokens should equal { List("unit", "tests", "for", "fun", "and", "profit!") }

  }

}
