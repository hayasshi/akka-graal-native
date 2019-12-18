name := "akka-graal-native"

organization := "com.github.vmencik"

version := "0.1"

scalaVersion := "2.13.1"

val akkaVersion = "2.5.25"
val akkaHttpVersion = "10.1.8"
val graalAkkaVersion = "0.5.0"
val circeVersion = "0.12.1"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.slf4j" % "slf4j-jdk14" % "1.7.26", // java.util.logging works mostly out-of-the-box with SubstrateVM

  "com.github.vmencik" %% "graal-akka-http" % graalAkkaVersion,
  "com.github.vmencik" %% "graal-akka-slf4j" % graalAkkaVersion,
  "com.oracle.substratevm" % "svm" % "19.1.1" % Provided,

  "de.heikoseeberger" %% "akka-http-circe" % "1.29.1",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.scalikejdbc" %% "scalikejdbc" % "3.4.0",
  "org.mariadb.jdbc" % "mariadb-java-client" % "2.5.2"
)

enablePlugins(GraalVMNativeImagePlugin)

graalVMNativeImageOptions ++= Seq(
  "-H:IncludeResources=.*\\.properties",
  "-H:DynamicProxyConfigurationFiles=" + baseDirectory.value / "graal" / "proxy-config.json",
  "-H:ReflectionConfigurationFiles=" + baseDirectory.value / "graal" / "reflectconf-jul.json",
  "-H:+ReportUnsupportedElementsAtRuntime",
  "--initialize-at-build-time",
  "--initialize-at-build-time=org.mariadb",
  "--initialize-at-run-time=" +
    "akka.protobuf.DescriptorProtos," +
    "com.typesafe.config.impl.ConfigImpl$EnvVariablesHolder," +
    "com.typesafe.config.impl.ConfigImpl$SystemPropertiesHolder," +
    "org.mariadb.jdbc.credential.aws," +
    "org.mariadb.jdbc.internal.failover.impl.MastersSlavesListener," +
    "org.mariadb.jdbc.internal.com.send.authentication.SendPamAuthPacket",
  "--no-fallback",
  "--allow-incomplete-classpath"
)
