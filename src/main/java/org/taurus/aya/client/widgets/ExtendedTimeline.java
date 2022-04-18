package org.taurus.aya.client.widgets;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.*;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
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
import java.util.function.Consumer;

public class ExtendedTimeline extends Timeline {

	private Menu menu;
	private int blockSelectMode = 0;
	ListGridField lgf;
	TaskView panel;
	AdvancedCriteria laneSearchCriteria;
	boolean distinctByUsers = true;
	private Consumer<Boolean> enableButtonsCallback;
	private Runnable generateAdvicesCallback;
	private MenuItem dialogOpenMenu, eventPropertiesMenu;
	private CalendarEvent selectedEvent=null;
	private boolean thisIsFirstCall = true;
	private Runnable updateCallback = null;
	
	ExtendedTimeline extendedTimeline;
	private  Date startDate;
	private Date endDate;

    DateTimeFormat weekendFormat = DateTimeFormat.getFormat("c");
    DateTimeFormat numberFormat = DateTimeFormat.getFormat("dd.MM");
	private MenuItem setStateProcess;
	private MenuItem setStatePause;
	private MenuItem setStateReady;
	private MenuItem setStateFail;
	private MenuItem setStateNew;
	private boolean lastCanSwitch = false;
	private CalendarEvent indicator1;
	private CalendarEvent indicator2;

	public ExtendedTimeline(TaskView panel, final boolean distinctByUsers, Consumer<Boolean> enableButtonsCallback, Runnable generateAdvicesCallback)
	{
		this.panel = panel;
		this.distinctByUsers = distinctByUsers;
		this.enableButtonsCallback = enableButtonsCallback;
		this.generateAdvicesCallback = generateAdvicesCallback;
		extendedTimeline = this;
		
		CalendarView calendarView = new CalendarView();
		calendarView.setMinHeight(0);
		calendarView.setAutoFitHeaderHeights(true);
		//calendarView.setHeight100();
		setAutoChildProperties("timelineView", calendarView);
		setShowControlsBar(false);

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

		setShowAddEventButton(false);
		setSublaneNameField("sublane");
		setUseSublanes(false);
        setRowHeight(200);

		SC.logWarn("ExtendedTimeline: set initial criterias ");
        setDataSource(GlobalData.getDataSource_events());
		//setInitialCriteria(new AdvancedCriteria("isGraph", OperatorId.EQUALS, true));
		setImplicitCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{
				new Criterion("executor", OperatorId.EQUALS, GlobalData.getCurrentUser().getAttributeAsString("id")),
				new Criterion("isGraph", OperatorId.EQUALS, true)
		}));

		setAutoFetchData(true);

		indicator1 = new CalendarEvent();
		indicator1.setStartDate(new Date());
		Date d = new Date();
		indicator1.setEndDate(d);
		indicator1.setCanDrag(false);
        indicator1.setHeaderBackgroundColor("white");
        indicator1.setHeaderBorderColor("white");

		indicator2 = new CalendarEvent();
		d.setTime(d.getTime() + 24*3600*1000);
		indicator2.setStartDate(d);
		indicator2.setCanEdit(false);
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

		setTimeResolution(TimeUnit.DAY, TimeUnit.DAY, 7, 150);

		startDate = new Date();
		endDate = new Date();

		startDate.setDate(1);
		endDate.setDate(1);
		if (startDate.getMonth() < 11) {
			endDate.setMonth(startDate.getMonth() + 1);
			endDate.setYear(startDate.getYear());
		}
		else
		{
			endDate.setMonth(0);
			endDate.setYear(startDate.getYear() + 1);
		}
		endDate = new Date(endDate.getTime() - 24 * 3600 * 1000);

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
					selectedEvent.setHeaderTextColor(selectedEvent.getTextColor());
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
					selectedEvent.setHeaderBackgroundColor("#3764a3");//#DCDCDC
					selectedEvent.setHeaderTextColor("#FFFFFF");
					selectedEvent.setBorderColor("#297ACC");

					refreshEvent(selectedEvent);
					redraw();
				}

				// we cannot switch from NEW to any state
				setCanSwitchToAnyState(!event.getEvent().getAttributeAsInt("state").equals(EventState.NEW.ordinal()));

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
								GlobalData.getDataSource_events().removeData(selectedEvent, new DSCallback() {
									@Override
									public void execute(DSResponse dsResponse, Object o, DSRequest dsRequest) {
										//Если удаляемая задача была разбита на фрагменты
										SC.logWarn("Fragmented = " + selectedEvent.getAttributeAsBoolean("fragmented"));
										if (selectedEvent.getAttributeAsBoolean("fragmented"))
											updateTasks();
										ResourceLifeCycleManager.resourceDeleted(ResourceType.TASK, selectedEvent);
										selectedEvent = null;
										generateAdvicesCallback.run();
									}
								});
							}
						}});
				}
				else
					SC.warn("У вас недостаточно прав для удаления этой задачи");
				event.cancel();
			}});

		//Date modification
