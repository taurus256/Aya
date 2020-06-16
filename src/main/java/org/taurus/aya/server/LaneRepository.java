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
