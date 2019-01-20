name := "gallery"
scalaVersion := "2.12.8"

val compilerOptions = Seq("-Ypartial-unification")
val commonDeps = Seq()



val backendDeps = commonDeps ++ Seq(
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