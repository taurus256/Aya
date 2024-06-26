package org.taurus.aya.servlets.advicers;

import org.taurus.aya.server.entity.Event;

import java.util.LinkedList;
import java.util.List;

public class StatData {

    List<Double> velocityList = new LinkedList<>();
    int n=0;
    Double V = null;
    Double D = null;
    boolean valueIsEmpty = true;

    /** Добавление задачи в список для расчета статистики
    * @param e - entity задачи
    * */
    void addEvent(Event e)
    {
        System.out.println("StatData.addEvent");
        if (e.getDuration_h()!=null && e.getSpentTime()!=null && e.getSpentTime()!=0) {
            velocityList.add(e.getDuration_h().doubleValue() / e.getSpentTime().doubleValue());
            n++;
        }
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
    Double getD()
    {
        if (D == null) {
            Double mx = getV();
            D = velocityList.stream().mapToDouble(x -> x * x).sum() / n - mx * mx;
        }
        return D;
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
