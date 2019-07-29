package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import org.taurus.aya.client.*;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.AbstractPropertiesDialog;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BacklogTaskDialog extends AbstractPropertiesDialog {
	SelectItem lane,si,sip;

	public BacklogTaskDialog(Record r)
	{
		super(r, "task.png", ResourceType.TASK, GlobalData.getDataSource_tasks(), "задачи");
		setWidth(520);
		SC.logWarn("BacklogTaskDialog:start");

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

		si = new SelectItem("duration_h");
		si.setValueMap(valueMap);

		LinkedHashMap valueMapPriority = new LinkedHashMap<Integer,String>();
		valueMapPriority.put(0,"Низкий");
		valueMapPriority.put(1,"Нормальный");
		valueMapPriority.put(2,"Высокий");
		sip = new SelectItem("priority");
		sip.setValueMap(valueMapPriority);
		sip.setDefaultValue(1);
        SC.logWarn(">1");

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
	
	@Override
	protected void constructInterface()
	{
		this.addItem(createFormLayout());
		this.addItem(createTagsLayout());
		this.addItem(createSecurityLayout());

		this.addItem(createButtonsLayout());
		if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));
	}


}