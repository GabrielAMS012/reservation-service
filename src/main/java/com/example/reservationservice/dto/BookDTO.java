package com.example.reservationservice.dto;

import java.util.UUID;

public record BookDTO(UUID id, String titulo, String autor, String status) {
}