package org.taurus.aya.server.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.taurus.aya.client.EventState;
import org.taurus.aya.server.EventRepository;
import org.taurus.aya.server.entity.Event;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@Service
public class EventService {

    private EventRepository eventRepository;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    //2019-07-19T03:12:27.000
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public EventService(@Autowired EventRepository repository)
    {
        this.eventRepository = repository;
    }

    public ObjectMapper objectMapper = new ObjectMapper();

    public List<Event> getData(String[] criteria) throws RuntimeException, ParseException,  IOException
    {
        List<Event> eventList;
        HashMap<String,String> criteriaMap = parseCriteriaString(criteria);

        //System.out.println(criteriaMap);
        if (Boolean.valueOf(criteriaMap.getOrDefault("isGraph","false"))){
            if (criteriaMap.get("executor") != null)
                eventList = eventRepository.findAllByStartDateLessThanEqualAndEndDateGreaterThanAndIsGraphIsTrueAndExecutor(
                            formatter.parse(criteriaMap.getOrDefault("startDate", "2000-01-0")),
                            formatter.parse(criteriaMap.getOrDefault("endDate", "2050-01-01")),
                            Long.valueOf(criteriaMap.get("executor"))
                    );
            else
                eventList = eventRepository.findAllByStartDateLessThanEqualAndEndDateGreaterThanAndIsGraphIsTrue(
                        formatter.parse(criteriaMap.getOrDefault("startDate", "2000-01-0")),
                        formatter.parse(criteriaMap.getOrDefault("endDate", "2050-01-01"))
                );
        }
        else
            eventList = eventRepository.findAllByStartDateGreaterThanAndEndDateLessThanEqualAndIsGraphIsFalse(
                formatter.parse(criteriaMap.getOrDefault("startDate","2000-01-01")),
                formatter.parse(criteriaMap.getOrDefault("endDate","2050-01-01"))
            );

        return eventList;
    }


    public HashMap<String,String> parseCriteriaString(String[] criterias) throws IOException{

        HashMap<String,String> result = new HashMap<>();

        for (String crit: criterias)
            parseCriteria(objectMapper.readTree(crit),result);

        return result;
    }

    private void parseCriteria(JsonNode node, Map<String,String> result) throws IOException {

        if (node.has("fieldName") && node.has("value"))
            result.put(node.get("fieldName").textValue(), node.get("value").asText());
        if (node.has("criteria"))
            if (node.get("criteria").isArray())
                for (int e=0; e<node.get("criteria").size(); e++) {
                    JsonNode elem = node.get("criteria").get(e);
                    parseCriteria(elem,result);
                }
            else
                parseCriteria(node,result);
    }

    /**
    * Установка времени выполнения задачи.
    * Если задача начала выполняться - запоминает время старта, если прекратила (встала на паузу и т.п) -
    * записывает в задачу время выполнения как разницу между временем старта и текущим.
    * Если задача выполнялась более 1 дня - записывает время исходя из длительности "условного дня" (8 часов)
    * @param event- задача, состояние которой изменилось
    * */
    public boolean processEventStartAndSpentTime(Event event, Integer newEventState)
    {
        boolean needUserCorrection = false;

        //Считаем только, если задача на графике и было переключение либо в PROCESS, либо из него
        //Переключения типа PAUSE->FAIL не учитываются
        if (event.getIsGraph() && (event.getState()==EventState.PROCESS.ordinal() || newEventState==EventState.PROCESS.ordinal()))
            if (!newEventState.equals(EventState.PROCESS.ordinal())) //это переключение ИЗ режима process в какой-то другой
            {
                if (event.getStart() == null ) throw new RuntimeException("setEventSpentTime: event state was changed to (NEW/READY/PAUSE/FAIL) but start timestamp is NULL ");
                if (Duration.between(event.getStart().toInstant(),Instant.now()).toHours() > 24 ) // если больше суток - считаем по длительностям "условного дня"
                {
                    event.setSpentTime(event.getSpentTime() + Duration.between(event.getStart().toInstant(), Instant.now()).toDays() * 8.0);
                    needUserCorrection = true;
                }
                else
                    event.setSpentTime(event.getSpentTime() + Duration.between(event.getStart().toInstant(),Instant.now()).toMinutes()/60.0);
            }
            else
            {
                //если это переключение в режим process - запоминаем время
                event.setStart(new Date());
            }

        return needUserCorrection;
    }
}
