package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.taurus.aya.server.entity.Lane;

import java.util.List;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {

    /*Обновить имя потока в задачах*/
    @Modifying
    @Transactional
    @Query("UPDATE Event SET lane=?2 WHERE lane=?1")
    void updateEventsSetLaneName(String oldLaneName, String newLaneName);
}
