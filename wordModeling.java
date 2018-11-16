import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
public class wordModeling{
     public static void main(String[] args){
          interactiveActivation ia = new interactiveActivation("totalCombined.txt");
          bilingualInteractiveActivation bia = new bilingualInteractiveActivation("combined_possibles.txt", "combined_dutch_possibles.txt");
          ArrayList<String> files = new ArrayList<>(Arrays.asList("smallEnglish","largeEnglish","smallDutch","largeDutch"));
          testResponses(ia, bia, files);
          //testSequenceResponses(ia, bia, files);

          //System.out.println(ia.responseTimes(testwords, 20));
          //ia.modelWord("aunt", 50, 10);
          //System.out.println(bia.responseTimes(testwords, .9, 1));
          //bia.modelWord("aunt", 50, 10);
     }
     private static void testSequenceResponses(interactiveActivation ia, bilingualInteractiveActivation bia, ArrayList<String> files){
          ArrayList<String> EngWords = parseFile(files.get(0), "english");
          EngWords.addAll(parseFile(files.get(1), "english"));
          //EngWords.addAll(parseFile(files.get(2), "english"));
          //EngWords.addAll(parseFile(files.get(3), "english"));
          ArrayList<String> DutchWords = parseFile(files.get(0), "dutch");
          DutchWords.addAll(parseFile(files.get(1), "dutch"));
          //DutchWords.addAll(parseFile(files.get(2), "dutch"));
          //DutchWords.addAll(parseFile(files.get(3), "dutch"));
          ArrayList<String> MixedWords = new ArrayList<String>();
          for(int i = 0; i<EngWords.size(); i=i+2){
               MixedWords.add(EngWords.get(i));
               MixedWords.add(DutchWords.get(i+1));
          }
          System.out.println(ia.modelSequenceResponseTimes(EngWords, .9));
          System.out.println(ia.modelSequenceResponseTimes(DutchWords, .9));
          System.out.println(ia.modelSequenceResponseTimes(MixedWords, .9));
          System.out.println(bia.modelSequenceResponseTimes(EngWords, .9, 1));
          System.out.println(bia.modelSequenceResponseTimes(DutchWords, .9, 2));
          System.out.println(bia.modelSequenceResponseTimes(MixedWords, .9, 0));


     }
     private static void testResponses(interactiveActivation ia, bilingualInteractiveActivation bia, ArrayList<String> files){
          ArrayList<Integer> languages = new ArrayList<>(Arrays.asList(1,1,2,2));
          for(String filename : files){
               ArrayList<String> testwords = parseFile(filename, "dutch");
               System.out.println("IA-"+filename+": "+ia.responseTimes(testwords, .9, 2));
               System.out.println("BIA-"+filename+": "+bia.responseTimes(testwords, .9));
          }
     }
     private static ArrayList<String> parseFile(String filename, String folder){
          ArrayList<String> testwords = new ArrayList<String>();
          try{
               Scanner testScan = new Scanner(new File("input/"+folder+"/"+filename+".txt"));
               while(testScan.hasNext()){
                    testwords.add(testScan.next());
               }
          } catch (Exception e){
               System.out.println(e);
               System.exit(1);
          }
          return testwords;
     }
}
