scalaVersion := "2.13.6"
organization := "com.example"
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val SberHtmlParse = (project in file("."))
  .settings(
    name := "SberHtmlParse",
    version := "0.1",
    //libraryDependencies += "dev.zio" %% "zio" % "2.0.0-M3",
    libraryDependencies += "org.apache.poi" % "poi" % "5.0.0",
    libraryDependencies += "com.github.pcj" % "google-options" % "1.0.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test
  )



