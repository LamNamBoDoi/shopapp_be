package com.example.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {
//    @JsonProperty("fullname")
    private String fullName;

//    @JsonProperty("phone_number")
    private String phoneNumber;

    private String address;

    private String password;

    private MultipartFile thumbnail;

//    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

//    @JsonProperty("facebook_account_id")
    private int facebookAccountId;

//    @JsonProperty("google_account_id")
    private int googleAccountId;
}
