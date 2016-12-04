package application.beans;

import framework.core.annotations.Service;

@Service
public class ServiceExample {
    private String msg = "Service Example";

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ServiceExample{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
