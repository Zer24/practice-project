package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.CancelRequestStatus;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequestUpdateDto {
    private CancelRequestStatus status;
    private UUID processedBy;
}