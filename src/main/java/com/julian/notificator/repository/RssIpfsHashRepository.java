package com.julian.notificator.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.julian.notificator.entity.RssIpfsHash;

@Repository
public interface RssIpfsHashRepository extends JpaRepository<RssIpfsHash, Long> {
}
