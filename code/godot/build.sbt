val scalaVersion12 = "2.12.6"

lazy val commonSettings = Seq(
    name := "godot",
    version := "0.1.0",

    scalaVersion := scalaVersion12,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0"
)

lazy val root = project
  .in(file("."))
  .settings(
      name := "godot",
      version := "0.1.0",

      scalaVersion := scalaVersion12,

      libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
      libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5",
      libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0"
  )

//val dottyVersion = "0.13.0-RC1"
//val scalameta_code = project.in(file(".")).settings(commonSettings: _*)
//lazy val commonSettings = Seq(
//  name := "dotty-simple",
//  version := "0.1.0",
//
//  scalaVersion := dottyVersion,
//
//  //    libraryDependencies += ("org.scalatest" %% "scalatest" % "3.0.5").withDottyCompat(scalaVersion.value),
//  libraryDependencies += ("com.novocode" % "junit-interface" % "0.11" %
//    "test").withDottyCompat(scalaVersion.value),
//  libraryDependencies += ("org.scalameta" %% "scalameta" % "4.1.0").withDottyCompat(scalaVersion.value)
//)
//
//
//lazy val root = project
//  .in(file("."))
//  .dependsOn(scalameta_code)
//  .settings(
//    name := "dotty-simple",
//    version := "0.1.0",
//
//    scalaVersion := dottyVersion,
//    scalacOptions += "-Ypartial-unification",
//
//      libraryDependencies += ("com.novocode" % "junit-interface" % "0.11" %
//        "test").withDottyCompat(scalaVersion.value),
//    // libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"
//
//
//
//      libraryDependencies += ("org.typelevel" %% "cats-core" % "1.6.0").withDottyCompat(scalaVersion.value),
//
//
//
//      libraryDependencies += ("org.scalatest" %% "scalatest" % "3.0.5").withDottyCompat(scalaVersion.value),
////    libraryDependencies += ("org.scalameta" %% "scalameta" % "4.1.0")
////      .withDottyCompat(scalaVersion.value),
//    libraryDependencies += ("org.scala-lang.modules" %%
//      "scala-parser-combinators" % "1.1.0").withDottyCompat(scalaVersion.value)
//  )
