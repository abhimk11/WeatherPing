package com.weatherbot.climatebot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "subscribers")
public class Subscriber {
    @Id
    private String id;
    private Long chatId;
    private String firstName;
    private String lastName;
    private String username;
    private Double latitude;
    private Double longitude;
}
