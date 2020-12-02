package org.taurus.aya.server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.TaskRepository;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Task;
import org.taurus.aya.server.services.TaskService;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController extends GenericController {

    private TaskRepository taskRepository;
    private TaskService taskService;
    private UserRepository userRepository;

    public TaskController( @Autowired TaskRepository taskRepository, @Autowired TaskService taskService, @Autowired UserRepository userRepository)
    {
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @ResponseBody
    @PostMapping("/fetch")
    @Transactional
    public GwtResponse execute(HttpServletRequest request, @RequestParam String _operationType, @RequestParam (required=false) String[] criteria) throws RuntimeException, ParseException
    {

        System.out.println("Operation_type=" + _operationType);
        System.out.println("request body is:" + _operationType);

        List<Task> tasks = taskService.getData(criteria, getUserData(request, userRepository));
        return new GwtResponse(0,tasks.size(),tasks.size(), tasks);
    }

    @PostMapping("/modify")
    @ResponseBody
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GwtResponse executePost(
        @RequestParam String _operationType,
        @RequestParam (required = false) String id,              //integer,
        @RequestParam (required = false) String lane,
        @RequestParam (required = false) String name,
        @RequestParam (required = false) String description,
        @RequestParam (required = false) String author,            //Long author,
        @RequestParam (required = false) String wuser,          //Integer wuser,
        @RequestParam (required = false) String wgroup,         //Integer wgroup,
        @RequestParam (required = false) String ruser,          //Integer ruser,
        @RequestParam (required = false) String rgroup,         //Integer rgroup,
        @RequestParam (required = false) String executor,       //Integer executor,
        @RequestParam (required = false) String priority,                          //  Integer priority
        @RequestParam (required = false) String plannedDuration,     //former Integer duration_h,
        @RequestParam (required = false) String state,          //Integer state,
        @RequestParam (required = false) String processTime,     //Integer spent_time,
        @RequestParam (required = false) String showInBacklog,        //Boolean is_backlog,
        @RequestParam (required = false) String externalJiraTaskId        //Boolean is_backlog,
    ) throws ParseException
    {
        Task task;

        switch(_operationType){
            case "add":{
                //Добавление новой задачи
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
                    filterDoubleValue(plannedDuration),
                    true, // эту задачу нужно показывать в бэклоге
                    externalJiraTaskId
                );
                task = taskRepository.save(task);

                return new GwtResponse(0,1,1,new Task[] {task});
            }
            case "update":
            {
                Long taskId = filterLongValue(id);
                if (taskId==null) System.err.println("Task id cannot be NULL!");
                task = taskRepository.getOne(taskId);

                task.setLane(lane);
                task.setName(name);
                task.setDescription(description);


                task.setAuthor(filterLongValue(author));
                task.setWuser(filterLongValue(wuser));
                task.setWgroup(filterLongValue(wgroup));
                task.setRuser(filterLongValue(ruser));
                task.setRgroup(filterLongValue(rgroup));

                task.setExecutor(filterLongValue(executor));
                task.setPriority(filterIntValue(priority));
                task.setState(filterIntValue(state));
                task.setPlannedDuration(filterDoubleValue(plannedDuration));
                task.setShowInBacklog(filterBooleanValue(showInBacklog));
                task = taskRepository.save(task);
                return new GwtResponse(0,1,1,new Task[] {task});
            }
            case "remove":
            {
                taskRepository.deleteById(filterLongValue(id));
                return new GwtResponse(0,1,1,new Task[] {});
            }
        }
        return new GwtResponse(0,1,1,new Task[] {});
    }
}
