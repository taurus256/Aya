package org.taurus.aya.servlets.advicers;

import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Lane;
import org.taurus.aya.server.entity.Task;
import org.taurus.aya.server.entity.User;
import org.taurus.aya.servlets.AdviceException;
import org.taurus.aya.shared.Advice;
import org.taurus.aya.shared.AdviceState;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MatrixAdvicer {

    private HashMap<Long,Integer> userIds = new HashMap<>();
    private HashMap<String,Integer> laneIds = new HashMap<>();
    private HashMap<Long,String> users= new HashMap<>();

    private StatData[][] matrix;
    private static final double WORKDAY_HOURS = 8.0;

    private class Conglomerate{

        Date startDate, endDate;
        Double spentTime;

        public Conglomerate(Event e) throws AdviceException
        {
            if (e.getStartDate().getTime() >= e.getEndDate().getTime())
                throw new AdviceException("У задачи '" + e.getName() + "' некорректное время начала и окончания (" + e.getStartDate().toString() + " - " + e.getEndDate().toString() + ")");

            startDate = e.getStartDate();
            endDate = e.getEndDate();
            spentTime = e.getSpentTime();
        }

        public boolean addToCurrent(Event e) throws AdviceException
        {
            if (e.getStartDate().getTime() >= e.getEndDate().getTime())
                throw new AdviceException("У задачи '" + e.getName() + "' некорректное время начала и окончания (" + e.getStartDate().toString() + " -  " + e.getEndDate().toString() + ")");

            if (e.getStartDate().getTime() < endDate.getTime()) {
                add(e);
                return true;
            }
            else
                return false;
        }

        private void add(Event e)
        {
            endDate = e.getEndDate();
            spentTime+=e.getSpentTime();
        }

        public Double getCalculatedDayDuration()
        {
             return spentTime / ((endDate.getTime() - startDate.getTime())/(24*3600*1000.0));
        }
    }

    public List<Advice> compute(
            Long userId,
            List<User> userList,
            List<Lane> laneList,
            LinkedList<Task> oldTasksList,
            LinkedList<Event> oldEventsList,
            LinkedList<Event> futureEventsList
            )
    {
        List<Advice> advices = new LinkedList<>();
        try {

            if (futureEventsList.size() == 0) throw new AdviceException("Нет запланированных задач");
            if (oldEventsList.size() == 0) throw new AdviceException("Нет ни одной завершенной задачи за последние 60 дней");

            Optional<Event> b = oldEventsList.stream().filter(e -> e.getStartDate().getTime() >= e.getEndDate().getTime()).findFirst();
            if (b.isPresent())
                throw new AdviceException("У задачи '" + b.get().getName() + "' некорректное время начала и окончания (" + b.get().getStartDate().toString() + " -  " + b.get().getEndDate().toString() + ")");;
            b = futureEventsList.stream().filter(e -> e.getStartDate().getTime() >= e.getEndDate().getTime()).findFirst();
            if (b.isPresent())
                throw new AdviceException("У задачи '" + b.get().getName() + "' некорректное время начала и окончания (" + b.get().getStartDate().toString() + " -  " + b.get().getEndDate().toString() + ")");;

            initialize(userList, laneList, oldTasksList);

            // расчет прогнозируемых времен выполнения потоков
            Map<String, Double> lanePrognosisMap = computeFutureLanePrognosis(futureEventsList);

            for (String k : lanePrognosisMap.keySet()) System.out.println("lane prognosis: " + k + " " + lanePrognosisMap.get(k));

            // расчет прогнозируемых затрат времени для каждого пользователя
            Map<Long,Double> userPrognosisMap = computeFutureUserPrognosis(futureEventsList);
            System.out.println("userPrognosisMap = " + userPrognosisMap);
            Map<Long,Double> userDayDurations = getAverageUserDayDurations(userList, oldEventsList);

            // генерация советов (используют рассчитанные на предыдущем шаге времена)
            advices.add(generateLaneAdvice(futureEventsList,lanePrognosisMap, userDayDurations));
            advices.add(generateUserAdvice(futureEventsList, userPrognosisMap, userDayDurations));

            // генерация совета с данными о среднем времени, затрачиваемом в день на решение задач

            advices.add(generateAverageLoadAdvice(userId,userDayDurations));
        }
        catch(AdviceException ae)
        {
            advices.add(new Advice(AdviceState.NOT_DEFINED,ae.getLocalizedMessage()));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            advices.add(new Advice(AdviceState.NOT_DEFINED,ex.getLocalizedMessage()));
        }

        return advices;
    }

    public Map<String,Double> computeFutureLanePrognosis(LinkedList<Event>  eventFutureList) throws AdviceException
    {
        LinkedList<Event> internalList = new LinkedList(eventFutureList);
        Iterator iter = internalList.iterator();
        System.out.println("PAdvicer.computeFutureLanePrognosis hasNext=" + iter.hasNext());
        Map<String,LinkedList<Event>> laneMap = new HashMap<>();
        // Разбор задач по потокам
        while (iter.hasNext()) {
            Event e = internalList.remove();
            laneMap.computeIfAbsent(e.getLane(), k -> new LinkedList<>());
            laneMap.get(e.getLane()).add(e);
        }

        // Вычисление прогноза для каждого потока
        Map<String,Double> prognosis = new HashMap<>();
        for (String laneName: laneMap.keySet())
            prognosis.put(laneName,getLaneTime(laneMap.get(laneName)));

        return prognosis;
    }

    /** Вычисление суммарной прогнозируемой трудоемкости задач для каждого пользователя
     * @param eventFutureList список запланированных задач
     * @return Ассоциативный массив вида <Имя пользователя, вычисленная трудоемкость в часах>
    * */
    public Map<Long,Double> computeFutureUserPrognosis(LinkedList<Event>  eventFutureList) throws AdviceException
    {
        LinkedList<Event> internalList = new LinkedList(eventFutureList);
        Iterator iter = internalList.iterator();
        System.out.println("PAdvicer.computeFutureUserPrognosis hasNext=" + iter.hasNext());
        Map<Long,LinkedList<Event>> userMap = new HashMap<>();
        // Разбор задач по пользователям
        while (iter.hasNext()) {
            Event e = internalList.remove();
            userMap.computeIfAbsent(e.getExecutor().longValue(), k -> new LinkedList<>());
            userMap.get(e.getExecutor().longValue()).add(e);
        }

        // Вычисление прогноза для каждого пользователя
        Map<Long,Double> prognosis = new HashMap<>();
        for (Long user: userMap.keySet())
            prognosis.put(user,userMap.get(user).stream().map(e -> {try{ return getEventTimePrognosis(e);} catch(AdviceException ae){return 0;}}).mapToDouble(d -> d.doubleValue()).sum());

        return prognosis;
    }

    private Double getLaneTime(LinkedList<Event> lane) throws AdviceException
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

    private Double getEventTimePrognosis(Event event) throws AdviceException
    {
        if (laneIds.get(event.getLane()) == null) {
            System.err.println("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has unexpected lane " + event.getLane());
            throw new AdviceException("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has unexpected lane " + event.getLane());
        }
        if (userIds.get(event.getExecutor().longValue()) == null) {
            System.err.println("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has unexpected executor " + event.getExecutor());
            throw new AdviceException("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has unexpected executor " + event.getExecutor());
        }
        if (event.getDuration_h() == null)
        {
            System.err.println("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has null duration_h " + event.getExecutor());
            throw new AdviceException("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has null duration_h " + event.getExecutor());
        }

        double velocity = matrix[userIds.get(event.getExecutor().longValue())][laneIds.get(event.getLane())].getV();

        if (velocity==0)
        {
            System.err.println("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has zero velocity ");
            throw new AdviceException("PAdvicer: Event " + event.getId() + " '" + event.getName() + "' has zero velocity ");
        }
        else
            return event.getDuration_h() / velocity;
    }

    public void initialize(List<User> userList, List<Lane> laneList, List<Task> taskList) throws AdviceException
    {
        //Заполнение ассоциативного массива пользователей
        for (User u: userList)
            users.put(u.getId(),u.getShowedName());

        // Инициализация расчетной части.
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

        for (Task task: taskList) {
            int userIndex = userIds.getOrDefault(task.getExecutor().longValue(), -1);
            int laneIndex = laneIds.getOrDefault(task.getLane(), -1);

            if (laneIndex >=0 && userIndex >=0)
                matrix[userIndex][laneIndex].addTask(task);
            else {
                System.out.println("PAdvicer:initialize: Event " + task.getId() + " '" + task.getName() + "' has incorrect lane name or executor ID!" +
                        laneIndex + " " + userIndex + "e.getExecutor = " + task.getExecutor() + "  userIds.get=" + userIds.getOrDefault(task.getExecutor().longValue(), -1));
                throw new AdviceException("PAdvicer:initialize: Event " + task.getId() + " '" + task.getName() + "' has incorrect lane name or executor ID! " +
                        laneIndex + " " + userIndex + "e.getExecutor = " + task.getExecutor() + "  userIds.get=" + userIds.getOrDefault(task.getExecutor().longValue(), -1));
            }
        }

        prepareMatrix();

        System.out.println("PAdvicer: initialize: userIds");
        printList(userIds.keySet());
        System.out.println("PAdvicer: initialize: laneIds");
        printList(laneIds.keySet());
        System.out.println("PAdvicer: initialize: matrix");
        printMatrix(matrix);
    }

    private Advice generateLaneAdvice(List<Event> futureTaskList, Map<String,Double> prognosis, Map<Long,Double> userDayDurations) throws AdviceException
    {
        Date deadline = futureTaskList.stream().map(Event::getEndDate).max(Date::compareTo).orElseThrow(() -> new AdviceException("generateDeadlineAdvice: Cannot find max future date"));
        System.out.println("deadline = " + deadline);
        Duration toDeadline = Duration.between(Instant.now(),deadline.toInstant());
        System.out.println("toDeadline= = " + toDeadline.toDays());
        long daysToDeadline = toDeadline.toDays() + 1; //последний день перед дедлайном тоже считаем
        System.out.println("daysToDeadline = " + daysToDeadline);
        double prognosisHours = prognosis.values().stream().max(Double::compareTo).orElseThrow(() -> new AdviceException("generateDeadlineAdvice: Cannot find lane name with max duration"));
        double prognosisDays = prognosisHours/WORKDAY_HOURS;
        System.out.println("prognosisDays = " + prognosisHours/WORKDAY_HOURS);

        if (prognosisDays <= daysToDeadline)
            return new Advice(AdviceState.OK,"Расчетное время выполнения задач: <b>" + prognosisDays  + " дн.</b>" +
             "<br> Самый длинный поток: \"" +  prognosis.keySet().stream().filter(name -> prognosis.get(name).equals(prognosisHours)).findFirst().orElseThrow(() -> new AdviceException("generateDeadlineAdvice: Cannot find lane name with max value")) +
             "\"");
        else
            return new Advice(AdviceState.CRITICAL,
                "Расчетное время выполнения задач больше планируемого на <b>" + (prognosisDays-daysToDeadline) + " дн.</b>" +
                "<br> Самый длинный поток: \"" +  prognosis.keySet().stream().filter(lane -> prognosis.get(lane).equals(prognosisHours)).findFirst().orElseThrow(() -> new AdviceException("generateDeadlineAdvice: Cannot find lane name with max value")) +
                "\"");
    }

    /** Анализ прогнозируемой нагрузки на пользователей и генерация советов. Выдает предупреждение в случае, если
     * прогнозируемое время выполнения работ одним или несколькими пользователями выходит за планируемое время окончания работ
     * (конец крайней планируемой задачи). В противном случае выдает сообщение о том, у какого пользователя нагрузка максимальна
     * @param futureTaskList - список запланированных задач
     * @param prognosis ассоциативный массив с рассчитанной нагрузкой на пользователей в часах
     * @param dayDurations - ассоциативный массив, содержащий рассчитанное количество часов в день, которое пользователь тратит на задачи, для каждого пользователя
     * @return Предупреждение со списком пользователей, у которых превышена нагрузка или сообщение с именем максимально загруженного пользователя
     * */
    private Advice generateUserAdvice(List<Event> futureTaskList, Map<Long,Double> prognosis, Map<Long,Double> dayDurations) throws AdviceException
    {
        Date deadline = futureTaskList.stream().map(Event::getEndDate).max(Date::compareTo).orElseThrow(() -> new AdviceException("generateDeadlineAdvice: Cannot find max future date"));
        System.out.println("deadline = " + deadline);
        Duration toDeadline = Duration.between(Instant.now(),deadline.toInstant());
        System.out.println("toDeadline= = " + toDeadline.toDays());
        long daysToDeadline = toDeadline.toDays() + 1; //последний день перед дедлайном тоже считаем
        System.out.println("daysToDeadline = " + daysToDeadline);

        double maxPrognosisDays = -1;
        Long maxLoadedUser = null;
        final List<String> overloadedUsers = new LinkedList<String>();
        for (Long user: prognosis.keySet()){

            double prognosisDays = prognosis.get(user) / dayDurations.get(user);
            if (prognosisDays> maxPrognosisDays)
            {
                maxPrognosisDays = prognosisDays;
                maxLoadedUser = user;
            }
            if (prognosisDays > daysToDeadline) // фиксируем превышение нагрузки
                overloadedUsers.add("<b>" + getUserName(user) + "</b> на <b>" + String.format("%4.2f",(prognosisDays - daysToDeadline)) + " дн.</b>");
        }

        if (maxPrognosisDays < 0 || maxLoadedUser==null)
            throw new AdviceException("generateUserAdvice: Не удалось расчитать нагрузку для пользователей: данные прогноза отсутствуют или некорректны");

        System.out.println("prognosisDays = " + maxPrognosisDays);

        if (maxPrognosisDays <= daysToDeadline)
            return new Advice(AdviceState.OK,"<br> Самый загруженный пользователь: <b>\"" + getUserName(maxLoadedUser) + "\"</b>");
        else
            return new Advice(AdviceState.CRITICAL,
                    "Превышение нагрузки у:<br>" +
                    overloadedUsers.stream().collect(Collectors.joining("<br>")));
    }

    /** Заполнение нулевых полей мартрицы вычисленными средними */
    private void prepareMatrix() throws AdviceException
    {
        List<Double> velocityList = new LinkedList<>(); // средняя скорость для каждого из пользователей, для которых её можно посчитать
        List<Double> dispersionList = new ArrayList<>(); // максимальная дисперсия для каждого из пользователей, для которых её можно посчитать
        for (int i=0; i< userIds.size(); i++)
        {
            Double average = Arrays.asList(matrix[i]).stream().filter(x-> !x.getValueIsEmpty()).mapToDouble(StatData::getV).average().orElse(Double.NaN);
            Double dispersion = Arrays.asList(matrix[i]).stream().filter(x-> !x.getValueIsEmpty()).mapToDouble(StatData::getD).max().orElse(Double.NaN);
            Arrays.asList(matrix[i]).stream().filter(StatData::getValueIsEmpty).forEach(p -> {if (!average.equals(Double.NaN)) p.setV(average);});
            velocityList.add(average);
            dispersionList.add(dispersion);
        }
        Double avgAll = velocityList.stream().filter(x -> !x.equals(Double.NaN)).mapToDouble(x -> x).average().orElse(Double.NaN);

        if (avgAll.equals(Double.NaN)) throw new AdviceException("У выполненных задач указано нулевое время выполнения");

        for (int i=0; i< userIds.size(); i++) {
            final Double dispersion =  dispersionList.get(i);
            Arrays.asList(matrix[i]).parallelStream().filter(StatData::getValueIsEmpty).forEach(x -> x.setV(avgAll));
            Arrays.asList(matrix[i]).parallelStream().filter(StatData::getValueIsEmpty).forEach(x -> x.setD(dispersion));
        }
    }

    private List<Conglomerate> getConglomerates(List<Event> eventList) throws AdviceException
    {
        Queue<Event> eventQueue = new PriorityQueue<Event>(eventList.size(), (e1, e2) -> {
            long result = e1.getStartDate().getTime() - e2.getStartDate().getTime();
            if (result>0)
                return 1;
            else return (result<0)?-1:0;
        });
        eventQueue.addAll(eventList);

        Conglomerate c = null;
        List<Conglomerate> conglomerateList = new LinkedList<>();
        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.remove();
            if (c == null)
                c = new Conglomerate(e);
            else
            if (!c.addToCurrent(e)) { // если задача входит в текущий конгломерат -добавляем, иначе
                conglomerateList.add(c); // добавляем предыдущий в список
                c = new Conglomerate(e); // и создаем новый
            }
        }
        conglomerateList.add(c); // добавляем последний конгломерат (он не может добавиться в цикле)

        return conglomerateList;
    }

    /** Генерирует совет, выводящий среднее время, которое указанны пользователь тратит на решение задач
     * @param userId идентификатор пользователя
     * @param dayDurations - ассоциативный массив, содержащий рассчитанное количество часов в день, которое пользователь тратит на задачи, для каждого пользователя
     */
    public Advice generateAverageLoadAdvice(Long userId, Map<Long,Double> dayDurations) throws AdviceException
    {
        Double averageLoad = dayDurations.get(userId);
        if (averageLoad == null)
            throw new AdviceException("Для текущего пользователя не определены данные по средней нагрузке за день");
        else
            if (averageLoad >= 1.0 || averageLoad <= 12.0)
                return new Advice(AdviceState.NOT_DEFINED,"Среднее время, затрачиваемое на решение задач: <b>" +
                        String.format("%4.2f",averageLoad ) + " ч.</b> в день");
            else
                return new Advice(AdviceState.WARNING,"Среднее время, затрачиваемое на решение задач: <b>" +
                        String.format("%4.2f",averageLoad ) + " ч.</b> в день"
                        + "<br>Возможна ошибка в исходных данных");

    }

    private String getUserName(Long id)
    {
        return users.get(id);
    }

    private Map<Long,Double> getAverageUserDayDurations( List<User> userList, LinkedList<Event> oldEventsList) throws AdviceException
    {
        Map<Long,Double> userDayDurations = new HashMap<>();
        for (User u: userList) {
            List<Event> list = oldEventsList.stream().filter(e -> e.getExecutor()==u.getId().intValue()).collect(Collectors.toList());
            if (list.size()==0)
            {
                userDayDurations.put(u.getId(),8.0); // установить значение по умолчанию
                continue;
            }

            List<Conglomerate> conglomerateList = getConglomerates(list);

            userDayDurations.put(
                    u.getId(),
                    conglomerateList.stream().mapToDouble(Conglomerate::getCalculatedDayDuration)
                            .average()
                            .orElseThrow(() -> new AdviceException("Ошибка при расчете нагрузки для пользователя "+ u.getShowedName()))
            );
        }

        System.out.println("userDayDurations = " + userDayDurations);

        return userDayDurations;
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
