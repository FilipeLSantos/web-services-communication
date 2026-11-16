package br.dev.filipesantos.viacepintegrationservice.api;

public record ViaCepResponse(
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    String localidade,
    String uf,
    String erro
) {}