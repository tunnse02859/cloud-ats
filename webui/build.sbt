name := "ats-cloud-webui"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
	cache,
	"org.ats" % "cloud-common" % "0.1-SNAPSHOT",
	"org.ats" % "cloud-cloudstack" % "0.1-SNAPSHOT",
	"org.ats" % "cloud-jenkins" % "0.1-SNAPSHOT",
	"org.apache.cloudstack" % "cloud-api" % "4.3.0",
	"org.mongodb" % "mongo-java-driver" % "2.10.1"
)     

resolvers += (
	"Local Maven Repository" at "file:///home/public/java/dependencies/repository"		
)

play.Project.playJavaSettings
