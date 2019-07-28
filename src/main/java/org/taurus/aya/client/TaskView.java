package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.calendar.CalendarEvent;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.widgets.ExtendedTimeline;
import org.taurus.aya.shared.TaskAnalyseData;
//import com.smartgwt.client.widgets.Window;


public class TaskView extends ContentPane {
	
	private ExtendedTimeline timeline;
	private ExtendedTimeline timeline2;
	TaskView taskView;
	DetailViewer props;
	AdvancedCriteria laneSearchCriteria;
	Record currentRecord;
	int tabUID;
	ListGridField lgf;
	ContentPane contentPane;
	ToolStrip toolStrip;
	boolean isShown = false;
	boolean distinctByUsers = true;
	HLayout hLayout;
	HTMLPane pane;
	Menu menu;
	Label analyseResultLabel;

public TaskView(Record currentRecord, int tabUID)
	{
		this.currentRecord = currentRecord;
		this.tabUID = tabUID;
		contentPane = this;
		taskView = this; 
		
		
		timeline = new ExtendedTimeline(null, false);
		//this.addMember(timeline);
		timeline2 = new ExtendedTimeline(null,true);
		timeline2.hide();
		VLayout vLayout = new VLayout();
		vLayout.setWidth100();
		vLayout.setHeight100();
		vLayout.setMinHeight(100);
		vLayout.setMargin(0);
		hLayout = new HLayout();
		hLayout.setMinHeight(0);
		hLayout.setWidth100();
		hLayout.setHeight100();
		hLayout.setMargin(0);
		hLayout.addMember(timeline);
		hLayout.addMember(timeline2);
		vLayout.addMember(hLayout);


		vLayout.addMember(createToolStripPanel());
		this.addMember(vLayout);

		//this.addMember(createPropsPanel());
		//this.setHeight100();
		this.setWidth100();
		setMargin(0);
		
		//!this.setOverflow(Overflow.HIDDEN);
	    this.setMinHeight(0);
	}

