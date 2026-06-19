// BookingUpdateDto.java
package org.example.dto;

import org.example.domain.enums.BookingStatus;

import java.time.LocalDate;

public record BookingUpdateDto(
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BookingStatus status
) {}