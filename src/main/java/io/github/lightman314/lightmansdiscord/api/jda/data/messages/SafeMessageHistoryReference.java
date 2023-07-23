package io.github.lightman314.lightmansdiscord.api.jda.data.messages;

import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nullable;
import java.util.List;

public class SafeMessageHistoryReference {

    private final MessageHistory history;
    public final MessageHistory getHistory() { return this.history; }
    private SafeMessageHistoryReference(MessageHistory history) { this.history = history; }
    @Nullable
    public static SafeMessageHistoryReference of(MessageHistory history) { if(history != null) return new SafeMessageHistoryReference(history); return null; }

    public final int size() { return this.history.size(); }
    public final boolean isEmpty() { return this.history.isEmpty(); }
    public final RestAction<List<SafeMessageReference>> retrievePast(int amount) { return this.history.retrievePast(amount).map(r -> r.stream().map(SafeMessageReference::of).toList()); }
    public final RestAction<List<SafeMessageReference>> retrieveFuture(int amount) { return this.history.retrieveFuture(amount).map(r -> r.stream().map(SafeMessageReference::of).toList()); }

    public final List<SafeMessageReference> getRetrievedHistory() { return this.history.getRetrievedHistory().stream().map(SafeMessageReference::of).toList(); }

}
