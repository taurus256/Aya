package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.AbstractPropertiesDialog;
import org.taurus.aya.client.generic.GenericPropertiesDialog;

import java.util.LinkedList;

public class GroupManagementDialog extends Dialog {

	private DataSource ds_groups;
	private IButton buttonDeleteGroup;
	private IButton buttonEditGroup;

	private ListGrid groups_list;

	private Record record;
	private boolean canWriteToThisResource;

	public GroupManagementDialog()
	{
		setCanDragReposition(true);
		setCanDragResize(false);
		setSize("450px", "180px");
		setTitle("Настройка групп");
		setBodyColor("rgb(253, 253, 253)");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		
		ds_groups = GlobalData.getDataSource_group();
		constructInterface();
		
		addKeyPressHandler(event -> {
			if (event.getKeyName().equals("Escape"))
				hide();
		});
	}

	
	private void constructInterface()
	{
		this.addItem(createGroupsBar());
		this.addItem(createBottomBar());
	}
	
	// Панель со списком потоков и кнопками редактирования
	private HLayout createGroupsBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMembersMargin(10);
		//lanes_list = new ListGrid();
		groups_list = new ListGrid();
		groups_list.setSize("300", "300");
		groups_list.setShowHeader(true);
		//lanes_list.setCanDragReposition(true);
		groups_list.setDataSource(ds_groups);
		groups_list.setCanEdit(false);
		//lanes_list.setDragDataAction(DragDataAction.MOVE);
		groups_list.setSelectionType(SelectionStyle.SINGLE);
		groups_list.setCanSort(false);
		groups_list.setCanReorderRecords(true);
		groups_list.setShowAllRecords(true);
		groups_list.setCanDragRecordsOut(false);
		groups_list.setAutoSaveEdits(false);

		groups_list.addRecordClickHandler(event -> enableEditButtons());

        groups_list.addRecordDoubleClickHandler(event -> {
			editSelectedGroup(event.getRecord());
			event.cancel();
        });
		
		DSRequest dsr = new DSRequest();
		groups_list.fetchData(GlobalData.createSearchCriteria(), (dsResponse, data, dsRequest) -> {}, dsr);

		hLayout.addMember(groups_list);
		
		VLayout buttons = new VLayout();
		buttons.setMargin(5);
		buttons.setMembersMargin(10);
		
		IButton buttonAddGroup = new IButton("Создать");
		buttonAddGroup.addClickHandler(event ->
		{
			Record r = new Record();
			r.setAttribute("name", "Новая группа");
			r.setAttribute("description", "");
			groups_list.addData(r);
		});

		buttonEditGroup = new IButton("Редактировать");
		buttonEditGroup.addClickHandler(event -> editSelectedGroup(groups_list.getSelectedRecord()));
		buttonEditGroup.disable();
		
		buttonDeleteGroup = new IButton("Удалить");
		buttonDeleteGroup.addClickHandler(event -> SC.ask("Удалить группу?", new BooleanCallback(){
			@Override
			public void execute(Boolean value) {
				if (value)
				{
					groups_list.removeSelectedData();
					groups_list.deselectAllRecords();
				}
			}}));
		disableEditButtons();
		buttons.addMember(buttonAddGroup);
		buttons.addMember(buttonEditGroup);
		buttons.addMember(buttonDeleteGroup);

		hLayout.addMember(buttons);
		
		return hLayout;
	}

	private HLayout createBottomBar()
	{
		HLayout bottomButtons = new HLayout();

		IButton cancelButton = new IButton("Закрыть окно");
		cancelButton.addClickHandler(event -> {
			hide();
		});
		
		bottomButtons.addMember(cancelButton);
		
		bottomButtons.setWidth100();
		bottomButtons.setHeight(40);
		bottomButtons.setAlign(Alignment.RIGHT);
		
		return bottomButtons;
	}

	private void editSelectedGroup(Record record) {
		(new AbstractPropertiesDialog(
				record,
				"lane.png",
				ResourceType.LANE,
				ds_groups,
				"группы") {
			@Override
			protected void constructInterface() {
				this.addItem(createFormLayout());
				this.addItem(createButtonsLayout());
			}
		}).show();
	}{}

	private void enableEditButtons() {
		buttonDeleteGroup.enable();
		buttonEditGroup.enable();
	}

	private void disableEditButtons() {
		buttonDeleteGroup.disable();
		buttonEditGroup.disable();
	}
}