package com.weatherbot.climatebot.repository;

import com.weatherbot.climatebot.entity.Subscriber;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SubscriberRepository extends MongoRepository<Subscriber,String> {
    boolean existsByChatId(Long chatId);
    Optional<Subscriber> findByChatId(Long chatId);
    void deleteByChatId(Long chatId);
}
