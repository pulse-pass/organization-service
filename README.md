# organization-service

Serviço de organização do PulsePass: situação das empresas organizadoras na plataforma, subconta no Asaas e extrato do saldo.

## Subir localmente

Para subir o serviço só é necessário o Docker: a imagem compila a aplicação com o JDK que ela mesma traz. O Java 21 só é necessário para rodar o `./mvnw` (lint e testes) fora do container.

```bash
./infrastructure/service-start.sh [test|development]
./infrastructure/service-stop.sh
```

O script cria a rede `pulse-pass` e o `infrastructure/.env` (a partir do `.env.example`) quando não existem, derruba o que estiver no ar e sobe o serviço. O modo padrão é `test`, em que as dependências externas (hoje o Asaas) são atendidas pelo MockServer. No modo `development` elas apontam para os ambientes de sandbox. Para cada par `<NAME>_TEST_URL` / `<NAME>_DEVELOPMENT_URL` do `.env`, o script exporta `<NAME>_URL` conforme o modo, então novas dependências externas não exigem mudar o script. O `service-stop.sh` remove containers, volumes e imagens locais.

A API fica em `http://localhost:8082` (`API_PORT`), o MySQL em `localhost:3308` e, no modo `test`, o MockServer em `localhost:1082`. A rota interna `POST /internal/organizations` exige o header `internal-token` com o valor de `INTERNAL_TOKEN`, que é o mesmo token para todas as aplicações do PulsePass.

## Lint e testes

```bash
./mvnw checkstyle:check        # Checkstyle
./mvnw test                    # testes unitários (relatório JaCoCo em target/site/jacoco)
./mvnw test -Pintegration      # testes de integração (exigem o compose no ar)
```
