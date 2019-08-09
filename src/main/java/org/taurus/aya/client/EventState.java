package org.taurus.aya.client;

public enum EventState {
    NEW("Сброс"),
    PROCESS("Начать выполнение"),
    READY("Задача завершена"),
    PAUSE("Пауза"),
    FAIL("Обратить внимание");

    private String name;

    EventState(String name){
        this.name = name;
    }


    public String getName(){
        return name;
    };

}
