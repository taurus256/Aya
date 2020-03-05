package org.taurus.aya.client;

public enum EventState {
    NEW("Сброс"),
    PROCESS("Начать"),
    READY("Готово"),
    PAUSE("Пауза"),
    FAIL("Внимание");

    private String name;

    EventState(String name){
        this.name = name;
    }


    public String getName(){
        return name;
    };

}
