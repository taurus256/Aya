package org.taurus.aya.client.widgets;


import com.smartgwt.client.data.AdvancedCriteria;
import com.smartgwt.client.data.Criterion;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import org.taurus.aya.client.generic.GenericPanel;

import java.util.Date;
import java.util.HashMap;

public class FilterWidget extends HLayout {
	
	private SelectItem selectItem;
	
	public FilterWidget(final GenericPanel panel)
	{
		Img img = new Img("forms/filter.png",24,24);
		img.setPadding(0);
		
		DynamicForm df = new DynamicForm();
		df.setMargin(0);
		df.setPadding(0);
		df.setWidth100();
				
		selectItem = new SelectItem();
		selectItem.setTitle("Решенные");
		
		HashMap<String,String> valueMap = new HashMap<String,String>();
		valueMap.put("no", "не показывать");
		valueMap.put("week", "за неделю");
		valueMap.put("month", "за месяц");
		valueMap.put("year", "за год");
		valueMap.put("all", "за всё время");
		selectItem.setValueMap(valueMap);
		selectItem.setDefaultValue("no");
		selectItem.setWidth(125);
		selectItem.setTooltip("Фильтр, задающий время, за которое отображаются решенные задачи. <br> Используется, чтобы разгрузить список задач");

		selectItem.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				AdvancedCriteria dateCriteria, activeTasksCriteria;
				
				if (((String)event.getValue()).equals("no")) 
				{
					//выбрать только активные задачи 
					activeTasksCriteria = new AdvancedCriteria("state", OperatorId.NOT_EQUAL,3);
					//критерий выборки: использовать выборку для данного виджета + выбирать все активные 
					panel.setSearchCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{(AdvancedCriteria)panel.getBaseCriteria(),activeTasksCriteria}));
				}
				
				if (((String)event.getValue()).equals("week")) 
				{
					dateCriteria = new AdvancedCriteria("startDate", OperatorId.GREATER_THAN, new Date((new Date()).getTime() - 1000*3600*24*7L));
					//выбрать либо активные задачи, либо завершенные и созданные не далее определенного времени 
					activeTasksCriteria = new AdvancedCriteria(OperatorId.OR, new Criterion[]{new AdvancedCriteria("state", OperatorId.NOT_EQUAL,3), new AdvancedCriteria(OperatorId.AND, new Criterion[]{new AdvancedCriteria("state", OperatorId.EQUALS,3), dateCriteria})});
					//критерий выборки: использовать выборку для данного виджета + выбирать все активные + неактивные задачи по дате
					panel.setSearchCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{(AdvancedCriteria)panel.getBaseCriteria(),activeTasksCriteria}));
				}
				
				if (((String)event.getValue()).equals("month")) 
				{
					dateCriteria = new AdvancedCriteria("startDate", OperatorId.GREATER_THAN, new Date((new Date()).getTime() - 1000*3600*24*30L));
					//выбрать либо активные задачи, либо завершенные и созданные не далее определенного времени 
					activeTasksCriteria = new AdvancedCriteria(OperatorId.OR, new Criterion[]{new AdvancedCriteria("state", OperatorId.NOT_EQUAL,3), new AdvancedCriteria(OperatorId.AND, new Criterion[]{new AdvancedCriteria("state", OperatorId.EQUALS,3), dateCriteria})});
					//критерий выборки: использовать выборку для данного виджета + выбирать все активные + неактивные задачи по дате
					panel.setSearchCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{(AdvancedCriteria)panel.getBaseCriteria(),activeTasksCriteria}));
				}
				
				if (((String)event.getValue()).equals("year")) 
				{
					dateCriteria = new AdvancedCriteria("startDate", OperatorId.GREATER_THAN, new Date((new Date()).getTime() - 1000*3600*24*365L));
					//выбрать либо активные задачи, либо завершенные и созданные не далее определенного времени 
					activeTasksCriteria = new AdvancedCriteria(OperatorId.OR, new Criterion[]{new AdvancedCriteria("state", OperatorId.NOT_EQUAL,3), new AdvancedCriteria(OperatorId.AND, new Criterion[]{new AdvancedCriteria("state", OperatorId.EQUALS,3), dateCriteria})});
					//критерий выборки: использовать выборку для данного виджета + выбирать все активные + неактивные задачи по дате
					panel.setSearchCriteria(new AdvancedCriteria(OperatorId.AND, new Criterion[]{(AdvancedCriteria)panel.getBaseCriteria(),activeTasksCriteria}));
				}
				
				if (((String)event.getValue()).equals("all")) 
				{
					panel.setSearchCriteria(panel.getBaseCriteria());
				}
				panel.update();
			}});
		
		df.setFields(selectItem);

		
		this.addMember(img);
		this.addMember(df);
		this.setHeight(26);
		this.setWidth100();
		this.setAlign(VerticalAlignment.BOTTOM);
		
		setMargin(0);
		setPadding(0);
		
		this.draw();
	}
	
	public FilterWidget setDefaultValue(String value)
	{
		selectItem.setDefaultValue(value);
		return this;
	}
	
}

