$projectHumanName$
--

## Local execution

You can test locally the example spark job included in this template directly from sbt: 

```bash 
sbt "run inputFile.txt outputFile.txt"
```

then choose `CountingLocalApp` when prompted.

## Packaging and deployment

You can also assemble a fat jar (see [sbt-assembly](https://github.com/sbt/sbt-assembly) for configuration details): 

```bash
sbt assembly
```

then [submit as usual](https://spark.apache.org/docs/latest/submitting-applications.html) to your spark cluster :

```bash
/path/to/spark-home/bin/spark-submit \
  --class $package$.CountingApp \
  --name the_awesome_app \
  --master <master url> \
  ./target/scala-2.11/<jar name> \
  <input file> <output file>
```
