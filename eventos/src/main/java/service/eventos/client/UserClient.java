package service.eventos.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class UserClient {

    private final RestTemplate rest;
    private final String baseUrl;
    
    public UserClient(@Value("${services.users.base-url}") String baseUrl) {
        this.rest = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    /**
     * Busca um usuário pelo ID.
     * GET {baseUrl}/usuarios/{userId}
     */
    public UserRespostaDto getUserById(UUID userId) {

        String url = baseUrl + "/usuarios/" + userId;

        try {
            return rest.getForObject(url, UserRespostaDto.class);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    @Data
    public static class UserRespostaDto {
        private UUID id;
        private String nome;
        private String tipo;
    }
}