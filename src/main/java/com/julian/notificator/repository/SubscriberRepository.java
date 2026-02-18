package com.julian.notificator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.julian.notificator.entity.Subscribers;


@Repository
public interface SubscriberRepository extends JpaRepository<Subscribers, Long> {

    List<Subscribers> findByActiveTrue();

    Optional<Subscribers> findByApiKey(String apiKey);

    Optional<Subscribers> findByCallbackUrl(String callbackUrl);

    Optional<Subscribers> findByName(String name);

    List<Subscribers> findByFailureCountGreaterThanEqual(int maxFailures);

    boolean existsByCallbackUrl(String callbackUrl);

    boolean existsByApiKey(String apiKey);

}

