package org.taurus.aya.client.dialogs;

import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.*;

import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.widgets.ExtendedTimeline;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EditEventDialog extends BacklogTaskDialog {

	public EditEventDialog(Record r)
	{
		super(r);
		r.setAttribute("isGraph",true);
	}

	@Override
	protected void constructInterface()
	{
		this.addItem(createFormLayout());
		this.addItem(createSecurityLayout());

		this.addItem(createButtonsLayout());

		if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));
	}
	
	public void setNewEvent(ExtendedTimeline tv, Record event)
	{
		record = event;
		event.setAttribute("parent", 0);
		event.setAttribute("eventWindowStyle", "s3_event_new");
		df.editNewRecord(event);
	}

	protected Widget createFormLayout()
	{
		df = (DynamicForm) super.createFormLayout();

		DateItem startDate = new DateItem("startDate");
		DateItem endDate = new DateItem("endDate");

		// Executor field
		LinkedHashMap<Integer,String> usersMap = new LinkedHashMap<>();
		for (Record u: GlobalData.getUsers())
			usersMap.put(u.getAttributeAsInt("id"),u.getAttributeAsString("nickname"));
		SelectItem executor = new SelectItem("executor");
		executor.setValueMap(usersMap);
		if (record.getAttribute("executor") == null)
			executor.setValue(GlobalData.getCurrentUser().getAttributeAsInt("id"));

		//Estimation field
		// setting selectItem values for estimation
		LinkedHashMap<Integer,String> valueMap = new LinkedHashMap<>();
		valueMap.put(0,"Время не указано");
		valueMap.put(1,"1 час");
		valueMap.put(3,"3 часа");
		valueMap.put(5,"5 часов");
		valueMap.put(8,"1 день");
		valueMap.put(16,"2 дня");
		valueMap.put(24,"3 дня");
		valueMap.put(56,"5 дней");

		IntegerItem durationH = new IntegerItem("duration_h");
		durationH.setValueMap(valueMap);

		//Priority field
		SelectItem priority = new SelectItem("priority");
		priority.setDefaultValue(1);

		//Lane field. To fill it, we have to make the server request
		SelectItem lane = new SelectItem("lane");
		lane.setWidth(300);
		GlobalData.getDataSource_lanes().fetchData(new Criteria(), (dsResponse, data, dsRequest) -> {
			Map<String,String> valueMapLane = new HashMap<>();
			for (Record r1 : dsResponse.getData())
				valueMapLane.put(r1.getAttributeAsString("name"), r1.getAttributeAsString("name"));

			lane.setValueMap(valueMapLane);

			TextItem name = new TextItem("name");
			name.setWidth(300);

			TextAreaItem description = new TextAreaItem("description");
			description.setWidth(300);
			description.setHeight(200);

			IntegerItem spentTime = new IntegerItem("spentTime");

			df.setFields(lane, name, startDate, endDate, description, executor, priority, durationH, spentTime);

			df.editRecord(record);

			if (!GlobalData.canWrite(record))
				df.disable();

			show();
		});

		return df;
	}

	@Override
	protected void saveDialogData() {
		//Date modification
		if (df.getValuesAsRecord().getAttribute("startDate") != null)
			df.setValue("startDate",new Date(df.getValuesAsRecord().getAttributeAsDate("startDate").getTime() + 24*3600*1000));

		if (df.getValuesAsRecord().getAttribute("endDate") != null)
			df.setValue("endDate",new Date(df.getValuesAsRecord().getAttributeAsDate("endDate").getTime() + 24*3600*1000));

		//Validate fields
		if (df.getField("executor").getValue() == null)
			SC.warn("Исполнитель должен быть задан");
		else
			if (df.getField("lane").getValue() == null || df.getField("lane").getValue().equals("null"))
				SC.warn("Необходимо задать поток для задачи");
			else
				super.saveDialogData();
	}
}