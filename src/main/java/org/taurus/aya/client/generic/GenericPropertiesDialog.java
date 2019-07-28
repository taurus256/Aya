package org.taurus.aya.client.generic;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.VerticalAlignment;
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
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.RecordDropEvent;
import com.smartgwt.client.widgets.grid.events.RecordDropHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.TreeGrid;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.ResourceLifeCycleManager;
import org.taurus.aya.client.TabManager.ResourceType;

import java.util.LinkedHashMap;

public class GenericPropertiesDialog extends Dialog {
	
	GenericPropertiesDialog thisDialog;
	protected Record record, previousRecord;
	protected RadioGroupItem radioGroupRead, radioGroupWrite;
	ComboBoxItem groupReadSelector, groupWriteSelector;
	protected Label labelPermissions;
	String canRead = "все", canWrite = "все";
	
	protected Label labelTags;
	protected ListGrid tagListGrid;
	
	protected DynamicForm df;
	protected DataSource dataSource;
	protected HandlerRegistration hrRecordDrop;
	
	protected boolean implicityCreated = false;
	
	String suffix = "";
	String imageName = "";
	final ResourceType resourceType;
	protected boolean canWriteToThisResource;
	
	
	public GenericPropertiesDialog(Record r, String imgName, ResourceType resType, DataSource ds, String suff)
	{
		thisDialog = this;
		record = r;
		dataSource = ds;
		imageName = imgName;
		resourceType = resType;
		suffix = suff;
		
		//создаем копию существующего состояния записи, чтобы отправить уведомление при изменении её свойств 
		previousRecord = Record.copyAttributes(r, new String[]{"name","id","author","rgroup","wgroup"});
		
				
		if (record == null )
		{
			SC.logWarn("SecurityDescriptorDialog: record IS NULL");
			return;
		}
		
		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("400px", "100px");
		if (record.getAttribute("id") == null)
			setTitle("Создание " + suffix);
		else
			setTitle("Свойства " + suffix);
		setBodyColor("rgb(253, 253, 253)");
	
		setHeaderIcon("header/" + getImageName(), 20, 20);
//		setBodyStyle("s3_windowBody");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		setAnimateTime(2000);
		//setHeaderIcon("header/note.png", 64, 64);
		
//		Map<String, Object> headerDefaults = new HashMap<String, Object>();
//		Integer newHeight = 70;
//		headerDefaults.put("height", newHeight);
//		setAttribute("headerDefaults", headerDefaults, true);
		
//		constructInterface();
//		thisDialog.show();
		
		//Add handler forEnter key
		addKeyPressHandler(new KeyPressHandler(){

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Enter"))
				{
					saveDialogData();
				}
			}});
		
