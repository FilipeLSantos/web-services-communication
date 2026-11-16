package br.dev.filipesantos.viacepintegrationservice.client;

public record ClientRequest(
        String cep,
        String uf,
        String cidade,
        String logadouro
) {}