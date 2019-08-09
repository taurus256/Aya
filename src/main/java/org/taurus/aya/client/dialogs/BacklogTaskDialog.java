package org.taurus.aya.client.dialogs;

import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.data.*;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.DateItem;
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
	public BacklogTaskDialog(Record r)
	{
		super(r, "task.png", ResourceType.TASK, GlobalData.getDataSource_tasks(), "задачи");
		setWidth(520);
		SC.logWarn("BacklogTaskDialog:start");
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

        SelectItem durationH = new SelectItem("duration_h");
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

            SC.logWarn(">2");
            df.setFields(lane, name, startDate, endDate, description, executor, priority, durationH);

            df.editRecord(record);
            SC.logWarn(">3");

            if (!GlobalData.canWrite(record))
                df.disable();

            show();
        });

        return df;
    }
}