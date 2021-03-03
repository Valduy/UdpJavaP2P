package tests.events;

import events.Event;
import events.EventArgs;
import events.EventHandler;
import org.junit.jupiter.api.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventHandlerTest {
    private static EventHandler<EventArgs> testEvent;
    public static Boolean[] invokations;

    @BeforeAll
    public static void setUp(){
        testEvent = new EventHandler<>();
    }

    @AfterEach
    public void afterEach(){
        testEvent.close();
        invokations = null;
    }

    @Test
    public void testInvoke(){
        invokations = new Boolean[]{false, false};
        testEvent.subscribe((o, e) -> invokations[0] = true);
        testEvent.subscribe((o, e) -> invokations[1] = true);
        testEvent.invoke(this, new EventArgs());
        assertTrue(Stream.of(invokations).allMatch(i -> i));
    }

    @Test
    public void testInvokeAfterClose(){
        invokations = new Boolean[]{false, false};
        testEvent.subscribe((o, e) -> invokations[0] = true);
        testEvent.subscribe((o, e) -> invokations[1] = true);
        testEvent.close();
        testEvent.invoke(this, new EventArgs());
        assertTrue(Stream.of(invokations).noneMatch(i -> i));
    }

    @Test
    public void testInvokeAfterUnSubscribe(){
        invokations = new Boolean[]{false, false};
        testEvent.subscribe((o, e) -> invokations[0] = true);
        Event<EventArgs> action = (o, e) -> invokations[1] = true;
        testEvent.subscribe(action);
        testEvent.unSubscribe(action);
        testEvent.invoke(this, new EventArgs());
        assertTrue(invokations[0]);
        assertFalse(invokations[1]);
    }
}
