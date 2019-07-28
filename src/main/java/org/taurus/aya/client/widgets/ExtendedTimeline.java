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
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.FetchDataEvent;
import com.smartgwt.client.widgets.events.FetchDataHandler;
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
        SC.logWarn("ETL: 2");
		if (distinctByUsers) setLaneNameField("executor");
		setShowLaneRollOver(false);
		setCanAcceptDrop(true);
		setCanResizeTimelineEvents(true);
		setCanResizeEvents(true);
		setHeight100();
		setMinHeight(0);
		setWidth100();
		setMargin(0);
		setPadding(0);

		setSublaneNameField("sublane");
		setUseSublanes(false);
        setRowHeight(200);

		setDataSource(GlobalData.getDataSource_tasks());
		setInitialCriteria(new AdvancedCriteria("isGraph", OperatorId.EQUALS,true));
		setAutoFetchData(true);
        CalendarEvent indicator = new CalendarEvent();
		indicator.setStartDate(new Date());
		Date d = new Date();
		d.setTime(d.getTime() + 24*3600*1000);
		indicator.setEndDate(d);
		indicator.setName("Текущая дата");
		indicator.setStyleName("s3_zone_style");
		addZone(indicator);
		setShowZones(true);
		setShowZoneHovers(true);

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
					//selectedEvent.setStyleName("s3_normal");
					selectedEvent.setHeaderBackgroundColor(selectedEvent.getBackgroundColor());
					selectedEvent.setBorderColor("gray");
					//!selectedEvent.setHeaderTextColor("black");
					refreshEvent(selectedEvent);
					}
				else
					eventPropertiesMenu.setEnabled(true);

				// Setting the selected event and its style
				selectedEvent = event.getEvent();
				SC.logWarn("TaskView. Selected event name is:" +  event.getEvent().getAttribute("name"));
				//selectedEvent.setStyleName("s3_selected");
				selectedEvent.setHeaderBackgroundColor("#DCDCDC");
				selectedEvent.setBorderColor("#297ACC");
				//!selectedEvent.setHeaderTextColor("white");
				
				refreshEvent(selectedEvent);
				redraw();

				// Setting menu title for dialog calling
				if (selectedEvent.getAttributeAsInt("author").equals(GlobalData.getCurrentUser().getAttributeAsInt("id")))
				{
					dialogOpenMenu.setTitle("Диалог с исполнителем");

					if (!selectedEvent.getAttributeAsInt("author").equals(selectedEvent.getAttributeAsInt("executor")))
						dialogOpenMenu.setEnabled(true);
					else
						dialogOpenMenu.setEnabled(false);
				}
				else
				{
					dialogOpenMenu.setTitle("Диалог с постановщиком");
					dialogOpenMenu.setEnabled(true);
				}
				
				menu.refreshRow(0);
				menu.refreshRow(menu.getItems().length-1);
				
				// Setting blocking or parent task in EditEventDialog, if it taken by blockSelectVode
				switch (blockSelectMode)
				{
				case 1: 
					{
						editEventDialog.setBlockingTask(selectedEvent);
						blockSelectMode = 0;
						setCursor(com.smartgwt.client.types.Cursor.AUTO);
					}; break;
				case 2: 
					{
						editEventDialog.setParentTask(selectedEvent);
						blockSelectMode = 0;
						setCursor(com.smartgwt.client.types.Cursor.AUTO);
					}; break;
				};
				event.cancel();
			}});

		/*addEventAddedHandler(new EventAddedHandler()
	    {

			@Override
			public void onEventAdded(CalendarEventAdded event) {
				event.getEvent().setStyleName("s3_normal");
				refreshEvent(event.getEvent());
			}
	    });*/
        
        addEventRemoveClickHandler(new EventRemoveClickHandler(){

			@Override
			public void onEventRemoveClick(CalendarEventRemoveClick event) {
				selectedEvent = event.getEvent();
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
				ev.setAttribute("startdate",event.getStartDate());
				ev.setAttribute("enddate",event.getEndDate());
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
					if (r.getAttributeAsString("showed_name").equals(lane.getTitle()))
					{
						ev.setAttribute("executor",r.getAttributeAsInt("id"));
						ev.setAttribute("executor_name",r.getAttributeAsString("showed_name"));
					}
				}
				else
				{
					ev.setAttribute("lane",lane.getName());
					if (sublane != null) ev.setAttribute("sublane", sublane.getName());
				}

				getEditEventDialog(ev).setNewEvent(extendedTimeline, ev);
				getEditEventDialog(ev).show();
				event.cancel();
			}});       
        

        /*addDayBodyClickHandler(new DayBodyClickHandler(){

			@Override
			public void onDayBodyClick(DayBodyClickEvent event) {
				event.cancel();
				
			}});*/
        
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
						String executor_name = (calendarEvent.getAttributeAsString("executor_name") == null)?"":calendarEvent.getAttributeAsString("executor_name");
						return "<i>" + executor_name + "</i>";
					}
			}
        });
        
