package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.*;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.events.DataArrivedEvent;
import com.smartgwt.client.widgets.tree.events.DataArrivedHandler;
import com.smartgwt.client.widgets.tree.events.FolderDropEvent;
import com.smartgwt.client.widgets.tree.events.FolderDropHandler;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.ResourceLifeCycleManager;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.TaskView;

import java.util.LinkedHashMap;

public class LaneCreationDialog extends Dialog {
	
	DataSource ds_lanes;
	DynamicForm df;
	
	IButton buttonSave;
	IButton buttonSaveOrder;
	IButton buttonDeleteLane;
	
	public TreeGrid lanes_list;
	Boolean dataWasUpdated = false;
	Boolean orderWasUpdated = false;
	private int treeElementsCount = 0;
	
	private DSRequest dsr;
	
	private Record record;
	private RadioGroupItem radioGroupRead, radioGroupWrite;
	private ComboBoxItem groupReadSelector, groupWriteSelector;
	protected Label labelPermissions;
	private String canRead = "все", canWrite = "все";
	private boolean canWriteToThisResource;
    private LinkedHashMap<Integer,String> groupMap = new LinkedHashMap<Integer,String>();
    private final ImgButton button = new ImgButton(){{
		setAlign(Alignment.CENTER);
		setSize("25px", "25px");
		setSrc("forms/down.png");
		setShowRollOver(false);
		setShowDown(false);
	}};;
	
