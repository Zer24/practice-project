package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.BookingStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingUpdateDto {

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BookingStatus status;
}