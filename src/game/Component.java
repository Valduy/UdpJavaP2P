package game;

public abstract class Component {
    private GameObject gameObject;

    public GameObject getContext(){
        return gameObject;
    }

    public void setContext(GameObject gameObject){
        this.gameObject = gameObject;
    }

    public void Update(long dt){

    }
}
