package br.dev.soapweather;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception
    {
        Scanner scanner = new Scanner(System.in);

        SoapClient client = new SoapClient();
        WeatherParser parser = new WeatherParser();

        while(true)
        {
            System.out.println("\n========================================");
            System.out.println("NOVA CONSULTA");
            System.out.println("========================================");

            System.out.println("Digite a latitude: ");
            double latitude = scanner.nextDouble();

            System.out.println("Digite a longitude: ");
            double longitude = scanner.nextDouble();

            String resposta = client.consultar(latitude, longitude);

            String dwml = parser.extrairDwml(resposta);

            List<WeatherData> previsoes = parser.parseWeatherData(dwml);

            System.out.println("\n========================================");
            System.out.println("PREVISÃO METEOROLÓGICA");
            System.out.println("========================================");

            for(WeatherData previsao : previsoes)
            {
                System.out.println("\n----------------------------------------");

                System.out.println("Data: " + previsao.getData());

                System.out.println("----------------------------------------");

                if(previsao.getTemperaturaMaxima() != null)
                {
                    System.out.println("Máxima: " + previsao.getTemperaturaMaxima() + " °C");
                }

                if(previsao.getTemperaturaMinima() != null)
                {
                    System.out.println("Mínima: " + previsao.getTemperaturaMinima() + " °C");
                }

                List<String> horarios = previsao.getHorarios();

                List<Integer> temperaturas = previsao.getTemperaturasHorarias();

                if(!horarios.isEmpty())
                {
                    System.out.println("\nHora  | Temp. | Vento");

                    System.out.println("---------------------------");

                    for(int i = 0; i < horarios.size(); i++)
                    {
                        String horario = horarios.get(i);

                        Integer vento = previsao.getVentoNoHorario(horario);

                        String textoVento;

                        if(vento != null)
                        {
                            textoVento = vento + " m/s";
                        }
                        else
                        {
                            textoVento = "-";
                        }

                        System.out.printf("%5s | %4d°C | %s%n", horario, temperaturas.get(i), textoVento);
                    }
                }

                List<String> periodos = previsao.getPeriodosPrecipitacao();

                List<Integer> probabilidades = previsao.getProbabilidadesPrecipitacao();

                if(!periodos.isEmpty())
                {
                    System.out.println("\nProbabilidade de precipitação: ");

                    for(int i = 0; i < periodos.size(); i++)
                    {
                        System.out.println(periodos.get(i) + " -> " + probabilidades.get(i) + "%" );
                    }
                }
            }

            System.out.println("\n========================================");
            System.out.println("Deseja realizar outra consulta?");
            System.out.println("1 - Sim");
            System.out.println("0 - Sair");

            int opcao = scanner.nextInt();

            if(opcao == 0)
            {
                break;
            }
        }

        System.out.println("\nPrograma encerrado.");

        scanner.close();
    }
}