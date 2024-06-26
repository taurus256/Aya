package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.calendar.CalendarEvent;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.widgets.ExtendedTimeline;
import org.taurus.aya.shared.Advice;
import org.taurus.aya.shared.AdviceState;
import org.taurus.aya.shared.TaskAnalyseData;
import java.util.List;
import java.util.Date;

public class TaskView extends ContentPane {
	
	private ExtendedTimeline timeline;
	private ExtendedTimeline timeline2;
	TaskView taskView;
	Record currentRecord;
	int tabUID;
	ListGridField lgf;
	ContentPane contentPane;
	ToolStrip toolStrip;
	HLayout hLayout;
	VLayout pane;
	final Menu menu = new Menu();
	Label analyseResultLabel;

	private class UpdateHandler implements Runnable
	{
		@Override
		public void run() {
			getServerAnalyseData();
		}
	}

public TaskView(Record currentRecord, int tabUID)
	{
		this.currentRecord = currentRecord;
		this.tabUID = tabUID;
		contentPane = this;
		taskView = this; 
		
		
		timeline = new ExtendedTimeline(this, false);
		timeline.addUpdateHandler(new UpdateHandler());
        timeline2 = new ExtendedTimeline(this,true);
		timeline2.addUpdateHandler(new UpdateHandler());
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

		ToolStripButton btnTaskProcess = new ToolStripButton(EventState.PROCESS.getName());
		btnTaskProcess.setIcon("buttons/task_play.png");
		btnTaskProcess.setWidth(16);
		btnTaskProcess.setHeight(16);
		btnTaskProcess.addClickHandler(event -> getCurrentTimeline().setEventState(EventState.PROCESS));
		toolStrip.addMember(btnTaskProcess);
		
		ToolStripButton btnTaskPause = new ToolStripButton(EventState.PAUSE.getName());
		btnTaskPause.setIcon("buttons/task_pause.png");
		btnTaskPause.setWidth(16);
		btnTaskPause.setHeight(16);
		btnTaskPause.addClickHandler(event -> getCurrentTimeline().setEventState(EventState.PAUSE));
		toolStrip.addMember(btnTaskPause);
		ToolStripButton btnTaskReady = new ToolStripButton(EventState.READY.getName());


		btnTaskReady.setIcon("buttons/task_ready.png");
		btnTaskReady.setWidth(16);
		btnTaskReady.setHeight(16);
		btnTaskReady.addClickHandler(event -> getCurrentTimeline().setEventState(EventState.READY));
		toolStrip.addMember(btnTaskReady);

		ToolStripButton btnTaskFail = new ToolStripButton(EventState.FAIL.getName());
		btnTaskFail.setIcon("buttons/task_fail.png");
		btnTaskFail.setWidth(16);
		btnTaskFail.setHeight(16);
		btnTaskFail.addClickHandler(event -> getCurrentTimeline().setEventState(EventState.FAIL));
		toolStrip.addMember(btnTaskFail);

		ToolStripButton btnNew = new ToolStripButton(EventState.NEW.getName());
		btnNew.setIcon("buttons/task_new.png");
		btnNew.setWidth(16);
		btnNew.setHeight(16);
		btnNew.addClickHandler(event -> getCurrentTimeline().setEventState(EventState.NEW));
		toolStrip.addMember(btnNew);


		toolStrip.addSeparator();

		final ToolStripButton showTimelineByLane = new ToolStripButton("Потоки");
		showTimelineByLane.setSelected(true);
		
		final ToolStripButton showTimelineByPeople = new ToolStripButton("Исполнители");
		
		showTimelineByLane.addClickHandler(event -> {
			showTimelineByLane.setSelected(true);
			showTimelineByPeople.setSelected(false);
			timeline.show();
			timeline2.hide();

		});
		
		toolStrip.addButton(showTimelineByLane); 
  
		showTimelineByPeople.addClickHandler(event -> {
			showTimelineByLane.setSelected(false);
			showTimelineByPeople.setSelected(true);
			timeline.hide();
			timeline2.show();

		});
		
		toolStrip.addButton(showTimelineByPeople);

		layout.addMember(toolStrip);

		ToolStrip toolStrip2 = new ToolStrip();
		toolStrip2.setHeight(30);

        analyseResultLabel = new Label("Тут пока ничего нет");
        analyseResultLabel.setWidth(200);
        analyseResultLabel.setHeight100();
        analyseResultLabel.setAlign(Alignment.CENTER);
        analyseResultLabel.setContextMenu(createPopupMenu());
        analyseResultLabel.addClickHandler(event -> {
			analyseResultLabel.setContents("Обновление...");
			getServerAnalyseData();
			showAdvicePanel();
		});

                toolStrip2.addMember(analyseResultLabel);

        layout.addMember(toolStrip2);
		
		return layout;
	}

