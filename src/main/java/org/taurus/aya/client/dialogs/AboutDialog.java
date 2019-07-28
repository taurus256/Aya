package org.taurus.aya.client.dialogs;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.VersionNumber;

import java.util.HashMap;
import java.util.Map;

public class AboutDialog extends Dialog {
	
	//final String contents = "<center><img src=\"images/S5.png\"/><br><br>Intranet collaboration system<br>alpha release, build at " + VersionNumber.getVersionBuildDate() + "<br><br><a href=\"http://s4.groupware.su\">http://s4.groupware.su</a><br><br>&copy; Evgene Ostapenko, 2014-2017<br><br>Open-source components, used at development, are listed below:<br><br><table>	<tr><th>Component</th><th>Website</th><th>License</th></tr>	<tr><td>Hawcons iconset</td><td><a href=\"http://hawcons.com\">http://hawcons.com</a></td><td>Free, custom</td></tr>	<tr><td>SHA-1 cryptographic library</td><td><a href=\"www.movable-type.co.uk/scripts/sha1.html \">www.movable-type.co.uk/scripts/sha1.html </a></td><td>MIT Licence</td></tr>	<tr><td>CKEditor web editor</td><td><a href=\"http://ckeditor.com\">http://ckeditor.com</a></td><td>GPL</td></tr>	<tr><td>ACEditor web editor</td><td><a href=\"http://ace.c9.io\">http://ace.c9.io</a></td><td>BSD</td></tr> <tr><td>CometD messaging</td><td><a href=\"http://cometd.org\">http://cometd.org</a></td><td>Creative Commons</td></tr> </table><br></center>";
	final String contents = "<center><img src=\"images/S5.png\"/><br><br>S5 \"Aya\" build " + VersionNumber.getVersionBuildDate() + "<br><br><br>Компоненты с открытыми исходными кодами, использованные при разработке:<br><br><table>	<tr><th>Компонент</th><th>Сайт</th><th>Лицензия</th></tr>	<tr><td>Hawcons iconset</td><td><a href=\"http://hawcons.com\">http://hawcons.com</a></td><td>Free, custom</td></tr>	<tr><td>SHA-1 cryptographic library</td><td><a href=\"www.movable-type.co.uk/scripts/sha1.html \">www.movable-type.co.uk/scripts/sha1.html </a></td><td>MIT Licence</td></tr>	<tr><td>CKEditor web editor</td><td><a href=\"http://ckeditor.com\">http://ckeditor.com</a></td><td>GPL</td></tr>	<tr><td>ACEditor web editor</td><td><a href=\"http://ace.c9.io\">http://ace.c9.io</a></td><td>BSD</td></tr> <tr><td>CometD messaging</td><td><a href=\"http://cometd.org\">http://cometd.org</a></td><td>Creative Commons</td></tr> </table><br></center>";
	Dialog thisDialog;
	
	public AboutDialog()
	{
		thisDialog = this;
		setCanDragReposition(true);  
		setCanDragResize(false);
		setSize("400px", "100px");
		setTitle("О программе");
		setBodyColor("rgb(253, 253, 253)");
		setHoverMoveWithMouse(true);
		setAutoSize(true);		
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		
		Map<String, Object> headerDefaults = new HashMap<String, Object>();
		Integer newHeight = 30;
		headerDefaults.put("height", newHeight);
		setAttribute("headerDefaults", headerDefaults, true);
		
		this.addItem(createLayout());
		
		show();
	}
	
	private VLayout createLayout()
	{
		final VLayout vLayoutMain = new VLayout();
		vLayoutMain.setWidth100();
		vLayoutMain.setHeight(50);
		vLayoutMain.setAlign(Alignment.CENTER);
		vLayoutMain.setMembersMargin(5);
		
		Label label = new Label();
		label.setContents(contents);
		vLayoutMain.addMember(label);
		
		HLayout hLayoutButton = new HLayout();
		IButton button = new IButton("OK");
		button.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				thisDialog.destroy();
			}});
		button.setWidth("100px");
		button.setAlign(Alignment.CENTER);
		hLayoutButton.setAlign(Alignment.CENTER);
		hLayoutButton.addMember(button);
		hLayoutButton.setWidth100();
		hLayoutButton.setHeight("40px");
		vLayoutMain.addMember(hLayoutButton);
		
		return vLayoutMain;
	}
}
