package $organization$.$name$

/**
 * Example of Spark unit-test, relying on the Spark shared context
  *
  * See https://github.com/holdenk/spark-testing-base
 */

import com.holdenkarau.spark.testing.SharedSparkContext
import org.scalatest.{FlatSpec, Matchers}

class WordCountSparkTest extends FlatSpec with Matchers with SharedSparkContext {

  "tokenizing some lines" should "remove the stop words" in {

    val linesRDD = sc.parallelize(Seq(
      "How happy was the panda? You ask.",
      "Panda is the most happy panda in all the#!?ing land!"))

    val stopWords: Set[String] = Set("a", "the", "in", "was", "there", "she", "he")
    val splitTokens: Array[Char] = "#%?!. ".toCharArray

    val wordCounts = WordCount.withStopWordsFiltered(
      linesRDD, splitTokens, stopWords)
    val wordCountsAsMap = wordCounts.collectAsMap()

    wordCountsAsMap should not contain "the"
    wordCountsAsMap should not contain "?"
    wordCountsAsMap should not contain "#!?ing"
    wordCountsAsMap should not contain "ing"

    wordCountsAsMap("panda") should be (3)

  }
}