    HLayout createToolStripPanel()
	{

        HLayout layout = new HLayout();

		toolStrip = new ToolStrip();
		toolStrip.setHeight(30);
		toolStrip.setWidth100();
		

		ToolStripButton btnTaskProcess = new ToolStripButton("Начать выполнение");
		btnTaskProcess.setIcon("buttons/task_play.png");
		btnTaskProcess.setWidth(16);
		btnTaskProcess.setHeight(16);
		btnTaskProcess.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				final CalendarEvent selectedEvent = getSelectedEvent() ;
				if (selectedEvent == null) return;
				selectedEvent.setAttribute("eventWindowStyle", "s3_event_process");
				selectedEvent.setAttribute("state", "1");
				selectedEvent.setAttribute("icon", "tree/task1.png");
				GlobalData.getDataSource_tasks().updateData(selectedEvent,new DSCallback(){
					@Override
					public void execute(DSResponse dsResponse, Object data,
                                        DSRequest dsRequest) {
						//ResourceLifeCycleManager.resourceChanged(ResourceType.TASK, getSelectedEvent());
					}});
			}
		});
		toolStrip.addMember(btnTaskProcess);
		
		ToolStripButton btnTaskPause = new ToolStripButton("Пауза");
		btnTaskPause.setIcon("buttons/task_pause.png");
		btnTaskPause.setWidth(16);
		btnTaskPause.setHeight(16);

		btnTaskPause.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				final CalendarEvent selectedEvent = getSelectedEvent() ;
				if (selectedEvent == null) return;
				selectedEvent.setAttribute("eventWindowStyle", "s3_event_pause");
				selectedEvent.setAttribute("state", "0");
				selectedEvent.setAttribute("icon", "tree/task2.png");
				GlobalData.getDataSource_tasks().updateData(selectedEvent,new DSCallback(){
					@Override
					public void execute(DSResponse dsResponse, Object data,
                                        DSRequest dsRequest) {
						ResourceLifeCycleManager.resourceChanged(ResourceType.TASK, getSelectedEvent());
					}});
			}
		});
		toolStrip.addMember(btnTaskPause);

		ToolStripButton btnTaskReady = new ToolStripButton("Задача решена");
		

		btnTaskReady.setIcon("buttons/task_ready.png");
		btnTaskReady.setWidth(16);
		btnTaskReady.setHeight(16);

		btnTaskReady.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				final CalendarEvent selectedEvent = getSelectedEvent() ;
				if (selectedEvent == null) return;
				selectedEvent.setAttribute("eventWindowStyle", "s3_event_ready");
				selectedEvent.setAttribute("state", "3");
				selectedEvent.setAttribute("icon", "tree/task3.png");
				GlobalData.getDataSource_tasks().updateData(selectedEvent,new DSCallback(){
					@Override
					public void execute(DSResponse dsResponse, Object data,
                                        DSRequest dsRequest) {
						ResourceLifeCycleManager.resourceChanged(ResourceType.TASK, getSelectedEvent());
					}});
			}
		});
		toolStrip.addMember(btnTaskReady);

		ToolStripButton btnTaskFail = new ToolStripButton("Возникла проблема");
		btnTaskFail.setIcon("buttons/task_fail.png");
		btnTaskFail.setWidth(16);
		btnTaskFail.setHeight(16);

		btnTaskFail.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				final CalendarEvent selectedEvent = getSelectedEvent() ;
				if (selectedEvent == null) return;
				selectedEvent.setAttribute("eventWindowStyle", "s3_event_fail");
				selectedEvent.setAttribute("state", "4");
				selectedEvent.setAttribute("icon", "tree/task4.png");
				GlobalData.getDataSource_tasks().updateData(getSelectedEvent(),new DSCallback(){
					@Override
					public void execute(DSResponse dsResponse, Object data,
                                        DSRequest dsRequest) {
						ResourceLifeCycleManager.resourceChanged(ResourceType.TASK, getSelectedEvent());
					}});
			}
		});
		toolStrip.addMember(btnTaskFail);
		
		toolStrip.addSeparator();

		final ToolStripButton showTimelineByLane = new ToolStripButton("Потоки");
		showTimelineByLane.setSelected(true);
		
		final ToolStripButton showTimelineByPeople = new ToolStripButton("Исполнители");
		
		showTimelineByLane.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				showTimelineByLane.setSelected(true);
				showTimelineByPeople.setSelected(false);
				timeline.show();
				timeline2.hide();

			}});
		
		toolStrip.addButton(showTimelineByLane); 
  
		showTimelineByPeople.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				showTimelineByLane.setSelected(false);
				showTimelineByPeople.setSelected(true);
				timeline.hide();
				timeline2.show();

			}});
		
		toolStrip.addButton(showTimelineByPeople);

		layout.addMember(toolStrip);

		ToolStrip toolStrip2 = new ToolStrip();
		toolStrip2.setHeight(30);

        analyseResultLabel = new Label("Тут пока ничего нет");
        analyseResultLabel.setWidth(200);
        analyseResultLabel.setHeight100();
        analyseResultLabel.setAlign(Alignment.CENTER);
        analyseResultLabel.setContextMenu(createPopupMenu());
        analyseResultLabel.addClickHandler(new ClickHandler() {
                                               @Override
                                               public void onClick(ClickEvent event) {
												   analyseResultLabel.setContents("Обновление...");
                                                   try {
                                                       GlobalData.getAnalyticService().getPrognosis(new AsyncCallback<TaskAnalyseData>() {
                                                           @Override
                                                           public void onFailure(Throwable caught) {
                                                               analyseResultLabel.setContents("Что-то пошло не так и не туда");
															   SC.warn("Ошибка выполнения запроса на сервере",caught.getLocalizedMessage());
                                                           }

                                                           @Override
                                                           public void onSuccess(TaskAnalyseData result) {
                                                               showMenu(result.getAdvancedText());
															   SC.logWarn("AnalyticResult:" + result.getAdvancedText());
                                                           }
                                                       });
                                                   } catch (Exception e) {
                                                       analyseResultLabel.setContents("Ой");
													   SC.warn("Ошибка отправки запроса",e.getLocalizedMessage());
                                                   }
                                               }
                                           });

                toolStrip2.addMember(analyseResultLabel);

        layout.addMember(toolStrip2);
		
		return layout;
	}
	
	DetailViewer createPropsPanel()
	{
		props = new DetailViewer();
		props.setDataSource(GlobalData.getDataSource_tasks());
		props.setHeight("180");
	    return props;
	}
	
      
	public Record getResource() {
		return currentRecord;
	}

	public ResourceType getResourceType() {
		return ResourceType.GRAPH;
	}
	
	public void updateContent(Boolean all)
	{
		if (all)
		{
			updateTimeline();
		}
		else
		{
			timeline.updateTasks();
			timeline2.updateTasks();
		}
	}
	
	public void updateTimeline()
	{
		timeline.updateTimeline();
		timeline2.updateTimeline();
	}

	public ExtendedTimeline getCurrentTimeline()
	{
		if (timeline.isVisible())
			return timeline;
		else
			return timeline2;
	}

	private CalendarEvent getSelectedEvent()
	{
		return getCurrentTimeline().getSelectedEvent();
	}

	private void showMenu(String s)
	{
		pane.setContents(s);
		menu.setHeight(500);
		menu.setWidth(300);
		menu.showNextTo(analyseResultLabel,"top",false);
	}

	private Menu createPopupMenu()
	{
		menu = new Menu();
		pane = new HTMLPane();
		pane.setWidth(300);
		pane.setHeight(500);

		final MenuItem commandItem = new MenuItem();
		commandItem.setEmbeddedComponent(pane);
		commandItem.setCanSelect(false);
		commandItem.setEnabled(false);
		menu.setItems(commandItem);
		return menu;
	}
	
}
