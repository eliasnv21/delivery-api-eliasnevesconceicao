# delivery-api-eliasnevesconceicao

Sistema de delivery desenvolvido com Spring Boot e Java 21, focado em robustez, segurança por Role (JWT) e testes em camadas.

---

## 🚀 Tecnologias
- **Java 21 LTS** (Versão mais recente com Virtual Threads)
- **Spring Boot 3.3.5**
- **Spring Security + JWT** (Autenticação e Autorização stateless)
- **Spring Data JPA** (Persistência de dados)
- **H2 Database** (Banco de dados em memória/arquivo)
- **SpringDoc OpenAPI 3** (Swagger para documentação viva)
- **ModelMapper** (Mapeamento de objetos DTO <-> Entity)
- **Cache Local** (`ConcurrentMapCache`)
- **Docker & Docker Compose** (Containerização com persistência de dados)

---

## ⚡ Recursos Modernos Utilizados
- **Java 21:** Uso de Records, Text Blocks, Pattern Matching e Virtual Threads.
- **Arquitetura em Camadas:** Controller, Service, Repository e Entity bem definidos.
- **Pattern DTO:** Separação entre modelo de domínio e dados de transferência (Request/Response).
- **Global Exception Handling:** Tratamento centralizado de erros com respostas JSON padronizadas (RFC 7807).
- **Segurança IDOR:** Implementação de validação na camada Service (`SecurityUtils`) para garantir que o cliente só acesse seus próprios dados (Busca por ID) e que o restaurante só gerencie seus próprios produtos.

---

## 📖 Documentação da API (Swagger)
A documentação completa e interativa dos endpoints está disponível via Swagger UI.
Após iniciar a aplicação, acesse:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### Fluxo de Autenticação

A API é protegida por **JWT** e exige o Token na maioria das rotas.

1.  **Faça Login:** Use `POST /auth/login` com email/senha para obter o token (utilize os usuários do `data.sql`).
2.  **Autorize no Swagger:** Clique no botão **Authorize**, cole o Token (apenas o código, sem "Bearer") e valide.
3.  **Acesso por Role:**
    * **CLIENTE:** Pode criar pedidos, listar restaurantes e ver seus próprios dados/pedidos.
    * **RESTAURANTE:** Pode cadastrar/atualizar produtos, atualizar status de pedidos e ver seus relatórios de vendas.
    * **ADMIN:** Acesso total a todas as listagens e cadastros.

### Comportamento por Role
* **CLIENTE:**
    * Pode listar restaurantes e produtos imediatamente.
    * Ao criar o **primeiro pedido**, o sistema gera automaticamente o perfil de cliente vinculado.
* **RESTAURANTE:**
    * Após o login, deve obrigatoriamente usar `POST /restaurantes` para cadastrar os dados da loja.
    * Gerencia produtos e atualiza status dos pedidos recebidos.
* **ADMIN:** Acesso total a todas as listagens e cadastros.

---

## 🐳 Execução com Containers
### Pré-requisitos
* Docker Desktop ou Podman instalado.

### Como rodar (Docker)
Para iniciar a aplicação e todos os serviços necessários: docker-compose up --build 
Para encerrar a execução do container: docker-compose down

💾 Persistência de Dados
O projeto está configurado com Volumes do Docker. Isso significa que o banco de dados H2 (configurado para salvar em arquivo) persiste os dados na pasta ./dados_banco do host, garantindo que as informações não sejam perdidas ao reiniciar o container.

**OBS:** Caso vá finalize o container e, ao iniciar novamente, pretenda repetir métodos POST recomenda-se apagar a pasta dados_banco para que os dados inseridos no POST anterior a finalização do container sejam eliminados do banco de dados.

---

## 🏃‍♂️ Como executar (Sem Docker)
1. **Pré-requisitos:** JDK 21 e Maven instalados
2. Clone o repositório
3. Execute: `./mvnw spring-boot:run`
4. Acesse: http://localhost:8080

---

## 📋 Principais Endpoints
- **GET** `/swagger-ui/index.html` - Documentação interativa da API.
- **GET** `/v3/api-docs` - Especificação OpenAPI em JSON.
- **GET** `/h2-console` - Interface administrativa do banco de dados.
- **GET** `/actuator/health` - Status de saúde da aplicação.
- **GET** `/clientes/{id}` - Busca perfil (seguro, só permite o próprio ID).
- **GET** `/produtos/disponiveis` - Lista produtos ativos para venda (Com Cache).
- **POST** `/pedidos` - Criação de pedido (valida estoque, calcula total e taxa de entrega).
- **PUT** `/pedidos/{id}/{status}` - Atualiza status (máquina de estados).
- **GET** `/restaurantes/relatorio-vendas` - Relatório de vendas (Apenas Restaurante/Admin).

---

## 🛠️ Acesso ao Banco de Dados (PostgreSQL)
O projeto utiliza **PostgreSQL** rodando em container. Para inspecionar as tabelas e dados, recomenda-se o uso de clientes externos como **DBeaver**, **PgAdmin** ou a aba **Database do IntelliJ**.

### Credenciais de Conexão (Local)
* **Host:** `localhost`
* **Porta:** `5432`
* **Database:** `deliverydb`
* **Usuário:** `delivery` 
* **Senha:** `delivery123` 

### Connection Strings (JDBC)
* **Aplicação (Interno Docker):** `jdbc:postgresql://postgres:5432/deliverydb`
* **Cliente Externo (Seu PC):** `jdbc:postgresql://localhost:5432/deliverydb`

---

## 👨‍💻 Desenvolvedor
Elias Neves Conceição - UNIFACS Ciência da Computação  
Desenvolvido com JDK 21 e Spring Boot 3.3.5
