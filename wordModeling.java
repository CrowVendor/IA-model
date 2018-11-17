import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
public class wordModeling{
     /* Takes command lne input for type of analysis and parameters to run on
      * an IA and/or BIA model
      * java wordModeling sequence output_file
      * java wordModeling response language_int
      * java wordModeling word inputWord num_cycles num_results output_file
      */
     public static void main(String[] args){
          String option = args[0];
          interactiveActivation ia = new interactiveActivation("totalCombined.txt");
          bilingualInteractiveActivation bia = new bilingualInteractiveActivation("combined_possibles.txt", "combined_dutch_possibles.txt");
          ArrayList<String> files = new ArrayList<>(Arrays.asList("smallEnglish","largeEnglish","smallDutch","largeDutch"));
          if(option.equals("sequence")){
               testSequenceResponses(ia, bia, files, args[1]);
          } else if (option.equals("response")){
               int language = Integer.parseInt(args[1]);
               testResponses(ia, bia, files, language);
          } else if (option.equals("word")){
               String type = args[1];
               String input = args[2];
               int cycles = Integer.parseInt(args[3]);
               int num_results = Integer.parseInt(args[4]);
               String output = args[5];
               if(type.equals("ia")){
                    ia.modelWord(input, cycles, num_results, output);
               } else if (type.equals("bia")){
                    bia.modelWord(input, cycles, num_results, output);
               } else {
                    System.out.println("invalid type");
               }
          } else {
               System.out.println("invalid input");
          }
     }
     /* Runs sequences of words taken from files of dutch and english words
      * into an uncleared IA and BIA model
      */
     private static void testSequenceResponses(interactiveActivation ia, bilingualInteractiveActivation bia, ArrayList<String> files, String output){
          ArrayList<String> EngWords = parseFile(files.get(0), "english");
          EngWords.addAll(parseFile(files.get(1), "english"));
          ArrayList<String> DutchWords = parseFile(files.get(0), "dutch");
          DutchWords.addAll(parseFile(files.get(1), "dutch"));
          ArrayList<String> MixedWords = new ArrayList<String>();
          for(int i = 0; i<EngWords.size(); i=i+2){
               MixedWords.add(EngWords.get(i));
               MixedWords.add(DutchWords.get(i+1));
          }
          try{
               FileWriter outputFile = new FileWriter(new File(output));
               outputFile = new FileWriter(new File(output), true);
               outputFile.write("Eng IA: "+ia.modelSequenceResponseTimes(EngWords, .9)+"\n");
               outputFile.write("Dutch IA: "+ia.modelSequenceResponseTimes(DutchWords, .9)+"\n");
               outputFile.write("Mixed IA: "+ia.modelSequenceResponseTimes(MixedWords, .9)+"\n");
               outputFile.write("Eng BIA: "+bia.modelSequenceResponseTimes(EngWords, .9, 1)+"\n");
               outputFile.write("Dutch BIA: "+bia.modelSequenceResponseTimes(DutchWords, .9, 2)+"\n");
               outputFile.write("Mixed BIA: "+bia.modelSequenceResponseTimes(MixedWords, .9, 0)+"\n");
               outputFile.flush();
               outputFile.close();
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
     }
     /* Computes average runtimes for four files of varying inputs for both IA
      * and BIA models, in a specified language.
      */
     private static void testResponses(interactiveActivation ia, bilingualInteractiveActivation bia, ArrayList<String> files, int language){
          ArrayList<Integer> languages = new ArrayList<>(Arrays.asList(1,1,2,2));
          for(String filename : files){
               ArrayList<String> testwords=null;
               if (language == 1){
                    testwords = parseFile(filename, "english");
               } else if (language == 2){
                    testwords = parseFile(filename, "dutch");
               } else {
                    System.out.println("invalid language");
                    System.exit(1);
               }
               System.out.println("IA-"+filename+": "+ia.responseTimes(testwords, .9));
               System.out.println("BIA-"+filename+": "+bia.responseTimes(testwords, .9, language));
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
