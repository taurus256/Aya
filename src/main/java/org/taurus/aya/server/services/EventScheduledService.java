package org.taurus.aya.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.taurus.aya.server.EventRepository;

@Service
public class EventScheduledService {

    private EventRepository eventRepository;

    public EventScheduledService(@Autowired  EventRepository eventRepository)
    {
        this.eventRepository = eventRepository;
    }

    @Scheduled(cron = "0 0 9 * * ?") //запуск каждый день в 9:00
    @Transactional
    public void runEveryHour()
    {
        System.out.println("State of running tasks changed");
        eventRepository.updateEndDateForTasksInProcess();
    }
}
