package org.taurus.aya.client;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.*;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.events.DragStartEvent;
import com.smartgwt.client.widgets.events.DragStartHandler;
import com.smartgwt.client.widgets.grid.GroupSortNormalizer;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.dialogs.BacklogTaskDialog;
import org.taurus.aya.client.dialogs.EditEventDialog;
import org.taurus.aya.client.generic.GenericPanel;
import org.taurus.aya.client.widgets.FilterWidget;
import org.taurus.aya.shared.Command;

import java.util.Date;

public class TaskPanel extends VLayout implements SidePanel {
	
	private TabSet tabset;
	private Record selectedRecord;

	
	private class TaskGenericPanel extends GenericPanel{
		public TaskGenericPanel(DataSource ds, String iconFile, ResourceType resType, String messageNew, String objectName)
		{

			super(ds, iconFile, resType, messageNew, objectName);
			
			hrCreateResource.removeHandler();
		    
			menuCreateResource.addClickHandler(new ClickHandler(){

				@Override
				public void onClick(MenuItemClickEvent event) {
					
					Record r = new Record();
					r.setAttribute("name","Новая задача");
					r.setAttribute("parent",0);
					r.setAttribute("author",GlobalData.getCurrentUser().getAttribute("id"));
					r.setAttribute("rgroup",GlobalData.ACCESS_ALL);
					r.setAttribute("wgroup",GlobalData.ACCESS_ALL);
					
					EditEventDialog ee = new EditEventDialog(r);
				}});
		}
	}

	private class BacklogPanel extends GenericPanel{

		public BacklogPanel(DataSource ds, String iconFile, ResourceType resType, String messageNew, String objectName) {
			super(ds, iconFile, resType, messageNew, objectName);

			hrCreateResource.removeHandler();

			menuCreateResource.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(MenuItemClickEvent event) {
					CommandExecutor.exec(new Command(Command.CommandType.CREATE_BACKLOG_TASK));
				}
			});

			hrProperties.removeHandler();
			menuProperties.addClickHandler(new ClickHandler(){

				@Override
				public void onClick(MenuItemClickEvent event) {
					new BacklogTaskDialog(treeGrid.getSelectedRecord());
				}});

		}
	}
	