		canWriteToThisResource = GlobalData.canWrite(r);
	}
	
	public void constructInterface()
	{
		this.addItem(createFormLayout());
		if (resourceType != ResourceType.TAG) this.addItem(createTagsLayout());
		this.addItem(createSecurityLayout());
		this.addItem(createButtonsBar());

		//Update the Tag label
		if (resourceType != ResourceType.TAG)
			if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));
	}
	
	// name of column in table 'links' for this resource
	protected String getColumnName()
	{
		/*switch (resourceType)
		{
			case TASK: return "task_id";
			case DOCUMENT: return "doc_id";
			case SCRIPT: return "script_id";
			default: return "";
		}*/
		return "to_id";
	}
	
	protected String getSuffix()
	{
		return suffix;
	}
	
	/* Return value for this function must be set when defining subclass */
	protected ResourceType getRecourceType()
	{
		return resourceType;
	}
	
	/* Return value for this function must be set when defining subclass */
	protected String getImageName()
	{
		return imageName;
	}
	
	protected Widget createFormLayout()
	{
		df = new DynamicForm();
		df.setDataSource(dataSource);
		df.editRecord(record);
//		df.setShowInlineErrors(true);
//		df.setShowErrorIcons(true);
		df.setShowErrorText(false);
		df.setRequiredTitlePrefix("");
		df.setRequiredTitleSuffix("*&nbsp;:");
		df.setWrapItemTitles(false);
		df.setWidth100();
		df.setMargin(10);
		df.setColWidths(new Object[]{
            new Integer(200),
            new Integer(350),
            new Integer(0)});
		df.setShowInlineErrors(false);
		df.setShowErrorIcons(false);
		return df;
	}
	
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
		labelPermissions = new Label();
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
				
		final ImgButton button = new ImgButton(){{
			setAlign(Alignment.CENTER);
			setSize("25px", "25px");
			setSrc("forms/down.png");
			setShowRollOver(false);
			setShowDown(false);
		}};
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

		if (canWriteToThisResource) vLayoutButton.addMember(button);

		hLayoutButton.addMember(vLayoutButton);
		hLayoutMain.addMember(hLayoutButton);
		
		return hLayoutMain;
	}

	protected HLayout createTagsLayout()
	{
		final HLayout hLayoutMain = new HLayout();
		hLayoutMain.setWidth100();
		hLayoutMain.setHeight(50);
		hLayoutMain.setMembersMargin(5);
		
		VLayout vlayoutContent =  new VLayout();
		vlayoutContent.setHeight100();
		
		final HLayout hlayoutLabel = new HLayout();
		hlayoutLabel.setMembersMargin(5);
		labelTags = new Label();
		if(canWriteToThisResource)
			labelTags.setIcon("forms/tag.png");
		else
			labelTags.setIcon("forms/tag_gray.png");
		labelTags.setIconSize(32);
		labelTags.setHeight(50);
		labelTags.setWidth100();
		labelTags.setMargin(10);
		labelTags.setContents("Тегов нет");
		hlayoutLabel.addMember(labelTags);
		
		vlayoutContent.setWidth100();
		vlayoutContent.addMember(hlayoutLabel);
		final VLayout vLayoutControls = createTagControls();
		vLayoutControls.setVisible(false);
		vlayoutContent.addMember(vLayoutControls);
		
		hLayoutMain.addMember(vlayoutContent);
		
		HLayout hLayoutButton = new HLayout();
		hLayoutButton.setWidth(50);
		hLayoutButton.setHeight100();
		hLayoutButton.setAlign(Alignment.CENTER);
		
		VLayout vLayoutButton =  new VLayout();
		vLayoutButton.setWidth("25px");
		vLayoutButton.setHeight100();
		vLayoutButton.setAlign(Alignment.CENTER);
				
		final ImgButton button = new ImgButton(){{
			setAlign(Alignment.CENTER);
			setSize("25px", "25px");
			setSrc("forms/down.png");
			setShowRollOver(false);
			setShowDown(false);
		}};
		button.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (hlayoutLabel.isVisible())
				{
					// All required fields must be filled
					if (!df.validate()) return;
					
					if (record.getAttributeAsInt("id") == null ) // Here we need to create record and obtain its ID from database
					{
						SC.logWarn("SecurityDescriptorDialog: record id IS NULL... Create new record");
						
						for (FormItem f : df.getFields())
						{
							record.setAttribute(f.getName(), f.getValue());
							SC.logWarn("GenericPropertiesDialog. Save data " + f.getName() + "=" + f.getValue());
						}
						
						dataSource.addData(record, new DSCallback(){

							@Override
							public void execute(DSResponse dsResponse, Object data,
                                                DSRequest dsRequest) {
								if (dsResponse.getDataAsRecordList().getLength()>0)
								{
									implicityCreated = true;
									record = dsResponse.getDataAsRecordList().get(0);
									SC.logWarn("GenericPropertiesDialog: new record implicitly added to DB, id = " + record.getAttributeAsString("id"));
									
									tagListGrid.setCriteria(new Criteria(getColumnName(), record.getAttributeAsString("id")));
									
									hlayoutLabel.hide();
									vLayoutControls.show();
									button.setSrc("forms/up.png");
									hLayoutMain.setHeight(130);
								}
								else
									SC.logWarn("GenericPropertiesDialog: Could not implicitly create record");
							}});
					}
					else
					{
						SC.logWarn("GenericPropertiesDialog: Record is already created");
						
						hlayoutLabel.hide();
						vLayoutControls.show();
						button.setSrc("forms/up.png");
						hLayoutMain.setHeight(130);
					}
				}
				else
				{
					// close controls panel
					hlayoutLabel.show();
					vLayoutControls.hide();
					button.setSrc("forms/down.png");
					hLayoutMain.setHeight(50);
					updateTagLabel();
				}
			}});
		if (canWriteToThisResource) vLayoutButton.addMember(button);
		hLayoutButton.addMember(vLayoutButton);
		hLayoutMain.addMember(hLayoutButton);
		
		return hLayoutMain;
	}

	
	protected VLayout createTagControls()
	{
		VLayout vLayout = new VLayout();
		vLayout.setMargin(10);
		vLayout.setMembersMargin(5);
		
		tagListGrid = new ListGrid();
		tagListGrid.setWidth100();
		tagListGrid.setHeight(100);
		tagListGrid.setShowHeader(false);
		tagListGrid.setAutoSaveEdits(true);
		tagListGrid.setAutoFetchData(true);
		tagListGrid.setInitialCriteria( new Criteria(getColumnName(), record.getAttributeAsString("id")));
		tagListGrid.setDataSource(GlobalData.getDataSource_links());
		
		ListGridField tagField = new ListGridField("from_name", "Тэг");
		tagListGrid.setFields(tagField);
		
		tagListGrid.addDataArrivedHandler( new DataArrivedHandler(){

			@Override
			public void onDataArrived(DataArrivedEvent event) {
				updateTagLabel();
			}});
		
		tagListGrid.setCanAcceptDroppedRecords(true); 
		tagListGrid.setCanAcceptDrop(true);
		hrRecordDrop = tagListGrid.addRecordDropHandler(new RecordDropHandler(){

			@Override
			public void onRecordDrop(RecordDropEvent event) {
				SC.logWarn("GenericPropsDialog: enter to addRecordDropHandler");
				DataSource ds = ((TreeGrid)(event.getSourceWidget())).getDataSource();
				///Проверка на то, что передается, тоже нужна!
				Record[] cloneOfDroppedRecords = ds.copyRecords(event.getDropRecords());
				for (Record r:cloneOfDroppedRecords)
					{
						r.setAttribute("from_id", r.getAttribute("id"));
						r.setAttribute("from_type", ResourceType.TAG);
						r.setAttribute("to_id", record.getAttribute("id"));
						r.setAttribute("to_type",resourceType.ordinal());
						r.setAttribute("name", record.getAttributeAsString("name"));
						r.setAttribute("resource_type",getRecourceType().ordinal());
						r.setAttribute("image", getImageName());
						tagListGrid.addData(r);
					}
				SC.logWarn("GenericPropsDialog: data added from recordDrophandler");
				event.cancel();
			}});

		tagListGrid.setCanRemoveRecords(true);
		tagListGrid.setRemoveIcon("forms/remove.png");
		//IButton buttonDeleteTag = new IButton("Удалить тег");
		
		vLayout.addMember(tagListGrid);
		//vLayout.addMember(buttonDeleteTag);
		vLayout.setAlign(Alignment.CENTER);
		return vLayout;
	}
	
	protected void updateTagLabel()
	{
		SC.logWarn("GenericPropertiesDialog: updateTagLabel");
		ListGridRecord[] recordList = tagListGrid.getRecords();
		
		String str = "";
		if (recordList.length == 0)
		{
			labelTags.setIcon("forms/tag_gray.png");
			labelTags.setContents("Тегов нет");
		}
		else
		{
			str = "Теги: ";
			for (ListGridRecord lgR:recordList)
			{
				if (str.length() > 6) str += ", ";
				str += lgR.getAttributeAsString("from_name");
			}
			labelTags.setIcon("forms/tag.png");
			labelTags.setContents("<b>" + str + "</b>");
		}
	}
	
	private DynamicForm createSecurityControls()
	{
		DynamicForm form = new DynamicForm();
		form.setMargin(5);
		form.setWidth100();
		
	    LinkedHashMap<Integer,String> groupMap = new LinkedHashMap<Integer,String>();
	    for (Record r: GlobalData.getCurrentUserGroups())
	    	groupMap.put(r.getAttributeAsInt("id"),r.getAttributeAsString("name"));
	    
		radioGroupRead = new RadioGroupItem();
	    radioGroupRead.setVertical(false);
	    radioGroupRead.setTitle("Могут просматривать");
	    
	    if (record.getAttributeAsInt("author").equals(GlobalData.getCurrentUser().getAttributeAsInt("id")))
	    	radioGroupRead.setValueMap("Все", "Выбранная группа", "Только я");
	    else
	    	radioGroupRead.setValueMap("Все", "Выбранная группа");
	    
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
	    
	    if (record.getAttributeAsInt("author").equals(GlobalData.getCurrentUser().getAttributeAsInt("id")))
	    	radioGroupWrite.setValueMap("Все", "Выбранная группа", "Только я");
	    else
	    	radioGroupWrite.setValueMap("Все", "Выбранная группа");

	    groupWriteSelector = new ComboBoxItem();
	    groupWriteSelector.setTitle("Группа");
	    groupWriteSelector.setValueMap(groupMap);
	    groupWriteSelector.setAddUnknownValues(false);
	    groupWriteSelector.setDefaultToFirstOption(true);
	    //groupWriteSelector.setOptionDataSource(GlobalData.getDataSource_group());
	    
	    form.setFields(radioGroupRead, groupReadSelector, radioGroupWrite, groupWriteSelector);
    
	    SC.logWarn("rgroup = " + record.getAttributeAsInt("rgroup"));
	    SC.logWarn("wgroup = " + record.getAttributeAsInt("wgroup"));
	    
	    String rGroupName=groupMap.get(record.getAttributeAsInt("rgroup")) == null?"чужая группа":groupMap.get(record.getAttributeAsInt("rgroup"));
	    String wGroupName=groupMap.get(record.getAttributeAsInt("wgroup")) == null?"чужая группа":groupMap.get(record.getAttributeAsInt("wgroup"));

	    SC.logWarn("groupMap.get(record.getAttributeAsInt(\"rgroup\")) = " + rGroupName);
	    SC.logWarn("groupMap.get(record.getAttributeAsInt(\"wgroup\")) = " + wGroupName);
	    
	    // Установка начальных значений
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
		    	groupReadSelector.setValue(rGroupName);
		    	canRead = groupMap.get(rGroupName);
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
		    } 
		    else
		    {
		    	radioGroupWrite.setValue("Выбранная группа");
		    	groupWriteSelector.enable();
		    	groupWriteSelector.setValue(wGroupName);
		    	canWrite = wGroupName;
		    }
	    
	    if (canWriteToThisResource)
	    	updateSecurityLabel();
	    else
	    	labelPermissions.setContents("<b>У вас нет прав для записи в этот ресурс</b>");
	    
	    // Установка обработчиков
	    
	    groupReadSelector.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn("GenericProertiesDialog:descriptor:" + groupReadSelector.getValueAsString());
				record.setAttribute("rgroup", groupReadSelector.getValue());
				canRead = groupReadSelector.getDisplayValue();
				updateSecurityLabel();
			}});

	    groupWriteSelector.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn("GenericProertiesDialog:descriptor:" + groupWriteSelector.getValueAsString());
				record.setAttribute("wgroup", groupWriteSelector.getValue());
				canWrite = groupWriteSelector.getDisplayValue();
				updateSecurityLabel();
			}});
	    
	    radioGroupRead.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn("GenericProertiesDialog:descriptor:" +  event.getValue().toString());
				if (event.getValue().toString().equals("Все"))
				{
					groupReadSelector.disable();
					//!!record.setAttribute("ruser", (String)null);
					record.setAttribute("rgroup", GlobalData.ACCESS_ALL);
					canRead = "все";					
				}
				if (event.getValue().toString().equals("Только я"))
				{
					groupReadSelector.disable();
					//!!record.setAttribute("author", GlobalData.getCurrentUser().getAttribute("id"));
					record.setAttribute("rgroup", GlobalData.ACCESS_ONLY_AUTHOR);
					canRead = "только я";
				}
				if (event.getValue().toString().equals("Выбранная группа"))
				{
					groupReadSelector.enable();
					//!!record.setAttribute("ruser", (String)null);
					//if (groupReadSelector.getValue() == null && GlobalData.getCurrentUserGroups().length>0) groupReadSelector.setValue(GlobalData.getCurrentUserGroups()[0].getAttribute("name"));
					record.setAttribute("rgroup", groupReadSelector.getValue());
					canRead = groupReadSelector.getDisplayValue();
				}
				updateSecurityLabel();
			}});

	    radioGroupWrite.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				SC.logWarn("GenericProertiesDialog:descriptor:" +  event.getValue().toString());
				if (event.getValue().toString().equals("Все"))
				{
					groupWriteSelector.disable();
					//!record.setAttribute("wuser", (String)null);
					record.setAttribute("wgroup", GlobalData.ACCESS_ALL);
					canWrite = "все";					
				}
				if (event.getValue().toString().equals("Только я"))
				{
					groupWriteSelector.disable();
					//!record.setAttribute("author", GlobalData.getCurrentUser().getAttribute("id"));
					record.setAttribute("wgroup", GlobalData.ACCESS_ONLY_AUTHOR);
					canWrite = "только я";					
				}
				if (event.getValue().toString().equals("Выбранная группа"))
				{
					groupWriteSelector.enable();
					//!record.setAttribute("wuser", (String)null);
					record.setAttribute("wgroup", groupWriteSelector.getValue());
					canWrite = groupWriteSelector.getDisplayValue();					
				}
				
				updateSecurityLabel();
			}});
	    return form;
	}
	
	protected VLayout createButtonsBar()
	{
		VLayout vlayout = new VLayout();
		vlayout.setHeight("50px");
		vlayout.setAlign(VerticalAlignment.CENTER);

		HLayout hlayout = new HLayout();
		hlayout.setAlign(VerticalAlignment.CENTER);
		
		final IButton submitButton = new IButton("Сохранить");
		submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				SC.logWarn("SecurtyDescriptor set to: ruser:\t" + record.getAttributeAsString("ruser") + "\t rgroup:" + record.getAttributeAsString("rgroup"));
				SC.logWarn("SecurtyDescriptor set to: wuser:\t" + record.getAttributeAsString("wuser") + "\t wgroup:" + record.getAttributeAsString("wgroup"));
				saveDialogData();
			}
		});
		
		submitButton.setWidth("150px");
		
		final IButton cancelButton = new IButton("Закрыть");
		if (canWriteToThisResource) cancelButton.setTitle("Отменить");
			
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (implicityCreated)
				{
					dataSource.removeData(record, new DSCallback(){

						@Override
						public void execute(DSResponse dsResponse, Object data,
                                            DSRequest dsRequest) {
							SC.logWarn("GenericPropertiesDialog: remove implicitly created record");
							
						}});
				}
				hide();
			}
		});
		
		if (canWriteToThisResource) hlayout.addMember(submitButton);
		hlayout.addMember(cancelButton);
		hlayout.setWidth("400px");
		hlayout.setMembersMargin(10);  
		hlayout.setMargin(10);
		hlayout.setAlign(Alignment.RIGHT);
		
		vlayout.addMember(hlayout);
		
		return vlayout;
	}
	
	protected void saveDialogData()
	{
		if (df.validate())
		{
			if (record.getAttributeAsInt("id") != null)
			{
				//df.saveData();
				//Getting data from DynamicForm
				for (FormItem f : df.getFields())
				{
					record.setAttribute(f.getName(), f.getValue());
					SC.logWarn("GenericPropertiesDialog. Update data " + f.getName() + "=" + f.getValue());
				}


				dataSource.updateData(record, new DSCallback(){
	
					@Override
					public void execute(DSResponse dsResponse, Object data,
                                        DSRequest dsRequest) {
//						SC.logWarn("GPD: Предудущее rgroup:" + previousRecord.getAttributeAsString("rgroup") + " текущее:" + record.getAttributeAsString("rgroup"));
//
//						if (previousRecord.getAttributeAsInt("rgroup") == GlobalData.ACCESS_ALL )
//						{
//							ResourceLifeCycleManager.resourceChanged(resourceType, previousRecord);
//							return;
//						}
//
//						if (record.getAttributeAsInt("rgroup") == GlobalData.ACCESS_ALL )
//						{
//							ResourceLifeCycleManager.resourceChanged(resourceType, record);
//							return;
//						}
//
//						if (previousRecord.getAttributeAsInt("rgroup").equals(record.getAttributeAsInt("rgroup")))
//							ResourceLifeCycleManager.resourceChanged(resourceType, record);
//						else
//						{
//							ResourceLifeCycleManager.resourceChanged(resourceType, record);
//							ResourceLifeCycleManager.resourceChanged(resourceType, previousRecord);
//						}
					}});
			}
			else
			{

				record.setAttribute("author",GlobalData.getCurrentUser().getAttributeAsInt("id"));

				for (FormItem f : df.getFields())
				{
					record.setAttribute(f.getName(), f.getValue());
					SC.logWarn("GenericPropertiesDialog. Save data " + f.getName() + "=" + f.getValue());
				}

				SC.logWarn("Задача добавляется...");
				dataSource.addData(record, new DSCallback(){
	
					@Override
					public void execute(DSResponse dsResponse, Object data,
                                        DSRequest dsRequest) {
						//ResourceLifeCycleManager.resourceCreated(resourceType, dsResponse.getData()[0]);
						SC.logWarn("Задача добавлена");
					}});
			}
			hide();
		}
	}
	
	protected void updateSecurityLabel()
	{
		labelPermissions.setContents("Могут просматривать: <b>"+ canRead + "</b>, редактировать: <b>" + canWrite + "</b>");
	}
}