	public LaneCreationDialog(DataSource ds, TaskView taskView)
	{
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
		
		addKeyPressHandler(new KeyPressHandler(){

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Escape"))
					hide();
			}});
	}

	
	private void constructInterface()
	{
		this.addItem(createLanesBar());
		this.addItem(createDynamicForm());
		this.addItem(createBottomBar());
	}
	
	// Панель со списком потоков и кнопками редактирования
	private HLayout createLanesBar()
	{
		HLayout hLayout = new HLayout();
		hLayout.setMembersMargin(10);
		//lanes_list = new ListGrid();
		lanes_list = new TreeGrid();
		lanes_list.setSize("300", "300");
		lanes_list.setShowHeader(false);
		//lanes_list.setCanDragReposition(true);
		lanes_list.setDataSource(ds_lanes);
		lanes_list.setCanEdit(true);
		lanes_list.setDragDataAction(DragDataAction.MOVE);
		lanes_list.setSelectionType(SelectionStyle.SINGLE);
		lanes_list.setCanReparentNodes(true);
		lanes_list.setCanReorderRecords(true);
		lanes_list.setFolderIcon("tree/moon.png");
		lanes_list.setNodeIcon("tree/moon.png");
		lanes_list.setOpenIconSuffix("");
		lanes_list.setClosedIconSuffix("");
		lanes_list.setAutoSaveEdits(false);
		
//		lanes_list.setFolderIcon("tree/moon.png");
//		lanes_list.setNodeIcon("tree/moon.png");
//		lanes_list.setIconSize(20);
//		lanes_list.setSelectionType(SelectionStyle.SINGLE);
//		lanes_list.setShowHeader(false);
//		lanes_list.setCanEdit(true);
//		lanes_list.setCanReparentNodes(true);
//		lanes_list.setCanRemoveRecords(false);
//		lanes_list.setCanAcceptDroppedRecords(false);
//		lanes_list.setDragDataAction(DragDataAction.MOVE); 
//		lanes_list.setCanDragRecordsOut(true);//!
		

		//lanes_list.setAutoSaveEdits(false);
//		lanes_list.setAutoFetchData(true);
		
		lanes_list.addRecordClickHandler(new RecordClickHandler() {
            public void onRecordClick(RecordClickEvent event) {
                
            	record = event.getRecord();
                canWriteToThisResource =  GlobalData.canWrite(record);
                
                updateSecurityControls();

                if (canWriteToThisResource)
                {
	                buttonDeleteLane.enable();
                	df.editRecord(record);
	                buttonSave.enable();
                }
                else
                {
  	                buttonDeleteLane.disable();
                	df.clearValues();
 	                buttonSave.disable();
                }
            }
        });
		
		lanes_list.addDataArrivedHandler(new DataArrivedHandler(){
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				//treeElementsCount	
				if (lanes_list.getData().getLength() > treeElementsCount) 
				{
					lanes_list.getData().openAll();
					treeElementsCount = lanes_list.getData().getLength();
//					lanes_list.deselectAllRecords();
//					lanes_list.selectRecord(treeElementsCount-1);
					SC.logWarn("Initialization. LaneCreationDialog, number of nodes:" + String.valueOf(treeElementsCount));
				}
			}});
		
		lanes_list.addFolderDropHandler(new FolderDropHandler(){

			@Override
			public void onFolderDrop(FolderDropEvent event) {
				dataWasUpdated = true;
				orderWasUpdated = true;
				SC.logWarn("LaneCreationDialog: " + event.getFolder());
				SC.logWarn("getAttributeAsInt('parent') " + getAttributeAsInt("parent"));
				if (event.getFolder() != null )
					if (event.getFolder().getAttributeAsInt("parent") != null && event.getFolder().getAttributeAsInt("parent") != 0)
					{
						SC.warn("Потоки","Можно использовать только два уровня вложенности");
						event.cancel();
					}
			}});
		
		DSRequest dsr = new DSRequest();
		SortSpecifier sortSpecifier = new SortSpecifier("lane_order", SortDirection.ASCENDING);
		SortSpecifier[] sortSpecifiers = { sortSpecifier };
		dsr.setSortBy(sortSpecifiers);
		lanes_list.fetchData(GlobalData.createSearchCriteria(), new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest)
			{
				lanes_list.getData().openAll();
			}}, dsr);
		

		hLayout.addMember(lanes_list);
		
		VLayout buttons = new VLayout();
		buttons.setMargin(5);
		buttons.setMembersMargin(10);
		
		IButton buttonAddLane = new IButton("Создать поток");
		buttonAddLane.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				Record r = new Record();
				r.setAttribute("name", "Новый поток");
				r.setAttribute("parent", 0);
				r.setAttribute("isLane", true);
				r.setAttribute("author", GlobalData.getCurrentUser().getAttributeAsInt("id"));
				r.setAttribute("lane_order", lanes_list.getTree().getLength());
				r.setAttribute("visible", true);
				lanes_list.addData(r);
				dataWasUpdated = true;
				orderWasUpdated = true;
			}
		});

		
		buttonDeleteLane = new IButton("Удалить поток");
		buttonDeleteLane.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				SC.ask("Вы уверены, что хотите удалить выделенный поток?\nЭто не приведет к удалению входящих в него задач", new BooleanCallback(){
					@Override
					public void execute(Boolean value) {
						if (value)
						{
							lanes_list.removeSelectedData();
							lanes_list.deselectAllRecords();
							dataWasUpdated = true;
							orderWasUpdated = true;
						}
					}});
			}
		});
		
		buttonSaveOrder = new IButton("Соxранить <br> порядок");
		buttonSaveOrder.setHeight(50);
		buttonSaveOrder.disable();
		buttonSaveOrder.addClickHandler(new ClickHandler()
		{

			@Override
			public void onClick(ClickEvent event)
			{
					// Rewrite 'lane_order' field for every record in the list and set is_link to false
					
			}
		});

		buttons.addMember(buttonAddLane);
		buttons.addMember(buttonDeleteLane);
		buttons.addMember(buttonSaveOrder);
		
		hLayout.addMember(buttons);
		
		return hLayout;
	}

	// Rewrite 'lane_order' field for every record in the list 
	private void setNewLaneOrder()
	{
		for (int i=0; i< lanes_list.getRecords().length; i++)
		{
			Record r = lanes_list.getRecords()[i];
			lanes_list.setEditValue(i, "lane_order", i);
		}
		
		lanes_list.saveAllEdits(new com.smartgwt.client.core.Function(){

			@Override
			public void execute() {
				SC.logWarn("LaneCreationDialod. buttonSaveAll. records order written to DB");

				GlobalData.getStatusBar().indicateMessage("Новый порядок следования потоков сохранен");
				
				// оюновить данные потоков
				// ссылка на конкретную запись не нужна - для потоков всё равно обновляем все ресурсы
				Record r = new Record();
				r.setAttribute("id",-1);
				ResourceLifeCycleManager.resourceChanged(ResourceType.LANE, r);
			}
		});
	}
	
	// Форма редактирования свойств потока
	private VLayout createDynamicForm()
	{
		VLayout vlayout = new VLayout();
		vlayout.setWidth100();
		vlayout.setMargin(5);
		vlayout.setMembersMargin(10);
		
		df = new DynamicForm();
		df.setDataSource(ds_lanes);
		df.setSize("100%", "50px");
		
		
		buttonSave = new IButton("Задать");
		buttonSave.disable();
		buttonSave.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
            	if (!df.hasErrors()) {
            		Record r = df.getValuesAsRecord();
            		
            		record.setAttribute("name", r.getAttributeAsString("name"));
            		record.setAttribute("description", r.getAttributeAsString("description"));
            		record.setAttribute("visible", r.getAttributeAsString("visible"));
            		record.setAttribute("author", GlobalData.getCurrentUser().getAttributeAsInt("id"));
            		
            		GlobalData.getDataSource_lanes().updateData(record, new DSCallback(){

						@Override
						public void execute(DSResponse dsResponse, Object data,
                                            DSRequest dsRequest) {
							GlobalData.getStatusBar().indicateMessage("Свойства потока изменены");
							lanes_list.fetchData(GlobalData.createSearchCriteria(), new DSCallback(){

								@Override
								public void execute(DSResponse dsResponse, Object data,
                                                    DSRequest dsRequest)
								{
									lanes_list.getData().openAll();
								}}, dsr);
						}});
            		
            		dataWasUpdated = true;
                }
            }
        });

		vlayout.addMember(df);
		vlayout.addMember(createSecurityLayout());
		vlayout.addMember(buttonSave);
		
		vlayout.setAlign(Alignment.RIGHT);
		vlayout.setWidth100();
		
		return vlayout;
	}
	
	// Создание контейнера для элементов управления доступом
	protected HLayout createSecurityLayout()
	{

		final HLayout hLayoutMain = new HLayout();
		hLayoutMain.setWidth100();
		hLayoutMain.setHeight(50);
		hLayoutMain.setMembersMargin(5);
		
		VLayout vlayoutContent =  new VLayout();
		vlayoutContent.setHeight100();

		final HLayout hlayoutLabel = new HLayout();
		hlayoutLabel.setMembersMargin(5);
		labelPermissions = new Label("Поток не выбран");
		if (canWriteToThisResource) 
			labelPermissions.setIcon("forms/lock-open.png");
		else
			labelPermissions.setIcon("forms/lock.png");
		labelPermissions.setIconSize(32);
		labelPermissions.setHeight(50);
		labelPermissions.setWidth100();
		labelPermissions.setMargin(10);
		hlayoutLabel.addMember(labelPermissions);

		final HLayout hlayoutControls = new HLayout();
		hlayoutControls.setWidth100();
		hlayoutControls.setVisible(false);
		vlayoutContent.setWidth100();
		hlayoutControls.addMember(createSecurityControls());
		vlayoutContent.addMember(hlayoutLabel);
		vlayoutContent.addMember(hlayoutControls);

		hLayoutMain.addMember(vlayoutContent);
		
		HLayout hLayoutButton = new HLayout();
		hLayoutButton.setWidth(50);
		hLayoutButton.setHeight100();
		hLayoutButton.setAlign(Alignment.CENTER);

		VLayout vLayoutButton =  new VLayout();
		vLayoutButton.setWidth("25px");
		vLayoutButton.setHeight100();
		vLayoutButton.setAlign(Alignment.CENTER);
				
		button.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (hlayoutLabel.isVisible()) 
				{
					// open controls panel and set suitable interface elements
					hlayoutLabel.hide();
					hlayoutControls.show();
					button.setSrc("forms/up.png");
					hLayoutMain.setHeight(130);
				}
				else
				{
					// close controls panel  and set suitable interface elements
					hlayoutLabel.show();
					hlayoutControls.hide();
					button.setSrc("forms/down.png");
					hLayoutMain.setHeight(50);
				}
			}});

		vLayoutButton.addMember(button);

		hLayoutButton.addMember(vLayoutButton);
		hLayoutMain.addMember(hLayoutButton);

		return hLayoutMain;
	}

	// Создание элементов управления доступом
	private DynamicForm createSecurityControls()
	{
		DynamicForm form = new DynamicForm();
		form.setMargin(5);
		form.setWidth100();
		
	    for (Record r: GlobalData.getCurrentUserGroups())
	    	groupMap.put(r.getAttributeAsInt("id"),r.getAttributeAsString("name"));

	    radioGroupRead = new RadioGroupItem();
	    radioGroupRead.setVertical(false);
	    radioGroupRead.setTitle("Могут просматривать");
	    
    	radioGroupRead.setValueMap("Все", "Выбранная группа", "Только я");
	    
	    groupReadSelector = new ComboBoxItem();
	    groupReadSelector.setDisplayField("name");
	    groupReadSelector.setTitle("Группа");
	    groupReadSelector.setValueField("id");
	    groupReadSelector.setValueMap(groupMap);
	    groupReadSelector.setAddUnknownValues(false);
	    groupReadSelector.setDefaultToFirstOption(true);
	    //groupReadSelector.setOptionDataSource(GlobalData.getDataSource_group());
	    
	    radioGroupWrite = new RadioGroupItem();
	    radioGroupWrite.setVertical(false);
	    radioGroupWrite.setTitle("Могут редактировать");
	   
	    radioGroupWrite.setValueMap("Все", "Выбранная группа", "Только я");
	    
	    groupWriteSelector = new ComboBoxItem();
	    groupWriteSelector.setTitle("Группа");
	    groupWriteSelector.setValueMap(groupMap);
	    groupWriteSelector.setAddUnknownValues(false);
	    groupWriteSelector.setDefaultToFirstOption(true);
	    //groupWriteSelector.setOptionDataSource(GlobalData.getDataSource_group());
	    
	    form.setFields(radioGroupRead, groupReadSelector, radioGroupWrite, groupWriteSelector);
	    
	    // Установка начальных значений
	    groupReadSelector.disable();
	    
	    // Установка обработчиков
	    
	    groupReadSelector.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn(groupReadSelector.getValueAsString());
				record.setAttribute("rgroup", groupReadSelector.getValue());
				canRead = groupReadSelector.getDisplayValue();
				SC.logWarn("canRead=" + record);
				updateSecurityLabel();
			}});

	    groupWriteSelector.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn(groupWriteSelector.getValueAsString());
				record.setAttribute("wgroup", groupWriteSelector.getValue());
				canWrite = groupWriteSelector.getDisplayValue();
				SC.logWarn("canWrite=" + record);
				updateSecurityLabel();
			}});
	    
	    radioGroupRead.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn("SecurtyDescriptorDialog:" + event.getValue().toString());
				if (event.getValue().toString().equals("Все"))
				{
					groupReadSelector.disable();
					//!!record.setAttribute("ruser", (Integer)null);
					record.setAttribute("rgroup", GlobalData.ACCESS_ALL);
					
					canRead = "все";				
					SC.logWarn("canRead=" + record);
				}
				if (event.getValue().toString().equals("Только я"))
				{
					groupReadSelector.disable();
					//!!record.setAttribute("ruser", GlobalData.getCurrentUser().getAttributeAsInt("id"));
					record.setAttribute("rgroup", GlobalData.ACCESS_ONLY_AUTHOR);
					
					canRead = "только я";
					SC.logWarn("canRead=" + record);
				}
				if (event.getValue().toString().equals("Выбранная группа"))
				{
					groupReadSelector.enable();
					//!!record.setAttribute("ruser", (Integer)null);
					//if (groupReadSelector.getValue() == null && GlobalData.getCurrentUserGroups().length>0) groupReadSelector.setValue(GlobalData.getCurrentUserGroups()[0].getAttribute("name"));
					record.setAttribute("rgroup", groupReadSelector.getValue());
					
					canRead = groupReadSelector.getDisplayValue();
					SC.logWarn("canRead=" + record);
				}
				updateSecurityLabel();
			}});

	    radioGroupWrite.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn("SecurtyDescriptorDialog:" + event.getValue().toString());
				if (event.getValue().toString().equals("Все"))
				{
					groupWriteSelector.disable();
					//!!record.setAttribute("wuser", (String)null);
					record.setAttribute("wgroup", GlobalData.ACCESS_ALL);
					
					canWrite = "все";
				}
				if (event.getValue().toString().equals("Только я"))
				{
					groupWriteSelector.disable();
					//!!record.setAttribute("wuser", GlobalData.getCurrentUser().getAttribute("id"));
					record.setAttribute("wgroup", GlobalData.ACCESS_ONLY_AUTHOR);

					canWrite = "только я";

				}
				if (event.getValue().toString().equals("Выбранная группа"))
				{
					groupWriteSelector.enable();
					//!!record.setAttribute("wuser", (String)null);
					record.setAttribute("wgroup", groupWriteSelector.getValue());
					
					canWrite = groupWriteSelector.getDisplayValue();
				}
				
				updateSecurityLabel();
			}});

	    return form;
	}

	
	private void updateSecurityControls()
	{
		 if (record.getAttributeAsInt("author").equals(GlobalData.getCurrentUser().getAttributeAsInt("id")))
		 {
		    	radioGroupRead.setValueMap("Все", "Выбранная группа", "Только я");
		    	radioGroupWrite.setValueMap("Все", "Выбранная группа", "Только я");
		 }
		 else
		 {
		    	radioGroupRead.setValueMap("Все", "Выбранная группа");
		      	radioGroupWrite.setValueMap("Все", "Выбранная группа");
		 }
		 
	    groupReadSelector.disable();
	    
	    if (record.getAttributeAsInt("rgroup") == GlobalData.ACCESS_ALL)
	    {
		    radioGroupRead.setValue("Все");
		    canRead = "все";
	    }
	    else
		    if (record.getAttributeAsInt("rgroup") == GlobalData.ACCESS_ONLY_AUTHOR)
		    {
		    	radioGroupRead.setValue("Только я");
		    	canRead = "только я";
		    }
		    else 
		    {
		    	radioGroupRead.setValue("Выбранная группа");
		    	groupReadSelector.enable();
		    	groupReadSelector.setValue(groupMap.get(record.getAttributeAsInt("rgroup")));
		    	canRead = groupMap.get(record.getAttributeAsInt("rgroup"));
		    }
	    
	    groupWriteSelector.disable();
	    
	    if (record.getAttributeAsInt("wgroup") == GlobalData.ACCESS_ALL)
	    {
		    radioGroupWrite.setValue("Все");
		    canWrite = "все";
	    }
	    else
		    if (record.getAttributeAsInt("wgroup") == GlobalData.ACCESS_ONLY_AUTHOR)
		    {
		    	radioGroupWrite.setValue("Только я");
		    	canWrite = "только я";
		    } else 
		    {
		    	radioGroupWrite.setValue("Выбранная группа");
		    	groupWriteSelector.enable();
		    	groupWriteSelector.setValue(groupMap.get(record.getAttributeAsInt("wgroup")));
		    	canWrite = groupMap.get(record.getAttributeAsInt("wgroup"));
		    }
	    
	    updateSecurityLabel();
	}
	
	private HLayout createBottomBar()
	{
		HLayout bottomButtons = new HLayout();

		IButton cancelButton = new IButton("Закрыть окно");
		cancelButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (dataWasUpdated)
				{
					if (orderWasUpdated) 
						setNewLaneOrder(); // обновить порядок потоков, записать в БД и разослать уведомления
					else
					{
						// разослать уведомления
						// ссылка на конкретную запись не нужна - для потоков всё равно обновляем все ресурсы
						Record r = new Record();
						r.setAttribute("id",-1);
						ResourceLifeCycleManager.resourceChanged(ResourceType.LANE, r);
					}
				}
				hide();
			}
		});
		
		bottomButtons.addMember(cancelButton);
		
		bottomButtons.setWidth100();
		bottomButtons.setHeight(40);
		bottomButtons.setAlign(Alignment.RIGHT);
		
		return bottomButtons;
	}
	
	private void updateSecurityLabel()
	{
		SC.logWarn("updateSecurityLabel canRead=" + canRead + " canWrite=" + canWrite);
		if (canWriteToThisResource)
	    {
	    	labelPermissions.setIcon("forms/lock-open.png");
			labelPermissions.setContents("Могут просматривать: <b>"+ canRead + "</b>, редактировать: <b>" + canWrite + "</b>");
	    	button.show();
	    }
	    else
	    {
	    	labelPermissions.setIcon("forms/lock.png");
	    	labelPermissions.setContents("<b>У вас нет прав на изменение свойств этого потока</b>");
	    	button.hide();
	    }
	    labelPermissions.redraw();
		SC.logWarn("LaneCreationDialog: updateSecurityLabel: label contents is " + labelPermissions.getContents());
	}

}