package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.*;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.GenericPropertiesDialog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BacklogTaskDialog extends GenericPropertiesDialog {
	
	DataSource dataSource;
	Boolean testForСompleteness;

	public BacklogTaskDialog(Record r, Boolean testForСompleteness)
	{
		super(r, "task.png", ResourceType.TASK, GlobalData.getDataSource_tasks(), "отложенной задачи");
		this.testForСompleteness = testForСompleteness;

		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("500px", "300px");
		setTitle("Свойства отложенной задачи");
		setBodyColor("rgb(253, 253, 253)");
//		setBodyStyle("s3_windowBody");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		SC.logWarn("Before CI");
		constructInterface();
        SC.logWarn("After CI");

		//setting selectItem values for estimation
		LinkedHashMap valueMap = new LinkedHashMap<Integer,String>();
		valueMap.put(0,"Время не указано");
		valueMap.put(1,"1 час");
		valueMap.put(3,"3 часа");
		valueMap.put(5,"5 часов");
		valueMap.put(8,"1 день");
		valueMap.put(16,"2 дня");
		valueMap.put(24,"3 дня");
		valueMap.put(56,"5 дней");
		SelectItem si = new SelectItem("duration_h");
		si.setValueMap(valueMap);

		LinkedHashMap valueMapPriority = new LinkedHashMap<Integer,String>();
		valueMapPriority.put(0,"Низкий");
		valueMapPriority.put(1,"Нормальный");
		valueMapPriority.put(2,"Высокий");
		SelectItem sip = new SelectItem("priority");
		sip.setValueMap(valueMapPriority);
		sip.setDefaultValue(1);
        SC.logWarn(">1");
		this.dataSource = GlobalData.getDataSource_tasks();
		df.setDataSource(dataSource);
		SelectItem lane = new SelectItem("lane");
        lane.setWidth(300);
		GlobalData.getDataSource_lanes().fetchData(new Criteria(), new DSCallback() {
					@Override
					public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
						Map<String,String> valueMapLane = new HashMap<>();
						for (Record r: dsResponse.getData())
							valueMapLane.put(r.getAttributeAsString("name"),r.getAttributeAsString("name"));

						lane.setValueMap(valueMapLane);

						TextItem name = new TextItem("name");
						name.setWidth(300);

						TextAreaItem description = new TextAreaItem("description");
						description.setWidth(300);
						description.setHeight(200);

						SC.logWarn(">2");
						df.setFields(lane, name, description, sip, si);

						df.editRecord(r);
						SC.logWarn(">3");

						if (!GlobalData.canWrite(r))
							df.disable();

						show();
					}
				});


	}
	
	
	public void constructInterface()
	{
		this.addItem(createFormLayout());
		
		this.addItem(createTagsLayout());
		this.addItem(createSecurityLayout());
		
		this.addItem(createButtonsLayout());
		if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));
	}
	
	
	private VLayout createButtonsLayout()
	{
		VLayout vlayout = new VLayout();
		vlayout.setMembersMargin(10);
		vlayout.setMargin(10);
		vlayout.setWidth100();
		vlayout.setHeight("32px");
		

		HLayout hlayout = new HLayout();
		final IButton submitButton = new IButton("Сохранить");
		submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                if (testForСompleteness && (df.getField("lane").getValue() == null || df.getField("lane").getValue().equals("null")))
                    SC.warn("Необходимо задать поток для задачи");
                else
				    saveDialogData();
			}
		});
		
		final IButton cancelButton = new IButton("Отменить");
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		if (canWriteToThisResource) hlayout.addMember(submitButton);
		
		hlayout.addMember(cancelButton);
		hlayout.setWidth("100%");
		hlayout.setMembersMargin(10);  
		hlayout.setAlign(Alignment.RIGHT);
		vlayout.addMember(hlayout);
		vlayout.setWidth("100%");
		
		return vlayout;
	}

}