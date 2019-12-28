package org.taurus.aya.client.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasAutoHorizontalAlignment;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.*;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.calendar.*;
import com.smartgwt.client.widgets.calendar.events.*;
import com.smartgwt.client.widgets.events.*;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

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
	private Runnable updateCallback = null;
	
	ExtendedTimeline extendedTimeline;
	private  Date startDate;
	private Date endDate;

    DateTimeFormat weekendFormat = DateTimeFormat.getFormat("c");
    DateTimeFormat numberFormat = DateTimeFormat.getFormat("dd.MM");
	
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
		setCanEditLane(true);
        setHeight100();
		setMinHeight(0);
		setWidth100();
		setMargin(0);
		setPadding(0);
		setDisableWeekends(false);

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
        indicator1.setHeaderBackgroundColor("white");
        indicator1.setHeaderBorderColor("white");

		CalendarEvent indicator2 = new CalendarEvent();
		indicator2.setStartDate(d);
		indicator2.setName("");
        indicator2.setHeaderBackgroundColor("white");
        indicator2.setHeaderBorderColor("white");

		addIndicator(indicator1);
		addIndicator(indicator2);
		setShowIndicators(true);
//		setShowZones(true);
//		setShowZoneHovers(true);

//		Configure lane field
		lgf = new ListGridField("title","Поток");
		lgf.setAutoFitWidth(true);
		lgf.setAutoFitWidthApproach(AutoFitWidthApproach.VALUE);
		lgf.setWrap(true);
		//lgf.setMinWidth(200);
		lgf.setWidth(200);


		setLaneFields(new ListGridField[]{lgf});

