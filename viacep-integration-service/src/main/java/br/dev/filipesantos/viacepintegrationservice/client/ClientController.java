package br.dev.filipesantos.viacepintegrationservice.client;

import br.dev.filipesantos.viacepintegrationservice.api.ViaCepService;
import br.dev.filipesantos.viacepintegrationservice.api.ViaCepResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client")
public class ClientController {
    private final ViaCepService viaCepService;

    public ClientController(ViaCepService viaCepService) {
        this.viaCepService = viaCepService;
    }

    @PostMapping("/cep")
    public ResponseEntity<?> searchCEP(@RequestBody ClientRequest request) {
        ViaCepResponse endereco = viaCepService.searchCEP(request.cep());

        if(endereco == null || "true".equalsIgnoreCase(endereco.erro())){
            return ResponseEntity.badRequest().body(Map.of("message", "CEP inválido ou não encontrado"));
        }

        ClientResponse response = new ClientResponse(
                endereco.cep(),
                endereco.logradouro(),
                endereco.bairro(),
                endereco.localidade(),
                endereco.uf()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/address")
    public ResponseEntity<?> searchAddress(@RequestBody ClientRequest request) {
        ViaCepResponse endereco = viaCepService.searchAddress(
                request.uf(),
                request.cidade(),
                request.logadouro()
        );

        if(endereco == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Endereço não encontrado"));

        ClientResponse response = new ClientResponse(
                endereco.cep(),
                endereco.logradouro(),
                endereco.bairro(),
                endereco.localidade(),
                endereco.uf()
        );

        return ResponseEntity.ok(response);
    }
}