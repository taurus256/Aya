package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.*;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.TabManager;
import org.taurus.aya.client.generic.AbstractPropertiesDialog;

public class UserManagementDialog extends Window {

		
	private DataSource ds_user = null;
	private DataSource ds_relation_user_group = null;
	private DataSource ds_group = null;
	
	private DynamicForm df;
	private IButton buttonEditUser, buttonDeleteGroup, buttonAddGroup;
	
	private ListGrid groups_list, users_list;
	private SelectItem si;
	private int userId = -1;
	private Label groupsLabel;
	private boolean userGroupsWarChanged =false;
	
	public UserManagementDialog()
	{
		ds_user = GlobalData.getDataSource_user();
		ds_relation_user_group = GlobalData.getDataSource_relation_user_group();
		ds_group = GlobalData.getDataSource_group();

		
		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("500px", "180px");
		setTitle("Управление пользователями");
		setBodyColor("rgb(253, 253, 253)");
		setBodyStyle("s3_windowBody");
		setHoverMoveWithMouse(true);
		setAutoSize(true);
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		
		VLayout mainLayout = new VLayout();
		mainLayout.setWidth("500px");
		mainLayout.setMembersMargin(5);
		mainLayout.addMember(createTabSet());
		mainLayout.addMember(createBottomBar());
		addItem(mainLayout);
	}

	private TabSet createTabSet()
	{
		TabSet tabset = new TabSet();
		tabset.setWidth100();
		tabset.setHeight("500px");
		
		Tab tabUsers = new Tab("Данные");
		tabUsers.setPaneMargin(1);
		VLayout vLayout =new VLayout();
		vLayout.setBackgroundColor("rgb(236, 236, 236)");
		vLayout.setWidth100();
		vLayout.setHeight100();
		vLayout.addMember(createUsersBar());
		tabUsers.setPane(vLayout);
		
		Tab tabGroups = new Tab("Настройка групп");
		VLayout vLayout2 =new VLayout();
		
		vLayout2.addMember(createGroupLabel());
		vLayout2.addMember(createGroupAddBar());
		vLayout2.addMember(createGroupsBar());
		vLayout2.setWidth100();
		vLayout2.setHeight100();
		tabGroups.setPane(vLayout2);
		
		tabset.addTab(tabUsers);
		tabset.addTab(tabGroups);
		
		return tabset;
	}
	
