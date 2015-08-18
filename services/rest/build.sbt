name := """rest"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
	"org.ats" % "cloud-common" % "1.0.0",
	"org.ats" % "cloud-jenkins" % "1.0.0",
	"org.ats.services" % "database" % "1.0.0",
	"org.ats.services" % "organization" % "1.0.0",
	"org.ats.services" % "event" % "1.0.0",
	"org.ats.services" % "datadriven" % "1.0.0",
	"org.ats.services" % "keyword" % "1.0.0",
	"org.ats.services" % "performance" % "1.0.0",
	"org.ats.services" % "vmachine" % "1.0.0",
	"org.ats.services" % "generator" % "1.0.0",
	"org.ats.services" % "executor" % "1.0.0",
	"org.ats.services" % "report" % "1.0.0"
)


resolvers += (
	"Local Maven Repository" at "file://" + Path.userHome.absolutePath  + "/java/dependencies/repository"		
)

resolvers ++= Seq(
	Resolver.sonatypeRepo("snapshots"),
	Resolver.mavenLocal
)
