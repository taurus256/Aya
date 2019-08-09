package org.taurus.aya.servlets;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.taurus.aya.client.AnalyticService;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Lane;
import org.taurus.aya.server.entity.User;
import org.taurus.aya.servlets.advicers.PAdvicer;
import org.taurus.aya.shared.TaskAnalyseData;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.annotation.WebServlet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

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


    @Override
    public TaskAnalyseData getPrognosis() throws Exception {

        Lane lane1 = new Lane();
        lane1.setName("Lane_1");

        Lane lane2 = new Lane();
        lane2.setName("Lane_2");

        User user1 = new User();
        user1.setId(1L);
        user1.setNickname("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setNickname("user2");

        User user3 = new User();
        user3.setId(3L);
        user3.setNickname("user3");

        Event e1 = new Event();
        e1.setId(1L);
        e1.setName("Name1");
        e1.setDuration_h(8);
        e1.setSpentTime(8);
        e1.setLane(lane1.getName());
        e1.setExecutor(user1.getId().intValue());

        Event e2 = new Event();
        e2.setId(2L);
        e2.setName("Name2");
        e2.setDuration_h(4);
        e2.setSpentTime(16);
        e2.setLane(lane1.getName());
        e2.setExecutor(user1.getId().intValue());

        Event e3 = new Event();
        e3.setId(3L);
        e3.setName("name3");
        e3.setDuration_h(8);
        e3.setStartDate(Date.from(Instant.now()));
        e3.setEndDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        e3.setLane(lane1.getName());
        e3.setExecutor(user1.getId().intValue());

        Event e4 = new Event();
        e4.setId(4L);
        e4.setName("name4");
        e4.setDuration_h(8);
        e4.setStartDate(Date.from(Instant.now().plus(1, ChronoUnit.HALF_DAYS)));
        e4.setEndDate(Date.from(Instant.now().plus(3, ChronoUnit.HALF_DAYS)));
        e4.setLane(lane1.getName());
        e4.setExecutor(user2.getId().intValue());

        Event e5 = new Event();
        e5.setId(5L);
        e5.setName("name5");
        e5.setDuration_h(8);
        e5.setStartDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        e5.setEndDate(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)));
        e5.setLane(lane1.getName());
        e5.setExecutor(user2.getId().intValue());

        Event e6 = new Event();
        e6.setId(6L);
        e6.setName("name6");
        e6.setDuration_h(8);
        e6.setSpentTime(6);
        e6.setLane(lane2.getName());
        e6.setExecutor(user2.getId().intValue());

        Event e7 = new Event();
        e7.setId(7L);
        e7.setName("name7");
        e7.setStartDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        e7.setEndDate(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)));
        e7.setDuration_h(8);
        e7.setSpentTime(8);
        e7.setLane(lane2.getName());
        e7.setExecutor(user3.getId().intValue());

        PAdvicer pAdvicer = new PAdvicer();

        List<User> userList = new LinkedList<User>();
        List<Lane> laneList = new LinkedList<>();
        LinkedList<Event> oldEventsList = new LinkedList<>();
        LinkedList<Event> futureEventsList = new LinkedList<>();

        userList.add(user1);
        userList.add(user2);
        userList.add(user3);

        laneList.add(lane1);
        laneList.add(lane2);

        oldEventsList.add(e1);
        oldEventsList.add(e2);
        oldEventsList.add(e6);

        futureEventsList.add(e3);
        futureEventsList.add(e4);
        futureEventsList.add(e5);
        futureEventsList.add(e7);

        pAdvicer.initialize(userList,laneList,oldEventsList);

        Map<String,Double> prognosisMap = pAdvicer.computeFuturePrognosis(futureEventsList);
        for (String k: prognosisMap.keySet())
            System.out.println("prognosis: " + k + " " + prognosisMap.get(k));

        return new TaskAnalyseData("Задач за предыдущий месяц: " + 5,"ЗАГОЛОВОК");
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
    private void createEMF()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.jdbc.user", "postgres");
        properties.put("javax.persistence.jdbc.password", "postgres");
        properties.put("javax.persistence.jdbc.driver","org.postgresql.Driver");
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("jdbc:postgresql://localhost:5432/s5", properties);
        }catch(Exception e)
        {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