//        setDateHeaderCustomizer(new DateHeaderCustomizer(){
//
//			@Override
//			public String getHeaderTitle(Date date, int dayOfWeek, String defaultValue, CalendarView calendarView)
//			{
//				if (defaultValue.contains(":"))
//				{
//				  long minutes = (date.getTime() - startDate.getTime())/60000;
//
//	              return "Ч + " + String.valueOf(minutes/60) + ":" + String.valueOf(minutes%60) + "<br> " + defaultValue + "";
//				}
//				else return defaultValue;
//			}});


        addDropHandler(new DropHandler() {
                           @Override
                           public void onDrop(DropEvent event) {
                               Record r = GlobalData.getNavigationArea().getTaskPanel().getTreeSelectedRecord();

                               //TODO:: вынести проверку на null в отдельный метод
                               if (r.getAttribute("lane") == null || r.getAttribute("lane").equals("null"))
                                    new BacklogTaskDialog(r,true);
                               else {
                                   r.setAttribute("isGraph", true);
                                   GlobalData.getDataSource_tasks().updateData(r);
                                   CommandExecutor.exec(new Command(CommandType.UPDATE_TASK_PANEL));
                               }
                           }
                       });
                //  set menu and load the data

                setContextMenu(getContextMenu());

		addFetchDataHandler(new FetchDataHandler() {
								@Override
								public void onFilterData(FetchDataEvent event) {
									int delta = d.getDate() *60; //+ currentRecord.getAttributeAsDate("endDate").getMinutes()/5*150 - getTimelineView().getWidth()/2;

									SC.logWarn("ExtendedTimeline: need to scroll window to " + delta + " pixels (granuality: " + getTimelineGranularity().toString() + ")");
									getTimelineView().scrollBodyTo(delta, 0);
								}
							});

		updateTimeline();
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
		
			GlobalData.getDataSource_user().fetchData(GlobalData.getUserFilterCriteria(), new DSCallback(){
	
				@Override
				public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
				{
					SC.logWarn("TaskView: users amount: " + dsResponse.getData().length);
					for (int i=0; i< dsResponse.getData().length; i++)
					{
						Lane lane = new Lane(dsResponse.getData()[i].getAttribute("id"),dsResponse.getData()[i].getAttribute("showed_name"));
						lane.setHeight(200);
			        	addLane(lane);
					}
					
					
					updateTasks();
				}},dsr);	
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

//        dialogOpenMenu = new MenuItem("Задача не выбрана");
//        dialogOpenMenu.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler()
//	        {
//				@Override
//				public void onClick(MenuItemClickEvent event) {
//					if (selectedEvent != null)
//						if (selectedEvent.getAttributeAsInt("author").equals(GlobalData.getCurrentUser().getAttributeAsInt("id")))
//							CommandExecutor.exec(new org.taurus.aya.shared.Command(CommandType.OPEN_USER_CHAT, "timeline: open user chat",selectedEvent.getAttributeAsInt("executor")));
//						else
//							CommandExecutor.exec(new org.taurus.aya.shared.Command(CommandType.OPEN_USER_CHAT, "timeline: open user chat",selectedEvent.getAttributeAsInt("author")));
//				}
//	        }
//		);
//        dialogOpenMenu.setEnabled(false);
//        menu.addItem(dialogOpenMenu);

        MenuItem moveToBacklogMenu = new MenuItem("Вернуть в список");
        moveToBacklogMenu.addClickHandler(new ClickHandler() {
                                              @Override
                                              public void onClick(MenuItemClickEvent event) {
                                                  selectedEvent.setAttribute("isGraph",false);
                                                  GlobalData.getDataSource_tasks().updateData(selectedEvent);
                                                  }
                                          });
        menu.addItem(moveToBacklogMenu);
        menu.addItem(new com.smartgwt.client.widgets.menu.MenuItemSeparator());
        
        menu.addItem(
        		getMenuItem("Временной шаг: 1 день ",  TimeUnit.DAY, TimeUnit.DAY, 31, null)
            );

        menu.addItem(new com.smartgwt.client.widgets.menu.MenuItemSeparator());

        eventPropertiesMenu = new MenuItem("Свойства задачи");
        eventPropertiesMenu.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				getEditEventDialog(selectedEvent).setUpdateEvent(extendedTimeline, selectedEvent);
				getEditEventDialog(selectedEvent).show();
			}});
        eventPropertiesMenu.setEnabled(false);
        menu.addItem(eventPropertiesMenu);
        menu.setHeight(menu.getItems().length*ApplicationMenu.ITEM_MENU_HEIGHT - (menu.getItems().length-2));

        return menu;  
    } 
    
	private EditEventDialog getEditEventDialog(Record event)
	{
		if (event == null)
			SC.warn("Задача не выбрана");
		
		if (editEventDialog == null)
			editEventDialog = new EditEventDialog(event);
		return editEventDialog;
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
}
