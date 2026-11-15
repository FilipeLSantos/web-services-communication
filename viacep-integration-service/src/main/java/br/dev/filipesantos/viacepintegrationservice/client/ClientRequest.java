package br.dev.filipesantos.viacepintegrationservice.client;

public record ClientRequest(
        String name,
        String cep
) {}