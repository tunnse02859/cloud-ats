name := """rest"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
	"org.ats" % "cloud-common" % "1.0.0-Alpha-1-SNAPSHOT",
	"org.ats.services" % "database" % "1.0.0-Alpha-1-SNAPSHOT",
	"org.ats.services" % "organization" % "1.0.0-Alpha-1-SNAPSHOT",
	"org.ats.services" % "event" % "1.0.0-Alpha-1-SNAPSHOT",
	"org.ats.services" % "datadriven" % "1.0.0-Alpha-1-SNAPSHOT",
	"org.ats.services" % "functional" % "1.0.0-Alpha-1-SNAPSHOT"
)


resolvers += (
	"Local Maven Repository" at "file://" + Path.userHome.absolutePath  + "/java/dependencies/repository"		
)

resolvers ++= Seq(
	Resolver.sonatypeRepo("snapshots"),
	Resolver.mavenLocal
)
