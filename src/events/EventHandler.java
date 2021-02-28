package events;

import java.util.ArrayList;

public class EventHandler <TEventArgs>{
    private ArrayList<Event<TEventArgs>> eventDelegateArray = new ArrayList<>();

    public void subscribe(Event<TEventArgs> methodReference){
        eventDelegateArray.add(methodReference);
    }

    public void unSubscribe(Event<TEventArgs> methodReference){
        eventDelegateArray.remove(methodReference);
    }

    public void invoke(Object source, TEventArgs eventArgs){
        eventDelegateArray.forEach(p -> p.invoke(source, eventArgs));
    }

    public void close(){
        eventDelegateArray.clear();
    }
}
