package com.salesflow.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactApiResponse<T> {
    private String message;
    private T data;
    private boolean success;

    public static <T> ContactApiResponse<T> success(String message, T data) {
        return new ContactApiResponse<>(message, data, true);
    }

    public static <T> ContactApiResponse<T> error(String message) {
        return new ContactApiResponse<>(message, null, false);
    }
} 