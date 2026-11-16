package br.dev.filipesantos.viacepintegrationservice.api;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Service
public class ViaCepService {

    public ViaCepResponse searchCEP(String cep){
        try{
            String url = "https://viacep.com.br/ws/{cep}/json/";
            RestTemplate restTemplate = new RestTemplate();

            return restTemplate.getForObject(url, ViaCepResponse.class, cep);
        }catch(HttpClientErrorException e){
            return null;
        }
    }

    public ViaCepResponse searchAddress(String uf, String cidade, String logadouro){
        try{
            String url = "https://viacep.com.br/ws/{uf}/{cidade}/{logadouro}/json/";
            RestTemplate restTemplate = new RestTemplate();
            ViaCepResponse[] endereco = restTemplate.getForObject(
                    url, ViaCepResponse[].class, uf, cidade, logadouro
            );

            if(endereco != null && endereco.length > 0){
                return endereco[0];
            }

            return null;
        }catch(Exception e){
            return null;
        }
    }
}
