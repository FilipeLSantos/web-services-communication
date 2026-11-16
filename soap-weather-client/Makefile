build:
	mvn compile

run:
	mvn compile
	java -cp target/classes br.dev.soapweather.Main

clean:
	mvn clean

web:
	mvn compile
	java --add-modules jdk.httpserver -cp target/classes br.dev.soapweather.WeatherServer