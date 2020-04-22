package org.taurus.aya.server.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.taurus.aya.server.EventRepository;
import org.taurus.aya.server.TaskRepository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private TaskRepository taskRepository;
    private EventRepository eventRepository;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    //2019-07-19T03:12:27.000
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public TaskService(@Autowired TaskRepository repository)
    {
        this.taskRepository = repository;
    }


    public List<Task> getData(String[] criteria) throws RuntimeException, ParseException
    {
        List<Task> taskList;

        HashMap<String,String> criteriaMap = parseCriteria(Arrays.stream(criteria).collect(Collectors.joining(",")));

        taskList = taskRepository.findAll();

        if (Boolean.valueOf(criteriaMap.getOrDefault("showInBacklog","true")))
                 taskList = taskRepository.findAllByShowInBacklogIsTrue();

        return taskList;
    }


    private HashMap<String,String> parseCriteria(String c)
    {
        HashMap<String,String> result = new HashMap<>();
        if (c != null)
                //System.out.println("input:'" + c + "'");
                if (c.contains("fieldName")) {
                    if (c.contains("\"criteria\":")) {
                        result.putAll(parseCriteria(
                                c.substring(c.indexOf("[") + 1, c.indexOf("]") - 1)
                                )
                        );
                    }
                    else {
                        String[] params = c.substring(1, c.length() - 1).split(",");
                        HashMap<String, String> parameterParts = new HashMap<>();
                        for (String param : params) {
                            String rawParam = param.replace("\"", "");
                            parameterParts.put(rawParam.substring(0, rawParam.indexOf(":")), rawParam.substring(rawParam.indexOf(":") + 1, rawParam.length()));
                        }
                        //System.out.println("Name=" + parameterParts.get("fieldName") + " Value=" + parameterParts.get("value"));
                        result.put(parameterParts.get("fieldName"), parameterParts.get("value"));
                    }
                }


        return result;
    }

    public Task moveToBacklog(Long id)
    {
        Optional<Task> optTask = taskRepository.findById(id);
        if (!optTask.isPresent()) throw new IllegalArgumentException("Cannot find task with id=" + id);
        Task task = optTask.get();
        task.setShowInBacklog(true);
        task.getEvents().clear();
        System.out.println("Task service - OK");
        return taskRepository.saveAndFlush(task);
    }


}
