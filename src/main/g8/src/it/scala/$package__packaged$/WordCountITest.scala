package $organization$.$name$

import java.io.File
import java.nio.file.{Path, Paths}

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.commons.io.FileUtils
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

import scala.io.Source


/**
  * Short example of an integration test:
  *
  * - we use FeatureSpec to allow a more verbose and high level description
  * - execution of the test typically involves interacting with other systems 
  *   (e.g. here: just reading and writing to local files)
  *
  * See http://www.scalatest.org/getting_started_with_feature_spec
  *
  */
class WordCountITest extends FeatureSpec with GivenWhenThen with SharedSparkContext with Matchers {

  info("As a journalist")
  info("I want to count words in any article")
  info("So I can focus on the most talked about subjects")

  feature("Word counting") {
    scenario("Counting words from a local file") {

      // Put here code that sets up the test,
      // which typically involves setting up connection to external systems
      Given("a file containing a journal article on the local file system")

      val rdd = sc.textFile("./build.sbt")

      // the actual test
      When("the jobs counts words from that article")

      WordCount.withStopWordsFiltered(rdd, " ,\"\'!=:".toCharArray)
        .repartition(1)
        .saveAsTextFile(wordCountOutputDir)

      // post-conditions
      Then("the output file should contain 1 org.scalatest")

      val readResult = Source.fromFile(Paths.get(wordCountOutputDir, "part-00000").toString).getLines().toList
      readResult should contain ("(org.scalatest,1)")

    }
  }


  // clean up

  val tmpDir: Path = Paths.get(System.getProperty("java.io.tmpdir"))
  val wordCountOutputDir: String = Paths.get(tmpDir.toString, "word-count-test-result.txt").toString

  def cleanup(): Unit = {
    FileUtils.deleteDirectory(new File(wordCountOutputDir))
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    cleanup()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    cleanup()
  }


}
