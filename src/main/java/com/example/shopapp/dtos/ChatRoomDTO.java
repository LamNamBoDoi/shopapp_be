package com.example.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    @JsonProperty("customer_id")
    private Long customerId;

    @NotNull(message = "Admin ID is required")
    @Positive(message = "Admin ID must be positive")
    @JsonProperty("admin_id")
    private Long adminId;

    @JsonProperty("active")
    private Boolean active;
}
