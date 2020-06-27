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

    @Query("SELECT l FROM Lane l WHERE l.author=:author OR l.rgroup IS NULL OR (l.rgroup IN (:groups)) ORDER BY l.laneOrder")
    List<Lane> findAll(Long author, List<Long> groups);

    @Query("SELECT l.name FROM Lane l WHERE l.author=?1 OR l.rgroup IS NULL OR l.rgroup IN (?2) ORDER BY l.laneOrder")
    public List<String> findAllNames(Long author, List<Long> groups);

    /*Обновить имя потока в задачах*/
    @Modifying
    @Transactional
    @Query("UPDATE Task SET lane=?2 WHERE lane=?1")
    void updateEventsSetLaneName(String oldLaneName, String newLaneName);

    /*Удалить все фрагменты задач (events) указанного потока*/
    @Modifying
    @Transactional
    @Query("DELETE FROM Event e WHERE e.taskId IN (SELECT t.id FROM Task t WHERE t.lane=?1)")
    void deleteEventsForGivenLane(String laneName);

    /*Удалить все задачи указанного потока*/
    @Modifying
    @Transactional
    @Query("DELETE FROM Task WHERE lane=?1")
    void deleteTasksForGivenLane(String laneName);
}
