# organization-service

Serviço de organização do PulsePass: situação das empresas organizadoras na plataforma, subconta no Asaas e extrato do saldo.

## Subir localmente

Para subir o serviço só é necessário o Docker: a imagem compila a aplicação com o JDK que ela mesma traz. O Java 21 só é necessário para rodar o `./mvnw` (lint e testes) fora do container.

```bash
./infrastructure/service-start.sh [test|development]
./infrastructure/service-stop.sh [test|development]
```

O script cria a rede `pulse-pass` e o `infrastructure/.env` (a partir do `.env.example`) quando não existem, derruba o ambiente do mesmo modo e sobe o serviço. O modo padrão é `test`, também no `service-stop.sh`. Para cada par `<NAME>_TEST_URL` / `<NAME>_DEVELOPMENT_URL` do `.env`, o script exporta `<NAME>_URL` conforme o modo, então novas dependências externas não exigem mudar o script.

Modo `test`: o serviço é autossuficiente e sobe a API, um MySQL próprio e um MockServer no lugar do Asaas, no projeto `organization-service-test`. A API fica em `http://localhost:8082` (`API_PORT`), o MySQL em `localhost:3308` e o MockServer em `localhost:1082`. É o modo usado pela CI e pelos testes de integração. O `service-stop.sh test` remove containers, volumes e imagens locais desse modo.

Modo `development`: sobe só a API (container `organization-service-api`, projeto `organization-service`), em `http://localhost:8092` (`DEVELOPMENT_API_PORT`), apontando para o sandbox do Asaas e usando o MySQL compartilhado `pulse-pass-mysql`. Por isso o `pulse-pass-infrastructure` precisa estar de pé e saudável; se não estiver, o script termina com erro pedindo para subi-lo. No MySQL compartilhado o script cria, como root, o usuário do `.env` com permissão apenas no schema `organization` e aplica o `database.sql`. O `database.sql` é idempotente, então rodar o script de novo não duplica nada, mas também não recria o que já existe: mudanças em tabelas só aparecem no development depois de reiniciar a infraestrutura compartilhada (que apaga o MySQL) e subir o serviço de novo. O `service-stop.sh development` derruba só a API e não mexe no MySQL compartilhado.

Quem já tem um `infrastructure/.env` de uma versão anterior deve apagá-lo para que o script o recrie a partir do `.env.example`. Usuário, senha e schema do `.env` não podem conter aspas nem barra invertida, pois são interpolados no SQL do modo `development`.

Os dois modos podem rodar ao mesmo tempo. A rota interna `POST /organization/internal/organizations` exige o header `internal-token` com o valor de `INTERNAL_TOKEN`, que é o mesmo token para todas as aplicações do PulsePass.

## Lint e testes

```bash
./mvnw checkstyle:check        # Checkstyle
./mvnw test                    # testes unitários (relatório JaCoCo em target/site/jacoco)
./mvnw test -Pintegration      # testes de integração (exigem o compose no ar)
```
