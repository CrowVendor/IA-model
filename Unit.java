public class Unit{
  private ArrayList<doubles> connections;
  public Unit(int level){
    on = false;
  }
  public void addConnection(double connect){
    connections.append(connect);
  }
  public ArrayList<Unit> getConnections(){
    return connnections;
  }

}
