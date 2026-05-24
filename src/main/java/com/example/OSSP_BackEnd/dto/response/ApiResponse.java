package com.example.OSSP_BackEnd.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(httpStatus.value(), message, data);
    }

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message) {
        return new ApiResponse<>(httpStatus.value(), message, null);
    }

    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String message) {
        return new ApiResponse<>(httpStatus.value(), message, null);
    }
}
