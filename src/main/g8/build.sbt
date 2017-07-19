// give the user a nice default project!

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

    sparkVersion := "$sparkVersion$",
    sparkComponents := Seq(),

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
        case VersionRegex(v,"") => Some(v)
        case VersionRegex(v,s) => Some(s"\$v+\$s")
        case _ => None
      }
    )),

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

lazy val VersionRegex = "v([0-9]+.[0-9]+.[0-9]+)-?(.*)?".r
