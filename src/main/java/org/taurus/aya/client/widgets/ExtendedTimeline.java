package org.taurus.aya.client.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.types.TimeUnit;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.calendar.*;
import com.smartgwt.client.widgets.calendar.events.*;
import com.smartgwt.client.widgets.events.*;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import org.taurus.aya.client.*;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.dialogs.BacklogTaskDialog;
import org.taurus.aya.client.dialogs.EditEventDialog;
import org.taurus.aya.shared.Command;
import org.taurus.aya.shared.Command.CommandType;
import com.smartgwt.client.widgets.calendar.CalendarEvent;

import java.util.ArrayList;
import java.util.Date;

public class ExtendedTimeline extends Timeline {
	
	private Menu menu;
	private int blockSelectMode = 0;
	ListGridField lgf, sublane_field;
	Record currentRecord;
	AdvancedCriteria laneSearchCriteria;
	boolean distinctByUsers = true;
	private MenuItem dialogOpenMenu, eventPropertiesMenu;
	private CalendarEvent selectedEvent=null;
	private EditEventDialog editEventDialog;
	private boolean thisIsFirstCall = true;
	
	ExtendedTimeline extendedTimeline;
	public Date startDate, endDate;
	
	public ExtendedTimeline(Record currentRecord, final boolean distinctByUsers)
	{
		this.currentRecord = currentRecord;
		this.distinctByUsers = distinctByUsers;
		extendedTimeline = this;
		
		CalendarView calendarView = new CalendarView();
		calendarView.setMinHeight(0);
		calendarView.setAutoFitHeaderHeights(true);
		//calendarView.setHeight100();
		setAutoChildProperties("timelineView", calendarView);
		
		// Configure view
		if (distinctByUsers) setLaneNameField("executor");
		setShowLaneRollOver(false);
		setCanAcceptDrop(true);
		setCanResizeEvents(true);
		setHeight100();
		setMinHeight(0);
		setWidth100();
		setMargin(0);
		setPadding(0);
		setDisableWeekends(true);
		setShowWeekends(true);

		setSublaneNameField("sublane");
		setUseSublanes(false);
        setRowHeight(200);

		setDataSource(GlobalData.getDataSource_tasks());
		setInitialCriteria(new AdvancedCriteria("isGraph", OperatorId.EQUALS,true));
		setAutoFetchData(true);

        CalendarEvent indicator1 = new CalendarEvent();
		indicator1.setStartDate(new Date());
		Date d = new Date();
		d.setTime(d.getTime() + 24*3600*1000);
		indicator1.setEndDate(d);
		indicator1.setName("Текущая дата");


		CalendarEvent indicator2 = new CalendarEvent();
		indicator2.setStartDate(d);
		indicator2.setName("");

		addIndicator(indicator1);
		addIndicator(indicator2);
		setShowIndicators(true);
//		setShowZones(true);
//		setShowZoneHovers(true);

		//Configure lane field
		lgf = new ListGridField("title","Поток");
		lgf.setAutoFitWidth(true);
		lgf.setAutoFitWidthApproach(AutoFitWidthApproach.VALUE);
		lgf.setWrap(true);
		//lgf.setMinWidth(200);
		lgf.setWidth(200);
		
		
		setLaneFields(new ListGridField[]{lgf});

        // Configure the time range
        
    	startDate = new Date();
    	endDate = new Date();

		Long startTime = startDate.getTime();
		Long endTime = startDate.getTime() + 1000*60*60*24;

		if (endTime - startTime < 1000*60*60*24)	// if task duration is less than day
		{
			SC.logWarn("TASk VIEW: DURATION IS LESS THAN 1 DAY");
			if (endTime - startTime > 1000*60*60)
			{
				SC.logWarn("TASk VIEW: DURATION IS LESS THAN 1 DAY BUT MORE THAN 1 HOUR");
				startDate.setHours(0);
				endDate.setHours(24);
				setTimelineRange(startDate, endDate);
				setTimeResolution(TimeUnit.HOUR, TimeUnit.HOUR, 48, null);
				}
			else
			{
				SC.logWarn("TASk VIEW: DURATION IS LESS THAN 1 HOUR");
				SC.logWarn("TASk VIEW: Start date: " + startDate);
				//!startDate.setMinutes(0);
				//startDate.setTime( startDate.getTime() );
				startDate.setHours(0);
				startDate.setMinutes(0);
				setStartDate(startDate);
				setTimeResolution(TimeUnit.MINUTE, TimeUnit.MINUTE, 1440, 5);
			}
		}
		else
		{
			SC.logWarn("TASk VIEW: DURATION IS MORE THAN 1 DAY");
			
			startDate.setDate(1);
			endDate.setDate(1);
			if (startDate.getMonth() < 11)
				endDate.setMonth(startDate.getMonth() +1 );
			else
			{
				endDate.setMonth(0);
				endDate.setYear(startDate.getYear() + 1);
			}
			setTimelineRange(startDate, endDate);
			
			setTimeResolution(TimeUnit.DAY, TimeUnit.DAY, 31, null);
		}

		// Setting criteria for searching lanes
		laneSearchCriteria = new AdvancedCriteria();
		laneSearchCriteria.addCriteria(new AdvancedCriteria("visible", OperatorId.EQUALS,true));
		
		/*					 Setting handlers				*/

		addEventClickHandler(new EventClickHandler(){
			@Override
			public void onEventClick(CalendarEventClick event) {
				if (selectedEvent != null)
					{
					selectedEvent.setHeaderBackgroundColor(selectedEvent.getBackgroundColor());
					selectedEvent.setBorderColor("gray");
					refreshEvent(selectedEvent);
					}

				//Enable 'properties' menu for the event
				eventPropertiesMenu.setEnabled(true);
				menu.refreshRow(menu.getItems().length-1);

				if (event.isAltKeyDown())
				{
					EditEventDialog ee = getEditEventDialog(event.getEvent());
					ee.focusInNextTabElement();
				}
				else {
					// Setting the selected event and its style
					selectedEvent = event.getEvent();
					selectedEvent.setHeaderBackgroundColor("#DCDCDC");
					selectedEvent.setBorderColor("#297ACC");

					refreshEvent(selectedEvent);
					redraw();
				}

				event.cancel();
			}});

        addEventRemoveClickHandler(new EventRemoveClickHandler(){

			@Override
			public void onEventRemoveClick(CalendarEventRemoveClick event) {
				selectedEvent = event.getEvent();
				eventPropertiesMenu.setEnabled(true);
				if (GlobalData.canWrite(selectedEvent))
				{
					SC.confirm("Запрос", "Удалить задачу?", new BooleanCallback(){
	
						@Override
						public void execute(Boolean value) {
							if (value)
							{
								removeEvent(selectedEvent);
								ResourceLifeCycleManager.resourceDeleted(ResourceType.TASK, selectedEvent);
								selectedEvent = null;
							}
							
						}});
				}
				else
					SC.warn("У вас недостаточно прав для удаления этой задачи");
				event.cancel();
			}});

		//Date modification
        addEventResizeStopHandler(event -> {
            SC.logWarn("ExtendedTimeline: event resize");
            Date start = modifyCalendarEventStartDate(event.getNewEvent());
            SC.logWarn("ExtendedTimeline: startDate=" + start);
        });

		//Date modification (one more)
        addEventRepositionStopHandler(event -> {
                    SC.logWarn("ExtendedTimeline: event rep stop");
                    modifyCalendarEventStartDate(event.getNewEvent());
					modifyCalendarEventEndDate(event.getNewEvent());
                });

        addEventChangedHandler(new EventChangedHandler(){

			@Override
			public void onEventChanged(CalendarEventChangedEvent event) {
				updateTasks();
				SC.logWarn("EventChangedHandler task id = " + event.getEvent().getAttribute("id"));
//				Connector.sendSystemMessageToAll(CommandType.UPDATE_TASK_ARRANGEMENT, TabManager.ResourceType.TASK, "Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> обновил задачу <b>" + event.getEvent().getAttribute("name") + "</b>", (int)event.getEvent().getAttributeAsInt("id"));
				//updateTimeline();
		}});


        addBackgroundMouseUpHandler(new BackgroundMouseUpHandler(){

			@Override
			public void onBackgroundMouseUp(BackgroundMouseUpEvent event) {
				CalendarEvent ev = new CalendarEvent();
				ev.setStartDate(event.getStartDate());
				ev.setEndDate(event.getEndDate());

				SC.logWarn("Created at timeline: startDate:" + event.getStartDate() + " enddate: " + event.getEndDate() );

				ev.setAttribute("limitDate", event.getEndDate());
				ev.setName("Новая задача");
				ev.setAttribute("rgroup", GlobalData.ACCESS_ALL);
				ev.setAttribute("wgroup", GlobalData.ACCESS_ALL);
				ev.setAttribute("author", GlobalData.getCurrentUser().getAttributeAsInt("id"));
				//Lane lane = getLaneFromPoint(event.getX() - getTimelineView().getAbsoluteLeft(), event.getY() - getAbsoluteTop() - getTimelineView().getTop() - getTimelineView().getHeaderHeight());
				//Lane sublane = getSublaneFromPoint(event.getX() - getTimelineView().getAbsoluteLeft(), event.getY() - getAbsoluteTop() - getTimelineView().getTop() - getTimelineView().getHeaderHeight());
				Lane lane = getLaneFromPoint(null,null);
				Lane sublane = getSublaneFromPoint(null,null);
				
				if (distinctByUsers)
				{
					for (Record r: GlobalData.getUsers())
					if (r.getAttributeAsString("showedName").equals(lane.getTitle()))
					{
						ev.setAttribute("executor",r.getAttributeAsInt("id"));
						ev.setAttribute("executorName",r.getAttributeAsString("showedName"));
					}
				}
				else
				{
					ev.setAttribute("lane",lane.getName());
					if (sublane != null) ev.setAttribute("sublane", sublane.getName());
				}

				getEditEventDialog(ev).setNewEvent(extendedTimeline, ev);
				event.cancel();
			}});       
        

        // set customizers
        setEventHeaderHTMLCustomizer(new EventHeaderHTMLCustomizer(){

			@Override
			public String getEventHeaderHTML(CalendarEvent calendarEvent,
                                             CalendarView calendarView) {
				return  "<b>" + calendarEvent.getAttributeAsString("name") + "</b>";
			}
        });



        setEventBodyHTMLCustomizer(new EventBodyHTMLCustomizer(){

			@Override
			public String getEventBodyHTML(CalendarEvent calendarEvent,
					CalendarView calendarView) {

				if (distinctByUsers)
					return "<i>" + calendarEvent.getAttributeAsString("lane") + "</i>";
				else
					{
						String executor_name = (calendarEvent.getAttributeAsString("executorName") == null)?"":calendarEvent.getAttributeAsString("executorName");
						return "<i>" + executor_name + "</i>";
					}
			}
        });
        
        addDropHandler(new DropHandler() {
                           @Override
                           public void onDrop(DropEvent event) {
                               Record r = GlobalData.getNavigationArea().getTaskPanel().getTreeSelectedRecord();


                               //TODO:: вынести проверку на null в отдельный метод
                               if (r.getAttribute("lane") == null || r.getAttribute("lane").equals("null"))
                                    new BacklogTaskDialog(r);
                               else {
                                   r.setAttribute("isGraph", true);
                                   GlobalData.getDataSource_tasks().updateData(r, new DSCallback() {
									   @Override
									   public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
										   CommandExecutor.exec(new Command(CommandType.UPDATE_TASK_PANEL));
									   }
								   });
                               }
                           }
                       });
                //  set menu and load the data

