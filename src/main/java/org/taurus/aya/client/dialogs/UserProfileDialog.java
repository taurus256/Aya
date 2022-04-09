package org.taurus.aya.client.dialogs;

import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.validator.IsStringValidator;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.AbstractPropertiesDialog;
import org.taurus.aya.client.generic.GenericPropertiesDialog;

public class UserProfileDialog extends AbstractPropertiesDialog {

	UserProfileDialog thisDialog;
	private FormItem passwordField;

	public UserProfileDialog(Record user)
	{
		super(user, "document.png", ResourceType.TASK, GlobalData.getDataSource_user(), " профиля пользователя");
		thisDialog = this;
	}
	
	@Override
	public void constructInterface()
	{
		this.addItem(createFormLayout());
		this.addItem(createButtonsLayout());
	}

	protected Widget createFormLayout()
	{
		df.editRecord(record);
		passwordField = df.getField("password");
		if (passwordField !=null) {
			passwordField.addDoubleClickHandler(event -> passwordField.setCanEdit(true));
			passwordField.setHint("Щелкните дважды по полю ввода, чтобы сменить пароль");
			passwordField.setPrompt("Щелкните дважды, чтобы сменить пароль");
		}
		return df;
	}

	@Override
	protected void saveDialogData(){
		// if user switch passwordField to edit mode and remains it empty
		if (passwordField!=null && passwordField.getCanEdit() && passwordField.getDisplayValue().isEmpty())
			SC.warn("Пароль не может быть пустым");
		else
			super.saveDialogData();
	}
}
