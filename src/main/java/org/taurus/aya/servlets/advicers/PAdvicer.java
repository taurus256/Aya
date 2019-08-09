package org.taurus.aya.servlets.advicers;

import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Lane;
import org.taurus.aya.server.entity.User;

import java.time.Instant;
import java.util.*;

public class PAdvicer {

    private HashMap<Long,Integer> userIds = new HashMap<>();
    private HashMap<String,Integer> laneIds = new HashMap<>();

    private StatData[][] matrix;

    public static String compute(
            List<User> userList,
            List<Lane> laneList,
            ArrayList<Event> eventPastList,
            LinkedList<Event> eventFutureList
            )
    {
        return "";
    }

    public Map<String,Double> computeFuturePrognosis(LinkedList<Event>  eventFutureList)
    {
        Iterator iter = eventFutureList.iterator();
        System.out.println("PAdvicer.computeFuturePrognosis hasNext=" + iter.hasNext());
        Map<String,LinkedList<Event>> laneMap = new HashMap<>();
        // Разбор задач по потокам
        while (iter.hasNext()) {
            Event e = eventFutureList.remove();
            laneMap.computeIfAbsent(e.getLane(), k -> new LinkedList<>());
            laneMap.get(e.getLane()).add(e);
        }

        // Вычисление прогноза для каждого потока
        Map<String,Double> prognosis = new HashMap<>();
        for (String laneName: laneMap.keySet())
            prognosis.put(laneName,getLaneTime(laneMap.get(laneName)));

        return prognosis;
    }

    private Double getLaneTime(LinkedList<Event> lane)
    {
        lane.sort(new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                long diff = e1.getStartDate().getTime() - e2.getStartDate().getTime();
                if (diff<0)
                    return -1;
                else
                    if (diff>0) return 1;
                return 0;
            }
        });

        Double T=0.0;
        Double t=0.0;
        Instant cursor = Instant.MIN;

        Iterator it = lane.listIterator();
        while (it.hasNext())
        {
            Event e = lane.remove();
            System.out.println("PAdvicer: getLaneTime: e.getName() = " + e.getName());
            if (e.getEndDate().toInstant().isBefore(cursor) || e.getEndDate().toInstant().equals(cursor)) {
                Double prognosis = getEventTimePrognosis(e);
                if (prognosis> t) t = prognosis;
            }
            if (e.getStartDate().toInstant().isAfter(cursor) || e.getStartDate().toInstant().equals(cursor))
            {
                Double prognosis = getEventTimePrognosis(e);
                if (prognosis> t) t = prognosis;

                T += t;
                t = 0.0;
                cursor = e.getEndDate().toInstant();
            }
        }

        return T;
    }

    private Double getEventTimePrognosis(Event event)
    {
        if (laneIds.get(event.getLane()) == null) {
            System.err.println("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has unexpected lane " + event.getLane());
            return 0d;
        }
        if (userIds.get(event.getExecutor().longValue()) == null) {
            System.err.println("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has unexpected executor " + event.getExecutor());
            return 0d;
        }

        double velocity = matrix[userIds.get(event.getExecutor().longValue())][laneIds.get(event.getLane())].getV();

        if (velocity==0)
        {
            System.err.println("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has zero velocity ");
            return 0d;
        }
        else
            return event.getDuration_h() / velocity;
    }

    public void initialize(List<User> userList, List<Lane> laneList, List<Event> eventList)
    {
        // Инициализация списков значений Id
        int i=0;
        for (User u: userList) userIds.put(u.getId(),i++);

        i=0;
        for (Lane l: laneList) laneIds.put(l.getName(),i++);

        // Инициализация массива
        matrix = new StatData[userList.size()][laneList.size()];
        for (i=0; i < userList.size(); i++)
            for (int j=0; j < laneList.size(); j++)
                matrix[i][j] = new StatData();

        for (Event e: eventList) {
            int userIndex = userIds.getOrDefault(e.getExecutor().longValue(), -1);
            int laneIndex = laneIds.getOrDefault(e.getLane(), -1);

            if (laneIndex >=0 && userIndex >=0)
                matrix[userIndex][laneIndex].addEvent(e);
            else
                System.out.println("PAdvicer:initialize: gEvent " + e.getId() + " '" + e.getName() + "' has incorrect lane name or executor ID!"+
                laneIndex + " " + userIndex + "e.getExecutor = " + e.getExecutor() + "  userIds.get=" +  userIds.getOrDefault(e.getExecutor().longValue(), -1));
        }

        prepareMatrix();

        System.out.println("PAdvicer: initialize: userIds");
        printList(userIds.keySet());
        System.out.println("PAdvicer: initialize: laneIds");
        printList(laneIds.keySet());
        System.out.println("PAdvicer: initialize: matrix");
        printMatrix(matrix);
    }

    /** Заполнение нулевых полей мартрицы вычисленными средними */
    private void prepareMatrix()
    {
        List<Double> velocityList = new LinkedList<>(); // средняя скорость для каждого из пользователей, для которых её можно посчитать
        List<Double> dispersionList = new ArrayList<>(); // максимальная дисперсия для каждого из пользователей, для которых её можно посчитать
        for (int i=0; i< userIds.size(); i++)
        {
            Double average = Arrays.asList(matrix[i]).stream().filter(x-> !x.getValueIsEmpty()).mapToDouble(StatData::getV).average().orElse(Double.NaN);
            Double dispersion = Arrays.asList(matrix[i]).stream().filter(x-> !x.getValueIsEmpty()).mapToDouble(StatData::getD).max().orElse(Double.NaN);
            Arrays.asList(matrix[i]).stream().filter(x-> x.getValueIsEmpty()).forEach(p -> {if (!average.equals(Double.NaN)) p.setV(average);});
            velocityList.add(average);
            dispersionList.add(dispersion);
        }
        Double avgAll = velocityList.stream().filter(x -> !x.equals(Double.NaN)).mapToDouble(x -> x).average().orElse(Double.NaN);

        if (avgAll.equals(Double.NaN)) throw new RuntimeException("ALL velocity values is NaN: input data is incorrect!");

        for (int i=0; i< userIds.size(); i++) {
            final Double dispersion =  dispersionList.get(i);
            Arrays.asList(matrix[i]).parallelStream().filter(x -> x.getValueIsEmpty()).forEach(x -> x.setV(avgAll));
            Arrays.asList(matrix[i]).parallelStream().filter(x -> x.getValueIsEmpty()).forEach(x -> x.setD(dispersion));
        }
    }

    private void printList(Collection l)
    {
        System.out.print("[");
        Iterator it = l.iterator();
        while (it.hasNext())
        {
            Object o = it.next();
            System.out.print(o);
        }
        System.out.println("]");
    }

    private void printMatrix(StatData[][] m)
    {
        System.out.println("MATRIX[");
        for (int i=0; i< userIds.keySet().size(); i++) {
        System.out.print("[");
            for (int j = 0; j < laneIds.keySet().size(); j++)
                System.out.print(m[i][j].getV() + " ");
        System.out.println("]");
        }
    }
}
