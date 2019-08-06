package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.calendar.CalendarEvent;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.ContentPane;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.TabManager;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.TaskView;
import org.taurus.aya.client.generic.GenericPropertiesDialog;
import org.taurus.aya.client.widgets.ExtendedTimeline;

public class EditEventDialog extends BacklogTaskDialog {

//	private DialogCallback saveAction, updateAction;
//	private CalendarEvent calendarEvent = null;
	
	IButton selectButton;
	Label selectLabel = null;
	IButton dropSelectButton;
	
	final IButton parentButton = new IButton("");
	Label parentLabel = null;
	final IButton dropSelectParentButton = new IButton("");
	
	private final String NO_BLOCKING_TASK_MESSAGE = "Нет предшествующей задачи";
	private final String NO_PARENT_TASK_MESSAGE = "Нет родительской задачи";

	HLayout selectPreviousTaskPanel, selectParentTaskPanel;
	
	public EditEventDialog(Record r)
	{
		super(r);
		r.setAttribute("isGraph",true);
	}

	@Override
	protected void constructInterface()
	{
		this.addItem(createFormLayout());
		this.addItem(createSelectTasksPanel());
		//this.addItem(createSelectParentPanel());
		this.addItem(createTagsLayout());
		this.addItem(createSecurityLayout());

		this.addItem(createButtonsLayout());

		if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));
	}
	
	public void setUpdateEvent(ExtendedTimeline tv, CalendarEvent event)
	{
		SC.logWarn("SetUpdateEvent-1");
		record = event;
		df.editRecord(event);
//		SC.logWarn("SetUpdateEvent-2");
//		//search for name of previous task
//		if (record.getAttributeAsInt("prev") != null)
//		{
//			Criteria crit = new Criteria();
//			crit.addCriteria("id",record.getAttributeAsInt("prev"));
//			GlobalData.getDataSource_tasks().fetchData(crit, new DSCallback(){
//
//				@Override
//				public void execute(DSResponse dsResponse, Object data,
//						DSRequest dsRequest) {
//					if (dsResponse.getData().length>0)
//						setBlockingTaskInterface(dsResponse.getData()[0]);
//					else
//						selectLabel.setContents("Ошибка: не удалось найти в БД связанную задачу");
//				}
//				
//			});
//		}
//		else
//			setBlockingTaskInterface(null);
//		SC.logWarn("SetUpdateEvent-3");
//
//		//search for name of parent task
//		if (record.getAttributeAsInt("parent") != 0)
//		{
//			Criteria crit = new Criteria();
//			crit.addCriteria("id",record.getAttributeAsInt("parent"));
//			GlobalData.getDataSource_tasks().fetchData(crit, new DSCallback(){
//
//				@Override
//				public void execute(DSResponse dsResponse, Object data,
//						DSRequest dsRequest) {
//					if (dsResponse.getData().length>0)
//						setParentTaskInterface(dsResponse.getData()[0]);
//					else
//						parentLabel.setContents("Ошибка: не удалось найти в БД связанную задачу");
//				}
//				
//			});
//		}
//		else
//			setParentTaskInterface(null);
		SC.logWarn("SetUpdateEvent-4");
	}
	
	public void setNewEvent(ExtendedTimeline tv, Record event)
	{
		record = event;
		event.setAttribute("parent", 0);
		event.setAttribute("eventWindowStyle", "s3_event_new");
		df.editNewRecord(event);
	}

	public void setBlockingTask(Record event)
	{
		selectButton.enable();
		
		if (event.getAttributeAsInt("id") != null && event.getAttributeAsInt("id").equals(record.getAttributeAsInt("id"))) {
            SC.warn("Выберите другую задачу");return;}

		setBlockingTaskInterface(event);
		record.setAttribute("prev", event.getAttributeAsInt("id"));
	}

	public void setParentTask(Record event)
	{
		parentButton.enable();
		
		if (event.getAttributeAsInt("id") != null && event.getAttributeAsInt("id").equals(record.getAttributeAsInt("id"))) {
            SC.warn("Выберите другую задачу");return;}
		
		setParentTaskInterface(event);
		record.setAttribute("parent", event.getAttributeAsInt("id"));
	}
	
	
	// Set the interface elements for BLOCKING task. Null value in input event means that there are no blocking task
	private void setBlockingTaskInterface(Record r)
	{
		if (r != null)
		{
			selectLabel.setContents(r.getAttribute("name"));
			selectLabel.setIcon("forms/link_blue-24.png");
			selectButton.hide();
			dropSelectButton.show();
		}
		else
		{
			selectLabel.setContents(NO_BLOCKING_TASK_MESSAGE);
			selectLabel.setIcon("forms/link_gray-24.png");
			selectButton.show();
			dropSelectButton.hide();
		}		
	}
	
	// Set the interface elements for PARENT task. Null value in input event means that there are no parent task
	private void setParentTaskInterface(Record r)
	{
		if (r != null)
		{
			parentLabel.setContents(r.getAttribute("name"));
			parentLabel.setIcon("forms/note-up_blue.png");
			parentButton.hide();
			dropSelectParentButton.show();
		}
		else
		{
			parentLabel.setContents(NO_PARENT_TASK_MESSAGE);
			parentLabel.setIcon("forms/note-up_gray.png");
			parentButton.show();
			dropSelectParentButton.hide();
		}		
	}

