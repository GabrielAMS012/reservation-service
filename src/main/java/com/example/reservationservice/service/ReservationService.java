package com.example.reservationservice.service;

import com.example.reservationservice.client.BookServiceClient;
import com.example.reservationservice.dto.BookDTO;
import com.example.reservationservice.dto.CreateReservationRequest;
import com.example.reservationservice.exception.ReservationConflictException;
import com.example.reservationservice.exception.ResourceNotFoundException;
import com.example.reservationservice.model.Reservation;
import com.example.reservationservice.model.enums.ReservationStatus;
import com.example.reservationservice.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookServiceClient bookServiceClient;

    @Transactional
    public Reservation createReservation(CreateReservationRequest request) {
        BookDTO book = bookServiceClient.getBookById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Livro com ID " + request.getBookId() + " não encontrado."));

        if (!"disponivel".equalsIgnoreCase(book.status())) {
            throw new ReservationConflictException("O livro '" + book.titulo() + "' não está disponível para reserva.");
        }

        try {
            bookServiceClient.updateBookStatus(request.getBookId(), "reservado");
        } catch (Exception e) {
            throw new ReservationConflictException("Não foi possível reservar o livro no momento. Causa: " + e.getMessage());
        }

        Reservation reservation = Reservation.builder()
                .userId(request.getUserId())
                .bookId(request.getBookId())
                .status(ReservationStatus.ATIVA)
                .build();

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com o ID: " + id));

        if (reservation.getStatus() == ReservationStatus.CANCELADA) {
            throw new ReservationConflictException("Esta reserva já foi cancelada.");
        }

        try {
            bookServiceClient.updateBookStatus(reservation.getBookId(), "disponivel");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao comunicar com o serviço de livros para liberar o livro. O cancelamento foi abortado. Causa: " + e.getMessage());
        }

        reservation.setStatus(ReservationStatus.CANCELADA);
        reservationRepository.save(reservation);
    }
}