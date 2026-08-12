# Judi

Sistema de gestão jurídica com acompanhamento automatizado de processos. Permite cadastrar clientes e processos e sincroniza diariamente com a **API Pública do DataJud (CNJ)** para detectar novas movimentações e gerar alertas, com um dashboard de acompanhamento para a equipe.

## Stack

- **Backend**: Java 17, Spring Boot 3.2 (Web, Security, Data JPA, Validation, Mail, Cache, Redis, Actuator), MySQL.
- **Frontend**: HTML/CSS/JS estático servido pelo próprio Spring Boot (`src/main/resources/static`), Bootstrap 5 + jQuery — sem build step de front separado.
- **Autenticação**: sessão HTTP (Spring Security form login), 2FA por e-mail opcional, proteção contra força bruta.
- **Documentação de API**: springdoc-openapi — Swagger UI disponível em `/judi/swagger-ui.html` com a aplicação rodando.

## Configuração

O projeto lê variáveis de ambiente via [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) a partir de um arquivo `.env` na raiz (git-ignorado). Crie o seu com base nas chaves usadas em `src/main/resources/application.properties`:

```
APP_SERVER_ADDRESS=localhost
APP_SERVER_DB_PORT=3306
APP_SERVER_DB_NAME=judi
APP_DB_USERNAME=root
APP_DB_PASSWORD=
APP_DB_DRIVER=com.mysql.cj.jdbc.Driver
APP_BD_DDLAUTO=update
APP_PORT=8020
APP_CONTEXTPATH=/judi

APP_EMAIL_HOST=smtp.office365.com
APP_EMAIL_PORT=587
APP_EMAIL_USERNAME=
APP_EMAIL_PASSWORD=

# Integração DataJud (CNJ) — ver docs/DOMINIO-JURIDICO.md
DATAJUD_ENABLED=false
DATAJUD_API_KEY=
```

O schema do banco é criado/atualizado automaticamente pelo Hibernate (`ddl-auto=update`) — não há Flyway/Liquibase neste momento.

## Rodando localmente

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação sobe em `http://localhost:8020/judi` (ou nas portas/contexto definidos no `.env`). No primeiro acesso, `GET /judi/create` permite criar o primeiro funcionário (bootstrap do sistema).

## Estrutura do domínio

- **Funcionário / Cliente** — base de autenticação e cadastro (ver `docs/DOMINIO-JURIDICO.md` para detalhes de papéis/roles).
- **Processo / Movimentação / Alerta** — domínio jurídico central: processos monitorados, seu histórico de movimentações (manuais ou vindas do DataJud) e os alertas gerados automaticamente. Documentado em detalhe em [`docs/DOMINIO-JURIDICO.md`](docs/DOMINIO-JURIDICO.md).

## Documentação

- [`docs/DOMINIO-JURIDICO.md`](docs/DOMINIO-JURIDICO.md) — modelo de dados, endpoints REST e integração com o DataJud.
- Swagger UI (com a aplicação rodando): `http://localhost:8020/judi/swagger-ui.html`.
