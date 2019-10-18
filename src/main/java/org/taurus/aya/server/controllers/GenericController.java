package org.taurus.aya.server.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** SmartGWT sends string 'null' instead of null value.
 *  Methods below created for setting proper values in these cases
 * */
public class GenericController {

    //Format date based on template (old): 2019-07-19T03:12:27.000 - "yyyy-MM-dd'T'HH:mm:ss"
    //Now use short format for date-only
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());



    protected String filterStringValue(String  value) {
        return value == null || value.equals("null") ? null : value;
    }

    protected Integer filterIntValue(String value) {

        return value == null || value.equals("null") ? null : Integer.valueOf(value);
    }

    protected Boolean filterBooleanValue(String value) {
        return value == null || value.equals("null") ? null : Boolean.valueOf(value);
    }

    protected Long filterLongValue(String value) {
        return value == null || value.equals("null") ? null : Long.valueOf(value);
    }

    protected Date filterDateValue(String value) throws ParseException {
        return value == null || value.equals("null") ? null : formatter.parse(value);
    }
}
