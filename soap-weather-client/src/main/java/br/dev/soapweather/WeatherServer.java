package br.dev.soapweather;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherServer {

    private static final int PORTA = 8080;
    private static final Path FRONTEND = Path.of("frontend");

    public static void main(String[] args) throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);

        server.createContext("/api/weather", WeatherServer::consultarPrevisao);
        server.createContext("/", WeatherServer::servirArquivo);

        server.setExecutor(null);
        server.start();

        System.out.println("Servidor iniciado em http://localhost:" + PORTA);
        System.out.println("Pressione Ctrl+C para encerrar.");
    }

    private static void consultarPrevisao(HttpExchange exchange) throws IOException
    {
        if(!exchange.getRequestMethod().equalsIgnoreCase("GET"))
        {
            enviarResposta(exchange, 405, "application/json", "{\"erro\":\"Método não permitido\"}");
            return;
        }

        try
        {
            Map<String, String> parametros = lerParametros(exchange.getRequestURI().getRawQuery());

            if(!parametros.containsKey("latitude") || !parametros.containsKey("longitude"))
            {
                enviarResposta(exchange, 400, "application/json", "{\"erro\":\"Latitude e longitude são obrigatórias\"}");
                return;
            }

            double latitude = Double.parseDouble(parametros.get("latitude"));
            double longitude = Double.parseDouble(parametros.get("longitude"));

            SoapClient client = new SoapClient();
            String respostaSoap = client.consultar(latitude, longitude);

            WeatherParser parser = new WeatherParser();
            String dwml = parser.extrairDwml(respostaSoap);
            List<WeatherData> previsoes = parser.parseWeatherData(dwml);

            String json = criarJson(previsoes);

            enviarResposta(exchange, 200, "application/json; charset=UTF-8", json);
        }
        catch(Exception erro)
        {
            String mensagem = escaparJson(erro.getMessage() == null ? "Erro ao consultar previsão" : erro.getMessage());
            enviarResposta(exchange, 500, "application/json; charset=UTF-8", "{\"erro\":\"" + mensagem + "\"}");
        }
    }

    private static void servirArquivo(HttpExchange exchange) throws IOException
    {
        String caminho = exchange.getRequestURI().getPath();

        if(caminho.equals("/"))
        {
            caminho = "/index.html";
        }

        Path arquivo = FRONTEND.resolve(caminho.substring(1)).normalize();

        if(!arquivo.startsWith(FRONTEND) || !Files.exists(arquivo) || Files.isDirectory(arquivo))
        {
            enviarResposta(exchange, 404, "text/plain; charset=UTF-8", "Arquivo não encontrado");
            return;
        }

        byte[] conteudo = Files.readAllBytes(arquivo);
        String contentType = descobrirContentType(arquivo);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, conteudo.length);

        try(OutputStream output = exchange.getResponseBody())
        {
            output.write(conteudo);
        }
    }

    private static Map<String, String> lerParametros(String query)
    {
        Map<String, String> parametros = new HashMap<>();

        if(query == null || query.isBlank())
        {
            return parametros;
        }

        String[] pares = query.split("&");

        for(String par : pares)
        {
            String[] partes = par.split("=", 2);

            if(partes.length == 2)
            {
                String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
                String valor = URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
                parametros.put(chave, valor);
            }
        }

        return parametros;
    }

    private static String criarJson(List<WeatherData> previsoes)
    {
        StringBuilder json = new StringBuilder("[");

        for(int i = 0; i < previsoes.size(); i++)
        {
            WeatherData previsao = previsoes.get(i);

            if(i > 0)
            {
                json.append(",");
            }

            json.append("{");
            json.append("\"data\":\"").append(escaparJson(previsao.getData())).append("\",");
            json.append("\"maxima\":").append(valorOuNull(previsao.getTemperaturaMaxima())).append(",");
            json.append("\"minima\":").append(valorOuNull(previsao.getTemperaturaMinima())).append(",");

            json.append("\"horarios\":[");

            List<String> horarios = previsao.getHorarios();
            List<Integer> temperaturas = previsao.getTemperaturasHorarias();

            for(int j = 0; j < horarios.size(); j++)
            {
                if(j > 0)
                {
                    json.append(",");
                }

                String horario = horarios.get(j);
                Integer vento = previsao.getVentoNoHorario(horario);

                json.append("{");
                json.append("\"hora\":\"").append(escaparJson(horario)).append("\",");
                json.append("\"temperatura\":").append(temperaturas.get(j)).append(",");
                json.append("\"vento\":").append(valorOuNull(vento));
                json.append("}");
            }

            json.append("],");
            json.append("\"precipitacao\":[");

            List<String> periodos = previsao.getPeriodosPrecipitacao();
            List<Integer> probabilidades = previsao.getProbabilidadesPrecipitacao();

            for(int j = 0; j < periodos.size(); j++)
            {
                if(j > 0)
                {
                    json.append(",");
                }

                json.append("{");
                json.append("\"periodo\":\"").append(escaparJson(periodos.get(j))).append("\",");
                json.append("\"probabilidade\":").append(probabilidades.get(j));
                json.append("}");
            }

            json.append("]");
            json.append("}");
        }

        json.append("]");

        return json.toString();
    }

    private static String valorOuNull(Integer valor)
    {
        return valor == null ? "null" : valor.toString();
    }

    private static String escaparJson(String texto)
    {
        return texto
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private static String descobrirContentType(Path arquivo)
    {
        String nome = arquivo.getFileName().toString().toLowerCase();

        if(nome.endsWith(".html"))
        {
            return "text/html; charset=UTF-8";
        }

        if(nome.endsWith(".css"))
        {
            return "text/css; charset=UTF-8";
        }

        if(nome.endsWith(".js"))
        {
            return "application/javascript; charset=UTF-8";
        }

        return "application/octet-stream";
    }

    private static void enviarResposta(HttpExchange exchange, int status, String contentType, String resposta) throws IOException
    {
        byte[] conteudo = resposta.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, conteudo.length);

        try(OutputStream output = exchange.getResponseBody())
        {
            output.write(conteudo);
        }
    }
}
