package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.taurus.aya.server.entity.Task;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {

    @Query("SELECT t FROM Task t WHERE t.showInBacklog=true AND ((t.rgroup=-1 AND t.author=:author) OR t.rgroup IS NULL OR t.rgroup in (:groups))")
    List<Task> findAllByShowInBacklogIsTrueOrderByPriorityDesc(Long author, List<Long> groups);

    @Query("SELECT t FROM Task t WHERE t.endDate >= :from AND t.endDate <= :end AND t.state in (:states) AND t.lane in (:lanes)")
    LinkedList<Task> findAllByEndDateGreaterThanAndEndDateLessThanAndState(Date from, Date end, List<Integer> states, List<String> lanes);
}
