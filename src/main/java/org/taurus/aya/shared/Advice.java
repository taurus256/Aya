package org.taurus.aya.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Advice implements IsSerializable {

    private String description;
    private AdviceState state;

    public Advice(){};

    public Advice(AdviceState state, String description)
    {
        this.state = state;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AdviceState getState() {
        return state;
    }

    public void setState(AdviceState state) {
        this.state = state;
    }
}
