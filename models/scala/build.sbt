lazy val commonSettings = Seq(
	version := "1.0",
	organization := "com.example",
	scalaVersion := "2.11.4"
)

lazy val ZIscala = (project in file(".")).
	settings(commonSettings: _*).
	settings(
	// your settings here
	resolvers += "sonatype" at "https://oss.sonatype.org/content/repositories/releases/",
	libraryDependencies ++= Seq("com.mosesn" %% "pirate" % "0.1.2")
)

