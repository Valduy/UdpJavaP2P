package SwingTest;

import events.Event;
import events.EventArgs;
import events.EventHandler;

import javax.swing.*;
import java.awt.GridLayout;
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
    class PrimeNumbersTask extends SwingWorker<List<Integer>, Integer> {
        private final int numbersToFind = 10;
        private final Model model;

        PrimeNumbersTask(Model model) {
            this.model = model;
        }

        @Override
        public List<Integer> doInBackground() {
            final List<Integer> result = new ArrayList<>();
            boolean interrupted = false;
            for (int i = 0; !interrupted && (i < numbersToFind); i += 2) {
                interrupted = doIntenseComputing();
                result.add(i);
                publish(i); // sends data to process function
            }
            return result;
        }

        private boolean doIntenseComputing() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
            return false;
        }

        @Override
        protected void process(List<Integer> chunks) {
            for (int number : chunks) {
                model.setCurrentNumber(number);
                model.received.invoke(model, new EventArgs());
            }
        }
    }

    private final EventHandler<EventArgs> received = new EventHandler<>();
    private int currentNumber;

    protected void setCurrentNumber(int number){
        currentNumber = number;
    }

    public int getCurrentNumber(){
        return currentNumber;
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
        view.append(Integer.toString(model.getCurrentNumber()));
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
