package org.taurus.aya.servlets;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.taurus.aya.client.AnalyticService;
import org.taurus.aya.client.EventState;
import org.taurus.aya.server.EventRepository;
import org.taurus.aya.server.LaneRepository;
import org.taurus.aya.server.TaskRepository;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Group;
import org.taurus.aya.server.entity.Lane;
import org.taurus.aya.server.entity.User;
import org.taurus.aya.servlets.advicers.MatrixAdvicer;
import org.taurus.aya.servlets.advicers.StatData;
import org.taurus.aya.shared.GraphData;
import org.taurus.aya.shared.TaskAnalyseData;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/app/aya/analytic", loadOnStartup = 1)
public class AnalyticServiceimpl extends RemoteServiceServlet implements AnalyticService {

// current_date + interval '1 day'

//
//    // tasks at last month
//    com.isomorphic.criteria.AdvancedCriteria criteria = new AdvancedCriteria(DefaultOperators.And, new Criterion[]{
//            new SimpleCriterion("endDate", "greaterThan", new Date(current.getTime()-1000*3600*24*30L)),
//            new SimpleCriterion("endDate", "lessThan", current),
//            new SimpleCriterion("state", "equals", TaskState.READY.ordinal())
//    });


    @Autowired
    TaskRepository taskRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LaneRepository laneRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
                config.getServletContext());
    }

    // коэффициент, устанавливающий степень превышения значений графика группы над графиком пользователя
    double GROUP_USER_RAISE_FACTOR = 1.0;

    @Override
    public TaskAnalyseData getPrognosis(Long userId) throws Exception {

        System.out.println("AnalyticServiceimpl.getPrognosis");
        String labelText="";

        User user = userRepository.findById(userId).orElseThrow(Exception::new);
        List<Long> groups = user.getGroups().stream().map(Group::getId).collect(Collectors.toList());

        List<User> userList = userRepository.findAll();
        List<Lane> laneList = laneRepository.findAll(userId,groups);
        List<String> laneNames = laneList.stream().map(Lane::getName).collect(Collectors.toList());

            //Ищем "будущие" задачи на графике: имеющие статус NEW и дату завершения сегодня или в будущем
            LinkedList<Event> futureEventsList = eventRepository.findAllByStartDateGreaterThanAndIsState(
                    Date.from(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC)),
                    laneNames
            );

            System.out.println("eventRepository futureEventsList() = " + futureEventsList.size());

            Date deadline = eventRepository.findAllByStartDateGreaterThanAndIsState(new Date(), laneNames).stream().map(e -> e.getEndDate()).max(Date::compareTo).orElse(new Date());
            System.out.println("AnalyticServiceimpl deadline = " + deadline);

            // Получаем количество задач, выполненное в прошлом за время, равное горизонту планирования
            Duration interval = Duration.between(Instant.now(), deadline.toInstant());
            Instant start = Instant.now().minus(Duration.ofDays(60));
            Instant today = LocalDateTime.now().plus(1, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC);