                setContextMenu(getContextMenu());

		addFetchDataHandler(new FetchDataHandler() {
								@Override
								public void onFilterData(FetchDataEvent event) {
								if(thisIsFirstCall) {
									int delta = new Date().getDate() * 60; //+ currentRecord.getAttributeAsDate("endDate").getMinutes()/5*150 - getTimelineView().getWidth()/2;

									SC.logWarn("ExtendedTimeline: need to scroll window to " + delta + " pixels (current day: " + new Date().getDate() + ")");
									getTimelineView().scrollBodyTo(delta, 0);
									thisIsFirstCall=false;
									}
								}
							});
		updateTimeline();
	}

    public Date modifyCalendarEventStartDate(CalendarEvent newEvent) {
        Date start = newEvent.getStartDate();
        start = new Date(start.getTime() + 24*3600*1000);
        newEvent.setStartDate(start);
        return start;
    }

	public Date modifyCalendarEventEndDate(CalendarEvent newEvent) {
		Date end = newEvent.getEndDate();
		end = new Date(end.getTime() + 24*3600*1000);
		newEvent.setEndDate(end);
		return end;
	}

    private void setTimeResolution(TimeUnit headerUnit, TimeUnit rangeUnit, int columnCount, Integer minutesPerColumn)
    {
    	HeaderLevel[] headerLevels = {};
    	switch (headerUnit)
    	{
    	case MINUTE:{
//    			HeaderLevel h0 = new HeaderLevel(TimeUnit.MINUTE);
//
//    			h0.setTitleFormatter(new HeaderLevelTitleCustomizer (){
//		    		
//					@Override
//					public String getTitle(HeaderLevel headerLevel, Date startDate,
//							Date endDate, String defaultValue, Calendar calendar) {
//						 //DateTimeFormat fmt = DateTimeFormat.getFormat("HH:mm");
//			                return "*";
//					}});
//		        h0.setHeaderWidth(150);

    			HeaderLevel hl = new HeaderLevel(TimeUnit.MINUTE);
		        /*!!!hl.setTitleFormatter(new HeaderLevelTitleCustomizer (){
		
					@Override
					public String getTitle(HeaderLevel headerLevel, Date startDate,
							Date endDate, String defaultValue, Calendar calendar) {
						 DateTimeFormat fmt = DateTimeFormat.getFormat("HH:mm");
						 
						 if (extendedTimeline.startDate.equals(startDate))
							 titleFixedTimeCounter=0;
						 else
							 titleFixedTimeCounter+=5;
						 
			                return String.valueOf(titleFixedTimeCounter/60) + ":" + String.valueOf(titleFixedTimeCounter%60) + "<br> [" + fmt.format(startDate)  + "]";
					}});*/
		        hl.setHeaderWidth(150);
		       /// HeaderLevel hl2 = new HeaderLevel(TimeUnit.MINUTE);
		        headerLevels = new HeaderLevel[]{hl/*,hl2*/};
    		};break;
    	case HOUR:{
	        HeaderLevel hl = new HeaderLevel(TimeUnit.HOUR);
	        hl.setTitleFormatter(new HeaderLevelTitleCustomizer(){
	
				@Override
				public String getTitle(HeaderLevel headerLevel, Date startDate,
                                       Date endDate, String defaultValue, Calendar calendar) {
					 DateTimeFormat fmt = DateTimeFormat.getFormat("HH:mm");
		                return fmt.format(startDate);
				}});
	        headerLevels = new HeaderLevel[]{hl}; break;
    	}
    	case DAY:
    		{
    	        HeaderLevel hl = new HeaderLevel(TimeUnit.DAY);
    	        hl.setTitleFormatter(new HeaderLevelTitleCustomizer(){
    	
    				@Override
    				public String getTitle(HeaderLevel headerLevel, Date startDate,
                                           Date endDate, String defaultValue, Calendar calendar) {
    					 DateTimeFormat fmt = DateTimeFormat.getFormat("dd.MM");
    		                return fmt.format(startDate);
    				}});
    			headerLevels = new HeaderLevel[]{hl};
    			
    		}; break;
    	}
    	setResolution(headerLevels, rangeUnit, columnCount, minutesPerColumn);  
    }
	
	public void updateTimeline()
	{
		SC.logWarn("TaskView: updateTimeline");
		
		// clear Timeline
		if (getLanes().length > 0 )
			for (Lane l:getLanes())
				removeLane(l);
		
//		// Загрузка списка потоков
		
		DSRequest dsr = new DSRequest();
		
		if (!distinctByUsers) //use 'lanes' DS to create lanes 
		{
			SortSpecifier sortSpecifier = new SortSpecifier("lane_order", SortDirection.ASCENDING);
			SortSpecifier[] sortSpecifiers = { sortSpecifier };
			dsr.setSortBy(sortSpecifiers);
			
	        GlobalData.getDataSource_lanes().fetchData(laneSearchCriteria, new DSCallback(){
	
				@Override
				public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
				{
					SC.logWarn("TaskView: lanes amount: " + dsResponse.getData().length);
					
					ArrayList<Lane> sublanes = new ArrayList<Lane>();
					Lane[] emptyLaneArray = {};
					
					for (int i=0; i< dsResponse.getData().length; i++)
					{
						if (dsResponse.getData()[i].getAttributeAsInt("parent") == 0)
						{
							for (int j=0; j< dsResponse.getData().length; j++)
							{
								if (dsResponse.getData()[i].getAttributeAsInt("id").equals(dsResponse.getData()[j].getAttributeAsInt("parent")))
									sublanes.add(new Lane(dsResponse.getData()[j].getAttribute("name"),dsResponse.getData()[j].getAttribute("name")));
							}
						
						Lane lane = new Lane(dsResponse.getData()[i].getAttribute("name"),generateLaneTitle(dsResponse.getData()[i].getAttribute("name"),sublanes));
						//lane.setHeight(100*sublanes.size());
						
						//if lane have no sublanes
						if (sublanes.size()!=0) 
							lane.setSublanes(sublanes.toArray(emptyLaneArray)); //sublanes.add(new Lane(dsResponse.getData()[i].getAttribute("name"),dsResponse.getData()[i].getAttribute("name")));
						else
							lane.setHeight(100);

						addLane(lane);
						
						sublanes.clear();
						}
					}					
					//updateTasks();
				}},dsr);
		}
		else //use 'user' DS to create lanes
		{
			SC.logWarn("TaskView: users amount: " + GlobalData.getUsers().length);
			for (int i=0; i< GlobalData.getUsers().length; i++)
			{
				Lane lane = new Lane(GlobalData.getUsers()[i].getAttribute("id"),GlobalData.getUsers()[i].getAttribute("showedName"));
				lane.setHeight(200);
				addLane(lane);
			}

			updateTasks();
		}
	}
	
	private String generateLaneTitle(String laneName, ArrayList<Lane> sublanes)
	{
		String st = "";
		if (sublanes.size()>0)
		{
			st = "<table width='100%' height='" + String.valueOf(sublanes.size()*100 - 1) + "'><tr><td rowspan='" + sublanes.size() + "' style='border-right:1px solid #a0a0a0'>" + laneName + "</td>";
			for (int i=0; i<sublanes.size(); i++)
			{
				if (i > 0) st+= "<tr>";
				st += "<td>" + sublanes.get(i).getAttributeAsString("name") + "</td>";
				st+="</tr>";
			}
			st += "</table>";
		}
		else
			st = laneName;
		return st;
	}
	
	public void updateTasks()
	{
		invalidateCache();
		
		//Обновляем Timeline
    	fetchData(new AdvancedCriteria("isGraph", OperatorId.EQUALS,true), new DSCallback() {
			
			@Override
			public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
				
				SC.logWarn("updateTasks(): Task list size is " + dsResponse.getData().length);
				
				//resizeTo("100%","99%");
				//resizeTo("100%","100%");
			
				if (currentRecord != null) 
					Scheduler.get().scheduleDeferred(new com.google.gwt.user.client.Command(){
						public void execute()
						{
							selectRecord(currentRecord);
						}
					});
					
				GlobalData.getStatusBar().stopIndicateProcess();
				
				// Scroll feature availible only in DAY granuality 
				if (getTimelineGranularity() == TimeUnit.DAY && thisIsFirstCall)
				{
					Date d = new Date();
					int delta = d.getDay() *50; //+ currentRecord.getAttributeAsDate("endDate").getMinutes()/5*150 - getTimelineView().getWidth()/2;
					SC.logWarn("ExtendedTimeline: need to scroll window to " + delta + " pixels (granuality: " + getTimelineGranularity().toString() + ")");
					if ( delta > 0)
						getTimelineView().scrollBodyTo(delta, 0);
					thisIsFirstCall = false;
					//setTimeResolution(TimeUnit.DAY, TimeUnit.DAY, 31, null);
 				}
			
			}
		});
	}

    public Menu getContextMenu() {
        menu= new Menu();

        MenuItem moveToBacklogMenu = new MenuItem("Вернуть в список");
        moveToBacklogMenu.setIcon("buttons/task_backlog.png");
        moveToBacklogMenu.addClickHandler(new ClickHandler() {
                                              @Override
                                              public void onClick(MenuItemClickEvent event) {
                                                  selectedEvent.setAttribute("isGraph",false);
                                                  GlobalData.getDataSource_tasks().updateData(selectedEvent);
                                                  }
                                          });
        menu.addItem(moveToBacklogMenu);
        menu.addItem(new com.smartgwt.client.widgets.menu.MenuItemSeparator());
        
        MenuItem setStateProcess = new MenuItem(EventState.PROCESS.getName());
        setStateProcess.addClickHandler(event -> { setEventState(EventState.PROCESS);});
        setStateProcess.setIcon("buttons/task_play.png");
        menu.addItem(setStateProcess);

        MenuItem setStatePause = new MenuItem(EventState.PAUSE.getName());
        setStatePause.addClickHandler(event -> { setEventState(EventState.PAUSE);});
        setStatePause.setIcon("buttons/task_pause.png");
        menu.addItem(setStatePause);

        MenuItem setStateReady = new MenuItem(EventState.READY.getName());
        setStateReady.addClickHandler(event -> { setEventState(EventState.READY);});
        setStateReady.setIcon("buttons/task_ready.png");
        menu.addItem(setStateReady);

        MenuItem setStateFail = new MenuItem(EventState.FAIL.getName());
        setStateFail.addClickHandler(event -> { setEventState(EventState.FAIL);});
        setStateFail.setIcon("buttons/task_fail.png");
        menu.addItem(setStateFail);

        MenuItem setStateNew = new MenuItem(EventState.NEW.getName());
        setStateNew.addClickHandler(event -> { setEventState(EventState.NEW);});
        setStateNew.setIcon("buttons/task_new.png");
        menu.addItem(setStateNew);

        menu.addItem(new com.smartgwt.client.widgets.menu.MenuItemSeparator());

        eventPropertiesMenu = new MenuItem("Свойства задачи");
        eventPropertiesMenu.addClickHandler(event -> getEditEventDialog(selectedEvent));
        eventPropertiesMenu.setEnabled(false);
        menu.addItem(eventPropertiesMenu);
        menu.setHeight(menu.getItems().length*ApplicationMenu.ITEM_MENU_HEIGHT - (menu.getItems().length-2));

        return menu;  
    } 
    
	private EditEventDialog getEditEventDialog(Record event)
	{
		return new EditEventDialog(event);
	}
	
    public MenuItem getMenuItem(String title, final TimeUnit headerUnit, final TimeUnit rangeUnit, final Integer columnCount, final Integer minutesPerColumn) {
        MenuItem item = new MenuItem(title);
          
        item.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {  
			@Override
			public void onClick(MenuItemClickEvent event) {
				setTimeResolution(headerUnit, rangeUnit, columnCount, minutesPerColumn);
			}  
        });  
        return item;  
    }  
    
	public void setBlockSelectMode(int mode)
	{
		/*if (mode>0)
			SC.say("Укажите задачи, которуе блокируют выполнение данной");*/
		setCursor(com.smartgwt.client.types.Cursor.CROSSHAIR);
		blockSelectMode = mode;
	}
	
	public CalendarEvent getSelectedEvent()
	{
		if (selectedEvent == null) SC.logWarn("ExtendedTimeline: NO SELECTED EVENT!");
		return selectedEvent;
	}

    public void setEventState(EventState state) {
        switch(state) {
            case NEW: setEventState("s3_event_new", String.valueOf(state.ordinal()), "tree/task0.png"); break;
            case PROCESS: setEventState("s3_event_process", String.valueOf(state.ordinal()), "tree/task1.png"); break;
            case PAUSE: setEventState("s3_event_pause", String.valueOf(state.ordinal()), "tree/task2.png"); break;
            case READY: setEventState("s3_event_ready", String.valueOf(state.ordinal()), "tree/task3.png"); break;
            case FAIL: setEventState("s3_event_fail", String.valueOf(state.ordinal()), "tree/task4.png"); break;
        }
    }

    private void setEventState(String s3_event_pause, String s, String s2) {
        final CalendarEvent selectedEvent = getSelectedEvent();
        if (selectedEvent == null) return;
        selectedEvent.setAttribute("eventWindowStyle", s3_event_pause);
        selectedEvent.setAttribute("state", s);
        selectedEvent.setAttribute("icon", s2);
        modifyCalendarEventStartDate(selectedEvent);
		modifyCalendarEventEndDate(selectedEvent);
        GlobalData.getDataSource_tasks().updateData(selectedEvent, new DSCallback() {
            @Override
            public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
                updateTasks();
                ResourceLifeCycleManager.resourceChanged(ResourceType.TASK, getSelectedEvent());
            }
        });
    }
}