//         Configure the time range

		setTimeResolution(TimeUnit.DAY, TimeUnit.DAY, 31, null);

		startDate = new Date();
		endDate = new Date();

		startDate.setDate(1);
		endDate.setDate(1);
		if (startDate.getMonth() < 11) {
			endDate.setMonth(startDate.getMonth() + 1);
		}
		else
		{
			endDate.setMonth(0);
			endDate.setYear(startDate.getYear() + 1);
		}

		setTimelineRange(startDate,endDate);
		setStartDate(startDate);
		setEndDate(endDate);

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
					SC.logWarn("ExtendedTimeline: Сейчас буду показывать диалог");
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
            SC.logWarn("ExtendedTimeline: event resize startDate: " + event.getNewEvent().getStartDate() + " endDate: " + event.getNewEvent().getEndDate());
			if (!event.getEvent().getStartDate().equals(event.getNewEvent().getStartDate()))
				modifyCalendarEventStartDate(event.getNewEvent(),event.getEvent());
			SC.logWarn("ExtendedTimeline: event resize startDate: " + event.getNewEvent().getStartDate() + " endDate: " + event.getNewEvent().getEndDate());
        });

        addEventChangedHandler(new EventChangedHandler(){

			@Override
			public void onEventChanged(CalendarEventChangedEvent event) {
				//updateTasks();
				SC.logWarn("EventChangedHandler task id => " + event.getEvent().getAttribute("id"));
                SC.logWarn("EventChangedHandler date_start => " + event.getEvent().getAttribute("startDate") + " dateEnd = " + event.getEvent().getAttribute("endDate"));
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

				EditEventDialog ee = getEditEventDialog(ev);
				ee.setNewEvent(extendedTimeline, ev);
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

		addFetchDataHandler(new FetchDataHandler() {
								@Override
								public void onFilterData(FetchDataEvent event) {

								if(thisIsFirstCall) {
									int delta = new Date().getDate() * 60; //+ currentRecord.getAttributeAsDate("endDate").getMinutes()/5*150 - getTimelineView().getWidth()/2;

									SC.logWarn("ExtendedTimeline: FetchDataHandler: need to scroll window to " + delta + " pixels (current day: " + new Date().getDate() + ")");
									getTimelineView().scrollBodyTo(delta, 0);
									thisIsFirstCall=false;
									}

								}
							});

		//  set menu and load the data

		setContextMenu(getContextMenu());

		updateTimeline();
	}

    private void modifyCalendarEventStartDate(CalendarEvent newEvent, CalendarEvent oldEvent) {
        Date start = newEvent.getStartDate();
		start = new Date(start.getTime() + 24*3600*1000);

		// защита от установки нулевого или отрицательного размера прямоугольника задачи
        if (start.getTime() < newEvent.getEndDate().getTime())
        	newEvent.setStartDate(start);
    }

	private void modifyCalendarEventEndDate(CalendarEvent newEvent, CalendarEvent oldEvent) {
		Date end = newEvent.getEndDate();
		end = new Date(end.getTime() + 24*3600*1000);
		// защита от установки нулевого или отрицательного размера прямоугольника задачи
		if (end.getTime() >= newEvent.getStartDate().getTime())
			newEvent.setEndDate(end);
	}

    private void setTimeResolution(TimeUnit headerUnit, TimeUnit rangeUnit, int columnCount, Integer minutesPerColumn)
    {
    	HeaderLevel[] headerLevels = {};

		HeaderLevel hl = new HeaderLevel(TimeUnit.DAY);
		hl.setTitleFormatter(new HeaderLevelTitleCustomizer(){

			@Override
			public String getTitle(HeaderLevel headerLevel, Date startDate,
								   Date endDate, String defaultValue, Calendar   calendar) {
					if (weekendFormat.format(startDate).equals("6") || weekendFormat.format(startDate).equals("0"))
						return "<b>" + numberFormat.format(startDate) + "</b>";
					else
						return numberFormat.format(startDate);
			}});
		headerLevels = new HeaderLevel[]{hl};

    	setResolution(headerLevels, rangeUnit, columnCount);
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
			SortSpecifier sortSpecifier = new SortSpecifier("laneOrder", SortDirection.ASCENDING);
			SortSpecifier[] sortSpecifiers = { sortSpecifier };
			dsr.setSortBy(sortSpecifiers);
			
	        GlobalData.getDataSource_lanes().fetchData(laneSearchCriteria, new DSCallback(){
	
				@Override
				public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
				{
					SC.logWarn("TaskView: lanes amount: " + dsResponse.getData().length);
					ArrayList<Lane> sublanes = new ArrayList<Lane>();

					for (int i=0; i< dsResponse.getData().length; i++)
					{

						Lane lane = new Lane(dsResponse.getData()[i].getAttribute("name"),generateLaneTitle(dsResponse.getData()[i].getAttribute("name"),sublanes));

							lane.setHeight(100);

						addLane(lane);
					}
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
				
				if (currentRecord != null) {

					Scheduler.get().scheduleDeferred(new com.google.gwt.user.client.Command() {
						public void execute() {
							selectRecord(currentRecord);
						}
					});
				}

				if (updateCallback != null) updateCallback.run();

				Optional<Record> optTask = Arrays.stream(dsResponse.getData()).filter(d -> d.getAttributeAsBoolean("userCorrectSpentTime")).findFirst();
				if (optTask.isPresent()) {
					Record task = optTask.get();
					showRevisionDialog(task);
				}
				GlobalData.getStatusBar().stopIndicateProcess();
				
				// Scroll feature availible only in DAY granuality 
				if (getTimelineGranularity() == TimeUnit.DAY && thisIsFirstCall)
				{
					Date d = new Date();
					int delta = d.getDay() *60; //+ currentRecord.getAttributeAsDate("endDate").getMinutes()/5*150 - getTimelineView().getWidth()/2;
					SC.logWarn("ExtendedTimeline: need to scroll window to " + delta + " pixels (granuality: " + getTimelineGranularity().toString() + ")");
					if ( delta > 0)
						getTimelineView().scrollBodyTo(delta, 0);
					thisIsFirstCall = false;
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
	
	public void addUpdateHandler(Runnable callback)
	{
		updateCallback = callback;
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

        GlobalData.getDataSource_tasks().updateData(selectedEvent, new DSCallback() {
            @Override
            public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
                updateTasks();
                ResourceLifeCycleManager.resourceChanged(ResourceType.TASK, getSelectedEvent());
            }
        });

    }

    private void showRevisionDialog(Record task)
	{
		final Dialog dialog = new Dialog();
		dialog.setTitle("Уточнение значения");
		dialog.addItem(new Label("Завершенная вами задача " +
				"\"" + task.getAttribute("name") + "\" " +
				"длилась более одного дня.<br/>Система рассчитала время её выполнения, вы можете изменить его или оставить рассчитанное"));
		dialog.setWidth(500);
		dialog.setHeight(260);
		dialog.setMembersMargin(10);
		DynamicForm df = new DynamicForm();
		df.setHeight(50);

		TextItem value = new TextItem("Время выполнения (в часах)");
		Button btAccept = new Button("Изменить");
		btAccept.disable();
		Button btCancel = new Button("Оставить");


		value.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				btAccept.enable();
			}
		});
		value.addKeyPressHandler(new com.smartgwt.client.widgets.form.fields.events.KeyPressHandler() {
			@Override
			public void onKeyPress(com.smartgwt.client.widgets.form.fields.events.KeyPressEvent keyPressEvent) {
				if (keyPressEvent.getKeyName().equals("Enter"))
				{
					try {
						Integer duration = Integer.valueOf(value.getValueAsString());
						if (duration < 0 || duration > 1000)
							SC.warn("Введенное значение некорректно!");
						else {
							task.setAttribute("spentTime", duration);
							GlobalData.getDataSource_tasks().updateData(task, new DSCallback() {
								@Override
								public void execute(DSResponse dsResponse, Object o, DSRequest dsRequest) {
									dialog.hide();
								}
							});
						}
					} catch (NumberFormatException nfe) {
						SC.warn("Введенное значение не является числом!");
					}
				}
			}
		});
		value.setValue(task.getAttributeAsDouble("spentTime"));
		df.setFields(value);

		df.setHiliteRequiredFields(false);
		df.setWrapItemTitles(false);
		df.setWidth(450);
		df.setColWidths(200,350,0);
		dialog.addItem(df);

		VLayout vlayout = new VLayout();
		vlayout.setHeight("50px");
		vlayout.setAlign(VerticalAlignment.CENTER);

		HLayout hlayout = new HLayout();
		hlayout.setAlign(Alignment.RIGHT);
		hlayout.setMembersMargin(10);

		vlayout.addMember(hlayout);



		btAccept.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				try {
					Integer duration = Integer.valueOf(value.getValueAsString());
					if (duration < 0 || duration > 1000)
						SC.warn("Введенное значение некорректно!");
					else {
						task.setAttribute("spentTime", duration);
						GlobalData.getDataSource_tasks().updateData(task, new DSCallback() {
							@Override
							public void execute(DSResponse dsResponse, Object o, DSRequest dsRequest) {
								dialog.hide();
							}
						});
					}
				} catch (NumberFormatException nfe) {
					SC.warn("Введенное значение не является числом!");
				}
			}
		});

		btCancel.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				dialog.hide();
			}
		});
		hlayout.setMembers(btAccept,btCancel);
		dialog.addItem(vlayout);

		dialog.draw();
		dialog.centerInPage();
	}

}