//            Integer executedTaskCount = eventRepository.findAllByEndDateGreaterThanAndEndDateLessThanAndIsGraphIsTrueAndState(Date.from(start), Date.from(today), EventState.READY.ordinal(), laneNames).size();
//            System.out.println("AnalyticServiceimpl executedTaskCount = " + executedTaskCount);

            //Получаем список задач, выполненных за последний месяц
            LinkedList oldEventsList = eventRepository.findAllByEndDateGreaterThanAndEndDateLessThanAndIsGraphIsTrueAndState(Date.from(start), new Date(), EventState.READY.ordinal(), laneNames);
            LinkedList oldTasksList = taskRepository.findAllByEndDateGreaterThanAndEndDateLessThanAndState(Date.from(start), Date.from(today), EventState.READY.ordinal());
            System.out.println("AnalyticServiceimpl oldEventList.size = " + oldEventsList.size());
            System.out.println("AnalyticServiceimpl oldTasksList.size = " + oldTasksList.size());

            //        Lane lane1 = new Lane();
            //        lane1.setName("Lane_1");но
            //
            //        Lane lane2 = new Lane();
            //        lane2.setName("Lane_2");
            //
            //        User user1 = new User();
            //        user1.setId(1L);
            //        user1.setNickname("user1");
            //
            //        User user2 = new User();
            //        user2.setId(2L);
            //        user2.setNickname("user2");
            //
            //        User user3 = new User();
            //        user3.setId(3L);
            //        user3.setNickname("user3");
            //
            //        Event e1 = new Event();
            //        e1.setId(1L);
            //        e1.setName("Name1");
            //        e1.setDuration_h(8);
            //        e1.setSpentTime(8);
            //        e1.setLane(lane1.getName());
            //        e1.setExecutor(user1.getId().intValue());
            //
            //        Event e2 = new Event();
            //        e2.setId(2L);
            //        e2.setName("Name2");
            //        e2.setDuration_h(4);
            //        e2.setSpentTime(16);
            //        e2.setLane(lane1.getName());
            //        e2.setExecutor(user1.getId().intValue());

            //        Event e3 = new Event();
            //        e3.setId(3L);
            //        e3.setName("name3");
            //        e3.setDuration_h(8);
            //        e3.setStartDate(Date.from(Instant.now()));
            //        e3.setEndDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
            //        e3.setLane(lane1.getName());
            //        e3.setExecutor(user1.getId().intValue());
            //
            //        Event e4 = new Event();
            //        e4.setId(4L);
            //        e4.setName("name4");
            //        e4.setDuration_h(8);
            //        e4.setStartDate(Date.from(Instant.now().plus(1, ChronoUnit.HALF_DAYS)));
            //        e4.setEndDate(Date.from(Instant.now().plus(3, ChronoUnit.HALF_DAYS)));
            //        e4.setLane(lane1.getName());
            //        e4.setExecutor(user2.getId().intValue());
            //
            //        Event e5 = new Event();
            //        e5.setId(5L);
            //        e5.setName("name5");
            //        e5.setDuration_h(8);
            //        e5.setStartDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
            //        e5.setEndDate(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)));
            //        e5.setLane(lane1.getName());
            //        e5.setExecutor(user2.getId().intValue());
            //
            //        Event e6 = new Event();
            //        e6.setId(6L);
            //        e6.setName("name6");
            //        e6.setDuration_h(8);
            //        e6.setSpentTime(6);
            //        e6.setLane(lane2.getName());
            //        e6.setExecutor(user2.getId().intValue());
            //
            //        Event e7 = new Event();
            //        e7.setId(7L);
            //        e7.setName("name7");
            //        e7.setStartDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
            //        e7.setEndDate(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)));
            //        e7.setDuration_h(8);
            //        e7.setSpentTime(8);
            //        e7.setLane(lane2.getName());
            //        e7.setExecutor(user3.getId().intValue());
            //
            //        PAdvicer pAdvicer = new PAdvicer();
            //
            //        List<User> userList = new LinkedList<User>();
            //        List<Lane> laneList = new LinkedList<>();
            //        LinkedList<Event> oldEventsList = new LinkedList<>();
            //        LinkedList<Event> futureEventsList = new LinkedList<>();
            //
            //        userList.add(user1);
            //        userList.add(user2);
            //        userList.add(user3);
            //
            //        laneList.add(lane1);
            //        laneList.add(lane2);
            //
            //        oldEventsList.add(e1);
            //        oldEventsList.add(e2);
            //        oldEventsList.add(e6);
            //
            //        futureEventsList.add(e3);
            //        futureEventsList.add(e4);
            //        futureEventsList.add(e5);
            //        futureEventsList.add(e7);


            labelText = getLabel(oldEventsList, futureEventsList);

            return new TaskAnalyseData(labelText,new MatrixAdvicer().compute(
            userId,
            userList,
            laneList,
            oldTasksList,
            oldEventsList,
            futureEventsList
            ));


    }

    private String getLabel(List<Event> oldEventList, List<Event> futureEventList)
    {
        //return oldEventList.size() + "/" + futureEventList.size();
        return "Прогноз";
    }

    private int dayCount(Date start, Date end) throws Exception
    {
        int workingDays = 0;

        Calendar startdate = Calendar.getInstance();
        startdate.setTime(start);;

        Calendar enddate = Calendar.getInstance();
        enddate.setTime(end);

        while (!startdate.after(enddate)) {
            int day = startdate.get(Calendar.DAY_OF_WEEK);
            System.out.println(day);
            if ((day != Calendar.SATURDAY) && (day != Calendar.SUNDAY)) {
                workingDays++;
            }

            // increment start date, otherwise while will give infinite loop
            startdate.add(Calendar.DATE, 1);
        }

        return workingDays;
    }

    public GraphData getMonthGraph(Long userId, Date startDate, Date endDate) throws IllegalArgumentException
    {
        User user = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        List<Long> groups = user.getGroups().stream().map(Group::getId).collect(Collectors.toList());
        List<String> lanesString = laneRepository.findAll(userId,groups).stream().map(Lane::getName).collect(Collectors.toList());
        //здесь получить список потоков

        LocalDateTime startDateTime = LocalDateTime.ofInstant(startDate.toInstant(),ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0);//начало расчетного периода
        LocalDateTime endDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0).withSecond(0); // конец расчетного периода - день, следующий за последним запрашиваемым
        List<Event> list = eventRepository.findAllByEndDateGreaterThanAndEndDateLessThanAndState(Date.from(startDateTime.toInstant(ZoneOffset.UTC)), Date.from(endDateTime.toInstant(ZoneOffset.UTC)), EventState.READY.ordinal(), lanesString);
        Duration d = Duration.between(startDateTime,endDateTime);
        double[] daysLocal = new double[Long.valueOf(d.toDays()).intValue()];
        Arrays.setAll(daysLocal,(a) -> {return 0.0;});

        double[] daysGroup = new double[Long.valueOf(d.toDays()).intValue()];
        Arrays.setAll(daysGroup,(a) -> {return 0.0;});

        //setting the captions array
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM");
        String[] captions =new String[Long.valueOf(d.toDays()).intValue()];
        Arrays.setAll(captions,i -> {return startDateTime.plus(i, ChronoUnit.DAYS).format(df);});


        for (Event e: list){
            LocalDateTime eventLocalStart = e.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime eventLocalEnd = e.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            double dayCost = e.getSpentTime()/(Duration.between(eventLocalStart, eventLocalEnd).toDays() + 1);
            if (eventLocalStart.isBefore(startDateTime)) eventLocalStart = startDateTime;
            if (eventLocalEnd.isAfter(endDateTime)) throw new IllegalArgumentException("Для задачи '" + e.getName() + "' указано неверное время завершения");
            long offsetStart = Duration.between(startDateTime,eventLocalStart).toDays();
            long offsetEnd = Duration.between(startDateTime, eventLocalEnd).toDays();
            if (userId.equals(e.getTask().getExecutor()))
                for (int i=Long.valueOf(offsetStart).intValue(); i <= offsetEnd; i++) {
                    daysLocal[i] += dayCost;
                    daysGroup[i] += dayCost;
                }
            else
                for (int i=Long.valueOf(offsetStart).intValue(); i <= offsetEnd; i++)
                    daysGroup[i] += dayCost;

        }
        return new GraphData(
                            Arrays.stream(daysLocal).mapToLong(Math::round).boxed().toArray(Long[]::new),
                            Arrays.stream(daysGroup).mapToLong(Math::round).boxed().toArray(Long[]::new),
                            captions,
                            getStatistics(userId, startDate, endDate)
        );
    }

    public String getStatistics(Long userId, Date startDate, Date endDate)
    {
        MatrixAdvicer matrixAdvicer = new MatrixAdvicer();
        Instant start = LocalDateTime.now().minusMonths(1).toInstant(ZoneOffset.UTC); //используется смещение по умолчанию (UTC)

        User user = userRepository.findById(userId).get(); //!
        List<Long> groups = user.getGroups().stream().map(Group::getId).collect(Collectors.toList());

        List<User> userList = userRepository.findAll();
        List<Lane> laneList = laneRepository.findAll(userId, groups);

        try {

            StatData[] stats = matrixAdvicer.calculateUserStatustics(userId,
                    userList,
                    laneList,
                    taskRepository.findAllByEndDateGreaterThanAndEndDateLessThanAndState(startDate, endDate, EventState.READY.ordinal()));


            List<Pair<String,String>> pairs = new LinkedList<>();
            for (int i=0; i<laneList.size(); i++)
                pairs.add(Pair.of(laneList.get(i).getName(),String.format("%4.2f",stats[i].getTolalTime())));

            String totalTime = generateHTMLTable("Общее затраченное время", pairs);

            pairs.clear();
            for (int i=0; i<laneList.size(); i++)
                pairs.add(Pair.of(laneList.get(i).getName(),String.valueOf(String.format("%4.2f",stats[i].getSigma()))));

            String dispertions = generateHTMLTable("Среднее квадратичное отклонение", pairs);

            return totalTime + "<br>" + dispertions;

        }
        catch(AdviceException aex)
        {
            return aex.getMessage();
        }
    }

    private String generateHTMLTable(String caption, List<Pair<String,String>> data)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<br><b>" + caption + "</b><br><br>");
        sb.append("<center>");
        sb.append("<table class=\"aya_table\" ");
        sb.append("<tr><td>Поток</td><td>Значение</td></tr>");
        for (Pair<String,String> d: data)
            sb.append("<tr><td>" + d.getFirst() + "</td><td>" + d.getSecond() + "</td></tr>");
        sb.append("</table>");
        sb.append("</center>");
        return sb.toString();
    }

}
