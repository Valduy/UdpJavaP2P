package pong.host;

import game.GameObject;
import pong.AABBComponent;
import pong.InputComponent;
import pong.VelocityComponent;

public class Racket extends GameObject {
    private final AABBComponent aabb = new AABBComponent();
    private final VelocityComponent velocity = new VelocityComponent();
    private final InputComponent inputs = new InputComponent();

    @Override
    public void start() {
        super.start();

    }

    @Override
    public void Update(long dt) {
        super.Update(dt);
    }
}
