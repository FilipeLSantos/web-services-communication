# Web Services Communication

Projeto desenvolvido para a disciplina de **Sistemas Distribuídos** com o objetivo de explorar e comparar, na prática, duas formas de comunicação por Web Services:

- **REST**, utilizando a API pública ViaCEP;
- **SOAP**, utilizando o Web Service meteorológico NDFD da NOAA / National Weather Service.

As duas aplicações são independentes e possuem interfaces web próprias.

---

## Autores

- **Robert Afonso Aquino**
- **Filipe Lopes dos Santos**

---

## Estrutura do repositório

```text
web-services-communication/
│
├── viacep-integration-service/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── soap-weather-client/
│   ├── frontend/
│   │   ├── index.html
│   │   ├── styles.css
│   │   └── script.js
│   │
│   ├── src/main/java/br/dev/soapweather/
│   │   ├── Main.java
│   │   ├── SoapClient.java
│   │   ├── WeatherData.java
│   │   ├── WeatherParser.java
│   │   └── WeatherServer.java
│   │
│   ├── pom.xml
│   └── Makefile
│
└── README.md
```

---

# 1. Aplicação REST — ViaCEP

## 1.1 Objetivo

A aplicação REST permite realizar consultas de endereços brasileiros utilizando o serviço público **ViaCEP**.

Foram implementadas duas formas de consulta:

- Busca de endereço a partir de um **CEP**;
- Busca de CEP a partir de **UF, cidade e logradouro**.

A aplicação também possui uma interface web para facilitar a realização das consultas.

---

## 1.2 Tecnologias utilizadas

- Java 21;
- Spring Boot;
- Spring Web;
- RestTemplate;
- Maven;
- REST;
- JSON;
- HTML;
- CSS;
- JavaScript.

---

## 1.3 API utilizada

**ViaCEP — Webservice CEP e IBGE**

Documentação oficial:

