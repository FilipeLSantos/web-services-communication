SOAP Weather

Aplicação desenvolvida para a disciplina de Sistemas Distribuídos, com o objetivo de demonstrar o consumo de um Web Service SOAP público.

O projeto utiliza o serviço meteorológico da NOAA / National Weather Service, enviando requisições SOAP a partir de coordenadas geográficas e processando a resposta em XML/DWML.

Além da versão executada pelo terminal, o projeto possui uma interface web simples para visualização dos dados meteorológicos.

Funcionalidades

A aplicação permite informar uma latitude e longitude localizadas nos Estados Unidos e consultar informações meteorológicas como:

Temperatura máxima;

Temperatura mínima;

Temperatura por horário;

Velocidade do vento;

Probabilidade de precipitação.

Os dados retornados pelo Web Service são processados e organizados por dia.

Tecnologias utilizadas

Java 21;

Maven;

SOAP;

XML;

DWML;

HTTP;

HTML;

CSS;

JavaScript.

Web Service utilizado

O projeto utiliza o Web Service SOAP da NOAA – National Oceanic and Atmospheric Administration.

Endpoint SOAP

https://digital.weather.gov/xml/SOAP_server/ndfdXMLserver.php

WSDL

https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl

A operação utilizada pelo projeto é:

NDFDgen

Essa operação permite realizar consultas meteorológicas utilizando latitude e longitude.

Estrutura do projeto

soap-weather-client/
│
├── frontend/
│ ├── index.html
│ ├── styles.css
│ └── script.js
│
├── src/main/java/br/dev/soapweather/
│ ├── Main.java
│ ├── SoapClient.java
│ ├── WeatherParser.java
│ ├── WeatherData.java
│ └── WeatherServer.java
│
├── pom.xml
├── Makefile
└── README.md

Principais classes

SoapClient

Responsável pela comunicação com o Web Service SOAP da NOAA. A classe monta a requisição XML, envia a mensagem utilizando HTTP e recebe a resposta SOAP.

WeatherParser

Responsável pelo processamento da resposta XML. O parser extrai o conteúdo DWML retornado pelo serviço e identifica informações como temperaturas, vento e probabilidade de precipitação.

WeatherData

Classe utilizada para organizar os dados meteorológicos obtidos pelo parser.

Cada objeto representa os dados associados a um determinado dia.

Main

Permite utilizar o projeto diretamente pelo terminal.

WeatherServer

Servidor HTTP utilizado para integrar o backend Java com a interface web.

O servidor recebe latitude e longitude pelo frontend, realiza a consulta SOAP e retorna os dados processados em formato JSON.

Fluxo da aplicação

A versão web utiliza o seguinte fluxo:

Usuário
↓
Frontend HTML/CSS/JavaScript
↓ HTTP
WeatherServer
↓
SoapClient
↓ SOAP/XML
Web Service NOAA
↓
Resposta SOAP
↓
WeatherParser
↓
WeatherData
↓
JSON
↓
Frontend

Dessa forma, o Web Service SOAP continua sendo responsável pelo fornecimento dos dados meteorológicos, enquanto a interface web apenas facilita a visualização das informações.

Exemplo de requisição SOAP

A aplicação envia uma requisição semelhante à seguinte:

<soapenv:Envelope
xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
xmlns:ndf="https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl">

    <soapenv:Header/>

    <soapenv:Body>
        <ndf:NDFDgen>

            <latitude>40.7128</latitude>
            <longitude>-74.0060</longitude>

            <product>time-series</product>

            <XMLformat>DWML</XMLformat>

            <startTime></startTime>
            <endTime></endTime>

            <Unit>m</Unit>

            <weatherParameters>
                <maxt>true</maxt>
                <mint>true</mint>
                <temp>true</temp>
                <pop12>true</pop12>
                <wspd>true</wspd>
            </weatherParameters>

        </ndf:NDFDgen>
    </soapenv:Body>

</soapenv:Envelope>

No projeto completo, outros parâmetros definidos pelo serviço também são enviados com valor false.

Exemplo de resposta

A resposta SOAP contém um campo chamado XMLOut, que armazena os dados meteorológicos no formato DWML.

Exemplo simplificado:

<soapenv:Envelope>
<soapenv:Body>
<NDFDgenResponse>

            <XMLOut>

                &lt;dwml&gt;

                    &lt;temperature
                        type="maximum"
                        units="Celsius"&gt;

                        &lt;value&gt;13&lt;/value&gt;
                        &lt;value&gt;14&lt;/value&gt;

                    &lt;/temperature&gt;

                &lt;/dwml&gt;

            </XMLOut>

        </NDFDgenResponse>
    </soapenv:Body>

</soapenv:Envelope>

O conteúdo existente em XMLOut é extraído e posteriormente processado pelo WeatherParser.

Associação entre horários e valores

O DWML utiliza estruturas chamadas time-layout.

Os elementos meteorológicos possuem um atributo como:

<temperature
    type="hourly"
    time-layout="k-p1h-n61-4">

Esse valor referencia um bloco:

<time-layout>

    <layout-key>
        k-p1h-n61-4
    </layout-key>

    <start-valid-time>
        2026-11-16T16:00:00-05:00
    </start-valid-time>

</time-layout>

O parser utiliza essa relação para associar cada horário ao respectivo valor meteorológico.

Executando pelo terminal

Na pasta principal do projeto:

make run

O programa solicitará:

Digite a latitude:
40.7128

Digite a longitude:
-74.0060

Em seguida serão apresentados os dados meteorológicos obtidos pelo Web Service.

Executando a interface web

Utilize:

make web

O servidor será iniciado localmente.

Depois acesse no navegador:

http://localhost:8080

Na interface, basta informar a latitude e longitude e selecionar:

Consultar previsão

Exemplo de coordenadas para teste

Nova York:

Latitude: 40.7128
Longitude: -74.0060

O serviço da NOAA utilizado neste projeto atende coordenadas localizadas nos Estados Unidos.

Considerações

Durante o desenvolvimento, uma das principais etapas foi o tratamento da resposta retornada pelo Web Service.

A resposta SOAP possui uma estrutura XML externa e, dentro do campo XMLOut, contém outro documento XML no formato DWML.

Por esse motivo, o projeto inicialmente processa o envelope SOAP para extrair o DWML e posteriormente realiza um novo processamento para obter os dados meteorológicos.

Também foi necessário relacionar os elementos meteorológicos aos seus respectivos time-layout, permitindo associar corretamente horários e valores retornados pelo serviço.

Autores

Robert Afonso Aquino
[Nome do colega, se necessário]

Curso de Ciência da Computação
IFNMG
