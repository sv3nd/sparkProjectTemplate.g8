package $organization$.$name$

/**
 * Everyone's favourite wordcount example.
 */

import org.apache.spark.rdd._

object WordCount {

  val defaultSeparator = " ".toCharArray

  /**
   * A slightly more complex than normal wordcount example with optional
   * separators and stopWords. Splits on the provided separators, removes
   * the stopwords, and converts everything to lower case.
   */
  def withStopWordsFiltered(rdd : RDD[String],
    separators : Array[Char] = defaultSeparator,
    stopWords : Set[String] = Set("the")): RDD[(String, Int)] = {

    val tokens: RDD[String] = rdd.flatMap(tokenizeLc(_, separators))
    val lcStopWords = stopWords.map(_.trim.toLowerCase)
    val words = tokens.filter(token =>
      !lcStopWords.contains(token) && (token.length > 0))
    val wordPairs = words.map((_, 1))
    val wordCounts = wordPairs.reduceByKey(_ + _)
    wordCounts
  }

  /** 
   Splits a string into lower-case words 
  */
  def tokenizeLc(line: String, separators : Array[Char] = defaultSeparator): List[String] =
    line.split(separators).map(_.trim.toLowerCase).toList
}
