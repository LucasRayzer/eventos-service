# Título: Micro-Service de evento

**Status:** Aceito (Implementado)

## Contexto

O microsserviço `eventos-service` precisa proteger seus endpoints, garantindo que apenas usuários autenticados e com os papéis corretos possam executar ações específicas.

Este serviço não deve conter a lógica de gerenciamento de usuários, senhas ou papéis, pois essa responsabilidade é centralizada no `users-service`.

Surgem duas abordagens principais para proteger os endpoints:
1.  **Validação de Token Local:** O `eventos-service` receberia um token JWT, validaria sua assinatura e extrairia os papéis do token.
2.  **Delegação por Chamada de Serviço:** O `eventos-service` recebe um ID de usuário e, para cada requisição, pergunta ao `users-service` se aquele usuário é válido e quais são suas permissões.

## Decisão

Foi decidido implementar a abordagem de Delegação por Chamada de Serviço.

O fluxo de segurança opera da seguinte maneira:
1.  **Configuração de Segurança Nula:** A configuração do Spring Security no `eventos-service` é definida como `permitAll()`, permitindo que todas as requisições HTTP cheguem ao controlador.
2.  **ID via Query Param:** A aplicação cliente é responsável por enviar o `UUID` do usuário como um parâmetro de consulta na URL de cada endpoint protegido.
3.  **Chamada HTTP Síncrona:** O `EventoController` recebe esse ID e, antes de executar a lógica de negócio, utiliza o `UserClient` para fazer uma chamada HTTP síncrona ao `users-service`.
4.  **Autorização Manual:** O controlador inspeciona a resposta do `users-service`. Ele então aplica manualmente a lógica de autorização, verificando se o usuário existe e se o seu `tipo` é o esperado.
5.  **Comunicação Inter-Serviços:** O serviço também utiliza clientes HTTP (`RestTemplate`) para se comunicar com outros serviços de forma síncrona, como o `IngressosClient` ao realizar uma inscrição.

## Consequências

O que se torna mais fácil ou mais difícil como resultado dessa mudança?

* **Positivas:**
    * **Centralização da Lógica de Usuário:** O `users-service` permanece como a única fonte da verdade para dados de usuário, papéis e status. O `eventos-service` não precisa saber sobre senhas, tokens ou chaves secretas.
    * **Simplicidade no `eventos-service`:** Este serviço não precisa gerenciar dependências de segurança, chaves de assinatura de token, ou configurar um `SecurityFilterChain` complexo. A lógica de permissão é explícita no controlador.
    * **Atualização Imediata de Permissão:** Se um usuário tiver sua permissão alterada no `users-service`, a mudança é refletida imediatamente na próxima requisição ao `eventos-service`, pois os dados são buscados em tempo real.

* **Negativas e Riscos:**
    * **Latência e Performance:** Cada requisição protegida ao `eventos-service` gera no mínimo uma chamada de rede síncrona adicional ao `users-service`. Isso aumenta o tempo de resposta total.
    * **Acoplamento Forte:** O `eventos-service` fica fortemente acoplado à disponibilidade do `users-service`. Se o `users-service` ficar lento ou cair, todas as requisições protegidas do `eventos-service` falharão.
    * **Duplicação de Código:** A lógica de verificação de autenticação e autorização é repetida em quase todos os métodos do `EventoController`.
