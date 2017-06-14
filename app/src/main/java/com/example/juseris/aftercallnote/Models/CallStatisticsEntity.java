package com.example.juseris.aftercallnote.Models;

/**
 * Created by juseris on 12/8/2016.
 */

public class CallStatisticsEntity {
    private String number;
    private Integer IncomingCallCount;
    private Integer OutgoingCallCount;
    private Integer TypedNoteCount;
    private Integer RemindersAddedCount;
    private Integer OutgoingTimeTotal;
    private Integer IncomingTimeTotal;

    public CallStatisticsEntity() {
    }

    public CallStatisticsEntity(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getRemindersAddedCount() {
        return RemindersAddedCount;
    }

    public void setRemindersAddedCount(Integer remindersAddedCount) {
        RemindersAddedCount = remindersAddedCount;
    }

    public Integer getTypedNoteCount() {
        return TypedNoteCount;
    }

    public void setTypedNoteCount(Integer typedNoteCount) {
        TypedNoteCount = typedNoteCount;
    }

    public Integer getOutgoingCallCount() {
        return OutgoingCallCount;
    }

    public void setOutgoingCallCount(Integer outgoingCallCount) {
        OutgoingCallCount = outgoingCallCount;
    }

    public Integer getIncomingCallCount() {
        return IncomingCallCount;
    }

    public void setIncomingCallCount(Integer incomingCallCount) {
        IncomingCallCount = incomingCallCount;
    }

    public Integer getOutgoingTimeTotal() {
        return OutgoingTimeTotal;
    }

    public void setOutgoingTimeTotal(Integer outgoingTimeTotal) {
        OutgoingTimeTotal = outgoingTimeTotal;
    }

    public Integer getIncomingTimeTotal() {

        return IncomingTimeTotal;
    }

    public void setIncomingTimeTotal(Integer incomingTimeTotal) {
        IncomingTimeTotal = incomingTimeTotal;
    }
}
