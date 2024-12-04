package com.example.Demo.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserVo {
    private String username;
    private LocalDateTime dateCreated;

}