//        addEventResizeStopHandler(event -> {
//            SC.logWarn("ExtendedTimeline: event resize startDate: " + event.getNewEvent().getStartDate() + " endDate: " + event.getNewEvent().getEndDate());
//			if (!event.getEvent().getStartDate().equals(event.getNewEvent().getStartDate()))
//				modifyCalendarEventStartDate(event.getNewEvent(),event.getEvent());
//			SC.logWarn("ExtendedTimeline: event resize startDate: " + event.getNewEvent().getStartDate() + " endDate: " + event.getNewEvent().getEndDate());
//        });

        addEventChangedHandler(new EventChangedHandler(){

			@Override
			public void onEventChanged(CalendarEventChangedEvent event) {
				SC.logWarn("EventChangedHandler task id => " + event.getEvent().getAttribute("id"));
                SC.logWarn("EventChangedHandler date_start => " + event.getEvent().getAttribute("startDate") + " dateEnd = " + event.getEvent().getAttribute("endDate"));

				generateAdvicesCallback.run();
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
				ev.setAttribute("state",EventState.NEW.ordinal());
				ev.setAttribute("eventWindowStyle", "s3_event_new");
				ev.setAttribute("showInBacklog",false);

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
				ee.setNewEvent(ev);
				event.cancel();
			}});


        // set customizers
		/* Для event-ов, представляющих инликатор, возвращает пустую строку
		* Для остальных (представляющих задачи) имя или имя + индекс
  	    */
        setEventHeaderHTMLCustomizer(new EventHeaderHTMLCustomizer(){

			@Override
			public String getEventHeaderHTML(CalendarEvent calendarEvent,
                                             CalendarView calendarView) {
				String result = "";
				if (calendarEvent.getAttributeAsInt("index") != null) {
					if (calendarEvent.getAttributeAsInt("index").equals(0))
						result =  "<b>" + calendarEvent.getAttributeAsString("name") + "</b>";
					else
						result = "<b>" + calendarEvent.getAttributeAsString("name") + " " + calendarEvent.getAttributeAsString("index") + "</b>";
				}
				if (calendarEvent.getAttributeAsString("externalUrl") != null && !calendarEvent.getAttributeAsString("externalUrl").equals(""))
					result = "<a href=\"" + calendarEvent.getAttributeAsInt("externalUrl") +"\" target=\"_blank\">" +
							"<img src=\"/app/images/forms/paperclip.png\"/>" + "</a>" + result;
				return result;
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

		setEventHoverHTMLCustomizer(new EventHoverHTMLCustomizer(){
			@Override
			public String getEventHoverHTML(CalendarEvent calendarEvent, EventWindow eventWindow) {

				Date startDate = calendarEvent.getAttributeAsDate("startDate");
				Date endDate = calendarEvent.getAttributeAsDate("endDate");
				if (startDate == null || endDate == null) return "";

				endDate.setTime(endDate.getTime() - 24*3600 * 1000); // decrease amount of seconds in the endDate for setting it to the previous date

				final String eventStatus;
				switch(calendarEvent.getAttributeAsInt("state")){
					case 0: eventStatus = "[Не в работе]"; break;			//<img src="images/buttons/task_new.png" width="24" height="24"/> [
					case 1: eventStatus = "[Выполняется]"; break;			//<img src="images/buttons/task_play.png"  width="24" height="24"/>
					case 2: eventStatus = "[Готово]"; break;			//<img src="images/buttons/task_ready.png" width="24" height="24"/>
					case 3: eventStatus = "[Пауза]"; break;			//<img src="images/buttons/task_pause.png" width="24" height="24"/>
					case 4: eventStatus = "[Внимание]"; break;			//<img src="images/buttons/task_fail.png" width="24" height="24"/>
					default: eventStatus = "";
				}
				return 	"<span class= \"eventHoverSpan\">"
						+ numberFormat.format(startDate) + " - "  + numberFormat.format(endDate) + "         "
						+ eventStatus + "</span><br/><br/>"
						+ calendarEvent.getAttributeAsString("name") + "<br>"
						+ calendarEvent.getAttributeAsString("description") ;
			}
		});

		addDropHandler(new DropHandler() {
                           @Override
                           public void onDrop(DropEvent event) {
                               Record r = GlobalData.getNavigationArea().getTaskPanel().getTreeSelectedRecord();

                               if (r.getAttribute("lane") == null || r.getAttribute("lane").equals("null"))
								   new BacklogTaskDialog(r, () -> generateAdvicesCallback.run());
                               else {
                                   r.setAttribute("isGraph", true);
                                   GlobalData.getDataSource_events().updateData(r, new DSCallback() {
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
									int delta = new Date().getDate() * 60; //+ currentRecord.getAttributeAsDate("endDate").getMinutes()/5*150 - getTimelineView().getWidth()/2;
//
//									SC.logWarn("ExtendedTimeline: FetchDataHandler: need to scroll window to " + delta + " pixels (current day: " + new Date().getDate() + ")");
									if (thisIsFirstCall) getTimelineView().scrollBodyTo(delta, 0);
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

    public void setTimeResolution(TimeUnit headerUnit, TimeUnit rangeUnit, int columnCount, int headerWidth)
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
		hl.setHeaderWidth(headerWidth);
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

    	fetchData(getImplicitCriteria(), new DSCallback() {
			
			@Override
			public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {

				SC.logWarn("updateTasks(): Task list size is " + dsResponse.getData().length);
				
				if (updateCallback != null) updateCallback.run();

				// Поиск задачи с установленным флагом запроса корректировки времени
				Optional<Record> optTask = Arrays.stream(dsResponse.getData())
						.filter(d -> d.getAttributeAsBoolean("userCorrectSpentTime") && d.getAttributeAsInt("executor").equals(GlobalData.getCurrentUser().getAttributeAsInt("id")))
						.findFirst();
				if (optTask.isPresent()) {
					Record task = optTask.get();
					showRevisionDialog(task);
				}
				// Поиск задачи на сегодня в состоянии PROCESS
				Long currentTime = new Date().getTime();
				optTask = Arrays.stream(dsResponse.getData())
						.filter(d -> d.getAttributeAsInt("state").equals(EventState.PROCESS.ordinal())
								&& (d.getAttributeAsDate("startDate").getTime() < currentTime
								&& d.getAttributeAsDate("endDate").getTime() > currentTime)
								&& d.getAttributeAsInt("executor").equals(GlobalData.getCurrentUser().getAttributeAsInt("id"))
						)
						.findFirst();
				if (optTask.isPresent())
					panel.setBrowserIconToRunning(true, "Aya - " + optTask.get().getAttributeAsString("name"));
				else
					panel.setBrowserIconToRunning(false, "Aya");

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
                                              		Record currentRecord =  getSelectedEvent();
												  if (currentRecord != null) {
													  DSRequest dsr = new DSRequest();
													  dsr.setData(currentRecord);
													  GlobalData.getDataSource_events().performCustomOperation("moveToBacklog", currentRecord, new DSCallback() {
														  @Override
														  public void execute(DSResponse dsResponse, Object o, DSRequest dsRequest) {
														  	GlobalData.getNavigationArea().getTaskPanel().update();
														  	thisIsFirstCall = false;
														  	updateTasks();
														  }
													  }, dsr);
												  }
												  else
													  SC.logWarn("Задача не выбрана");
											  }
                                          });
        menu.addItem(moveToBacklogMenu);
        menu.addItem(new com.smartgwt.client.widgets.menu.MenuItemSeparator());

		setStateProcess = new MenuItem(EventState.PROCESS.getName());
        setStateProcess.addClickHandler(event -> { setEventState(EventState.PROCESS);});
        setStateProcess.setIcon("buttons/task_play.png");
        setStateProcess.setKeyTitle("<sup>Alt+A</sup>");
        menu.addItem(setStateProcess);

		setStatePause = new MenuItem(EventState.PAUSE.getName());
        setStatePause.addClickHandler(event -> { setEventState(EventState.PAUSE);});
        setStatePause.setIcon("buttons/task_pause.png");
        setStatePause.setKeyTitle("<sup>Alt+S</sup>");
        setStatePause.setEnabled(false);
        menu.addItem(setStatePause);

		setStateReady = new MenuItem(EventState.READY.getName());
        setStateReady.addClickHandler(event -> { setEventState(EventState.READY);});
        setStateReady.setIcon("buttons/task_ready.png");
        setStateReady.setKeyTitle("<sup>Alt+D</sup>");
        setStateReady.setEnabled(false);
        menu.addItem(setStateReady);

		setStateFail = new MenuItem(EventState.FAIL.getName());
        setStateFail.addClickHandler(event -> { setEventState(EventState.FAIL);});
        setStateFail.setIcon("buttons/task_fail.png");
        setStateFail.setKeyTitle("<sup>Alt+F</sup>");
        setStateFail.setEnabled(false);
        menu.addItem(setStateFail);

		setStateNew = new MenuItem(EventState.NEW.getName());
        setStateNew.addClickHandler(event -> { setEventState(EventState.NEW);});
        setStateNew.setIcon("buttons/task_new.png");
        setStateNew.setKeyTitle("<sup>Alt+G</sup>");
        menu.addItem(setStateNew);

        menu.addItem(new com.smartgwt.client.widgets.menu.MenuItemSeparator());

        eventPropertiesMenu = new MenuItem("Свойства задачи");
        eventPropertiesMenu.addClickHandler(event -> getEditEventDialog(selectedEvent));
        eventPropertiesMenu.setEnabled(false);
        eventPropertiesMenu.setKeyTitle("<sup>Alt+Click</sup>");
        menu.addItem(eventPropertiesMenu);
        menu.setHeight(menu.getItems().length*ApplicationMenu.ITEM_MENU_HEIGHT - (menu.getItems().length-2));

        return menu;  
    } 
    
	private EditEventDialog getEditEventDialog(Record event)
	{
		return new EditEventDialog(event, () -> generateAdvicesCallback.run());
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
        final CalendarEvent changedEvent = getSelectedEvent();
        if (changedEvent == null) return;
        changedEvent.setAttribute("eventWindowStyle", s3_event_pause);
        changedEvent.setAttribute("state", s);
        changedEvent.setAttribute("icon", s2);

        GlobalData.getDataSource_events().updateData(changedEvent, new DSCallback() {
            @Override
            public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				thisIsFirstCall=false;
				//проверка на то, что резуольтат всего один
				if (dsResponse.getData().length<1) {SC.logWarn("setEventState: server changes 0 tasks! Stop refreshing"); return;}

				//копирование в selectedEvent атрибутов, которые могли измениться на сервере
				Record.copyAttributesInto(selectedEvent,dsResponse.getData()[0],"spentTime");
                updateTasks();
                ResourceLifeCycleManager.resourceChanged(ResourceType.TASK, getSelectedEvent());
            }
        });
    }

    private void showRevisionDialog(Record task)
	{
		final Window dialog = new Window();
		dialog.setTitle("Уточнение значения");
		Label label = new Label("Завершенная вами задача " +
				"\"" + task.getAttribute("name") + "\" " +
				"длилась более одного дня.<br/>Система рассчитала время её выполнения, вы можете изменить его или оставить рассчитанное");
		label.setMargin(5);
		dialog.addItem(label);
		dialog.setWidth(500);
		dialog.setHeight(260);
		dialog.setCanDragReposition(true);
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
							GlobalData.getDataSource_events().updateData(task, new DSCallback() {
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
		df.setMargin(5);
		df.setWidth(450);
		df.setColWidths(200,350,0);
		dialog.addItem(df);

		VLayout vlayout = new VLayout();
		vlayout.setHeight("50px");
		vlayout.setAlign(VerticalAlignment.CENTER);

		HLayout hlayout = new HLayout();
		hlayout.setAlign(Alignment.RIGHT);
		hlayout.setMembersMargin(10);
		hlayout.setMargin(5);

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
						GlobalData.getDataSource_events().updateData(task, new DSCallback() {
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
				GlobalData.getDataSource_events().updateData(task, new DSCallback() {
					@Override
					public void execute(DSResponse dsResponse, Object o, DSRequest dsRequest) {
						dialog.hide();
					}
				});
			}
		});
		hlayout.setMembers(btAccept,btCancel);
		dialog.addItem(vlayout);

		dialog.draw();
		dialog.centerInPage();
	}

	private Criterion getComposedCriteria(){

		Criterion crt1 = new Criterion();
		crt1.addCriteria(getImplicitCriteria());
		Criterion crt2 = new Criterion();
		crt2.addCriteria(getInitialCriteria());
		Criterion composedCriteria = new Criterion(OperatorId.AND, new Criterion[]{crt1,crt2});
		return composedCriteria;
	}

	public void setViewMyTasksMode(boolean viewMyTasks){
		if (viewMyTasks)
			setImplicitCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{
					new Criterion("executor", OperatorId.EQUALS, GlobalData.getCurrentUser().getAttributeAsString("id")),
					new Criterion("isGraph", OperatorId.EQUALS, true),
			}));
		else
			setImplicitCriteria(new AdvancedCriteria("isGraph", OperatorId.EQUALS, true));

		updateTasks();
	}

	private void setCanSwitchToAnyState(boolean canSwitch)
	{
		if (lastCanSwitch != canSwitch) {
			lastCanSwitch = !lastCanSwitch;
			setStatePause.setEnabled(canSwitch);
			setStateReady.setEnabled(canSwitch);
			setStateFail.setEnabled(canSwitch);
			menu.refreshRow(3);
			menu.refreshRow(4);
			menu.refreshRow(5);
			enableButtonsCallback.accept(canSwitch);
		}
	}

	public void updateIndicators(){
		removeIndicator(indicator1);
		removeIndicator(indicator2);

		indicator1.setStartDate(new Date());
		Date d = new Date();
		indicator1.setEndDate(d);

		d.setTime(d.getTime() + 24*3600*1000);
		indicator2.setStartDate(d);

		SC.logWarn("updateIndicators," + d);

		addIndicator(indicator1);
		addIndicator(indicator2);
	}

}