name := "gallery"
scalaVersion := "2.12.8"

val compilerOptions = Seq(
  "-Ypartial-unification",
  "-feature",
  "-deprecation",
  "-Yno-adapted-args",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused",
  "-language:higherKinds",
)
val commonDeps = Seq()

val backendDeps = commonDeps ++ Seq(
  "org.slf4j"    % "slf4j-log4j12"        % Dependencies.slf4j,

  "org.http4s"   %% "http4s-dsl"          % Dependencies.http4s,
  "org.http4s"   %% "http4s-blaze-server" % Dependencies.http4s,

  "org.xerial"   %  "sqlite-jdbc"         % Dependencies.sqliteJdbc,
  "org.flywaydb" %  "flyway-core"         % Dependencies.flyway,
  "org.tpolecat" %% "doobie-core"         % Dependencies.doobie,
  "org.tpolecat" %% "doobie-hikari"       % Dependencies.doobie,
)

val backend = project.in(file("backend"))
  .settings(
    scalacOptions ++= compilerOptions,
    libraryDependencies ++= backendDeps
  )