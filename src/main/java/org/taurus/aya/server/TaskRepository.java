package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.taurus.aya.server.entity.Task;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findAllByShowInBacklogIsTrueOrderByPriorityDesc();

    @Query("SELECT t FROM Task t WHERE t.endDate>=:from AND t.endDate < :end AND t.state=:state AND t.lane in (:lanes)")
    LinkedList<Task> findAllByEndDateGreaterThanAndEndDateLessThanAndState(Date from, Date end, Integer state, List<String> lanes);
}
