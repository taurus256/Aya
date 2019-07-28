package org.taurus.aya.client.widgets;

import com.smartgwt.client.widgets.calendar.CalendarView;
import com.smartgwt.client.widgets.calendar.DateHeaderCustomizer;
import com.smartgwt.client.widgets.calendar.Timeline;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

import java.util.Date;

public class TestTimeline extends Timeline {
	
	final TestTimeline timeline;
	
	public TestTimeline()
	{
		timeline = this;
		setHeight100();
		setWidth100();
		
        setDateHeaderCustomizer(new DateHeaderCustomizer(){

			@Override
			public String getHeaderTitle(Date date, int dayOfWeek, String defaultValue, CalendarView calendarView)
			{
				return " 00:00 <br> 00:00 ";
			}});

        addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				timeline.getTimelineView().setAutoFitHeaderHeights(true);

			}});
        
	}
	
}
