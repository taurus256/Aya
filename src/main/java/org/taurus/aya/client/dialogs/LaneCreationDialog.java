package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.*;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.ResourceLifeCycleManager;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.GenericPropertiesDialog;

import java.util.LinkedList;
import java.util.function.Consumer;

public class LaneCreationDialog extends Dialog {
	
	private DataSource ds_lanes;
	private IButton buttonDeleteLane;
	private IButton buttonEditLane;
	
	private ListGrid lanes_list;
	private Boolean dataWasUpdated = false;
	private Boolean orderWasUpdated = false;

	private Record record;
	private boolean canWriteToThisResource;
	Consumer<Void> func;

	public LaneCreationDialog(DataSource ds, Consumer<Void> func)
	{
		this.func = func;
		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("450px", "180px");
		setTitle("Настройка потоков задач");
		setBodyColor("rgb(253, 253, 253)");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		
		ds_lanes = ds;
		constructInterface();
		
		addKeyPressHandler(event -> {
			if (event.getKeyName().equals("Escape"))
				hide();
		});
	}

	
	private void constructInterface()
	{
		this.addItem(createLanesBar());
		this.addItem(createBottomBar());
	}
	
	// Панель со списком потоков и кнопками редактирования
	private HLayout createLanesBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMembersMargin(10);
		//lanes_list = new ListGrid();
		lanes_list = new ListGrid();
		lanes_list.setSize("300", "300");
		lanes_list.setShowHeader(true);
		//lanes_list.setCanDragReposition(true);
		lanes_list.setDataSource(ds_lanes);
		lanes_list.setCanEdit(false);
		//lanes_list.setDragDataAction(DragDataAction.MOVE);
		lanes_list.setSelectionType(SelectionStyle.SINGLE);
		lanes_list.setCanSort(false);
		lanes_list.setCanReorderRecords(true);
		lanes_list.setShowAllRecords(true);
		lanes_list.setCanDragRecordsOut(false);
		lanes_list.setAutoSaveEdits(false);

		lanes_list.addRecordClickHandler(event -> {

			record = event.getRecord();

			canWriteToThisResource =  GlobalData.canWrite(record);
			if (canWriteToThisResource)
			{
				enableEditButtons();
			}
			else
			{
				disableEditButtons();
			}

		});

        lanes_list.addRecordDoubleClickHandler(event -> {
			editSelectedLane(event.getRecord());
			event.cancel();
        });
		
		lanes_list.addDropCompleteHandler( event -> {
			dataWasUpdated = true;
			orderWasUpdated = true;
		});

		DSRequest dsr = new DSRequest();
		SortSpecifier sortSpecifier = new SortSpecifier("laneOrder", SortDirection.ASCENDING);
		SortSpecifier[] sortSpecifiers = { sortSpecifier };
		dsr.setSortBy(sortSpecifiers);

		lanes_list.fetchData(GlobalData.createSearchCriteria(), (dsResponse, data, dsRequest) -> {}, dsr);
		

		hLayout.addMember(lanes_list);
		
		VLayout buttons = new VLayout();
		buttons.setMargin(5);
		buttons.setMembersMargin(10);
		
		IButton buttonAddLane = new IButton("Создать");
		buttonAddLane.addClickHandler(event ->
		{
			Record r = new Record();
			r.setAttribute("name", "Новый поток");
			r.setAttribute("isLane", true);
			r.setAttribute("author", GlobalData.getCurrentUser().getAttributeAsInt("id"));
			r.setAttribute("laneOrder", lanes_list.getRecords().length);
			r.setAttribute("visible", true);
			lanes_list.addData(r);
			dataWasUpdated = true;
			orderWasUpdated = true;
		});

		buttonEditLane = new IButton("Редактировать");
		buttonEditLane.addClickHandler(event -> editSelectedLane(lanes_list.getSelectedRecord()));
		buttonEditLane.disable();
		
		buttonDeleteLane = new IButton("Удалить");
		buttonDeleteLane.addClickHandler(event -> SC.ask("Вы уверены, что хотите удалить выделенный поток?\nЭто приведет к удалению входящих в него задач. Вы можете скрыть поток, если информация по задачам должна быть сохранена", new BooleanCallback(){
			@Override
			public void execute(Boolean value) {
				if (value)
				{
					lanes_list.removeSelectedData();
					lanes_list.deselectAllRecords();
					dataWasUpdated = true;
					orderWasUpdated = true;
				}
			}}));
		disableEditButtons();
		buttons.addMember(buttonAddLane);
		buttons.addMember(buttonEditLane);
		buttons.addMember(buttonDeleteLane);

		hLayout.addMember(buttons);
		
		return hLayout;
	}

	// Rewrite 'laneOrder' field for every record in the list
	private void setNewLaneOrder()
	{
		LinkedList<Long> indices = new LinkedList<>();

		for (int i=0; i< lanes_list.getRecords().length; i++)
		{
			Record r = lanes_list.getRecords()[i];
			indices.add(r.getAttributeAsLong("id"));
		}

		Record transport = new Record();
		transport.setAttribute("indices",indices);
		GlobalData.getDataSource_lanes().performCustomOperation("updateLaneOrder", transport, new DSCallback() {
					@Override
					public void execute(DSResponse dsResponse, Object o, DSRequest dsRequest) {
						func.accept(null);
					}
				});
	}
	

	private HLayout createBottomBar()
	{
		HLayout bottomButtons = new HLayout();

		IButton cancelButton = new IButton("Закрыть окно");
		cancelButton.addClickHandler(event -> {

		if (dataWasUpdated)
			{
				if (orderWasUpdated)
					setNewLaneOrder(); // обновить порядок потоков, записать в БД и разослать уведомления
				else
				{
					func.accept(null);
				}
			}
			hide();
		});
		
		bottomButtons.addMember(cancelButton);
		
		bottomButtons.setWidth100();
		bottomButtons.setHeight(40);
		bottomButtons.setAlign(Alignment.RIGHT);
		
		return bottomButtons;
	}

	private void editSelectedLane(Record record) {
		Consumer<Void> c = Void -> {
				SC.logWarn("Call resourceChanged");
				//ResourceLifeCycleManager.resourceChanged(ResourceType.LANE, record);
				func.accept(null);
		};
		new GenericPropertiesDialog(
				record,
				"lane.png",
				ResourceType.LANE,
				ds_lanes,
				"потока", c);
	}

	private void enableEditButtons() {
		buttonDeleteLane.enable();
		buttonEditLane.enable();
	}

	private void disableEditButtons() {
		buttonDeleteLane.disable();
		buttonEditLane.disable();
	}

}