	private void getServerAnalyseData()
	{
		try {
			GlobalData.getAnalyticService().getPrognosis(GlobalData.getCurrentUser().getAttributeAsLong("id"), new AsyncCallback<TaskAnalyseData>() {
				@Override
				public void onFailure(Throwable caught) {
					analyseResultLabel.setContents("Что-то пошло не так и не туда");
					SC.warn("Ошибка выполнения запроса на сервере",caught.getLocalizedMessage());
				}

				@Override
				public void onSuccess(TaskAnalyseData result) {
					analyseResultLabel.setContents(result.getPanelText());
					prepareAdvicePane(result.getAdvices());
					SC.logWarn("AnalyticResult:" + result.getAdvices().size());
				}
			});
		} catch (Exception e) {
			analyseResultLabel.setContents("Ой");
			SC.warn("Ошибка отправки запроса",e.getLocalizedMessage());
		}
	}


	public Record getResource() {
		return currentRecord;
	}

	public ResourceType getResourceType() {
		return ResourceType.GRAPH;
	}
	
	public void updateContent(Boolean all)
	{
	    SC.logWarn("TimeView:: update: UPDATE_CONTENT");
		if (all)
		{
			updateTimeline();
		}
		else
		{
			timeline.updateTasks();
			timeline2.updateTasks();
		}
        getServerAnalyseData();
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

	private void prepareAdvicePane(List<Advice> advices)
	{
	    pane.removeMembers(pane.getMembers());

        AdviceState panelState = AdviceState.OK;

		for (Advice a: advices) {
			Label l = new Label(a.getDescription());
			l.setWidth("100%");
			l.setMargin(10);
			l.setPadding(5);

			AdviceState adviceState = a.getState();
			l.setStyleName(adviceState.getStyleName());

			switch(adviceState)
			{
				case OK: if (panelState.ordinal() < AdviceState.OK.ordinal()) panelState = AdviceState.OK;	break;
				case WARNING: if (panelState.ordinal() < AdviceState.WARNING.ordinal()) panelState = AdviceState.WARNING; break;
				case CRITICAL: panelState = AdviceState.CRITICAL; break;
			}
			pane.addMember(l);
		}

		menu.setHeight(500);
		menu.setWidth(300);

        analyseResultLabel.setStyleName(panelState.getStyleName());
	}

	private void showAdvicePanel()
	{
		menu.showNextTo(analyseResultLabel,"top",false);
	}

	private Menu createPopupMenu()
	{
		pane = new VLayout();
		pane.setWidth(300);
		pane.setHeight(500);

		HLayout arrowDownLayout = new HLayout();
		arrowDownLayout.setAlign(Alignment.CENTER);
		arrowDownLayout.setWidth("100%");
		Img arrowDown = new Img("forms/down.png");
		arrowDown.setWidth(25);
		arrowDown.setHeight(25);
		arrowDown.setAlign(Alignment.CENTER);
		arrowDownLayout.addMember(arrowDown);
		arrowDownLayout.setHeight(25);
		arrowDownLayout.addClickHandler(event->{menu.hide();});

		final MenuItem closeItem = new MenuItem();
		closeItem.setEmbeddedComponent(arrowDownLayout);
		closeItem.addClickHandler(event -> {menu.hide();});

		final MenuItem commandItem = new MenuItem();
		commandItem.setEmbeddedComponent(pane);
		commandItem.setCanSelect(false);
		commandItem.setEnabled(false);
		menu.setItems(closeItem, commandItem);
		return menu;
	}

	public native void setBrowserIconToRunning(boolean setPlay, String title)
	/*-{
    	console.log("setPlay=" + setPlay);
		var link = window.top.document.querySelector("link[rel*='icon']") || window.top.document.createElement('link');
		link.type = 'image/x-icon';
		link.rel = 'shortcut icon';
		link.href = setPlay ? 'images/icon_play.png' : 'images/icon.png';
		console.log("href=" + link.href);
		window.top.document.getElementsByTagName('head')[0].appendChild(link);
		window.top.document.title = title;
	}-*/;
}
