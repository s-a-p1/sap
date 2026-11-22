# SAP — Sistema Inteligente de Acompanhamento e Personalização da Aprendizagem

## 1. Sobre o projeto

O SAP é um sistema web desenvolvido como Projeto Final de Curso (PFC/TCC) do curso de Sistemas de Informação.

Seu objetivo é auxiliar estudantes do ensino superior na organização dos estudos, no acompanhamento do desempenho acadêmico e na identificação de dificuldades de aprendizagem.

A aplicação oferece funcionalidades para estudantes, professores e administradores, incluindo avaliações, acompanhamento de resultados, recomendações de estudo e gerenciamento acadêmico.

## 2. Tecnologias utilizadas

### Frontend

- React
- JavaScript
- Vite
- HTML5
- CSS3

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Maven

### Banco de dados

- MySQL 8

### Ferramentas de desenvolvimento

- Visual Studio Code
- Git
- MySQL Workbench

## 3. Arquitetura

O sistema utiliza uma arquitetura cliente-servidor, com separação entre frontend, backend e banco de dados.

O frontend React é responsável pela interface e pela interação com os usuários.

O backend Spring Boot disponibiliza uma API REST responsável pelas regras de negócio, autenticação, validação e persistência dos dados.

O MySQL armazena as informações acadêmicas e os dados dos usuários.

Fluxo geral:

```text
Navegador
    |
    v
Frontend React + Vite
    |
    | Requisições HTTP
    v
API REST Spring Boot
    |
    | Spring Data JPA / Hibernate
    v
Banco de dados MySQL
```

## 4. Funcionalidades

### Estudante

- Cadastro e autenticação
- Configuração acadêmica
- Consulta de disciplinas
- Realização de avaliações
- Visualização de resultados
- Histórico de avaliações
- Análise de desempenho
- Recomendações de estudo
- Metas e planejamento de estudos
- Notificações e preferências
- Gerenciamento do perfil
- Alteração de senha
- Exclusão de conta

### Professor

- Autenticação
- Acesso à área do professor
- Gerenciamento de avaliações
- Gerenciamento de questões
- Acompanhamento acadêmico
- Gerenciamento do perfil

### Administrador

- Acesso à área administrativa
- Gerenciamento de usuários
- Funcionalidades administrativas do sistema

## 5. Pré-requisitos

Para executar o SAP localmente, é necessário instalar:

- JDK 21
- Node.js e npm
- MySQL 8
- Git, caso seja necessário clonar o repositório

O projeto backend possui Maven Wrapper, portanto não é obrigatório instalar o Maven globalmente.

Verifique as instalações:

```powershell
java -version
node -v
npm.cmd -v
mysql --version
```

O comando `mysql --version` depende da instalação do cliente MySQL e de sua configuração no PATH do Windows.

## 6. Estrutura do projeto

```text
sap/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── frontend/
│   ├── public/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 7. Configuração do banco de dados

O SAP utiliza um banco de dados MySQL denominado `sap`.

### 7.1. Iniciar o MySQL

Certifique-se de que o servidor MySQL esteja em execução.

A conexão padrão utiliza:

```text
Host: localhost
Porta: 3306
Banco: sap
```

### 7.2. Criar o banco

No MySQL Workbench ou em outro cliente MySQL, execute:

```sql
CREATE DATABASE IF NOT EXISTS sap
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 7.3. Criar um usuário para a aplicação

Utilize um usuário MySQL próprio para o SAP.

Exemplo:

```sql
CREATE USER IF NOT EXISTS 'sap_user'@'localhost'
IDENTIFIED BY 'defina_uma_senha_segura';

GRANT ALL PRIVILEGES ON sap.* TO 'sap_user'@'localhost';
```

Substitua a senha de exemplo por uma senha segura.

Caso o banco já exista, não é necessário recriá-lo.


### 7.4. Importar a estrutura do banco

O projeto disponibiliza o arquivo `backend/database/schema.sql`, contendo a estrutura das 18 tabelas do SAP, incluindo colunas, índices e relacionamentos.

O arquivo não contém registros de usuários ou dados acadêmicos.

Para configurar o banco em uma instalação nova:

1. Inicie o servidor MySQL.
2. Crie o banco de dados `sap`, conforme a seção 7.2.
3. Abra o MySQL Workbench.
4. Conecte-se ao servidor MySQL local.
5. Acesse File > Open SQL Script.
6. Selecione o arquivo `backend/database/schema.sql`.
7. Defina `sap` como banco de destino.
8. Execute o script SQL.

Também é possível importar o arquivo pelo Prompt de Comando do Windows:

```cmd
mysql -u root -p sap < backend\database\schema.sql
```

O comando deve ser executado na raiz do projeto, com o cliente MySQL disponível no PATH.

O backend utiliza a configuração:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Isso significa que o Hibernate verifica a estrutura do banco, mas não cria automaticamente as tabelas.

### 7.5. Dados acadêmicos iniciais

O arquivo `schema.sql` contém somente a estrutura do banco de dados.

Para utilizar as funcionalidades acadêmicas do SAP, será necessário cadastrar os dados iniciais, como:

- Cursos
- Disciplinas
- Semestres
- Ofertas de disciplinas
- Avaliações
- Questões

Esses dados não são incluídos no script de estrutura.

