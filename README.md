# LoginApp

Aplicação de autenticação multiempresa, na qual o primeiro usuário cadastrado será automaticamente definido como administrador responsável pela empresa, tendo a função de gerenciar os usuários, seus cadastros e suas permissões de acesso.

O administrador poderá conceder privilégios de forma granular, por meio de perfis de acesso predefinidos para cada aplicação ou por perfis personalizados, permitindo definir quais aplicações cada usuário poderá acessar e quais funcionalidades estarão disponíveis em cada uma delas.




## Tecnologias

- Java 17 e Spring Boot
- Spring Security e Spring Authorization Server
- OAuth 2.0 / OpenID Connect, com Authorization Code, PKCE e cliente BFF confidencial
- MySQL 8.4
- Redis 7
- Docker e Docker Compose

## Pré-requisitos

- Docker Engine com o plugin Docker Compose
- Git, para obter o código-fonte
- Um arquivo de chave PKCS#12 do servidor de autorização

## Configuração local

1. Copie o arquivo de exemplo para `.env`:

   ```sh
   cp .env.example .env
   ```

   No PowerShell, use:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Edite `.env` e defina valores próprios para estas variáveis:

   | Variável | Uso |
   | --- | --- |
   | `DB_USER` | Usuário da aplicação no MySQL |
   | `DB_PASSWORD` | Senha desse usuário |
   | `MYSQL_ROOT_PASSWORD` | Senha administrativa do MySQL |
   | `BFF_CLIENT_SECRET` | Segredo do cliente OAuth confidencial |
   | `BFF_CALLBACK_URI` | URI de callback registrada, por exemplo `http://localhost:9000/bff/callback` |
   | `BFF_AUTHORIZATION_URI` | Endpoint de autorização do servidor, por exemplo `http://localhost:9000/oauth2/authorize` |
   | `JWK_KEYSTORE_PASSWORD` | Senha do arquivo PKCS#12 |
   | `APP_PUBLIC_URL` | URL base pela qual o navegador acessa o backend |

   Para executar, use o endereço IP da máquina em `APP_PUBLIC_URL`, `BFF_CALLBACK_URI` e na URL do endpoint de autorização. O callback deve corresponder exatamente à URI registrada para o cliente OAuth. Não use `localhost` nessas URLs quando o navegador estiver em outra máquina: nesse caso, `localhost` aponta para a máquina do navegador.

3. Confirme que `mysql/init/` contém os scripts SQL de inicialização necessários. O MySQL executa esses scripts quando o volume de dados é criado pela primeira vez.

## Executar com Docker Compose

Na pasta que contém `compose.yaml`:

```sh
docker compose config --quiet
docker compose up -d --build
docker compose ps
```

No Alpine, acrescente `doas` a cada comando, se necessário:

```sh
doas docker compose config --quiet
doas docker compose up -d --build
doas docker compose ps
```

O MySQL e o Redis devem aparecer como `healthy`. O serviço `app` deve aparecer como `running`/`up`. Para acompanhar os logs:

```sh
docker compose logs -f app
docker compose logs -f mysql
docker compose logs -f redis
```

Para verificar a aplicação, acesse `http://localhost:9000` na própria máquina onde os containers estão executando. De outra máquina, substitua `localhost` pelo endereço IP da máquina, por exemplo `http://<IP-DA-VM>:9000`.

## Persistência e ciclo de vida

Os dados do MySQL e do Redis ficam nos volumes nomeados `mysql_data` e `redis_data`. Parar os serviços com `docker compose down` preserva esses volumes. O comando abaixo também apaga os volumes e, portanto, remove os dados persistidos; use-o somente se quiser reiniciar o ambiente do zero:

```sh
docker compose down -v
```

Os scripts de `mysql/init/` não são executados novamente em um volume MySQL já inicializado. Para aplicar alterações de esquema em um ambiente existente, use uma estratégia de migração de banco adequada.

## Estrutura relevante

```text
.
├── compose.yaml
├── Dockerfile
├── pom.xml
├── mysql/
│   └── init/       # scripts SQL de inicialização
├── secrets/        # chave local; não versionar
└── src/
    └── main/
```

