public class Unit{
  private ArrayList<Unit> connections;
  private boolean on;
  private Unit(int level){
    on = false;
  }
  public void addConnection(Unit connect){
    connections.append(connect);
  }
  public ArrayList<Unit> getConnections(){
    return connnections;
  }
  public void turnOn(){
    on = true;
  }
  public boolean isOn(){
    return on;
  }

}
