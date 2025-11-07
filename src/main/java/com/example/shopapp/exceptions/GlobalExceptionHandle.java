package com.example.shopapp.exceptions;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.utils.MessageKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandle extends TranslateMessages {

    @ExceptionHandler(value = {
            DataNotFoundException.class,
            InvalidParamException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleSpecificExceptions(Exception e){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        String detail = translate(MessageKeys.ERROR_MESSAGE);

        if(e instanceof DataNotFoundException){
            status = HttpStatus.NOT_FOUND;
            detail = e.getMessage();
        }else if(e instanceof InvalidParamException){
            status = HttpStatus.BAD_REQUEST;
            detail = e.getMessage();
        }
        ApiResponse<Object> apiResponse = new ApiResponse<Object>();
        apiResponse.setMessage(String.valueOf(status.value()));
        apiResponse.setError(detail);
        return ResponseEntity.status(status).body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handlingAllException(Exception e) {
        log.error("Exception: ", e);
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(String.valueOf(e));
        apiResponse.setError(translate(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Object>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Object> apiResponse = new ApiResponse<>();

        apiResponse.setMessage(String.valueOf(errorCode.getCode()));
        apiResponse.setError(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }
}
