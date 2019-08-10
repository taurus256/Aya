package org.taurus.aya.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/** Класс перечислений для хранения состояния совета.
* Помимо собственно состояния, может возвращать CSS-стройку стиля, которым совет
*  должен быть отображен
* */
public enum AdviceState implements IsSerializable {

        OK("s3_event_ready"),
        WARNING("s3_event_process"),
        CRITICAL("s3_event_fail");

        AdviceState(String style){
                this.style = style;
        }

        private String style;

        /** Возвращает стиль совета
        * @return класс CSS для отображения совета
        */
        public String getStyleName() {
                return style;
        }
}
