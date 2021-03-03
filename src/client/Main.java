package client;

import client.views.interfaces.ChildView;

public class Main {
    class TC{

    }

    class TCE extends TC{

    }

    static class TT<T extends TC>{
        public void test(Class<T> c){}
    }

    public static void main(String[] args) {
        //new MainFrame();
        var tt = new TT();
        tt.test(TCE.class);
        System.out.print(ChildView.class instanceof Object);
    }
}
