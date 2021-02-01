package com.itorix.hyggee.mockserver.ui;

import com.itorix.hyggee.mockserver.filters.MockServerEventLog;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;
import com.itorix.hyggee.mockserver.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *   
 */
public class MockServerEventLogNotifier extends ObjectWithReflectiveEqualsHashCodeToString {

    private final List<MockServerLogListener> listeners = Collections.synchronizedList(new ArrayList<MockServerLogListener>());
    private final Scheduler scheduler;

    public MockServerEventLogNotifier(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    protected void notifyListeners(final MockServerEventLog notifier) {
        scheduler.submit(
            new Runnable() {
                public void run() {
                    for (MockServerLogListener listener : new ArrayList<>(listeners)) {
                        listener.updated(notifier);
                    }
                }
            });

    }

    public void registerListener(MockServerLogListener listener) {
        listeners.add(listener);
    }
}