Antes de executar qualquer restauração em um banco existente, recomenda-se realizar um backup para evitar a perda de informações.

## 8. Variáveis de ambiente

As configurações sensíveis devem ser fornecidas por variáveis de ambiente, evitando armazenar senhas diretamente no código-fonte.

### 8.1. Backend

As propriedades de conexão com o banco devem ser configuradas conforme o arquivo `application.properties` ou `application.yml` do backend.

Um exemplo de configuração com variáveis de ambiente é:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/sap}
spring.datasource.username=${DB_USERNAME:sap_user}
spring.datasource.password=${DB_PASSWORD}
```

Essas são as variáveis utilizadas pelo backend do SAP. O arquivo application.properties exige DB_URL, DB_USERNAME e DB_PASSWORD para estabelecer a conexão com o MySQL.

No PowerShell, as variáveis podem ser definidas na sessão atual:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/sap"
$env:DB_USERNAME = "sap_user"
$env:DB_PASSWORD = "sua_senha_local"
```

Substitua os valores conforme sua instalação do MySQL.

Não publique senhas reais no repositório.

### 8.2. Frontend

Durante o desenvolvimento, o frontend utiliza o servidor do Vite.

As chamadas para a API são realizadas pelo caminho `/api`, que deve ser encaminhado ao backend Spring Boot.

A configuração do proxy pode ser consultada no arquivo `frontend/vite.config.js`.

Caso sejam utilizadas variáveis de ambiente no Vite, elas devem possuir o prefixo `VITE_` para serem disponibilizadas ao código do frontend.

Não armazene credenciais ou segredos em variáveis `VITE_`, pois seus valores podem ser incluídos no código enviado ao navegador.

## 9. Executando o backend

Abra um terminal na raiz do projeto e acesse:

```powershell
cd backend
```

Configure as variáveis de ambiente necessárias e execute:

```powershell
.\mvnw.cmd spring-boot:run
```

O backend utiliza, na configuração local esperada, o endereço:

```text
http://localhost:8080
```

Mantenha esse terminal aberto enquanto utilizar o sistema.

## 10. Executando o frontend

Abra outro terminal na raiz do projeto.

Acesse:

```powershell
cd frontend
```

Instale as dependências:

```powershell
npm.cmd install
```

Inicie o servidor de desenvolvimento:

```powershell
npm.cmd run dev
```

O endereço local normalmente utilizado é:

```text
http://localhost:5173
```

A porta efetiva deve ser confirmada na saída do Vite.

Com o frontend, o backend e o MySQL em execução, o sistema poderá ser acessado pelo navegador.

## 11. Autenticação e segurança

O SAP utiliza autenticação local com Spring Security.

As senhas dos usuários são armazenadas utilizando criptografia de hash BCrypt.

A autenticação utiliza sessões HTTP, e as operações protegidas incluem validação CSRF.

O sistema também disponibiliza recursos de recuperação de senha, atualização de perfil e exclusão de conta mediante confirmação da senha atual.

## 12. Executando os testes

### 12.1. Testes automatizados do backend

Na pasta `backend`, execute:

```powershell
.\mvnw.cmd clean verify
```

Esse comando realiza a compilação, executa os testes configurados e gera os relatórios de verificação.

Na validação realizada em 22/11/2026, o backend apresentou:

```text
Tests run: 571
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

### 12.2. Relatório de cobertura

O projeto utiliza JaCoCo para gerar relatórios de cobertura dos testes.

Após a execução do Maven, o relatório HTML pode ser consultado em:

```text
backend/target/site/jacoco/index.html
```

### 12.3. Build do frontend

Na pasta `frontend`, execute:

```powershell
npm.cmd run build
```

O Vite gera os arquivos de produção na pasta:

```text
frontend/dist/
```

A última validação registrada em 22/11/2026 concluiu o build do frontend com sucesso.

## 13. Ordem recomendada de inicialização

Para executar o sistema localmente:

1. Inicie o servidor MySQL.
2. Verifique se o banco `sap` está disponível.
3. Configure as variáveis de ambiente do backend.
4. Inicie o Spring Boot.
5. Inicie o Vite.
6. Acesse o endereço informado pelo frontend no navegador.

O backend e o frontend devem permanecer em execução durante a utilização do sistema.

## 14. Solução de problemas

### Erro de conexão com o banco

Verifique se o MySQL está iniciado, se o banco `sap` existe e se as credenciais configuradas estão corretas.

### Porta 8080 ocupada

Verifique se outra instância do backend está em execução.

### Porta 5173 ocupada

O Vite poderá utilizar outra porta disponível. Consulte o endereço exibido no terminal.

### Erro ao executar npm no PowerShell

Em ambientes Windows com restrições de execução de scripts, utilize:

```powershell
npm.cmd install
npm.cmd run dev
```

### Erros de autenticação

Verifique se o backend está acessível, se a sessão está válida e se o navegador está enviando os cookies necessários.

## 15. Considerações finais

O SAP foi desenvolvido para apoiar o acompanhamento e a personalização da aprendizagem no ensino superior.

O projeto integra tecnologias de desenvolvimento web, persistência de dados, autenticação e análise de desempenho acadêmico.

A aplicação está organizada para execução local, permitindo demonstrações, testes e manutenção durante o desenvolvimento acadêmico.

---

Projeto acadêmico — Sistemas de Informação — 2026.