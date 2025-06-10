# Documentação da API: Microsserviço reservation-service

## 1. Visão Geral e Execução

O `reservation-service` é responsável pelo gerenciamento de reservas de livros. Ele atua como um orquestrador, comunicando-se com o `book-service` para verificar a disponibilidade e garantir a consistência do estado dos livros.

### **Para executar o serviço:**

1.  Garanta que o `book-service` (https://github.com/GabrielAMS012/book-service) já esteja em execução.
2.  Navegue até o diretório raiz do projeto `reservation-service`.
3.  Execute o seguinte comando Maven:
    ```bash
    mvn spring-boot:run
    ```
4.  O serviço será executado em `http://localhost:8081`.

### **Acesso ao Banco de Dados H2:**

* **URL do Console**: `http://localhost:8081/h2-console`
* **JDBC URL**: `jdbc:h2:mem:reservationdb`
* **Username**: `sa`
* **Password**: `password`

---

## 2. Modelo de Dados: `Reservation`

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `Long` | Identificador único sequencial da reserva. |
| `userId` | `Long` | ID do usuário que realizou a reserva. |
| `bookId` | `UUID` | ID do livro (UUID) que foi reservado, correspondendo ao `book-service`. |
| `dataReserva`| `LocalDate` | Data em que a reserva foi efetuada (gerada automaticamente). |
| `status` | `Enum` (`ATIVA`, `CANCELADA`) | Status da reserva. |

---

## 3. Endpoints da API

### **Criar nova reserva**
* **`POST /reservations`**
* **Descrição:** Cria uma nova reserva. Este processo envolve comunicação com o `book-service` para verificar a disponibilidade e atualizar o status do livro.
* **Corpo da Requisição:** `application/json`
    ```json
    {
      "userId": 101,
      "bookId": "a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890"
    }
    ```
* **Respostas de Sucesso:**
    * `201 Created`: Retorna o objeto `Reservation` completo.
* **Respostas de Erro:**
    * `400 Bad Request`: Se `userId` ou `bookId` forem nulos.
    * `404 Not Found`: Se o `bookId` fornecido não existir no `book-service`.
    * `409 Conflict`: Se o livro correspondente ao `bookId` não estiver com status "disponível".

### **Listar reservas de um usuário**
* **`GET /reservations/user/{userId}`**
* **Descrição:** Retorna uma lista de todas as reservas (ativas e canceladas) para um determinado usuário.
* **Parâmetros de URL:**
    * `userId` (Long): O ID do usuário.
* **Respostas de Sucesso:**
    * `200 OK`: Retorna um array de objetos `Reservation`. O array será vazio se o usuário não tiver reservas.

### **Cancelar reserva**
* **`DELETE /reservations/{id}`**
* **Descrição:** Cancela uma reserva existente. Este processo comunica-se com o `book-service` para reverter o status do livro para "disponível".
* **Parâmetros de URL:**
    * `id` (Long): O ID da reserva a ser cancelada.
* **Respostas de Sucesso:**
    * `204 No Content`: Indica que a reserva foi cancelada com sucesso. A resposta não possui corpo.
* **Respostas de Erro:**
    * `404 Not Found`: Se nenhuma reserva for encontrada com o ID fornecido.
    * `409 Conflict`: Se a reserva já estiver com o status "CANCELADA".
    * `500 Internal Server Error` ou similar: Se a comunicação com o `book-service` falhar durante o processo.
