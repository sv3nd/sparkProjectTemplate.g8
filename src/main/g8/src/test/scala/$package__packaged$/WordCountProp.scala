package $organization$.$name$

import org.scalatest.{Matchers, PropSpec}
import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks

/**
  * 2 simple examples of property-based testing
  *
  * see property based testing: http://www.scalatest.org/user_guide/generator_driven_property_checks
  *
  * and matchers: http://www.scalatest.org/user_guide/using_matchers#checkingEqualityWithMatchers
  */
class WordCountProp extends PropSpec with Matchers with GeneratorDrivenPropertyChecks {

  // basic example: we use an existing data generator from scalacheck
  property ("tokenizing a single character should always yield itself") {
    forAll{
      anySingleChar: Char =>
        WordCount.tokenizeLc(anySingleChar.toString) should be {
          if (anySingleChar == ' ') List()
          else List(anySingleChar.toString)
        }
    }
  }


  // generator of strings made of space-separated words
  val textGen: Gen[String] = nonEmptyListOf(alphaNumChar).map(_.mkString(" "))

  // example with a custom data generator
  property ("any tokenized string should have at least one token (if not empty)") {
    forAll((textGen, "tokens")) {
      text => WordCount.tokenizeLc(text).size should be > 0
    }
  }

}
