import sbt.Project.projectToRef

lazy val clients = Seq(scalajsclient)
lazy val scalaV = "2.12.2"

lazy val playserver = (project in file("play")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  pipelineStages in Assets := Seq(scalaJSPipeline),
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  routesGenerator := InjectedRoutesGenerator,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.1",
    //"com.vmunier" %% "play-scalajs-scripts" % "1.1.1",
    "org.webjars" %% "webjars-play" % "2.6.0",
    "org.webjars" % "ace" % "01.08.2014",
    "org.webjars" % "jquery" % "1.12.4",
    "org.webjars" % "bootstrap" % "4.0.0-alpha.5",
    "org.webjars" % "octicons" % "4.3.0",
    "org.scala-lang" % "scala-compiler" % "2.12.2",
    "org.scala-js" % "scalajs-compiler_2.12.2" % "0.6.18",
    "org.scala-lang.modules" %% "scala-async" % "0.9.6",
    "org.scala-js" %% "scalajs-tools" % "0.6.14",
    guice
  )
).enablePlugins(PlayScala).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm)

lazy val scalajsclient = (project in file("scalajs")).settings(
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer := true,
  resolvers ++= Seq(
    "amateras-repo" at "http://amateras.sourceforge.jp/mvn/",
    "amateras-snapshot-repo" at "http://amateras.sourceforge.jp/mvn-snapshot/"
  ),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
    "com.lihaoyi" %%% "upickle" % "0.4.4",
    "com.scalawarrior" %%% "scalajs-createjs" % "0.0.3-SNAPSHOT",
    //"com.scalawarrior" %%% "scalajs-ace" % "0.0.1-SNAPSHOT",
    "org.scala-lang.modules" %% "scala-async" % "0.9.6" % "provided"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.6.5",
      "com.github.japgolly.scalacss" %%% "core" % "0.5.3"
    )
  ).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project playserver", _: State)) compose (onLoad in Global).value
