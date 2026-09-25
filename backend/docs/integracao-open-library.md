# Integração com a Open Library API

## 1. Objetivo

O SAP (Sistema de Acompanhamento e Personalização da Aprendizagem) utiliza a Open Library API como fonte externa de materiais bibliográficos.

A integração permite que usuários autenticados pesquisem livros relacionados aos conteúdos estudados, complementando as atividades e avaliações disponíveis no sistema.

## 2. API utilizada

**Nome:** Open Library API

**Serviço:** Open Library

**Endpoint utilizado:**

https://openlibrary.org/search.json

A integração utiliza o serviço de busca da Open Library para localizar livros a partir de um termo informado pelo usuário.

## 3. Fluxo da integração

O fluxo da funcionalidade ocorre da seguinte forma:

1. O usuário autenticado acessa a seção "Materiais para estudo" no SAP.
2. O usuário informa um termo de pesquisa, como "Java", "SQL" ou "Banco de Dados".
3. O frontend envia uma requisição para o backend do SAP.
4. O backend valida a requisição e consulta a Open Library API.
5. A Open Library retorna os resultados em formato JSON.
6. O backend seleciona os dados necessários e os converte para o formato utilizado pelo SAP.
7. O frontend recebe os resultados e apresenta os livros em cards para o usuário.

Fluxo resumido:

```text
Frontend SAP
    ↓
Backend SAP
    ↓
Open Library API
    ↓
Backend SAP
    ↓
Frontend SAP
```

## 4. Endpoint interno do SAP

**Método:**

```text
GET
```

**Rota:**

```text
/api/livros
```

**Parâmetro:**

```text
busca
```

**Exemplo:**

```text
/api/livros?busca=java
```

A rota exige que o usuário esteja autenticado no sistema.

Os perfis `ESTUDANTE` e `PROFESSOR` possuem permissão para acessar a consulta de livros.

## 5. Requisição à API externa

O backend realiza uma requisição HTTP GET para:

```text
https://openlibrary.org/search.json
```

São enviados os seguintes parâmetros:

- `q`: termo informado pelo usuário.
- `limit`: quantidade máxima de resultados retornados ao SAP.

Atualmente, o sistema limita a consulta a 8 resultados.

Exemplo:

```text
https://openlibrary.org/search.json?q=java&limit=8
```

## 6. Dados utilizados

A resposta original da Open Library contém diversos campos.

O SAP utiliza somente os dados necessários para a funcionalidade:

- `title`: título do livro.
- `author_name`: nome do autor.
- `first_publish_year`: ano da primeira publicação.
- `key`: identificador da obra na Open Library.

Esses dados são convertidos pelo backend para:

- `titulo`
- `autor`
- `anoPublicacao`
- `chave`

## 7. Estrutura da resposta do SAP

Exemplo de resposta:

```json
[
  {
    "titulo": "Exemplo de livro",
    "autor": "Exemplo de autor",
    "anoPublicacao": 2000,
    "chave": "/works/OL000000W"
  }
]
```

## 8. Componentes implementados

A integração foi separada em camadas.

### LivroController

Responsável por disponibilizar o endpoint:

```text
GET /api/livros
```

Recebe o termo informado pelo frontend e encaminha a solicitação para a camada de serviço.

### LivroService

Responsável pela comunicação com a Open Library API.

Suas responsabilidades incluem:

- validar o termo de pesquisa;
- montar a URL da API;
- realizar a requisição HTTP;
- interpretar a resposta JSON;
- selecionar os dados utilizados pelo SAP;
- retornar os livros encontrados.

### LivroResponse

DTO responsável por representar somente os dados bibliográficos utilizados pelo frontend.

## 9. Segurança

O endpoint interno da integração está protegido pelo Spring Security.

Somente usuários autenticados com os perfis:

- `ESTUDANTE`
- `PROFESSOR`

podem acessar a rota de consulta de livros.

A consulta à Open Library utilizada pelo SAP não exige que credenciais da API sejam armazenadas no frontend.

## 10. Tratamento de erros

O sistema possui tratamento para situações como:

- termo de busca vazio;
- resposta vazia da API;
- nenhum livro encontrado;
- falha na comunicação com o serviço externo;
- sessão de usuário não autenticada.

No frontend, uma mensagem é apresentada ao usuário quando a pesquisa não pode ser concluída.

## 11. Interface

A funcionalidade está disponível na seção **Materiais para estudo**.

O usuário pode informar um assunto e realizar a pesquisa diretamente pelo SAP.

Os resultados são apresentados em cards contendo:

- título;
- autor;
- ano da primeira publicação.

## 12. Teste realizado

A integração foi validada utilizando o termo:

```text
Java
```

O backend realizou a consulta à Open Library API e retornou os resultados em formato JSON.

Posteriormente, a funcionalidade foi validada pelo frontend do SAP, que exibiu os livros retornados pela API na seção **Materiais para estudo**.

Dessa forma, foi validado o seguinte fluxo:

```text
Frontend SAP
→ Backend SAP
→ Open Library API
→ Backend SAP
→ Frontend SAP
```

## 13. Arquivos relacionados

### Backend

```text
src/main/java/com/sap/controller/LivroController.java
src/main/java/com/sap/service/LivroService.java
src/main/java/com/sap/dto/LivroResponse.java
src/main/java/com/sap/config/SecurityConfig.java
```

### Frontend

```text
frontend/src/App.jsx
frontend/src/App.css
```

## 14. Considerações sobre privacidade

O SAP utiliza a Open Library para realizar pesquisas bibliográficas.

A integração não envia para a Open Library dados internos do SAP como:

- senha do usuário;
- resultados das avaliações;
- respostas das questões;
- perfil acadêmico;
- logs de auditoria.

Para realizar a pesquisa bibliográfica, o backend envia o termo de busca informado pelo usuário.

## 15. Resultado da integração

A integração com a Open Library permite complementar o contexto educacional do SAP por meio da pesquisa de materiais bibliográficos.

A funcionalidade está integrada ao backend e ao frontend da aplicação, possui controle de acesso e apresenta os resultados diretamente na interface do sistema.