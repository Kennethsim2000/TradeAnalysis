package com.example.Demo.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;
    private boolean success;

    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<>(200, message, data, true);
    }

    public static <T> CommonResult<T> fail(String message) {
        return new CommonResult<>(400, message, null, false);
    }
}
