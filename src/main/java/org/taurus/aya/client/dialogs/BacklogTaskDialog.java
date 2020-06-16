package org.taurus.aya.client.dialogs;

import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.data.*;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.fields.*;
import org.taurus.aya.client.*;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.AbstractPropertiesDialog;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BacklogTaskDialog extends AbstractPropertiesDialog {
	public BacklogTaskDialog(Record r)
	{
		super(r, "task.png", ResourceType.TASK, GlobalData.getDataSource_tasks(), "задачи");
		setWidth(520);
        SC.logWarn("Конструктор BacklogTaskDialog отработал");
	}

    public BacklogTaskDialog(Record r, Consumer<Void> f)
    {
        super(r, "task.png", ResourceType.TASK, GlobalData.getDataSource_tasks(), "задачи", f);
        setWidth(520);
        SC.logWarn("Конструктор BacklogTaskDialog отработал");
    }

	@Override
	protected void constructInterface()
	{
        SC.logWarn("constructInterface для BacklogTaskDialog вызван");
        this.addItem(createFormLayout());
		this.addItem(createTagsLayout());
		this.addItem(createSecurityLayout());

		this.addItem(createButtonsLayout());
		if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));
        SC.logWarn("constructInterface для BacklogTaskDialog отработал");
	}

    protected Widget createFormLayout()
    {
        // Executor field
        LinkedHashMap<Integer,String> usersMap = new LinkedHashMap<>();
        for (Record u: GlobalData.getUsers())
            usersMap.put(u.getAttributeAsInt("id"),u.getAttributeAsString("nickname"));
        SelectItem executor = new SelectItem("executor");
        executor.setValueMap(usersMap);

        //Estimation field
        FloatItem plannedDuration = new FloatItem("plannedDuration");

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

            df.setFields(lane, name, description, executor, priority, plannedDuration);

            df.editRecord(record);

            if (!GlobalData.canWrite(record))
                df.disable();

            show();
        });

        return df;
    }
}