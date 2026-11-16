package br.dev.soapweather;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SoapClient {

    private static final String ENDPOINT = 
    "https://digital.weather.gov/xml/SOAP_server/ndfdXMLserver.php";
    
    public String consultar(double latitude, double longitude) throws Exception
    {
        String xml = """
        <soapenv:Envelope
            xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
            xmlns:ndf="https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl"
        >
            <soapenv:Header/>
            <soapenv:Body>
                <ndf:NDFDgen>
                    <latitude>%.4f</latitude>
                    <longitude>%.4f</longitude>
                    <product>time-series</product>
                    <XMLformat>DWML</XMLformat>
                    <startTime></startTime>
                    <endTime></endTime>
                    <Unit>m</Unit>
                    <weatherParameters>
                        <maxt>true</maxt>
                        <mint>true</mint>
                        <temp>true</temp>
                        <dew>false</dew>
                        <appt>false</appt>
                        <pop12>true</pop12>
                        <qpf>false</qpf>
                        <snow>false</snow>
                        <sky>false</sky>
                        <rh>false</rh>
                        <wspd>true</wspd>
                        <wdir>false</wdir>
                        <wx>false</wx>
                        <icons>false</icons>
                        <waveh>false</waveh>
                        <incw34>false</incw34>
                        <incw50>false</incw50>
                        <incw64>false</incw64>
                        <cumw34>false</cumw34>
                        <cumw50>false</cumw50>
                        <cumw64>false</cumw64>
                        <wgust>false</wgust>
                    </weatherParameters> 
                </ndf:NDFDgen>
            </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(latitude, longitude);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(ENDPOINT))
        .header("Content-Type", "text/xml; charset=utf-8")
        .header("SOAPAction",
        "https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl#NDFDgen"
)
        .POST(HttpRequest.BodyPublishers.ofString(xml))
        .build();

        HttpResponse<String> response;

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}