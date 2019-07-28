package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Lane;
import org.taurus.aya.server.entity.Users;

import java.util.List;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {
    @Override
    @Query("SELECT l FROM Lane l WHERE l.id > 0")
    public List<Lane> findAll();
}
