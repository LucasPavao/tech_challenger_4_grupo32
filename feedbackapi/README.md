# feedbackapi

API de avaliações (feedback) do Tech Challenge FIAP — Fase 4 — Grupo 32.

## Pré-requisitos
- Java 21
- Docker com Docker Compose

## 1. Subir o banco (SQL Server)

```bash
docker compose up -d
```

Na pasta `feedbackapi`, isso sobe um SQL Server 2022 em `localhost:1433` e cria o banco `TechChallenge`.
A senha padrão do usuário `sa` é `Feedback@Local123`.

Para usar outra senha, passe a mesma variável para o compose e para a aplicação:

```bash
DB_PASSWORD='OutraSenha@123' docker compose up -d
```

## 2. Rodar a API

```bash
DB_PASSWORD='Feedback@Local123' ./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Para usar outra porta, adicione `SERVER_PORT=8090` antes do comando.

No IntelliJ, configure as variáveis em **Run → Edit Configurations → Environment variables**:
`DB_PASSWORD=Feedback@Local123`

## 3. Testar

```bash
curl -X POST http://localhost:8080/evaluations \
  -H 'Content-Type: application/json' \
  -d '{"descricao":"Aula excelente","nota":9}'

curl http://localhost:8080/evaluations
```

Também há uma collection do Postman em `postman/`.

## Variáveis de ambiente

| Variável      | Padrão                                                   | Obrigatória |
|---------------|----------------------------------------------------------|-------------|
| `DB_PASSWORD` | —                                                        | sim         |
| `DB_USERNAME` | `sa`                                                     | não         |
| `DB_URL`      | `jdbc:sqlserver://localhost:1433;databaseName=TechChallenge;...` | não |
| `SERVER_PORT` | `8080`                                                   | não         |

## Parar o banco

```bash
docker compose down      # para os containers e mantém os dados
docker compose down -v   # para os containers e apaga os dados
```

## Testes automatizados

```bash
./mvnw test
```

Os testes usam um banco H2 em memória, então não precisam do Docker.
