package tests.game;

import game.Component;
import game.GameObject;
import game.GameWorld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameWorldTest {
    public class TestComponent extends Component{
        private double sum;

        public double getSum() {
            return sum;
        }

        public void setSum(double sum) {
            this.sum = sum;
        }
    }

    private class TestGameObject extends GameObject{

        @Override
        public void Update(long dt) {
            var component = (TestComponent)getComponent(TestComponent.class);
            component.setSum(component.getSum() + dt);
        }
    }

    @Test
    public void UpdateTest(){
        var world = new GameWorld();
        var go = new TestGameObject();
        var component = new TestComponent();
        go.addComponent(TestComponent.class, component);
        world.addGameObject(go);
        var dt = 1;
        world.update(dt);
        assertEquals(component.getSum(), dt);
    }
}
