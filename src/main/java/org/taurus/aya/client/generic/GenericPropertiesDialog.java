package org.taurus.aya.client.generic;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
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
import org.taurus.aya.client.TabManager.ResourceType;

import java.util.LinkedHashMap;

public class GenericPropertiesDialog extends AbstractPropertiesDialog {

	public GenericPropertiesDialog(Record r, String imgName, ResourceType resType, DataSource ds, String suff)
	{
		super(r,imgName,resType,ds,suff);
		df.show();
		show();
	}

	public GenericPropertiesDialog(Record r, String imgName, ResourceType resType, DataSource ds, String suff, java.util.function.Consumer<Void> f)
	{
		super(r,imgName,resType,ds,suff, f);
		df.show();
		show();
	}

	protected void constructInterface()
	{
		this.addItem(createFormLayout());
		if (resourceType != ResourceType.TAG)
			this.addItem(createTagsLayout());
		else
			//Update the Tag label
			if (resourceType != ResourceType.TAG)
				if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));

		this.addItem(createSecurityLayout());
		this.addItem(createButtonsLayout());
	}
}
