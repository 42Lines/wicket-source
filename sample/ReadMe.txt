Customizations that are important to WicketSource working:

1) WicketApplication.java got an additional line in init(). 
   Suggested that you wrap it in an "if (isDevEnvironment()) { }" for performance.
   
        WicketSource.configure(this);
  
2) pom.xml got the addition of wicketsource:

		<dependency>
			<groupId>net.ftlines.wicketsource</groupId>
			<artifactId>wicketsource</artifactId>
			<version>1.5.1_03</version>
		</dependency>
		
3) pom.xml also got an additional repository until such time as this is hosted publicly.

		<repository>
			<id>central</id>
			<name>42lines central mirror</name>
			<url>https://repo.aws.42lines.net/content/groups/public</url>
		</repository>

4) The home page class and html were customized merely to give the user 
   something worthwhile to click on (tags with wicketsource="" attributes).
