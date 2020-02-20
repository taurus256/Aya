package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.taurus.aya.server.entity.Task;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findAllByShowInBacklogIsTrue();
    LinkedList<Task> findAllByEndDateGreaterThanAndEndDateLessThanAndState(Date from, Date end, Integer state);
}
