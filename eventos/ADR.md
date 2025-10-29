# Título: Micro-Service de evento

**Status:** Aceito (Implementado)

## Contexto
O microsserviço `eventos-service` precisa proteger seus endpoints, garantindo que apenas usuários autenticados e com os papéis corretos possam executar ações.

Este serviço não deve conter a lógica de gerenciamento de tokens ou senhas, pois essa responsabilidade é centralizada no `autenticacao-service` e gerenciada pelo `API Gateway`.

## Decisão

Foi decidido implementar a abordagem de **Delegação Passiva ao API Gateway (Trusted Headers)**.

O fluxo de segurança opera da seguinte maneira:
1.  **Validação no Gateway:** O `API Gateway` intercepta 100% das requisições. Ele é responsável por validar o `Authorization: Bearer` token.
2.  **Injeção de Headers:** Após validar o token com sucesso, o Gateway remove o token e injeta cabeçalhos (`X-User-Id`, `X-User-Roles`) na requisição antes de encaminhá-la ao `eventos-service`.
3.  **Autorização Manual no Controlador:** O `EventoController` lê os headers `X-User-Id` e `X-User-Roles` em cada método protegido.
4.  **Verificação Local:** A lógica de autorização é aplicada manualmente no controlador, verificando se os headers existem e se contêm o `role` esperado.

## Consequências

O que se torna mais fácil ou mais difícil como resultado dessa mudança?

* **Positivas:**
  * **Centralização da Autenticação:** A lógica de validação de token JWT existe *apenas* no Gateway e no `autenticacao-service`.
  * **Simplicidade no `eventos-service`:** Este serviço não precisa gerenciar dependências de segurança (Spring Security, JWT) ou chaves secretas. A lógica de permissão é explícita no controlador.
  * **Desacoplamento da Lógica:** O `eventos-service` foca apenas na sua lógica de negócio, sem se preocupar em *como* o usuário foi validado.

* **Negativas e Riscos:**
  * **Latência Adicional:** A arquitetura do Gateway introduz uma chamada de rede extra (Gateway -> Auth-Service) *para cada* requisição autenticada, o que impacta a performance dos endpoints deste serviço.
  * **Segurança da Rede Interna:** A segurança deste microsserviço depende da topologia da rede. Se um ator malicioso conseguir acesso à rede interna e fazer uma chamada direta ao `eventos-service`.
  * **Duplicação de Código:** A lógica de verificação de headers é repetida em quase todos os métodos do `EventoController`.