package com.julian.notificator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.julian.notificator.entity.SportEvent;

@Repository
public interface SportEventRepository extends JpaRepository<SportEvent, Long> {

}
