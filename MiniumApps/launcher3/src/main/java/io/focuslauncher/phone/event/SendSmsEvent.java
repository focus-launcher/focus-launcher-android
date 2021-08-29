package io.focuslauncher.phone.event;

/**
 * Created by rajeshjadi on 19/8/17.
 */

public class SendSmsEvent {
    private boolean isSendSms = false;
    private String strNumber;
    private String strMessage;
    private boolean isClearList = false;

    public SendSmsEvent(boolean isClearList) {
        this.isClearList = isClearList;
    }

    public SendSmsEvent(boolean sendSms, String strNumber, String strMessage) {
        this.isSendSms = sendSms;
        this.strNumber = strNumber;
        this.strMessage = strMessage;
    }

    public boolean isClearList() {
        return isClearList;
    }

    public void setClearList(boolean clearList) {
        isClearList = clearList;
    }

    public boolean isSendSms() {
        return isSendSms;
    }

    public void setSendSms(boolean sendSms) {
        isSendSms = sendSms;
    }

    public String getStrNumber() {
        return strNumber;
    }

    public void setStrNumber(String strNumber) {
        this.strNumber = strNumber;
    }

    public String getStrMessage() {
        return strMessage;
    }

    public void setStrMessage(String strMessage) {
        this.strMessage = strMessage;
    }
}
