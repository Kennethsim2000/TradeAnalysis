package com.example.Demo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsVo {
    Long count;
    Double max;
    Double avg;
    Double min;
    Double sum;
}
