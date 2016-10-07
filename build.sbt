import sbt.Project.projectToRef

lazy val clients = Seq(scalajsclient)
lazy val scalaV = "2.11.7"

lazy val playserver = (project in file("play")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  routesGenerator := InjectedRoutesGenerator,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.5.0",
    "org.webjars" %% "webjars-play" % "2.5.0-1",
    "org.webjars" % "ace" % "01.08.2014",
    "org.webjars" % "jquery" % "1.11.1",
    "org.webjars" % "bootstrap" % "4.0.0-alpha.3",
    "org.webjars" % "octicons" % "4.3.0",
    "org.scala-lang" % "scala-compiler" % "2.11.7",
    "org.scala-js" % "scalajs-compiler_2.11.7" % "0.6.10",
    "org.scala-lang.modules" %% "scala-async" % "0.9.1",
    "org.scala-js" %% "scalajs-tools" % "0.6.10"
  )
).enablePlugins(PlayScala).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm)

lazy val scalajsclient = (project in file("scalajs")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn-snapshot/",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.2",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
    "com.lihaoyi" %%% "upickle" % "0.2.8",
    "com.scalawarrior" %%% "scalajs-createjs" % "0.0.1-SNAPSHOT",
    //"com.scalawarrior" %%% "scalajs-ace" % "0.0.1-SNAPSHOT",
    "org.scala-lang.modules" %% "scala-async" % "0.9.1" % "provided"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project playserver", _: State)) compose (onLoad in Global).value
