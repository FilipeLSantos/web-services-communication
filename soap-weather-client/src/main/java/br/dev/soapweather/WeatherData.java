package br.dev.soapweather;

import java.util.ArrayList;
import java.util.List;

public class WeatherData {

    private String data;

    private Integer temperaturaMaxima;
    private Integer temperaturaMinima;

    private List<String> horarios;
    private List<Integer> temperaturasHorarias;

    private List<String> horariosVento;
    private List<Integer> velocidadesVento;

    private List<String> periodosPrecipitacao;
    private List<Integer> probabilidadesPrecipitacao;

    public WeatherData(String data)
    {
        this.data = data;

        this.horarios = new ArrayList<>();
        this.temperaturasHorarias = new ArrayList<>();

        this.horariosVento = new ArrayList<>();
        this.velocidadesVento = new ArrayList<>();

        this.periodosPrecipitacao = new ArrayList<>();
        this.probabilidadesPrecipitacao = new ArrayList<>();
    }

    public String getData()
    {
        return data;
    }

    public Integer getTemperaturaMaxima()
    {
        return temperaturaMaxima;
    }

    public void setTemperaturaMaxima(Integer temperaturaMaxima)
    {
        this.temperaturaMaxima = temperaturaMaxima;
    }

    public Integer getTemperaturaMinima()
    {
        return temperaturaMinima;
    }

    public void setTemperaturaMinima(Integer temperaturaMinima)
    {
        this.temperaturaMinima = temperaturaMinima;
    }

    public List<String> getHorarios()
    {
        return horarios;
    }

    public List<Integer> getTemperaturasHorarias()
    {
        return temperaturasHorarias;
    }

    public void adicionarTemperaturaHoraria(
        String horario,
        int temperatura
    )
    {
        horarios.add(horario);
        temperaturasHorarias.add(temperatura);
    }

    public void adicionarVento(
        String horario,
        int velocidade
    )
    {
        horariosVento.add(horario);
        velocidadesVento.add(velocidade);
    }

    public Integer getVentoNoHorario(String horario)
    {
        for(int i = 0; i < horariosVento.size(); i++)
        {
            if(horariosVento.get(i).equals(horario))
            {
                return velocidadesVento.get(i);
            }
        }

        return null;
    }

    public void adicionarPrecipitacao(
        String periodo,
        int probabilidade
    )
    {
        periodosPrecipitacao.add(periodo);
        probabilidadesPrecipitacao.add(probabilidade);
    }

    public List<String> getPeriodosPrecipitacao()
    {
        return periodosPrecipitacao;
    }

    public List<Integer> getProbabilidadesPrecipitacao()
    {
        return probabilidadesPrecipitacao;
    }
}