package co.com.crediya.api.dto.user;

import jakarta.validation.constraints.NotBlank;


public record UserDocumentDto(
        @NotBlank(message = "USR_008")
        String documentId) {
}
