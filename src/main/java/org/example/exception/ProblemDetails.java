package org.example.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {
    @Builder.Default
    private String type = "about:blank";
    private String title;
    private int status;
    private String detail;
    private String instance;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private Map<String, Object> additionalInfo;
    public ProblemDetails(String title, int status, String detail) {
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.timestamp = LocalDateTime.now();
        this.type = "about:blank";
    }
}