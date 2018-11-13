import java.util.ArrayList;
public class Unit{
     private ArrayList<Unit> excitatoryConnections;
     private ArrayList<Unit> inhibitoryConnections;
     private int level;
     private String word;
     private double activation;
     private double avgActivation;
     private static final double oRate = 0.5;
     private double netInput;
     private int language;

     public Unit(int level){
          this(level, null);
     }
     public Unit(int level, String word){
          this.word=word;
          this.level=level;
          avgActivation = 0.0;
          activation = 0.0;
          netInput = 0.0;
          excitatoryConnections=new ArrayList<Unit>();
          inhibitoryConnections=new ArrayList<Unit>();
     }
     /*public Unit(int level, String word, int language){
          this.language=language;
          this.word=word;
          this.level=level;
          activation = 0.0;
          netInput = 0.0;
          excitatoryConnections=new ArrayList<Unit>();
          inhibitoryConnections=new ArrayList<Unit>();
     }*/
     public void addConnection(Unit connect, boolean exc){
          if(exc){
               excitatoryConnections.add(connect);
          }else{
               inhibitoryConnections.add(connect);
          }
     }
     public ArrayList<Unit> getEConnections(){
          return excitatoryConnections;
     }
     public ArrayList<Unit> getIConnections(){
          return inhibitoryConnections;
     }
     public double getActivation(){
          return activation;
     }
     public void addActivation(double a){
          activation += a;
          updateRunningAverage();
     }
     private void updateRunningAverage(){
          avgActivation = (oRate * activation) + ((1-oRate) * avgActivation);
     }
     public double getResponseStrength(double oscale){
          return Math.exp(oscale * avgActivation);
     }
     public void setActivation(double a){
          activation = a;
     }
     public void addInput(double i){
          netInput += i;
     }
     public void setInput(double i){
          netInput = i;
     }
     public double getNet(){
          return netInput;
     }
     public int getLevel(){
          return level;
     }
     public String getWord() throws Exception{
          if (level!=2){
               throw new Exception("Not on the word level");
          }
          return word;
     }
     public char getLetter(int position) throws Exception{
          if (level!=2){
               throw new Exception("Not on the word level");
          }
          if (position>3){
               throw new IndexOutOfBoundsException();
          }
          return word.charAt(position);
     }

}
