package org.taurus.aya.servlets.advicers;

import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Task;

import java.util.LinkedList;
import java.util.List;

public class StatData {

    List<Double> velocityList = new LinkedList<>();
    int n=0;
    Double V = null;
    Double D = null;
    boolean valueIsEmpty = true;
    Double totalTime = 0.0;

    /** Добавление задачи в список для расчета статистики
    * @param t - entity задачи
    * */
    void addTask(Task t)
    {
        System.out.println("StatData.addEvent");
        if (t.getPlannedDuration()!=null && t.getSpentTime()!=null && t.getSpentTime()!=0) {
            velocityList.add(t.getPlannedDuration() / t.getSpentTime());
            n++;
        }
        totalTime += t.getSpentTime();
    }

    /** Возвращает среднюю скорость выполнения задач. При первом обращении - считает её
    * @return Значение средней скорости
    * */
    Double getV()
    {
        if (V == null)
            V = velocityList.stream().mapToDouble(x -> x).average().orElse(Double.NaN);

        return V;
    }

    void setV(Double V)
    {
        if (!V.equals(Double.NaN)) {
            this.V = V;
            valueIsEmpty = false;
        }
    }

    /** Считает дисперсию средних скоростей.  При первом обращении - считает её
    * @return Значение дисперсии
    * */
    public Double getD()
    {
        if (D == null) {
            double mx = getV();
            D = velocityList.stream().mapToDouble(x -> x * x).sum() / n - mx * mx;
        }
        return D;
    }
    /** Возвращает среднее квадратичное отклонение
     * @return Значение СКО
     * */
    public double getSigma()
    {
        return Math.sqrt(getD());
    }

    public Double getTolalTime()
    {
        return totalTime;
    }

    void setD(Double D)
    {
        this.D = D;
    }

    public boolean getValueIsEmpty()
    {
        return (n == 0) && valueIsEmpty;
    }
}