	private void showUserGroups(int userId)
	{
		groupsLabel.setContents("Здесь вы можете редактировать список групп, в которые входит пользователь");
		si.enable();
		
		Criteria criteria = new Criteria("userid",String.valueOf(userId));
		groups_list.fetchData(criteria, new DSCallback(){
			@Override
			public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
			}});
	}

	private Label createGroupLabel()
	{
		groupsLabel = new Label("Пользователь не выбран");
		groupsLabel.setPadding(5);;
		groupsLabel.setHeight("50px");
		return groupsLabel;
	}
	
	private HLayout createGroupAddBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMargin(5);
		hLayout.setMembersMargin(5);
		hLayout.setHeight("50px");
		
		buttonAddGroup = new IButton("Добавить");
		buttonAddGroup.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				groups_list.addData(si.getSelectedRecord());
				groups_list.saveEdits();
			}
		});
		buttonAddGroup.disable();
		DynamicForm wrapper = new DynamicForm();
		
		si = new SelectItem();
		si.setPickListWidth(200);
		si.setTitle("Группы");
		si.setName("name");
		si.setOptionDataSource(ds_group);
		si.setAutoFetchData(true);
		si.addChangedHandler(new ChangedHandler(){
			@Override
			public void onChanged(ChangedEvent event) {
				buttonAddGroup.enable();
			}});
		si.setWidth(140);
		si.setAlign(Alignment.RIGHT);
		si.disable();
		wrapper.setItems(si);
		wrapper.setSize("270px", "50px");
		
		hLayout.addMember(wrapper);
		hLayout.addMember(buttonAddGroup);
		
		hLayout.setWidth100();
		
		return hLayout;
	}
	
	private HLayout createGroupsBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMargin(5);
		hLayout.setMembersMargin(5);
		hLayout.setHeight100();
		groups_list = new ListGrid();
		groups_list.setSize("350", "200");
		groups_list.setDataSource(ds_relation_user_group);

        /*ListGridField nameField = new ListGridField("firstname", "Имя");  
        ListGridField surnameField = new ListGridField("surname", "Фамилия");  
        ListGridField patronymicField = new ListGridField("patronymic", "Отчество");  
        groups_list.setFields(nameField, patronymicField, surnameField);*/
		
        groups_list.addRecordClickHandler(new RecordClickHandler() {
            public void onRecordClick(RecordClickEvent event) {
            	buttonDeleteGroup.enable();
            }
        });
		
        groups_list.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				groups_list.deselectAllRecords();
				groups_list.selectRecord(groups_list.getRecords().length-1);
				userGroupsWarChanged = true;
			}
		});
        groups_list.addEditCompleteHandler(new EditCompleteHandler(){

			@Override
			public void onEditComplete(EditCompleteEvent event) {
				groups_list.fetchData();
			}});
        hLayout.addMember(groups_list);
		
		VLayout buttons = new VLayout();
		buttons.setMargin(5);
		buttons.setMembersMargin(5);
		
		buttonDeleteGroup = new IButton("Удалить");
		buttonDeleteGroup.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (groups_list.getRecordList().getLength()==1)
					SC.warn("Список групп пользователя не может быть пустым.<br>Если вы хотите включить пользователя в другие группы, сначала добавьте их, а потом удалите данную");
				else
				SC.ask("Удалить группу из списка?", new BooleanCallback(){

					@Override
					public void execute(Boolean value) {
						if (value) 
						{
							groups_list.removeSelectedData();
							groups_list.deselectAllRecords();
						}
					}});
			}
		});
		buttonDeleteGroup.disable();
		hLayout.addMember(buttonDeleteGroup);
		return hLayout;
	}
	
	private HLayout createBottomBar()
	{
		HLayout bottomButtons = new HLayout();

		bottomButtons.setMargin(5);
		bottomButtons.setMembersMargin(10);

		IButton cancelButton = new IButton("Закрыть окно");
		cancelButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		bottomButtons.addMember(cancelButton);
		
		bottomButtons.setWidth100();
		bottomButtons.setAlign(Alignment.RIGHT);
		
		return bottomButtons;
	}

	// Панель со списком пользователей и кнопками редактирования
	private HLayout createUsersBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMembersMargin(10);
		//lanes_list = new ListGrid();
		users_list = new ListGrid();
		users_list.setSize("350", "400");
		users_list.setShowHeader(true);
		//lanes_list.setCanDragReposition(true);
		users_list.setDataSource(ds_user);
		users_list.setCanEdit(false);
		//lanes_list.setDragDataAction(DragDataAction.MOVE);
		users_list.setSelectionType(SelectionStyle.SINGLE);
		users_list.setCanSort(false);
		users_list.setCanReorderRecords(true);
		users_list.setShowAllRecords(true);
		users_list.setCanDragRecordsOut(false);
		users_list.setAutoSaveEdits(false);

		users_list.addSelectionChangedHandler((selectionEvent) -> {
				if (selectionEvent.getState())
				{
					Record record = selectionEvent.getRecord();
					showUserGroups(record.getAttributeAsInt("id"));
					enableEditButtons();
				}
			}
		);

		users_list.addRecordDoubleClickHandler(event -> {
			editSelectedUser(event.getRecord());
			event.cancel();
		});

		DSRequest dsr = new DSRequest();
		users_list.fetchData(GlobalData.createSearchCriteria(), (dsResponse, data, dsRequest) -> {}, dsr);

		hLayout.addMember(users_list);

		VLayout buttons = new VLayout();
		buttons.setMargin(5);
		buttons.setMembersMargin(10);

		IButton buttonAddUser = new IButton("Создать");
		buttonAddUser.addClickHandler(event ->
		{
			Record r = new Record();
			r.setAttribute("name", "Новый пользователь");
			r.setAttribute("description", "");
			users_list.addData(r);
		});

		buttonEditUser = new IButton("Редактировать");
		buttonEditUser.addClickHandler(event -> editSelectedUser(users_list.getSelectedRecord()));
		buttonEditUser.disable();

		buttonDeleteGroup = new IButton("Удалить");
		buttonDeleteGroup.addClickHandler(event -> SC.ask("Удалить пользователя " + users_list.getSelectedRecord().getAttribute("showedName") + " ?", new BooleanCallback(){
			@Override
			public void execute(Boolean value) {
				if (value)
				{
					users_list.removeSelectedData();
					users_list.deselectAllRecords();
				}
			}}));
		disableEditButtons();
		buttons.addMember(buttonAddUser);
		buttons.addMember(buttonEditUser);
		buttons.addMember(buttonDeleteGroup);

		hLayout.addMember(buttons);

		return hLayout;
	}

	private void editSelectedUser(Record record) {
		if (record == null) { SC.logWarn("Record is NULL"); return;}
		(new AbstractPropertiesDialog(
				record,
				"lane.png",
				TabManager.ResourceType.LANE,
				ds_user,
				"пользователя") {
			@Override
			protected void constructInterface() {
				this.addItem(createFormLayout());
				this.addItem(createButtonsLayout());
			}
		}).show();
	}{}

	private void enableEditButtons() {
		buttonDeleteGroup.enable();
		buttonEditUser.enable();
	}

	private void disableEditButtons() {
		buttonDeleteGroup.disable();
		buttonEditUser.disable();
	}
}
