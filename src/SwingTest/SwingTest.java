package SwingTest;

import events.Event;
import events.EventArgs;
import events.EventHandler;

import javax.swing.*;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class View extends JFrame{
    private JTextArea textArea;

    public View(){
        setSize(400, 400);
        setLayout(new GridLayout(1, 1));
        textArea = new JTextArea();
        add(textArea);
    }

    public void append(String data){
        textArea.append(data);
    }
}

class Model{
    class PrimeNumbersTask extends SwingWorker<Void, String> {
        private final Model model;

        PrimeNumbersTask(Model model) {
            this.model = model;
        }

        @Override
        public Void doInBackground() throws IOException {
            var reader = new BufferedReader(new InputStreamReader(System.in));

            while (true){
                publish(reader.readLine());
            }
        }

        @Override
        protected void process(List<String> chunks) {
            for (var s : chunks) {
                model.setCurrentString(s);
                model.received.invoke(model, new EventArgs());
            }
        }
    }

    private final EventHandler<EventArgs> received = new EventHandler<>();
    private String currentString;

    protected void setCurrentString(String number){
        currentString = number;
    }

    public String getCurrentString(){
        return currentString;
    }

    public void addReceived(Event<EventArgs> methodReference){
        received.subscribe(methodReference);
    }

    public void start(){
        var worker = new PrimeNumbersTask(this);
        worker.execute();
    }
}

class Presenter{
    private final View view;
    private final Model model;

    public Presenter(View view, Model model){
        this.view = view;
        this.model = model;
        model.addReceived(this::onReceived);
        model.start();
    }

    private void onReceived(Object sender, EventArgs e){
        view.append(model.getCurrentString());
    }
}

public class SwingTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            var view = new View();
            var model = new Model();
            var presenter = new Presenter(view, model);
            view.setVisible(true);
        });
    }
}
