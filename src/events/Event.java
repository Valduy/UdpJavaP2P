package events;

@FunctionalInterface
public interface Event<TEventArgs> {
    void invoke(Object source, TEventArgs eventArgs);
}
