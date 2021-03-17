package pong.components;

import game.Component;

public class InputComponent extends Component {
    private boolean isUp;
    private boolean isDown;

    public boolean getIsUp(){
        return isUp;
    }

    public void setIsUp(boolean isUp){
        this.isUp = isUp;
    }

    public boolean getIsDown(){
        return isDown;
    }

    public void setIsDown(boolean isDown){
        this.isDown = isDown;
    }

    @Override
    public void update(long dt) {
        super.update(dt);
    }
}
