package br.dev.soapweather;

import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class WeatherParser {

    private Document parseXml(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(
            new InputSource(new StringReader(xml))
        );
    }

    public String extrairDwml(String respostaSoap) throws Exception
    {
        Document soapDocument = parseXml(respostaSoap);

        NodeList xmlOutNodes = soapDocument.getElementsByTagName("XMLOut");

        return xmlOutNodes.item(0).getTextContent();
    }

    public List<WeatherData> parseWeatherData(String dwml) throws Exception
    {
        List<WeatherData> previsoes = new ArrayList<>();

        Document dwmlDocument = parseXml(dwml);

        lerTemperaturasHorarias(dwmlDocument, previsoes);
        lerTemperaturasMaximas(dwmlDocument, previsoes);
        lerTemperaturasMinimas(dwmlDocument, previsoes);
        lerVentos(dwmlDocument, previsoes);
        lerPrecipitacao(dwmlDocument, previsoes);

        return previsoes;
    }

    private void lerTemperaturasHorarias(Document dwmlDocument, List<WeatherData> previsoes) throws Exception
    {
        NodeList temperaturas = dwmlDocument.getElementsByTagName("temperature");

        for(int i = 0; i < temperaturas.getLength(); i++)
        {
            String tipo = temperaturas.item(i)
                .getAttributes()
                .getNamedItem("type")
                .getNodeValue();

            if(tipo.equals("hourly"))
            {
                String timeLayout = temperaturas.item(i)
                    .getAttributes()
                    .getNamedItem("time-layout")
                    .getNodeValue();

                NodeList valores = temperaturas.item(i).getChildNodes();
                NodeList layouts = dwmlDocument.getElementsByTagName("time-layout");

                for(int j = 0; j < layouts.getLength(); j++)
                {
                    NodeList filhosLayout = layouts.item(j).getChildNodes();

                    String chaveLayout = null;

                    for(int k = 0; k < filhosLayout.getLength(); k++)
                    {
                        if(filhosLayout.item(k).getNodeName().equals("layout-key"))
                        {
                            chaveLayout = filhosLayout.item(k).getTextContent();
                        }
                    }

                    if(timeLayout.equals(chaveLayout))
                    {
                        int indiceValor = 0;

                        for(int k = 0; k < filhosLayout.getLength(); k++)
                        {
                            if(filhosLayout.item(k).getNodeName().equals("start-valid-time"))
                            {
                                String horario = filhosLayout.item(k).getTextContent();

                                OffsetDateTime dataHora = OffsetDateTime.parse(horario);

                                String data = dataHora.format(DateTimeFormatter.ofPattern("dd/MM"));
                                String hora = dataHora.format(DateTimeFormatter.ofPattern("HH:mm"));

                                while(indiceValor < valores.getLength())
                                {
                                    if(valores.item(indiceValor).getNodeName().equals("value"))
                                    {
                                        String texto = valores.item(indiceValor).getTextContent();

                                        if(!texto.isBlank())
                                        {
                                            int temperatura = Integer.parseInt(texto);

                                            WeatherData previsao = buscarOuCriarDia(previsoes, data);

                                            previsao.adicionarTemperaturaHoraria(hora, temperatura);
                                        }

                                        indiceValor++;
                                        break;
                                    }

                                    indiceValor++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void lerTemperaturasMaximas(Document dwmlDocument, List<WeatherData> previsoes) throws Exception
    {
        NodeList temperaturas = dwmlDocument.getElementsByTagName("temperature");

        for(int i = 0; i < temperaturas.getLength(); i++)
        {
            String tipo = temperaturas.item(i)
                .getAttributes()
                .getNamedItem("type")
                .getNodeValue();

            if(tipo.equals("maximum"))
            {
                String timeLayout = temperaturas.item(i)
                    .getAttributes()
                    .getNamedItem("time-layout")
                    .getNodeValue();

                NodeList valores = temperaturas.item(i).getChildNodes();
                NodeList layouts = dwmlDocument.getElementsByTagName("time-layout");

                for(int j = 0; j < layouts.getLength(); j++)
                {
                    NodeList filhosLayout = layouts.item(j).getChildNodes();

                    String chaveLayout = null;

                    for(int k = 0; k < filhosLayout.getLength(); k++)
                    {
                        if(filhosLayout.item(k).getNodeName().equals("layout-key"))
                        {
                            chaveLayout = filhosLayout.item(k).getTextContent();
                        }
                    }

                    if(timeLayout.equals(chaveLayout))
                    {
                        int indiceValor = 0;

                        for(int k = 0; k < filhosLayout.getLength(); k++)
                        {
                            if(filhosLayout.item(k).getNodeName().equals("start-valid-time"))
                            {
                                String horario = filhosLayout.item(k).getTextContent();

                                OffsetDateTime dataHora = OffsetDateTime.parse(horario);
                                String data = dataHora.format(DateTimeFormatter.ofPattern("dd/MM"));

                                while(indiceValor < valores.getLength())
                                {
                                    if(valores.item(indiceValor).getNodeName().equals("value"))
                                    {
                                        String texto = valores.item(indiceValor).getTextContent();

                                        if(!texto.isBlank())
                                        {
                                            int temperatura = Integer.parseInt(texto);

                                            WeatherData previsao = buscarOuCriarDia(previsoes, data);

                                            previsao.setTemperaturaMaxima(temperatura);
                                        }

                                        indiceValor++;
                                        break;
                                    }

                                    indiceValor++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void lerTemperaturasMinimas(Document dwmlDocument, List<WeatherData> previsoes) throws Exception
    {
        NodeList temperaturas = dwmlDocument.getElementsByTagName("temperature");

        for(int i = 0; i < temperaturas.getLength(); i++)
        {
            String tipo = temperaturas.item(i)
                .getAttributes()
                .getNamedItem("type")
                .getNodeValue();

            if(tipo.equals("minimum"))
            {
                String timeLayout = temperaturas.item(i)
                    .getAttributes()
                    .getNamedItem("time-layout")
                    .getNodeValue();

                NodeList valores = temperaturas.item(i).getChildNodes();
                NodeList layouts = dwmlDocument.getElementsByTagName("time-layout");

                for(int j = 0; j < layouts.getLength(); j++)
                {
                    NodeList filhosLayout = layouts.item(j).getChildNodes();

                    String chaveLayout = null;

                    for(int k = 0; k < filhosLayout.getLength(); k++)
                    {
                        if(filhosLayout.item(k).getNodeName().equals("layout-key"))
                        {
                            chaveLayout = filhosLayout.item(k).getTextContent();
                        }
                    }

                    if(timeLayout.equals(chaveLayout))
                    {
                        int indiceValor = 0;

                        for(int k = 0; k < filhosLayout.getLength(); k++)
                        {
                            if(filhosLayout.item(k).getNodeName().equals("start-valid-time"))
                            {
                                String horario = filhosLayout.item(k).getTextContent();

                                OffsetDateTime dataHora = OffsetDateTime.parse(horario);
                                String data = dataHora.format(DateTimeFormatter.ofPattern("dd/MM"));

                                while(indiceValor < valores.getLength())
                                {
                                    if(valores.item(indiceValor).getNodeName().equals("value"))
                                    {
                                        String texto = valores.item(indiceValor).getTextContent();

                                        if(!texto.isBlank())
                                        {
                                            int temperatura = Integer.parseInt(texto);

                                            WeatherData previsao = buscarOuCriarDia(previsoes, data);

                                            previsao.setTemperaturaMinima(temperatura);
                                        }

                                        indiceValor++;
                                        break;
                                    }

                                    indiceValor++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void lerVentos(Document dwmlDocument, List<WeatherData> previsoes) throws Exception
    {
        NodeList ventos = dwmlDocument.getElementsByTagName("wind-speed");

        for(int i = 0; i < ventos.getLength(); i++)
        {
            String tipo = ventos.item(i)
                .getAttributes()
                .getNamedItem("type")
                .getNodeValue();

            if(tipo.equals("sustained"))
            {
                String timeLayout = ventos.item(i)
                    .getAttributes()
                    .getNamedItem("time-layout")
                    .getNodeValue();

                NodeList valores = ventos.item(i).getChildNodes();
                NodeList layouts = dwmlDocument.getElementsByTagName("time-layout");

                for(int j = 0; j < layouts.getLength(); j++)
                {
                    NodeList filhosLayout = layouts.item(j).getChildNodes();

                    String chaveLayout = null;

                    for(int k = 0; k < filhosLayout.getLength(); k++)
                    {
                        if(filhosLayout.item(k).getNodeName().equals("layout-key"))
                        {
                            chaveLayout = filhosLayout.item(k).getTextContent();
                        }
                    }

                    if(timeLayout.equals(chaveLayout))
                    {
                        int indiceValor = 0;

                        for(int k = 0; k < filhosLayout.getLength(); k++)
                        {
                            if(filhosLayout.item(k).getNodeName().equals("start-valid-time"))
                            {
                                String horario = filhosLayout.item(k).getTextContent();

                                OffsetDateTime dataHora = OffsetDateTime.parse(horario);

                                String data = dataHora.format(DateTimeFormatter.ofPattern("dd/MM"));
                                String hora = dataHora.format(DateTimeFormatter.ofPattern("HH:mm"));

                                while(indiceValor < valores.getLength())
                                {
                                    if(valores.item(indiceValor).getNodeName().equals("value"))
                                    {
                                        String texto = valores.item(indiceValor).getTextContent();

                                        if(!texto.isBlank())
                                        {
                                            int velocidade = Integer.parseInt(texto);

                                            WeatherData previsao = buscarOuCriarDia(previsoes, data);

                                            previsao.adicionarVento(hora, velocidade);
                                        }

                                        indiceValor++;
                                        break;
                                    }

                                    indiceValor++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void lerPrecipitacao(Document dwmlDocument, List<WeatherData> previsoes) throws Exception
    {
        NodeList precipitacoes = dwmlDocument.getElementsByTagName("probability-of-precipitation");

        for(int i = 0; i < precipitacoes.getLength(); i++)
        {
            String tipo = precipitacoes.item(i)
                .getAttributes()
                .getNamedItem("type")
                .getNodeValue();

            if(tipo.equals("12 hour"))
            {
                String timeLayout = precipitacoes.item(i)
                    .getAttributes()
                    .getNamedItem("time-layout")
                    .getNodeValue();

                NodeList valores = precipitacoes.item(i).getChildNodes();
                NodeList layouts = dwmlDocument.getElementsByTagName("time-layout");

                for(int j = 0; j < layouts.getLength(); j++)
                {
                    NodeList filhosLayout = layouts.item(j).getChildNodes();

                    String chaveLayout = null;

                    for(int k = 0; k < filhosLayout.getLength(); k++)
                    {
                        if(filhosLayout.item(k).getNodeName().equals("layout-key"))
                        {
                            chaveLayout = filhosLayout.item(k).getTextContent();
                        }
                    }

                    if(timeLayout.equals(chaveLayout))
                    {
                        List<String> inicios = new ArrayList<>();
                        List<String> fins = new ArrayList<>();

                        for(int k = 0; k < filhosLayout.getLength(); k++)
                        {
                            if(filhosLayout.item(k).getNodeName().equals("start-valid-time"))
                            {
                                inicios.add(filhosLayout.item(k).getTextContent());
                            }

                            if(filhosLayout.item(k).getNodeName().equals("end-valid-time"))
                            {
                                fins.add(filhosLayout.item(k).getTextContent());
                            }
                        }

                        List<Integer> probabilidades = new ArrayList<>();

                        for(int k = 0; k < valores.getLength(); k++)
                        {
                            if(valores.item(k).getNodeName().equals("value"))
                            {
                                String texto = valores.item(k).getTextContent();

                                if(!texto.isBlank())
                                {
                                    probabilidades.add(Integer.parseInt(texto));
                                }
                            }
                        }

                        int quantidade = Math.min(inicios.size(), probabilidades.size());

                        for(int k = 0; k < quantidade; k++)
                        {
                            OffsetDateTime inicio = OffsetDateTime.parse(inicios.get(k));

                            String data = inicio.format(DateTimeFormatter.ofPattern("dd/MM"));
                            String inicioFormatado = inicio.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));

                            String periodo = inicioFormatado;

                            if(k < fins.size())
                            {
                                OffsetDateTime fim = OffsetDateTime.parse(fins.get(k));
                                String fimFormatado = fim.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));

                                periodo = inicioFormatado + " - " + fimFormatado;
                            }

                            WeatherData previsao = buscarOuCriarDia(previsoes, data);

                            previsao.adicionarPrecipitacao(periodo, probabilidades.get(k));
                        }
                    }
                }
            }
        }
    }

    private WeatherData buscarOuCriarDia(List<WeatherData> previsoes, String data)
    {
        for(WeatherData previsao : previsoes)
        {
            if(previsao.getData().equals(data))
            {
                return previsao;
            }
        }

        WeatherData novaPrevisao = new WeatherData(data);

        previsoes.add(novaPrevisao);

        return novaPrevisao;
    }
}