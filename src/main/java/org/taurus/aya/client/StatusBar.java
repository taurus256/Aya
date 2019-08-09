package org.taurus.aya.client;

import com.google.gwt.user.client.Timer;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.MouseDownEvent;
import com.smartgwt.client.widgets.events.MouseDownHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.shared.Command;

public class StatusBar extends HLayout {
	
	StatusBar statusBar = this;
	
	final Label networkLabel;
	final Label commandListlabel;
	final Label statusLabel;
	
	private int notificationCounter = 0;
	private final ListGrid messageGrid;

	final Menu menu;
	
	Timer timerNetwork = new Timer(){

		@Override
		public void run() {
			statusBar.networkLabel.setContents("");
			this.cancel();
		}};

	Timer timerStatus = new Timer(){

		@Override
		public void run() {
			statusBar.statusLabel.setContents("");
			this.cancel();
		}};
		
	public StatusBar()
	{
		
		setWidth100();
        setHeight("0px");
		hide();

        setStyleName("s3_status");
        
        networkLabel = new Label("Соединения нет");
        networkLabel.setWidth(250);
        networkLabel.setHeight100();
        networkLabel.setIconSize(20);
        networkLabel.setIcon("disconnect.png");
        
        commandListlabel = new Label("");
        commandListlabel.setWidth(200);
        commandListlabel.setHeight100();
        commandListlabel.setIconSize(20);
        //commandListlabel.setIcon("forms/notification.png");
        
        menu = new Menu();
		messageGrid = new ListGrid();
        setMenuItems(menu);
        
        commandListlabel.addMouseDownHandler(new MouseDownHandler(){

			@Override
			public void onMouseDown(MouseDownEvent event) {
				menu.setHeight(550);
				menu.showNextTo(commandListlabel,"top",false);;
			}});
        commandListlabel.setContextMenu(menu);
        
        statusLabel = new Label();
        statusLabel.setWidth100();
        statusLabel.setHeight100();
        statusLabel.setAlign(Alignment.RIGHT);
        
        addMember(networkLabel);
        addMember(commandListlabel);
        addMember(statusLabel);
        GlobalData.setStatusBar(this);

	}
	
	public void indicateProcess(String text)
	{
		statusLabel.setContents(text);
		statusLabel.setIcon("rolling.gif");
		statusLabel.markForRedraw();
	}
	
	public void indicateMessage(String text)
	{
		statusLabel.setContents(text);
		statusLabel.setIcon(null);
		timerStatus.schedule(3000);
	}
	
	public void stopIndicateProcess()
	{
		statusLabel.setIcon(null);
		statusLabel.setContents("");
	}
	
	public void addNotification(final Command command)
	{
			setNotificationCount( ++notificationCounter );
			commandListlabel.setIcon("forms/notifications.png");
			messageGrid.addData(convertCommandToRecord(command));
	}
	
	private void setNotificationCount(int count)
	{
		if (count>0)
		{
			commandListlabel.setContents("+" + String.valueOf(count));
			commandListlabel.setIcon("forms/notifications.png");
		}
		else
		{
			commandListlabel.setContents("");
			commandListlabel.setIcon("blank");
		}
	}
	
	public void indicateConnect(String text)
	{
		networkLabel.setIcon("connect.png");
		networkLabel.setContents(text);
		networkLabel.markForRedraw();
	}
	
	public void indicateDisconnect(String text)
	{	
		networkLabel.setIcon("disconnect.png");
		networkLabel.setContents(text);
		networkLabel.markForRedraw();
	}
	
	private void setMenuItems(Menu m)
	{
		messageGrid.setEmptyMessage("");
		messageGrid.setShowHeader(false);
		messageGrid.setWidth(400);
		messageGrid.setHeight(500);
		messageGrid.setFields(new ListGridField("message","Уведомления о действиях других пользователей"));
		messageGrid.addRecordClickHandler(new RecordClickHandler(){

			@Override
			public void onRecordClick(RecordClickEvent event) {
				if (event.getRecord().getAttributeAsString("type") == null || event.getRecord().getAttributeAsString("id") == null)
					SC.say("StatusBar: Ошибка при открытии вкладки - не определен тип или идентификатор ресурса <br> type=" + event.getRecord().getAttributeAsString("type") + " id=" + event.getRecord().getAttributeAsString("id"));
				else
				{
					messageGrid.removeData(event.getRecord());
					menu.hide();
					setNotificationCount( --notificationCounter );
					GlobalData.getStatusBar().indicateProcess("Открываю выбранный ресурс...");
					switch(TabManager.ResourceType.valueOf(event.getRecord().getAttributeAsString("resourceType")))
					{
						case TASK:
						{
							GlobalData.getDataSource_tasks().fetchData(new Criteria("id",event.getRecord().getAttributeAsString("id")),new DSCallback(){

								@Override
								public void execute(DSResponse dsResponse,
                                                    Object data, DSRequest dsRequest) {
									if (dsResponse.getData().length>0)
										TabManager.openTab(ResourceType.TASK, dsResponse.getData()[0]);
									else
										SC.warn("Задача отсутствует в БД");
								}});
							
						}
			
					default:
						TabManager.openTab(TabManager.ResourceType.valueOf(event.getRecord().getAttributeAsString("resourceType")), event.getRecord());
			
					}
				}
			}});
		
		final MenuItem commandTableItem = new MenuItem();
		commandTableItem.setEmbeddedComponent(messageGrid);
		
		final MenuItem clearAll = new MenuItem("Очистить список                                                                          ");
		clearAll.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				messageGrid.setData();
				notificationCounter = 0;
				setNotificationCount(notificationCounter);
			}});
		
		menu.setItems(commandTableItem,clearAll);
	}
	
	private Record convertCommandToRecord(Command c)
	{
		Record r = new Record();
		r.setAttribute("type", c.getType());
		r.setAttribute("resourceType", TabManager.ResourceType.valueOf(c.getResourceTypeString()));
		r.setAttribute("message", c.getMessage());
		r.setAttribute("id", c.getId());
		return r;
	}
}