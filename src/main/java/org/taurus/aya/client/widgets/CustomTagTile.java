package org.taurus.aya.client.widgets;

import com.smartgwt.client.bean.BeanFactory;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.ValueIconMapper;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;


@BeanFactory.Generate
public class CustomTagTile extends DynamicForm {
	
	CustomTagTile thisTile = null;
	
	public CustomTagTile()
	{
		SC.logWarn("CustomTagTile: enter to constructor!");
		setOverflow(Overflow.HIDDEN);
		setHeight(100);
		setWidth(300);
		//setMargin(0);
		setPadding(0);
        setCanFocus(false);


        /*StaticTextItem linksIdField = new StaticTextItem("id");
        linksIdField.setAttribute("primaryKey",true);
        linksIdField.setAttribute("hidden",true);
        linksIdField.setVisible(false);*/
		
		StaticTextItem pictureItem = new StaticTextItem("image");
		pictureItem.setHeight(100);
		pictureItem.setWidth(100);
		pictureItem.setShowTitle(false);  
	    pictureItem.setCanEdit(false);
        pictureItem.setShowValueIconOnly(true);  
        pictureItem.setValueIconHeight(64);  
        pictureItem.setValueIconWidth(64);
        pictureItem.setRowSpan(2);
        pictureItem.setImageURLPrefix("filetype/");
        pictureItem.setAlign(Alignment.CENTER);
        pictureItem.setValueIconMapper(new ValueIconMapper() {
            @Override  
            public String getValueIcon(Object value) {  
                return String.valueOf(value);  
            }  
        }); 
        
        StaticTextItem nameField = new StaticTextItem("name");
        nameField.setCanEdit(false);
        nameField.setCanFocus(false);
        nameField.setCanSelectText(false);
        nameField.setShowTitle(false);
        nameField.setWidth(200);
        //nameField.setClipStaticValue(true);
        nameField.setAlign(Alignment.LEFT);
        nameField.setTextAlign(Alignment.LEFT);

        
        setFields(/*linksIdField,*/pictureItem,nameField);
	}
}
