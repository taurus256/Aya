package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
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
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.*;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import org.taurus.aya.client.CommandExecutor;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.shared.Command;
import org.taurus.aya.shared.Command.CommandType;

public class UserManagementDialog extends Window {

		
	private DataSource ds_user = null;
	private DataSource ds_relation_user_group = null;
	private DataSource ds_group = null;
	
	private DynamicForm df;
	private IButton buttonSave, buttonAddGroup, buttonDeleteGroup;
	
	private ListGrid users_list, groups_list;
	private SelectItem si;
	private int userId = -1;
	private Label groupsLabel;
	private boolean userGroupsWarChanged =false;
	
	public UserManagementDialog()
	{
		ds_user = DataSource.get("user");
		ds_relation_user_group = DataSource.get("relation_user_group");
		ds_group = DataSource.get("group");

		
		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("400px", "180px");
		setTitle("Управление пользователями");
		setBodyColor("rgb(253, 253, 253)");
//		setBodyStyle("s3_windowBody");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		
		VLayout mainLayout = new VLayout();
		mainLayout.setWidth("400px");
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
		vLayout.addMember(createDynamicForm());
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
	
	private HLayout createUsersBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMargin(5);
		users_list = new ListGrid();
		users_list.setSize("100%", "200");
		users_list.setDataSource(ds_user);

        ListGridField nameField = new ListGridField("firstname", "Имя");
        ListGridField surnameField = new ListGridField("surname", "Фамилия");
        ListGridField patronymicField = new ListGridField("patronymic", "Отчество");
        users_list.setFields(nameField, patronymicField, surnameField);
		
        users_list.addSelectionChangedHandler(new SelectionChangedHandler() {
			
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				if (event.getState())
				{
	                Record record = event.getRecord();
	                showUserGroups(record.getAttributeAsInt("id"));
	                df.editRecord(record);
	                df.enable();
	                buttonSave.enable();
				}
			}
		});
        
//		users_list.addRecordClickHandler(new RecordClickHandler() {
//            public void onRecordClick(RecordClickEvent event) {
//                Record record = event.getRecord();
//                showUserGroups(record.getAttributeAsInt("id"));
//                df.editRecord(record);
//                df.enable();
//                buttonSave.enable();
//            }
//        });
		
		users_list.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				users_list.deselectAllRecords();
				users_list.selectRecord(users_list.getRecords().length-1);
				df.editRecord(users_list.getRecords()[users_list.getRecords().length-1]);
			}
		});
		
		
		GlobalData.getDataSource_relation_user_group().invalidateCache();
		users_list.fetchData();
		
		hLayout.addMember(users_list);
		
		VLayout buttons = new VLayout();
		buttons.setMargin(5);
		buttons.setMembersMargin(5);
		
		IButton buttonAddLane = new IButton("Создать");
		buttonAddLane.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				Record newRecord = new Record();
				GlobalData.getDataSource_user().addData(newRecord);
				df.enable();
				buttonSave.enable();
				users_list.fetchData();
			}
		});

		
		IButton buttonDeleteLane = new IButton("Удалить");
		buttonDeleteLane.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				SC.ask("Удалить выбранного пользователя?", new BooleanCallback(){

					@Override
					public void execute(Boolean value) {
						if (value) 
						{
							users_list.removeSelectedData();
							users_list.deselectAllRecords();
							df.disable();
							buttonSave.disable();
						}
					}});
				
			}
		});
		buttons.addMember(buttonAddLane);
		buttons.addMember(buttonDeleteLane);
		hLayout.addMember(buttons);
		
		return hLayout;
	}

	private VLayout createDynamicForm()
	{
		VLayout vlayout = new VLayout();
		vlayout.setWidth100();
		vlayout.setMargin(5);
		vlayout.setMembersMargin(5);
		
		df = new DynamicForm();
		df.setDataSource(ds_user);
		df.setSize("100%", "200px");
		df.disable();
		
		buttonSave = new IButton("Сохранить");
		buttonSave.disable();
		buttonSave.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                df.saveData(new DSCallback(){

					@Override
					public void execute(DSResponse dsResponse, Object data,
                                        DSRequest dsRequest) {
						if (dsResponse.getData().length > 0)
							userId = dsResponse.getData()[0].getAttributeAsInt("id");
						else SC.logWarn("Cannot creat user!");
					}});
                if (!df.hasErrors()) {
                    df.clearValues();
                    df.disable();
                    buttonSave.disable();
                }
            }
        });
		
		vlayout.addMember(df);
		vlayout.addMember(buttonSave);
		
		vlayout.setAlign(Alignment.RIGHT);
		vlayout.setWidth100();

		return vlayout;
	}

	private void showUserGroups(int userId)
	{
		groupsLabel.setContents("Здесь вы можете редактировать список групп, в которые входит пользователь");
		si.enable();
		
		Criteria criteria = new Criteria("userid",String.valueOf(userId));
		groups_list.fetchData(criteria, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
				if (dsResponse.getData().length == 0)
					SC.warn("Для этого пользователя не задано ни одной группы. \n Задайте их, в противном случае доступ к данным для этого пользователя будет невозможен.\n ID пользователя:" + dsResponse.getData()[0].getAttributeAsString("userid"));
				
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
				Record newRecord = new Record();
				int groupId = si.getSelectedRecord().getAttributeAsInt("id");
				newRecord.setAttribute("userid", groups_list.getCriteria().getValues().get("userid"));
				newRecord.setAttribute("id", groupId);
				groups_list.addData(newRecord);
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
		groups_list.setSize("270", "200");
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
				// Execute command only locally: there are not necessary to advertise other users about changing local user groups
				if (userGroupsWarChanged) 
				{
					CommandExecutor.exec(new Command(CommandType.UPDATE_GROUP_LIST));
					CommandExecutor.exec(new Command(CommandType.UPDATE_LANES));
				}
				hide();
			}
		});
		
		bottomButtons.addMember(cancelButton);
		
		bottomButtons.setWidth100();
		bottomButtons.setAlign(Alignment.RIGHT);
		
		return bottomButtons;
	}
}