[https://viacep.com.br/](https://viacep.com.br/)

Exemplo de endpoint utilizado:

```text
https://viacep.com.br/ws/01001000/json/
```

Na implementação Java, o endereço é montado dinamicamente:

```java
String url = "https://viacep.com.br/ws/{cep}/json/";
```

A requisição é realizada utilizando `RestTemplate`:

```java
RestTemplate restTemplate = new RestTemplate();

return restTemplate.getForObject(
    url,
    ViaCepResponse.class,
    cep
);
```

---

## 1.4 Exemplo real de JSON recebido

Exemplo real retornado pela consulta ao CEP `01001000`:

```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "unidade": "",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP",
  "estado": "São Paulo",
  "regiao": "Sudeste",
  "ibge": "3550308",
  "gia": "1004",
  "ddd": "11",
  "siafi": "7107"
}
```

A aplicação mapeia os campos necessários para o record `ViaCepResponse`:

```java
public record ViaCepResponse(
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    String localidade,
    String uf,
    String erro
) {}
```

---

## 1.5 Endpoints da aplicação

### Consulta por CEP

```text
POST /client/cep
```

Exemplo de corpo:

```json
{
  "cep": "01001000"
}
```

### Consulta por endereço

```text
POST /client/address
```

Exemplo:

```json
{
  "uf": "SP",
  "cidade": "São Paulo",
  "logadouro": "Praça da Sé"
}
```

A aplicação consulta o ViaCEP e devolve ao frontend os dados tratados.

---

## 1.6 Executando a aplicação REST

Entre na pasta:

```bash
cd viacep-integration-service
```

No Linux:

```bash
./mvnw spring-boot:run
```

Também é possível utilizar Maven instalado no sistema:

```bash
mvn spring-boot:run
```

No Windows:

```text
mvnw.cmd spring-boot:run
```

Depois, acesse no navegador:

```text
http://localhost:8080
```

---

# 2. Aplicação SOAP — SOAP Weather

## 2.1 Objetivo

A aplicação SOAP consulta dados meteorológicos utilizando o Web Service **NDFD XML Web Service**, disponibilizado pela **NOAA / National Weather Service**.

O usuário informa uma latitude e longitude e a aplicação solicita informações meteorológicas para aquela localização.

Entre os dados processados estão:

- Temperatura máxima;
- Temperatura mínima;
- Temperatura por horário;
- Velocidade do vento;
- Probabilidade de precipitação.

Os dados retornados são organizados por dia e podem ser visualizados pelo terminal ou por uma interface web.

---

## 2.2 Tecnologias utilizadas

- Java 21;
- Maven;
- SOAP;
- WSDL;
- XML;
- DWML;
- HTTP;
- Java HttpClient;
- Java HttpServer;
- HTML;
- CSS;
- JavaScript.

---

## 2.3 Web Service utilizado

**NOAA / National Weather Service — National Digital Forecast Database XML Web Service**

Documentação oficial:

[https://digital.weather.gov/xml/](https://digital.weather.gov/xml/)

Servidor SOAP:

[https://digital.weather.gov/xml/SOAP_server/ndfdXMLserver.php](https://digital.weather.gov/xml/SOAP_server/ndfdXMLserver.php)

### WSDL utilizado

Link direto para o arquivo WSDL:

[https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl](https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl)

A operação utilizada pela aplicação é:

```text
NDFDgen
```

Essa operação recebe informações como:

- Latitude;
- Longitude;
- Tipo de produto;
- Formato XML;
- Sistema de unidades;
- Parâmetros meteorológicos desejados.

---

# 3. Funcionamento da aplicação SOAP

O fluxo principal da aplicação é:

```text
Usuário
   ↓
Frontend / Terminal
   ↓
Aplicação Java
   ↓
SoapClient
   ↓
Requisição SOAP
   ↓
NOAA / NDFD
   ↓
Resposta SOAP
   ↓
XMLOut
   ↓
DWML
   ↓
WeatherParser
   ↓
WeatherData
   ↓
Frontend / Terminal
```

---

## 3.1 SoapClient

A classe `SoapClient` é responsável pela comunicação com o Web Service.

Ela monta o envelope SOAP, envia a mensagem por HTTP e recebe a resposta XML.

O endpoint utilizado é:

```java
private static final String ENDPOINT =
    "https://digital.weather.gov/xml/SOAP_server/ndfdXMLserver.php";
```

A requisição é enviada utilizando o `HttpClient` do Java.

---

## 3.2 Exemplo de requisição SOAP

Exemplo baseado na consulta utilizada pela aplicação:

```xml
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
                <wgust>false</wgust>
            </weatherParameters>

        </ndf:NDFDgen>
    </soapenv:Body>

</soapenv:Envelope>
```

Os principais parâmetros solicitados pela aplicação são:

| Parâmetro | Informação                                |
| --------- | ----------------------------------------- |
| `maxt`    | Temperatura máxima                        |
| `mint`    | Temperatura mínima                        |
| `temp`    | Temperatura ao longo do tempo             |
| `pop12`   | Probabilidade de precipitação em 12 horas |
| `wspd`    | Velocidade do vento                       |

O valor:

```xml
<Unit>m</Unit>
```

indica que os dados devem ser retornados utilizando o sistema métrico.

---

# 4. Exemplo real do XML recebido pelo SOAP

A resposta da NOAA possui um envelope SOAP contendo o campo `XMLOut`.

Dentro de `XMLOut` é retornado outro documento XML no formato **DWML — Digital Weather Markup Language**.

Trecho real obtido durante os testes da aplicação:

```xml
<XMLOut>

    &lt;parameters applicable-location=&quot;point1&quot;&gt;

        &lt;temperature
            type=&quot;maximum&quot;
            units=&quot;Celsius&quot;
            time-layout=&quot;k-p24h-n7-1&quot;&gt;

            &lt;name&gt;Daily Maximum Temperature&lt;/name&gt;

            &lt;value&gt;14&lt;/value&gt;
            &lt;value&gt;15&lt;/value&gt;
            &lt;value&gt;17&lt;/value&gt;
            &lt;value&gt;17&lt;/value&gt;
            &lt;value&gt;19&lt;/value&gt;
            &lt;value&gt;18&lt;/value&gt;
            &lt;value&gt;18&lt;/value&gt;

        &lt;/temperature&gt;

        &lt;temperature
            type=&quot;minimum&quot;
            units=&quot;Celsius&quot;
            time-layout=&quot;k-p24h-n6-2&quot;&gt;

            &lt;name&gt;Daily Minimum Temperature&lt;/name&gt;

            &lt;value&gt;6&lt;/value&gt;
            &lt;value&gt;8&lt;/value&gt;
            &lt;value&gt;8&lt;/value&gt;
            &lt;value&gt;8&lt;/value&gt;
            &lt;value&gt;9&lt;/value&gt;
            &lt;value&gt;10&lt;/value&gt;

        &lt;/temperature&gt;

    &lt;/parameters&gt;

</XMLOut>
```

Os caracteres:

```text
&lt;  = <
&gt;  = >
```

indicam que o DWML está armazenado como conteúdo XML escapado dentro de `XMLOut`.

Por esse motivo, a aplicação realiza o processamento em duas etapas:

```text
Resposta SOAP
      ↓
Extrair XMLOut
      ↓
Obter DWML
      ↓
Interpretar novamente o XML
```

---

# 5. Processamento do DWML

A classe `WeatherParser` é responsável por interpretar os dados retornados.

Primeiro, o conteúdo de `XMLOut` é extraído:

```java
NodeList xmlOutNodes =
    soapDocument.getElementsByTagName("XMLOut");

return xmlOutNodes.item(0).getTextContent();
```

Em seguida, o DWML é processado novamente para identificar os dados meteorológicos.

---

## 5.1 Associação entre horários e valores

O DWML utiliza estruturas chamadas `time-layout` para relacionar os valores meteorológicos aos horários em que eles são válidos.

Um elemento pode possuir:

```xml
<temperature
    type="hourly"
    units="Celsius"
    time-layout="k-p1h-n61-4">
```

A chave:

```text
k-p1h-n61-4
```

referencia uma estrutura semelhante a:

```xml
<time-layout>

    <layout-key>
        k-p1h-n61-4
    </layout-key>

    <start-valid-time>
        2026-11-16T16:00:00-05:00
    </start-valid-time>

</time-layout>
```

O `WeatherParser` utiliza essa associação para relacionar cada horário ao respectivo valor de temperatura, vento ou precipitação.

---

# 6. Organização dos dados

Depois do processamento, as informações são armazenadas em objetos `WeatherData`.

Cada objeto representa a previsão de um dia e pode conter:

```text
WeatherData
├── Data
├── Temperatura máxima
├── Temperatura mínima
├── Temperaturas por horário
├── Velocidades do vento
└── Probabilidades de precipitação
```

Essa etapa separa o processamento do XML da apresentação dos dados ao usuário.

---

# 7. Executando a aplicação SOAP

Entre na pasta:

```bash
cd soap-weather-client
```

## 7.1 Versão pelo terminal

Execute:

```bash
make run
```

O programa solicitará:

```text
Digite a latitude:
40.7128

Digite a longitude:
-74.0060
```

Depois, a previsão é apresentada diretamente no terminal.

Exemplo de coordenadas para teste:

```text
Latitude: 40.7128
Longitude: -74.0060
```

Essas coordenadas correspondem à região de Nova York.

---

## 7.2 Interface web

Execute:

```bash
make web
```

O servidor será iniciado em:

```text
http://localhost:8080
```

Abra esse endereço no navegador.

Na interface web é possível informar latitude e longitude e selecionar:

```text
Consultar previsão
```

Os dados são então apresentados em cards e tabelas organizados por dia.

---

## 7.3 Execução sem Make

Caso o sistema não possua o `make`, também é possível executar manualmente.

### Terminal

```bash
mvn compile
java -cp target/classes br.dev.soapweather.Main
```

### Interface web

```bash
mvn compile
java --add-modules jdk.httpserver -cp target/classes br.dev.soapweather.WeatherServer
```

---

# 8. Observação sobre as portas

As duas aplicações utilizam a porta:

```text
8080
```

por padrão.

Portanto, se nenhuma configuração for alterada, execute a aplicação REST e a aplicação SOAP **separadamente**.

Caso seja necessário executar as duas ao mesmo tempo, uma das aplicações deverá ser configurada para utilizar outra porta.

---

# 9. REST × SOAP

Durante o desenvolvimento foi possível observar diferenças práticas entre as duas abordagens.

| Aspecto                | REST / ViaCEP       | SOAP / NOAA               |
| ---------------------- | ------------------- | ------------------------- |
| Formato principal      | JSON                | XML / DWML                |
| Comunicação            | Requisições HTTP    | Mensagens SOAP sobre HTTP |
| Contrato               | Documentação da API | WSDL                      |
| Requisição             | Mais direta         | Envelope SOAP estruturado |
| Tratamento             | Mapeamento de JSON  | Parsing de XML            |
| Aplicação desenvolvida | Consulta de CEP     | Previsão meteorológica    |

Na aplicação REST, a comunicação foi mais direta, utilizando requisições HTTP e respostas em JSON.

Na aplicação SOAP, foi necessário trabalhar com uma estrutura de mensagens mais formal, definida pelo WSDL, além de processar o envelope SOAP e o conteúdo DWML retornado pelo serviço.

Isso não significa que uma abordagem seja universalmente superior à outra. Cada tecnologia possui características adequadas a diferentes cenários e requisitos.

---

# 10. Principais dificuldades e aprendizados

Na aplicação REST, o principal fluxo consiste em realizar a requisição HTTP ao ViaCEP, mapear o JSON retornado para objetos Java e disponibilizar esses dados para a interface.

Na aplicação SOAP, uma das principais dificuldades foi o tratamento da estrutura XML retornada pelo Web Service.

A resposta possui uma estrutura SOAP externa e um segundo documento XML, em formato DWML, dentro do campo `XMLOut`.

Também foi necessário relacionar os dados meteorológicos às estruturas `time-layout`, responsáveis por indicar os horários correspondentes aos valores retornados.

O desenvolvimento das duas aplicações possibilitou acompanhar o fluxo completo de consumo de Web Services:

```text
Requisição
    ↓
Comunicação com serviço remoto
    ↓
Resposta
    ↓
Tratamento dos dados
    ↓
Conversão para objetos da aplicação
    ↓
Apresentação ao usuário
```

---

# 11. Documentações utilizadas

## REST — ViaCEP

Documentação oficial:

[https://viacep.com.br/](https://viacep.com.br/)

Exemplo de consulta:

```text
https://viacep.com.br/ws/01001000/json/
```

---

## SOAP — NOAA / NDFD

Documentação oficial do NDFD XML Web Service:

[https://digital.weather.gov/xml/](https://digital.weather.gov/xml/)

Servidor SOAP:

[https://digital.weather.gov/xml/SOAP_server/ndfdXMLserver.php](https://digital.weather.gov/xml/SOAP_server/ndfdXMLserver.php)

WSDL utilizado pela aplicação:

[https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl](https://digital.weather.gov/xml/wsdl/ndfdXML.wsdl)

#

---

## Considerações finais

O trabalho permitiu comparar duas formas diferentes de comunicação entre aplicações distribuídas.

A abordagem REST utilizada com o ViaCEP apresentou uma comunicação mais direta e simples de consumir, utilizando HTTP e JSON.

Já o Web Service SOAP da NOAA apresentou uma estrutura mais formal, baseada em mensagens XML e em um contrato definido pelo WSDL, exigindo um processamento maior dos dados retornados.

A implementação das duas abordagens permitiu compreender não apenas como realizar uma requisição a um serviço remoto, mas também todo o processo necessário para receber, interpretar, organizar e apresentar os dados ao usuário.
