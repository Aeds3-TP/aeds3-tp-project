# Trabalho TP AEDs3

Este projeto é a implementação completa de um sistema de E-commerce com um motor de banco de dados customizado, construído do zero em Java. O projeto utiliza manipulação direta de arquivos binários (`RandomAccessFile`), sem o uso de SGBDs comerciais, aplicando conceitos avançados de Algoritmos e Estruturas de Dados III.

O sistema possui uma arquitetura híbrida: oferece um **Terminal Interativo (CLI)** no console da aplicação para testes e manutenção direta, e simultaneamente levanta uma **API RESTful** (utilizando Spark Java) consumida por um **Front-end Web**.

## Alunos integrantes da equipe

* Rafael Assis Carvalho Lacerda
* Rafael Portilho de Andrade 
* Bernardo Barbosa Heronvile
* Gabriel Assis Carvalho Lacerda
* Pedro Henrique Lopes De Melo

## Professor responsáveL

* Walisson Ferreira de Carvalho

## 🚀 Arquitetura e Tecnologias
O projeto segue a arquitetura em camadas (MVC/DAO) para total separação de responsabilidades:
* **app:** Contém a classe principal (`Aplicacao.java`) que inicializa as rotas web e o terminal.
* **model:** Classes de entidade (POJOs) do domínio (Produto, Usuario, Pedido, etc.).
* **dao:** Data Access Objects, responsáveis pela persistência física dos bytes em disco (`.db`).
* **indice:** Algoritmos de estruturas de dados para indexação (Hash Extensível).
* **service:** Regras de negócio, segurança (Criptografia de senhas com XOR) e controle de rotas REST.
* **util:** Utilitários, como o motor de Ordenação Externa.
* **Backend Web:** Spark Java (Porta 3000).
* **Frontend:** HTML5, CSS3, JavaScript (Fetch API) localizado em `src/main/resources/public`.

## 📁 Estrutura de Diretórios de Dados (`/dados`)
O sistema gerencia autonomamente seus arquivos de persistência nas seguintes pastas:
- `registros/`: Arquivos de dados principais (`.db`).
- `indices/`: Arquivos de indexação exemplo: Hash (`.hash_d`, `.hash_b`).
- `relatorios/`: Destino dos arquivos gerados pela Ordenação Externa (`produto_ordenado.db`).
- `temp/`: Arquivos temporários (apagados automaticamente após ordenações).

## 🧰 Ferramentas Necessárias (Pré-requisitos)
Antes de baixar e executar o projeto, certifique-se de ter as seguintes ferramentas instaladas em sua máquina:
* **Java Development Kit (JDK):** Essencial para compilar e rodar a aplicação baseada no `pom.xml`.
* **Apache Maven:** Gerenciador responsável por baixar as dependências automaticamente (Spark Java, Gson, JWT).
* **IDE Java:** Recomendado **Eclipse** (IDE principal utilizada no desenvolvimento) ou **VS Code** (com a extensão *Extension Pack for Java* instalada).
* **Navegador Web:** Google Chrome, Edge ou Firefox para visualizar a interface de usuário.

## 🛠️ Como Compilar e Executar

A porta de entrada do projeto é o arquivo principal **`Aplicacao.java`** localizado no pacote `app`. 

**Passo a passo via VS Code ou Eclipse(IDE principal utilizada):**
1. Clone este repositório e abra o projeto na sua IDE.
2. Certifique-se de que as dependências do **Maven** foram baixadas (Spark Java).
3. Navegue até `src/main/java/app/Aplicacao.java`.
4. Execute a classe (`Run Java`).

### Utilizando o Sistema
Caso tenha interesse em testar o site pode utilizar os dois usuarios de teste ja criados, caso você queira criar outro usuario pode utilizar a pagina do frontend
para criar um novo Usuario comum. Porem caso queira criar um novo ADMIN você tem duas opções, ou criar o fetch de um novo usuario com a role ADMIN manualmente no terminal F12 (já que a segurança dessa rota está desativada para testes) ou utilizar o terminal de testes do backend, criando manualmente o ADMIN.

Administrador:
Email: admin@gmail.com
Senha: 123

Usuario comum:
Email: teste@gmail.com
Senha: 123

Ao rodar a aplicação, dois ambientes estarão disponíveis simultaneamente:

**1. O Terminal Interativo (Backend CLI)**
Olhe o console da sua IDE. O sistema apresentará um menu interativo onde você pode realizar operações de CRUD (Adicionar, Atualizar, Remover, Mostrar) diretamente no banco de dados customizado, ideal para manutenção rápida e testes de arquitetura.

**2. A Interface Web (Frontend)**
O servidor web estará rodando localmente.
* Abra o seu navegador e acesse: `http://localhost:3000` (ou abra diretamente o `index.html` na pasta `public`).
* O sistema possui controle de acesso (CORS configurado). As rotas são divididas por níveis de permissão (`Role`):
  * **Público:** Visualização de produtos e categorias.
  * **Comum (`/api/comum/`):** Carrinho, pedidos e favoritos do usuário logado.
  * **Gestão (`/api/gestao/`):** CRUD de produtos e categorias (Apenas Admin/Gestor).
  * **Admin (`/api/admin/`):** Controle total de usuários e relatórios.
