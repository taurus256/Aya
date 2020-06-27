package org.taurus.aya.client.widgets;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.TimeUnit;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import org.taurus.aya.client.TaskView;

import java.util.Date;

public class DateControlWidget extends HLayout {
    TaskView taskView;
    Date current = new Date();
    Date start, end;
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM");
    DateTimeFormat monthNameAndYearFormat = DateTimeFormat.getFormat("LLLL yyyy");
    DateTimeFormat monthNameFormat = DateTimeFormat.getFormat("LLLL");

    IButton leftMonth, leftWeek, rightWeek, rightMonth;
    Label startDate, currentDate, endDate;

    public DateControlWidget(TaskView taskView){
        this.taskView = taskView;
        setWidth100();
        setHeight(30);

        leftMonth = new IButton("<<");
        leftMonth.setTooltip("Предыдущий месяц");
        leftMonth.setWidth(100);
        leftMonth.addClickHandler(leftMonthHandler);
        leftWeek = new IButton("<");
        leftWeek.setTooltip("Предыдущая неделя");
        leftWeek.setWidth(100);
        leftWeek.addClickHandler(leftWeekHandler);

//        startDate = new Label( dateFormat.format(start));
//        startDate.setAlign(Alignment.CENTER);
//        startDate.setWidth(100);
//        endDate = new Label( dateFormat.format(end));
//        endDate.setAlign(Alignment.CENTER);
//        endDate.setWidth(100);

        rightWeek = new IButton(">");
        rightWeek.setTooltip("Следующая неделя");
        rightWeek.setWidth(100);
        rightWeek.addClickHandler(rightWeekHandler);
        rightMonth = new IButton(">>");
        rightWeek.setTooltip("Следующий месяц");
        rightMonth.setWidth(100);
        rightMonth.addClickHandler(rightMonthHandler);

        setWeekRange(); //has side effects: set start&end!
        currentDate = new Label("<b>" + monthNameAndYearFormat.format(current) + "</b>");
        currentDate.setAlign(Alignment.CENTER);
        currentDate.setWidth("*");

        addMember(leftMonth);
        addMember(leftWeek);
//        addMember(startDate);
        addMember(currentDate);
//        addMember(endDate);
        addMember(rightWeek);
        addMember(rightMonth);
    }

    ClickHandler leftMonthHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            CalendarUtil.addMonthsToDate(start,-1);
            CalendarUtil.addMonthsToDate(end,-1);
            update();
        }
    };

    ClickHandler leftWeekHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            CalendarUtil.addDaysToDate(start,-7);
            CalendarUtil.addDaysToDate(end,-7);
            update();
        }
    };

    ClickHandler rightMonthHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            CalendarUtil.addMonthsToDate(start,1);
            CalendarUtil.addMonthsToDate(end,1);
            update();
        }
    };

    ClickHandler rightWeekHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
            CalendarUtil.addDaysToDate(start,7);
            CalendarUtil.addDaysToDate(end,7);
            update();
        }
    };

    private void update(){
//        startDate.setContents( dateFormat.format(start));
        Date localEnd = CalendarUtil.copyDate(end);
        CalendarUtil.addDaysToDate(localEnd,-1);
        if (start.getMonth() == localEnd.getMonth())
            currentDate.setContents("<b>" + monthNameAndYearFormat.format(start) + "</b>");
        else
            currentDate.setContents("<b>" + monthNameFormat.format(start) + " - " + monthNameAndYearFormat.format(end) + "</b>");
//        endDate.setContents( dateFormat.format(end));
        taskView.getCurrentTimeline().setTimelineRange(start,end);
    }

    private void setToFirstDayOfWeek(Date date){
        date.setDate(date.getDate() - date.getDay() + 1);
    }

    public void setWeekRange(){
        start = new Date();
        setToFirstDayOfWeek(start);
        end = CalendarUtil.copyDate(start);
        CalendarUtil.addDaysToDate(end, 6);

        taskView.getCurrentTimeline().setTimeResolution(TimeUnit.DAY, TimeUnit.DAY, 7, 150);
        taskView.getCurrentTimeline().setTimelineRange(start, end);

        leftMonth.hide();
        rightMonth.hide();
    }

    public void setMonthRange(){
        start = new Date();
        CalendarUtil.setToFirstDayOfMonth(start);

        end = CalendarUtil.copyDate(start);
        CalendarUtil.addMonthsToDate(end,1);

        taskView.getCurrentTimeline().setTimeResolution(TimeUnit.DAY, TimeUnit.DAY, 31, 60);
        taskView.getCurrentTimeline().setTimelineRange(start, end);

        leftMonth.show();
        rightMonth.show();
    }
}
