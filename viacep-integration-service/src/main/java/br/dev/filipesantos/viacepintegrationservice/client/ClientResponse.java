package br.dev.filipesantos.viacepintegrationservice.client;

public record ClientResponse (
        String cep,
        String logadouro,
        String bairro,
        String cidade,
        String estado
){}