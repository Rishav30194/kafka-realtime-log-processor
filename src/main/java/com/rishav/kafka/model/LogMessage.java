package com.rishav.kafka.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LogMessage {

    private String level;
    private long timestamp;
    private String message;

}
