package org.maestro.client;

import org.maestro.common.client.callback.MaestroNoteCallback;
import org.maestro.client.exchange.MaestroMonitor;
import org.maestro.common.client.notes.MaestroNote;

import java.util.List;
import java.util.function.Predicate;

public interface Collector {
    void monitor(final MaestroMonitor monitor);
    void remove(final MaestroMonitor monitor);
    List<MaestroNote> collect(Predicate<? super MaestroNote> predicate);
    void addCallback(MaestroNoteCallback callback);
    void removeCallback(MaestroNoteCallback callback);
    void clear();
}
