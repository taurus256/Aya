package org.taurus.aya.server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.client.EventState;
import org.taurus.aya.server.EventRepository;
import org.taurus.aya.server.TaskRepository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Task;
import org.taurus.aya.server.services.EventService;
import org.taurus.aya.server.services.TaskService;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.time.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController extends GenericController {

    private EventRepository eventRepository;

    private TaskRepository taskRepository;

    private TaskService taskService;

    private EventService eventService;

    public EventController(@Autowired EventRepository eventRepository, @Autowired TaskRepository taskRepository, @Autowired TaskService taskService, @Autowired EventService service)
    {
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.eventService = service;
    }

    @ResponseBody
    @PostMapping("/fetch")
    public GwtResponse getEvents(HttpServletRequest request, @RequestParam String _operationType, @RequestParam (required=false) String[] criteria) throws RuntimeException, ParseException, IOException
    {
        System.out.println("Operation_type=" + _operationType);
        System.out.println("request body is:" + _operationType);
        System.out.println("criteria length is:" + criteria.length);

        List<Event> events = eventService.getData(request, criteria);
        return new GwtResponse(0,events.size(),events.size(), events);
    }

    @PostMapping("/modify")
    @ResponseBody
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GwtResponse executePost(
        @RequestParam String _operationType,
        @RequestParam (required = false) String id,          //integer,
        @RequestParam (required = false) String lane,
        @RequestParam (required = false) String name,
        @RequestParam (required = false) String description,
        @RequestParam (required = false) String startDate,         //Date startDate,
        @RequestParam (required = false) String endDate,           //Date endDate,
        @RequestParam (required = false) String author,            //Long author,
        @RequestParam (required = false) String wuser,          //Integer wuser,
        @RequestParam (required = false) String wgroup,         //Integer wgroup,
        @RequestParam (required = false) String ruser,          //Integer ruser,
        @RequestParam (required = false) String rgroup,         //Integer rgroup,
        @RequestParam (required = false) String eventWindowStyle,//String eventwindowstyle,
        @RequestParam (required = false) String executor,       //Integer executor,
        @RequestParam (required = false) String priority,                          //  Integer priority
        @RequestParam (required = false) String duration_h,     //Integer duration_h,
        @RequestParam (required = false) String state,          //Integer state,
        @RequestParam (required = false) String spentTime,     //Integer spent_time,
        @RequestParam (required = false) String isGraph,        //Boolean is_graph,
        @RequestParam (required = false) String userCorrectSpentTime,        //Boolean userCorrectSpentTime
        @RequestParam (required = false) String taskId        //Task ID

    ) throws ParseException, IllegalArgumentException
    {
        Event event;
        if (filterLongValue(id) == null)
            event = new Event();
        else
            event = eventRepository.findById(filterLongValue(id)).orElseThrow(IllegalArgumentException::new);

        Task task;

        switch(_operationType){
            case "add":{
                //Добавление новой задачи с графика
                Long idTask = filterLongValue(taskId);
                if (idTask ==  null)
                {
                    task = new Task(
                        name,
                        filterStringValue(description),
                        lane,
                        filterLongValue(author),
                        filterLongValue(executor),
                        filterIntValue(priority),
                        filterLongValue(wuser),
                        filterLongValue(wgroup),
                        filterLongValue(ruser),
                        filterLongValue(rgroup),
                        filterDoubleValue(duration_h),
                        false // эту задачу не нужно показывать в бэклоге
                    );
                    task = taskRepository.save(task);
                }
                else
                    task = taskRepository.findById(idTask).orElseThrow(IllegalArgumentException::new );

                Date dEnd = filterDateValue(endDate);
                dEnd.setHours(23);
                dEnd.setMinutes(59);
                dEnd.setSeconds(59);

                Event e = new Event(
                        task,
                        0,
                        filterLongValue(executor),
                        filterDateValue(startDate),
                        dEnd,
                        eventWindowStyle,
                        filterIntValue(state)
                );
                event = eventRepository.save(e);
                event.setTaskId(task.getId());
                System.out.println("Event created");
                //сохранение границ интервала (startTime, endTime) в Task
                //TODO:: delete this after testing
//                task.recalculateFields();
                taskRepository.save(task);
                return new GwtResponse(0,1,1,new Event[] {event});
            }
            case "update":
            {

                Boolean needsCorrection = filterBooleanValue(userCorrectSpentTime);
                if ( needsCorrection == null ? false: needsCorrection ) // может быть null
                {
                    // если флаг коррекции поднят - записываем то, что пришло с клиента, и выходим
                    event.setSpentTime(filterDoubleValue(spentTime));
                    event.setUserCorrectSpentTime(false);
                    event = eventRepository.save(event);
                    return new  GwtResponse(0,1,1,event);
                }

                if (event.getState() != null && !event.getState().equals(filterIntValue(state)))  // если состояние задачи изменилось - считаем время выполнения
                {
                    // Установка времени выполнения задачи
                    needsCorrection = eventService.processEventStartAndSpentTime(event, filterIntValue(state));

                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime lastDate = event.getTask().getEvents().stream().map(e -> e.getEndDate()).max(Date::compareTo).orElseGet(Date::new).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                    // Модификация конечной даты для выполняющегося event-а
                    // Выполняется, если текущее состояние - PROCESS, и последнее изменение состояния было ранее, чем сегодня
                    if (filterIntValue(state).equals(EventState.PROCESS.ordinal()) && lastDate.getDayOfYear() != currentDate.getDayOfYear() && Duration.between(lastDate, currentDate).toDays() >= 0) {

                        LocalDateTime evStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

                        LocalDateTime evEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);;

                        //Нужно ли выполнять "разделение" задачи (создание нового event-а)?
                        if (Duration.between(lastDate, currentDate).toDays() > 0) {
                            event.setState(EventState.PROCESS.ordinal());
                            Event ev = new Event(
                                    event.getTask(),
                                    event.getTask().getEvents().size(),
                                    event.getExecutor(),
                                    Date.from(evStart.atZone(ZoneId.systemDefault()).toInstant()),
                                    Date.from(evEnd.atZone(ZoneId.systemDefault()).toInstant()),
                                    "s3_process",
                                    EventState.PROCESS.ordinal()
                            );
                            ev = eventRepository.save(ev);
                            return new GwtResponse(0, 1, 1, new Event[]{ev});
                        } else
                            event.setEndDate(Date.from(evEnd.atZone(ZoneId.systemDefault()).toInstant()));
                    }
                }
                else {
                    event.setSpentTime(filterDoubleValue(spentTime)); // иначе - просто пишем время выполнения с клиента
                }
                event.setUserCorrectSpentTime(needsCorrection);

                event.setLane(lane);
                event.setName(name);
                event.setDescription(description);

                //Block date modification where state is changed (client sends incorrect date in this case)
                LocalDateTime  d = (event.getStartDate() == null || event.getState().equals(filterIntValue(state))) ? filterLocalDateTimeValue(startDate) : LocalDateTime.ofInstant(event.getStartDate().toInstant(),ZoneId.systemDefault());
                event.setStartDate(Date.from(d.atZone(ZoneId.systemDefault()).toInstant()));

                d = (event.getEndDate() == null || event.getState().equals(filterIntValue(state))) ? filterLocalDateTimeValue(endDate) : LocalDateTime.ofInstant(event.getEndDate().toInstant(),ZoneId.systemDefault());
                event.setEndDate(Date.from(d.withHour(23).withMinute(59).withSecond(59).atZone(ZoneId.systemDefault()).toInstant()));

                event.setAuthor(filterLongValue(author));
                event.setWuser(filterLongValue(wuser));
                event.setWgroup(filterLongValue(wgroup));
                event.setRuser(filterLongValue(ruser));
                event.setRgroup(filterLongValue(rgroup));

                event.setExecutor(filterLongValue(executor));
                event.setPriority(filterIntValue(priority));
                event.setDuration_h(filterDoubleValue(duration_h));
                event.setState(filterIntValue(state));
                event.setIsGraph(filterBooleanValue(isGraph));

                event = eventRepository.save(event);
                System.out.println("Event saved");

                task = event.getTask();
                task.recalculateFields();
                taskRepository.save(task);

                return new GwtResponse(0,1,1,event.getTaskId()==null ? event : task.getEvents());
            }
            case "remove":
            {
                taskRepository.delete(event.getTask());
                return new GwtResponse(0,1,1,new Event[] {});
            }
        }
        return new GwtResponse(0,1,1,event.getTaskId()==null ? event : eventRepository.findAllByTaskId(event.getTaskId()));
    }

    @PostMapping("/moveToBacklog")
    @ResponseBody
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GwtResponse moveToBacklog(@RequestParam String taskId)
    {
        taskService.moveToBacklog(filterLongValue(taskId));
        return new GwtResponse(0,1,1,new Event[] {});
    }

}
