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

then [submit as usual](https://spark.apache.org/docs/latest/submitting-applications.html) to your spark cluster:

```bash
/path/to/spark-home/bin/spark-submit \
  --class $package$.CountingApp \
  --name the_awesome_app \
  --master <master url> \
  ./target/scala-2.11/<jar name> \
  <input file> <output file>
```

## Test and test coverage

Run the following to execute all the unit tests as well as obtain an html coverage report:

```bash
sbt test coverage coverageReport
``` 

See [sbt-coverage](https://github.com/scoverage/sbt-scoverage) for details.

## Release process 

The build script includes a basic skeleton of a release process, based on [sbt-git](https://github.com/sbt/sbt-git) and [sbt-release](https://github.com/sbt/sbt-release).

The project version is controlled with git tags. The first thing to do for this to work is to make this a git repository.  

```bash
git init
```

The release process essentially runs the tests, bumps the project version, maintains git tags, and builds the spark fat jar (it could also publish it to artifactory, though that's not activated in the current script).

You can only release from a clean repository (i.e. without any uncommitted nor untracked files), so let's first add everything in one first commit: 

```bash
git add . 
git commit -m "generated job from template"
```

The next step is typically happening on Jenkins, after you pushed your commits to a feature branch on some remote server and that branch gets merged into master (the following assumes the base version was 0.0.0). 

```bash
>sbt "release with-defaults"
[info] Setting version to '0.0.1'.
...

```

This should result in a fat jar called `target/scala-2.11/<projecName>-assembly-0.0.1.jar` that should probably be pushed to artifactory (not done here at the moment).

And the sbt version should now be:

```bash 
> sbt version
[info] 0.0.1
```

The release task should also have put a tag on the commit that was used: 

```git
> git log --pretty=oneline
bd2f6e4cc077cba53b9d5eeabb6b9c4970cd80ed (HEAD -> master, tag: v0.0.1) a change
ed3d10dbbe9d860a727b40a470446562b79a9f95 generated job from template
```

Note that you can ommit to specify `with-defaults` when launching `release`, which allows for example to manually bump the minor or major version. 

Say you add a couple of commits to the repo and the log now looks as follows: 

```git
git log --pretty=oneline
9bb9d833b99b3e671e1495dd884dbbc8bb75519e (HEAD -> master) even more modifications
a2559027b8582caa2ed27829a6df043497b8ef64 some more modifications
81db83b4886f92a8dba5112b5e5bed991189c350 some modifications
bd2f6e4cc077cba53b9d5eeabb6b9c4970cd80ed (tag: v0.0.1) a change
ed3d10dbbe9d860a727b40a470446562b79a9f95 generated job from template
``` 

On sbt, the version should now look something like: 

```bash
>sbt version
0.0.1-3-g9bb9d83
```

Under the hood this is obtained from `git describe()`. It reads: the current commit is 3 commits ahead of the latest release, which is 0.0.1, and the commit sha is g9bb9d83. Keep in mind that this is typicaly happening on a feature branch, typically branched from master at the location of the 'v0.0.1' tag. This means that several parallel feature branches could potentially be "3 commits ahead of v0.0.0." at some point, and the sha hash makes then sure that all the tags are unique.

We can do a release again: 

```bash
sbt "release with-defaults"
[info] Setting version to '0.0.2'.
...
```

This should this time produce a jar versioned as follows: `target/scala-2.11/<projecName>-assembly-0.0.2.jar`

The sbt version should now be:

```bash 
> sbt version
[info] 0.0.2
```

And the git log should now contain 2 tags:

```
git log --pretty=oneline
9bb9d833b99b3e671e1495dd884dbbc8bb75519e (HEAD -> master, tag: v0.0.2) even more modifications
a2559027b8582caa2ed27829a6df043497b8ef64 some more modifications
81db83b4886f92a8dba5112b5e5bed991189c350 some modifications
bd2f6e4cc077cba53b9d5eeabb6b9c4970cd80ed (tag: v0.0.1) a change
ed3d10dbbe9d860a727b40a470446562b79a9f95 generated job from template
~
```

Again, if several feature branches based on `v0.0.0.1` exist, they should now be rebased on the new released master before being mergeable. By doing so, their own version will automatically be based on the new tag, their own version will bump the 



