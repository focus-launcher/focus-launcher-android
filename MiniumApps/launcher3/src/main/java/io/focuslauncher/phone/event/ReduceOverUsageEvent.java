package io.focuslauncher.phone.event;

public class ReduceOverUsageEvent {

    private boolean isStartEvent;

    public ReduceOverUsageEvent(boolean isStartEvent) {
        this.isStartEvent = isStartEvent;
    }

    public boolean isStartEvent() {
        return isStartEvent;
    }
}
