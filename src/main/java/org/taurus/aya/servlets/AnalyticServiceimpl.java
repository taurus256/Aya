package org.taurus.aya.servlets;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.taurus.aya.client.AnalyticService;
import org.taurus.aya.shared.TaskAnalyseData;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.annotation.WebServlet;
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

        Date current = new Date();

        // future tasks
//        List<HashMap<String,String>>  futureList =  dataSource_tasks.fetch(new com.isomorphic.criteria.AdvancedCriteria(DefaultOperators.And, new com.isomorphic.criteria.Criterion[]{new SimpleCriterion("startDate", "greaterThan",current)}));
//        System.out.println("::::2" +futureList);
//
//        int sumFutureTime = 0;
//        for (HashMap r: futureList) {
//            if (r.get("duration_h") != null && (Double) (r.get("duration_h")) != 0) {
//                sumFutureTime += (Double) (r.get("duration_h"));
//                System.out.println("FUTURE:" + r.get("duration_h"));
//
//            }
//
//        }
//
//        //Retrieving data from datasource
//        @SuppressWarnings("rawtypes")
//
//        //TODO: simplify this code
//        DSRequest dsr_fetch = new DSRequest(dataSource_tasks,"fetch");
//        dsr_fetch.setOperationId("getTasks");
//        com.isomorphic.datasource.DSResponse resp = dataSource_tasks.executeCustom(dsr_fetch);
//        List<HashMap> list = resp.getRecords();
//        System.out.println(list);
//        System.out.println(list.size());
//
//        DSRequest dsr = new DSRequest(dataSource_tasks,"fetch");
//        dsr.addToCriteria("start",new Timestamp((new Date().getTime()-3600*10000*72L)));
//        dsr.addToCriteria("end",new Timestamp(new Date().getTime()));
//        dsr.setOperationId("getTime");
//        com.isomorphic.datasource.DSResponse k=null;
//        try{
//        k = dataSource_tasks.executeCustom(dsr);
//        }
//        catch(Exception e)
//        {
//            System.out.println(e.getLocalizedMessage());
//        }
//        System.out.println(dsr.getCriteria());
//        System.out.println(new Timestamp((new Date().getTime()-3600*10000*72L)));
//        System.out.println(new Timestamp(new Date().getTime()));
//        System.out.println(k.getRecords());
//        System.out.println(k.getRecords().size());
//
//        List<Double> tasksVelocityList = new LinkedList<Double>();
//
//        double sumTime = 0;
//        for (HashMap r: list) {
//            if (r.get("active_time") != null && (Double) (r.get("active_time")) != 0 && r.get("duration_h") != null && (Double) (r.get("duration_h")) != 0) {
//                tasksVelocityList.add(1.0 * (Double) (r.get("duration_h")) / (Double) (r.get("active_time")));
//                sumTime += (Double) (r.get("duration_h"));
//            }
//        }
//
//        Double velocity = tasksVelocityList.stream().mapToDouble(a -> a).average().getAsDouble();
//        System.out.println("Velocity= " + velocity);

//        String s="<h3>Статистика по задачам</h3><br>";
//        s+="<b>Средняя скорость</b>: " + velocity + "<br>";
//        s+="<b>Трудоемкость за месяц</b>: " + sumTime/8.0 + " условных дней <br><hr>";
//        s+="<b>Трудоемкость будущих задач</b>: " + sumFutureTime/8.0 + " условных дней <br>";
//        s+="<b>Прогнозируемое время выполнения</b>: " + sumFutureTime/velocity/8.0 + " дней <br><hr>";
//
//        for (Double t: tasksVelocityList) s += t + " ";
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