//	private final TaskGenericPanel panelMyTasks, panelOtherTasks, panelAllTasks;
	private final BacklogPanel panelBacklog;
	
	public TaskPanel()
	{
		
		tabset = new TabSet();
		tabset.setWidth100();
		tabset.setHeight100();
		

		// Backlog panel
		
		panelBacklog = new BacklogPanel(GlobalData.getDataSource_tasks(), "task.png", ResourceType.TASK, "Новая задача", "задачи");
		//найти все задачи бэклога, к которым у пользователя есть доступ
		AdvancedCriteria backlogCriteria = new AdvancedCriteria(OperatorId.AND, new Criterion[]{GlobalData.createSearchCriteria(),new AdvancedCriteria("showInBacklog", OperatorId.EQUALS,"true")});
//
//		panelBacklog.setBaseCriteria(backlogCriteria);

//		panelBacklog.getTreeGrid().setCriteria(backlogCriteria);
		panelBacklog.getTreeGrid().setInitialCriteria(backlogCriteria);
		panelBacklog.getTreeGrid().setCriteria(backlogCriteria);
		ListGridField imageField = new ListGridField("icon",32);
		imageField.setType(ListGridFieldType.IMAGE);
		imageField.setAlign(Alignment.CENTER);
		imageField.setImageWidth(24);
		imageField.setImageHeight(24);
		ListGridField nameField = new ListGridField("name", 220);
		nameField.setShowTitle(true);
		panelBacklog.getTreeGrid().setFields(imageField, nameField);
		panelBacklog.getTreeGrid().setGroupByField("priority");
		panelBacklog.getTreeGrid().setCanGroupBy(true);
		panelBacklog.getTreeGrid().setSortByGroupFirst(true);
		panelBacklog.getTreeGrid().setGroupSortNormalizer(new GroupSortNormalizer(){
															  @Override
															  public Object normalize(ListGridRecord record, String fieldName, ListGrid context) {
																  switch (record.getAttribute("priority"))
																  {
																	  case GlobalData.LOW_PRIORITY: return 2;
																	  case GlobalData.NORMAL_PRIORITY: return 1;
																	  case GlobalData.HIGH_PRIORITY: return 0;
																  }
																  return -1;
															  }
														  });
		panelBacklog.getTreeGrid().groupBy("priority");

		panelBacklog.getTreeGrid().setMinWidth(250);
		panelBacklog.getTreeGrid().setGroupStartOpen(GroupStartOpen.ALL);
		panelBacklog.getTreeGrid().setAutoFetchData(true);
		panelBacklog.getTreeGrid().addDoubleClickHandler(new DoubleClickHandler() {
			
			@Override
			public void onDoubleClick(DoubleClickEvent event) {

				Record taskRecord = panelBacklog.getTreeSelectedRecord();

				if (taskRecord != null)
				{
					Record r = createEventRecord(taskRecord);;
					GlobalData.getDataSource_events().addData(r);
					taskRecord.setAttribute("showInBacklog",false);
					taskRecord.setAttribute("executor",GlobalData.getCurrentUser().getAttribute("id"));
					GlobalData.getDataSource_tasks().updateData(taskRecord, new DSCallback() {
						@Override
						public void execute(DSResponse dsResponse, Object o, DSRequest dsRequest) {
							panelBacklog.getTreeGrid().refreshData();
						}
					});
				}
			}
		});

		panelBacklog.addDragStartHandler(new DragStartHandler() {
											 @Override
											 public void onDragStart(DragStartEvent event) {
												 if (panelBacklog.getTreeSelectedRecord() != null) {
													 createEventRecord(panelBacklog.getTreeSelectedRecord() );
												 }
											 }
										 });

				panelBacklog.deleteCreateFolderItem();
		
		
//		panelMyTasks = new TaskGenericPanel(GlobalData.getDataSource_tasks(), "task.png", ResourceType.TASK, "Новая задача", "задачи");
//		//найти все задачи, к которым у пользователя есть доступ и где он является исполнителем
//		//AdvancedCriteria myCriteria = new AdvancedCriteria(OperatorId.AND, new Criterion[]{GlobalData.createSearchCriteria(),new AdvancedCriteria("executor", OperatorId.EQUALS,GlobalData.getCurrentUser().getAttributeAsInt("id"))});
//
//		//panelMyTasks.setBaseCriteria(myCriteria);
//
//		// загружаем при старте решенные задачи за последнюю неделю
//		//AdvancedCriteria dateCriteria = new AdvancedCriteria("startDate", OperatorId.GREATER_THAN, new Date((new Date()).getTime() - 1000*3600*24*7L));
//		//AdvancedCriteria activeTasksCriteria = new AdvancedCriteria(OperatorId.OR, new Criterion[]{new AdvancedCriteria("state", OperatorId.NOT_EQUAL,3), new AdvancedCriteria(OperatorId.AND, new Criterion[]{new AdvancedCriteria("state", OperatorId.EQUALS,3), dateCriteria})});
//		//panelMyTasks.getTreeGrid().setCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{myCriteria, activeTasksCriteria}));
//		panelMyTasks.getTreeGrid().setMinWidth(228);
//
//		panelMyTasks.getTreeGrid().addDoubleClickHandler(new DoubleClickHandler() {
//
//			@Override
//			public void onDoubleClick(DoubleClickEvent event) {
//				if (panelMyTasks.getTreeSelectedRecord() != null)
//				{
//					GlobalData.getStatusBar().indicateProcess("Открываю график задач...");
//					TabManager.openTab(ResourceType.TASK, panelMyTasks.getTreeSelectedRecord());
//				}
//			}
//		});
//		panelMyTasks.deleteCreateFolderItem();
		
		//найти все задачи, к которым у пользователя есть доступ и где он является постановщиком (автором)
//		panelOtherTasks = new TaskGenericPanel(GlobalData.getDataSource_tasks(), "task.png", ResourceType.TASK, "Новая задача", "задачи");
//		AdvancedCriteria otherCriteria = new AdvancedCriteria();
//		otherCriteria.addCriteria(GlobalData.createSearchCriteria());
//		otherCriteria.addCriteria("author",GlobalData.getCurrentUser().getAttributeAsInt("id"));
//		panelOtherTasks.setBaseCriteria(otherCriteria);
//
//		panelOtherTasks.getTreeGrid().setCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{otherCriteria, activeTasksCriteria}));
//
//		panelOtherTasks.getTreeGrid().addDoubleClickHandler(new DoubleClickHandler() {
//
//			@Override
//			public void onDoubleClick(DoubleClickEvent event) {
//				if (panelOtherTasks.getTreeSelectedRecord() != null)
//				{
//					GlobalData.getStatusBar().indicateProcess("Открываю график задач...");
//					TabManager.openTab(ResourceType.TASK, panelOtherTasks.getTreeSelectedRecord());
//				}
//			}
//		});
//		panelOtherTasks.deleteCreateFolderItem();
//
//		panelAllTasks = new TaskGenericPanel(GlobalData.getDataSource_tasks(), "task.png", ResourceType.TASK, "Новая задача", "задачи");
//		AdvancedCriteria allCriteria = new AdvancedCriteria();
//		allCriteria.addCriteria(GlobalData.createSearchCriteria());
//
//		panelAllTasks.setBaseCriteria(allCriteria);
//
//		panelAllTasks.getTreeGrid().setCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{allCriteria, activeTasksCriteria}));
//
//		panelAllTasks.getTreeGrid().addDoubleClickHandler(new DoubleClickHandler() {
//
//			@Override
//			public void onDoubleClick(DoubleClickEvent event) {
//				if (panelAllTasks.getTreeSelectedRecord() != null)
//				{
//					GlobalData.getStatusBar().indicateProcess("Открываю график задач...");
//					TabManager.openTab(ResourceType.TASK, panelAllTasks.getTreeSelectedRecord());
//				}
//			}
//		});
//		panelAllTasks.deleteCreateFolderItem();
//
		Tab backlogTab = new Tab("Ожидающие");
		backlogTab.setWidth(30);
		backlogTab.setPane(panelBacklog);
		backlogTab.setPaneMargin(2);

//		Tab myTab = new Tab("Мои");
//		myTab.setWidth(30);
//		myTab.setPane(addFilterWidgetTo(panelMyTasks));
//		myTab.setPaneMargin(2);
//
//		Tab otherTab = new Tab("Поставленные");
//		otherTab.setWidth(100);
//		otherTab.setPane(addFilterWidgetTo(panelOtherTasks));
//		otherTab.setPaneMargin(2);
		
//		Tab allTab = new Tab("Все");
//		allTab.setWidth(30);
//		allTab.setPane(addFilterWidgetTo(panelAllTasks));
//		allTab.setPaneMargin(2);
		
		tabset.addTab(backlogTab);
//		tabset.addTab(myTab);
//		tabset.addTab(otherTab);
//		tabset.addTab(allTab);

		addMember(tabset);
//	    treeGrid.setDataSource(GlobalData.getDataSource_tasks());
//	    
//	    
//		treeGrid.setShowOpenIcons(false);
//		treeGrid.setCanEdit(true);
		
		//!menu.removeItem(menuCreateFolder);
		
		
		//menu.removeItem(menuProperties);
		
//	    treeGrid.fetchData();
	    
	    
//	    hrCreateResource.removeHandler();
//	    menuCreateResource.addClickHandler(new ClickHandler(){
//
//			@Override
//			public void onClick(MenuItemClickEvent event) {
//				EditEventDialog ee = new EditEventDialog(GlobalData.getDataSource_tasks());
//				ee.show();
//			}});*/	    

//	    hrProperties.removeHandler();
//	    menuProperties.addClickHandler(new ClickHandler(){
//
//			@Override
//			public void onClick(MenuItemClickEvent event) {
				//EditEventDialog ee = new EditEventDialog(GlobalData.getDataSource_tasks());
				//ee.setUpdateEvent();
				//ee.show();
//			}});


	}

	private Record createEventRecord(Record record) {
		Record r = Record.copyAttributes(record,record.getAttributes());
		r.setAttribute("id","null");
		r.setAttribute("taskId",panelBacklog.getTreeSelectedRecord().getAttributeAsLong("id"));
		r.setAttribute("isGraph", true);
		r.setAttribute("executor", GlobalData.getCurrentUser().getAttributeAsInt("id"));
		r.setAttribute("executorName", GlobalData.getCurrentUser().getAttribute("firstname") + " " + GlobalData.getCurrentUser().getAttribute("surname"));
		r.setAttribute("startDate", new Date(new Date().getTime()));
		Long millis = r.getAttributeAsDate("startDate").getTime();

		if (r.getAttribute("duration_h") != null && (r.getAttributeAsInt("duration_h") >= 8))
			r.setAttribute("endDate", new Date(millis + r.getAttributeAsInt("duration_h") * 1000 * 3600 * 3 + 24*3600*1000));
		else
			r.setAttribute("endDate", new Date(millis + 1000 * 3600 * 24));

		return r;
	}

	private VLayout addFilterWidgetTo(GenericPanel panel)
	{
		VLayout vLayout = new VLayout();
	    vLayout.addMember(panel);
	    vLayout.addMember(new FilterWidget(panel).setDefaultValue("week"));
	    vLayout.setHeight100();
	    vLayout.setWidth100();
	    vLayout.setMargin(0);
	    vLayout.setPadding(0);
	    
	    return vLayout;
	}

	@Override
	public Record getTreeSelectedRecord() {
		
		switch(tabset.getSelectedTabNumber())
		{
		case 0: return panelBacklog.getTreeSelectedRecord(); 
//		case 1: return panelMyTasks.getTreeSelectedRecord();
//		case 2: return panelOtherTasks.getTreeSelectedRecord();
		default: return null;
		}
	}

	@Override
	public ResourceType getResourceType() {
		return ResourceType.TASK;
	}

	@Override
	public void update() {
			panelBacklog.update();
//			panelMyTasks.update();
//			panelOtherTasks.update();
	};
}
