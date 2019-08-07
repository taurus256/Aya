package org.taurus.aya.server.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenericController {

    //Format date based on template: 2019-07-19T03:12:27.000
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

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
