package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class GroupManagementDialog extends Window {

	private DataSource ds_group = null;
	
	private DynamicForm df;
	private IButton buttonSave;
	private ListGrid groups_list;
	
	public GroupManagementDialog() {
		ds_group = DataSource.get("group");
		
		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("380px", "180px");
		setTitle("Управление группами");
//		setBodyColor("rgb(236, 236, 236)");
//		setBodyStyle("s3_windowBody");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		
		VLayout mainLayout = new VLayout();
		mainLayout.setWidth("410px");
		mainLayout.setMembersMargin(5);
		mainLayout.addMember(createUsersBar());
		mainLayout.addMember(createDynamicForm());
		mainLayout.addMember(createBottomBar());
		addItem(mainLayout);

	}

	private HLayout createUsersBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMargin(5);
		groups_list = new ListGrid();
		groups_list.setSize("270", "200");
		groups_list.setDataSource(ds_group);

        /*ListGridField nameField = new ListGridField("firstname", "Имя");  
        ListGridField surnameField = new ListGridField("surname", "Фамилия");  
        ListGridField patronymicField = new ListGridField("patronymic", "Отчество");  
        groups_list.setFields(nameField, patronymicField, surnameField);*/
		
		groups_list.addRecordClickHandler(new RecordClickHandler() {
            public void onRecordClick(RecordClickEvent event) {
                Record record = event.getRecord();
                df.editRecord(record);
                df.enable();
                buttonSave.enable();
            }
        });
		
		groups_list.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				groups_list.deselectAllRecords();
				groups_list.selectRecord(groups_list.getRecords().length-1);
				df.editRecord(groups_list.getRecords()[groups_list.getRecords().length-1]);
			}
		});
		
		groups_list.fetchData();
		
		hLayout.addMember(groups_list);
		
		VLayout buttons = new VLayout();
		buttons.setMargin(5);
		buttons.setMembersMargin(10);
		
		IButton buttonAddLane = new IButton("Создать");
		buttonAddLane.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				Record newRecord = new Record();
				groups_list.addData(newRecord);
				df.enable();
				buttonSave.enable();
			}
		});

		
		IButton buttonDeleteLane = new IButton("Удалить");
		buttonDeleteLane.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				SC.ask("Удалить выбранную группу?", new BooleanCallback(){

					@Override
					public void execute(Boolean value) {
						if (value) 
						{
							groups_list.removeSelectedData();
							groups_list.deselectAllRecords();
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
		vlayout.setMembersMargin(10);
		
		df = new DynamicForm();
		df.setDataSource(ds_group);
		df.setSize("100%", "200px");
		df.disable();
		
		buttonSave = new IButton("Сохранить");
		buttonSave.disable();
		buttonSave.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                df.saveData();
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
				markForDestroy();
			}
		});
		
		bottomButtons.addMember(cancelButton);
		
		bottomButtons.setWidth100();
		bottomButtons.setAlign(Alignment.RIGHT);
		
		return bottomButtons;
	}
}