//	private VLayout createButtonsLayout()
//	{
//		VLayout vlayout = new VLayout();
//		vlayout.setMembersMargin(10);
//		vlayout.setMargin(10);
//		vlayout.setWidth100();
//		vlayout.setHeight("32px");
//
//		/*vlayout.addMember(df);
//		df.setWidth100();
//		df.setWrapItemTitles(false);
//
//		vlayout.addMember(createSelectParentPanel());
//		vlayout.addMember(createSelectTasksPanel());
//		*/
//		HLayout hlayout = new HLayout();
//		final IButton submitButton = new IButton("Сохранить");
//		submitButton.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				// Setting 'endDate', if user didn't set it. This value is necessary for timeline drawing
//				//TODO: delete this
//				//!if (df.getField("endDate").getValue() == null) df.getField("endDate").setValue(df.getField("limitDate").getValue());
//
//				//if (df.getField("sublane").getValue() == null) df.getField("sublane").setValue(df.getField("lane").getValue());
//
//				if (df.getField("executor").getSelectedRecord() !=null)
//				{
//					SC.logWarn("EditEventDialog. Setting executor_name to " + df.getField("executor").getSelectedRecord().getAttribute("firstname") + " " + df.getField("executor").getSelectedRecord().getAttribute("lastname"));
//					record.setAttribute("executor_name", df.getField("executor").getSelectedRecord().getAttribute("firstname") + " " + df.getField("executor").getSelectedRecord().getAttribute("surname"));
//				}
//				record.setAttribute("isGraph",true);
//
////				if (df.getField("ros_task").getSelectedRecord() != null)
////				{
////					/*String host = GWT.getHostPageBaseURL();
////					if (host.contains("127.0.0.1"))		host = host.replace("127.0.0.1", "10.7.41.76");*/
////					//df.getField("description").setValue("<a href=\"" + df.getField("ros_task").getSelectedRecord().getAttribute("url") + "\" target=\"_blank\">" + df.getField("ros_task").getSelectedRecord().getAttribute("name") + "</a>");
////					df.getField("description").setValue("<a href=\"http://redmine.vingrid.ru" + df.getField("ros_task").getValue() + "\" target=\"_blank\">" + "задача " + df.getField("ros_task") + "</a>");
////				}
//
//				//This method called from parent class
//				saveDialogData();
//			}
//		});
//
//		final IButton cancelButton = new IButton("Отменить");
//		cancelButton.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				//Возвращаем taskView в исходное состояние
//				//!taskView.setBlockSelectMode(false);
//				hide();
//			}
//		});
//
//		if (canWriteToThisResource) hlayout.addMember(submitButton);
//
//		hlayout.addMember(cancelButton);
//		hlayout.setWidth("100%");
//		hlayout.setMembersMargin(10);
//		hlayout.setAlign(Alignment.RIGHT);
//		vlayout.addMember(hlayout);
//		vlayout.setWidth("100%");
//
//		return vlayout;
//	}
	
	private HLayout createSelectTasksPanel()
	{
		HLayout hLayout = new HLayout();
		hLayout.setAlign(Alignment.RIGHT);
		hLayout.setWidth100();
		hLayout.setHeight("32px");
		hLayout.setMargin(10);
		SC.logWarn("createSelectTasksPanel-1");

		selectButton = new IButton("");
		selectLabel = new Label();
		dropSelectButton = new IButton("");
		selectButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				//переводим taskView в состояние выбора задач
				ContentPane selectedPane = TabManager.getSelectedPane();
				if (selectedPane == null || selectedPane.getResourceType() != ResourceType.TASK)
				{
					SC.warn("Предупреждение","Для выбора предыдущей задачи откройте вкладку графика задач");
				}
				else
				{
					((TaskView) selectedPane).getCurrentTimeline().setBlockSelectMode(1);
					selectButton.disable();
				}
			}});
		SC.logWarn("createSelectTasksPanel-2");
		selectButton.setVisible(true);
		selectButton.setIcon("forms/arrow-24.png");
		selectButton.setSize("25px","25px");
		SC.logWarn("createSelectTasksPanel-3");
		dropSelectButton.setIcon("forms/cross-24.png");
		dropSelectButton.setVisible(false);
		dropSelectButton.setSize("25px","25px");
		dropSelectButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				record.setAttribute("prev", (Object)null);
				selectLabel.setIcon("forms/link_gray-24.png");
				selectLabel.setContents(NO_BLOCKING_TASK_MESSAGE);
				selectButton.show();
				dropSelectButton.hide();
			}
		});
		SC.logWarn("createSelectTasksPanel-4");
		selectLabel = new Label(NO_BLOCKING_TASK_MESSAGE);
		selectLabel.setIcon("forms/link_gray-24.png");
		selectLabel.setIconSize(32);
		selectLabel.setWidth100();
		selectLabel.setHeight("16px");
		selectLabel.setAlign(Alignment.LEFT);
		selectLabel.disable();
        
		hLayout.addMember(selectLabel);
        hLayout.addMember(selectButton);
        hLayout.addMember(dropSelectButton);
        hLayout.setMembersMargin(5);
		return hLayout;
	}
	
	private HLayout createSelectParentPanel()
	{
		HLayout hLayout = new HLayout();
		hLayout.setAlign(Alignment.RIGHT);
		hLayout.setWidth100();
		hLayout.setHeight("32px");
		hLayout.setMargin(10);
		
		parentButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				//переводим taskView в состояние выбора задач
				ContentPane selectedPane = TabManager.getSelectedPane();
				if (selectedPane == null || selectedPane.getResourceType() != ResourceType.TASK)
				{
					SC.warn("Предупреждение","Для выбора родительской задачи откройте вкладку графика задач");
				}
				else
				{
					((TaskView)selectedPane).getCurrentTimeline().setBlockSelectMode(2);
					parentButton.disable();
				}
			}});
		parentButton.setVisible(true);
		parentButton.setIcon("forms/arrow-24.png");
		parentButton.setSize("25px","25px");
		
		dropSelectParentButton.setIcon("forms/cross-24.png");
		dropSelectParentButton.setVisible(false);
		dropSelectParentButton.setSize("25px","25px");
		dropSelectParentButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				record.setAttribute("parent", -1);
				parentLabel.setIcon("forms/note-up_gray.png");
				parentLabel.setContents(NO_PARENT_TASK_MESSAGE);
				parentButton.show();
				dropSelectParentButton.hide();
			}
		});
		
		parentLabel = new Label(NO_PARENT_TASK_MESSAGE);
		parentLabel.setIcon("forms/note-up_gray.png");
		parentLabel.setIconSize(32);
		parentLabel.setWidth100();
		parentLabel.setHeight("25px");
		parentLabel.setAlign(Alignment.LEFT);
		parentLabel.disable();
        
		hLayout.addMember(parentLabel);
        hLayout.addMember(parentButton);
        hLayout.addMember(dropSelectParentButton);
        hLayout.setMembersMargin(5);
		return hLayout;
	}

	@Override
	protected void saveDialogData() {
		if (df.getField("executor").getValue() == null)
			SC.warn("Исполнитель должен быть задан");
		else
			if (df.getField("lane").getValue() == null || df.getField("lane").getValue().equals("null"))
				SC.warn("Необходимо задать поток для задачи");
			else
				super.saveDialogData();
	}
}