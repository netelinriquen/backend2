package com.inkflow.api.repository;

import com.inkflow.api.entity.Booking;
import com.inkflow.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByDataAndHorario(LocalDate data, LocalTime horario);
    List<Booking> findByOrderByDataDescHorarioDesc();
}