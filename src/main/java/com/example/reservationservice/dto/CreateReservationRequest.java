package com.example.reservationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReservationRequest {

    @NotNull(message = "O ID do usuário (userId) não pode ser nulo.")
    private Long userId;

    @NotNull(message = "O ID do livro (bookId) não pode ser nulo.")
    private UUID bookId;
}