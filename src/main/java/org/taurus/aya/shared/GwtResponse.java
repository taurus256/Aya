package org.taurus.aya.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GwtResponse  {

    public GwtResponse(Integer startRow, Integer endRow, Integer totalRows, Object data)
    {
        response = new ResponseContainer(startRow, endRow, totalRows, data);
    }

    public ResponseContainer getResponse() {
        return response;
    }

    public void setResponse(ResponseContainer response) {
        this.response = response;
    }

    private ResponseContainer response;
}
