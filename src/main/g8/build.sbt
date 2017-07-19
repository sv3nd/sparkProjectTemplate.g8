// give the user a nice default project!

import sbtrelease._
import ReleaseTransformations._

lazy val root = (project in file("."))

  .enablePlugins(GitVersioning)

  .configs(IntegrationTest)
  .settings( Defaults.itSettings : _*)

  .settings(
    
    name := "$name$",
    
    inThisBuild(List(
      organization := "$organization$",
      scalaVersion := "2.11.8"
    )),

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    parallelExecution in Test := false,
    fork := true,

    coverageHighlighting := true,

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-streaming" % "$sparkVersion$" % "provided",
      "org.apache.spark" %% "spark-sql"       % "$sparkVersion$" % "provided",

      "org.scalatest"   %% "scalatest"          % "3.0.1"   % "test,it",
      "org.scalacheck"  %% "scalacheck"         % "1.13.4"  % "test,it", 
      "com.holdenkarau" %% "spark-testing-base" % "$sparkVersion$_$sparkTestingbaseRelease$" % "test,it"
    ),

    // uses compile classpath for the run task, including "provided" jar (cf http://stackoverflow.com/a/21803413/3827)
    run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated,

    // uses sbt-git to generate project version dynamically based on number of commits since last version tag
    inThisBuild(List(      
      git.useGitDescribe in ThisBuild := true,
      git.baseVersion in ThisBuild := "0.0.0",

      // replaces the '-' of git-describe with a '+', to better convey the idea that we have supplementary commits since that tag
      git.gitTagToVersionNumber := {

        case VersionRegex(ver,"") => Some(ver)
        case VersionRegex(ver, supp) => Some(s"\$ver-\$supp")
        case VersionRegex(ver, supp, sha) => Some(s"\$ver-\$supp")
        case unknown => None
      }

    )),


    // configuration of the release process
    releaseProcess := Seq[ReleaseStep](
      inquireOneVersion,
      runClean,
      runTest, 
      setReleaseVersionNoWrite,
      runAssembly, 
      tagRelease
      // TODO: put back publication here
    ),

    // based on the convention from sbt-git: the next release version is the tag version + 1
    releaseVersion := { ver =>
      if (ver.contains("SNAPSHOT"))
        sys.error(s"Version \$ver cannot be released: SNAPSHOT")

      Version(ver)
        .map(_.withoutQualifier)
        .map(_.bump.string)
        .getOrElse(sys.error(s"Version \$ver is not compatible with \$VersionRegex pattern")) 
    },    


    resolvers ++= Seq(
      "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",
      Resolver.sonatypeRepo("public")
    ),

    pomIncludeRepository := { x => false },

    // publish settings
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  )

lazy val VersionRegex = "v?([0-9]+.[0-9]+.[0-9]+)-?(.*)?".r

// hacked re-implementations of some components of sbt-release
// copy-pasted from https://github.com/sbt/sbt-release/blob/master/src/main/scala/ReleaseExtra.scala


// this version of inquireVersions prompts for just one and not 2 version number: we know the previous version number
// from the tag put by sbt-git and we prompt the user for the next version. The default should be ok in most cases, 
// except if we want to bump a minor or major version. 
lazy val inquireOneVersion: ReleaseStep = { st: State =>

  val extracted = Project.extract(st)

  val useDefs = st.get(ReleaseKeys.useDefaults).getOrElse(false)
  val currentV = extracted.get(version)

  val releaseFunc = extracted.runTask(releaseVersion, st)._2
  val suggestedReleaseV = releaseFunc(currentV)

  //flatten the Option[Option[String]] as the get returns an Option, and the value inside is an Option
  val releaseV = readVersion(suggestedReleaseV, "Release version [%s] : ", useDefs, st.get(ReleaseKeys.commandLineReleaseVersion).flatten)

  val nextFunc = extracted.runTask(releaseNextVersion, st)._2
  val suggestedNextV = nextFunc(releaseV)

  st.put(ReleaseKeys.versions, (releaseV, releaseV))

}

// This assigns the new value of the project version, without writting it to a version.sbt since we use tags to handle the version
def setVersionNoWrite(selectVersion: Versions => String): ReleaseStep =  { st: State =>
  val vs = st.get(ReleaseKeys.versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))
  val selected = selectVersion(vs)

  st.log.info("Setting version to '%s'." format selected)
  val useGlobal = Project.extract(st).get(releaseUseGlobalVersion)
  val versionStr = (if (useGlobal) globalVersionString else versionString) format selected

  reapply(Seq(
    if (useGlobal) version in ThisBuild := selected
    else version := selected
  ), st)
}

// our own version of the setReleaseVersion, without creating a version.sbt
lazy val setReleaseVersionNoWrite: ReleaseStep = setVersionNoWrite(_._1)


lazy val runAssembly: ReleaseStep = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    extracted.runAggregated(assembly in Global in ref, st)
  },
  enableCrossBuild = true
)
