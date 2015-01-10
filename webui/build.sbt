name := "cloud-ats-webui"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
	cache,
	"org.ats" % "cloud-common" % "0.1-SNAPSHOT",
	"org.ats" % "cloud-cloudstack" % "0.1-SNAPSHOT",
	"org.ats" % "cloud-jenkins" % "0.1-SNAPSHOT",
	"org.ats" % "cloud-users-mgt" % "0.1-SNAPSHOT",
	"org.ats" % "cloud-gitlab" % "0.1-SNAPSHOT",
	"org.ats" % "cloud-jmeter" % "0.1-SNAPSHOT",
	"org.apache.cloudstack" % "cloud-api" % "4.3.0",
	"org.mongodb" % "mongo-java-driver" % "2.12.4",
	"com.typesafe.akka" % "akka-actor_2.10" % "2.2.4"
)     

resolvers += (
	"Local Maven Repository" at "file://" + Path.userHome.absolutePath  + "/java/dependencies/repository"		
)

play.Project.playJavaSettings
