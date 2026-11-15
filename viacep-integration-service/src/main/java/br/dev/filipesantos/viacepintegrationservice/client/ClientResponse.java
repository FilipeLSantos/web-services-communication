package br.dev.filipesantos.viacepintegrationservice.client;

public record ClientResponse (
        String name,
        String cep,
        String street,
        String neighborhood,
        String city,
        String state
){}