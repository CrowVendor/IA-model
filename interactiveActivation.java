import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Collections;
public class interactiveActivation{
     private static final double alphaFL = 0.005;
     private static final double alphaLW = 0.07;
     private static final double alphaWL = 0.3;
     private static final double gammaFL = 0.15;
     private static final double gammaLW = 0.04;
     private static final double gammaWW = 0.21;
     private static final double gammaLL = 0;

     private static final double max = 1.0;
     private static final double min = -0.2;
     private static final double decay = 0.07;
     private static final double oscaleW = 20;
     private static final double oscaleL = 10;

     private static final String L_SEG = "letter_segmentation.txt";
     private String W_FILE;// = "totalCombined.txt";//"combined_possibles.txt";//"testwords.txt";
     private ArrayList<boolean[]> uc;
     private String[] lexicon;
     private ArrayList<ArrayList<Unit>> featureLevel;
     private ArrayList<ArrayList<Unit>> letterLevel;
     private ArrayList<Unit> wordLevel;
     private static final int WLEN = 4;

     public interactiveActivation(String lex){
          this.W_FILE = lex;
          uc = new ArrayList<boolean[]>();
          loadSegs();
          instantiateNetwork();
     }

     public ArrayList<Integer> modelSequenceResponseTimes(ArrayList<String> words, double threshold){
          clearNetwork();
          ArrayList<Integer> results = new ArrayList<Integer>();
          for (String word : words){
               results.add(modelResponseTime(word, threshold));
          }
          return results;
     }
     public double responseTimes(ArrayList<String> words, double threshold){
          double totalAverage=0;
          for(String word : words){
               double average = modelResponseTime(word, threshold);
               clearNetwork();
               totalAverage += average;
          }
          return totalAverage/words.size();
     }
     private int modelResponseTime(String word, double threshold){
          Unit wordUnit=null;
          try{
               for(Unit wordUnitTemp : wordLevel){
                    if (wordUnitTemp.getWord().equals(word)){
                         wordUnit=wordUnitTemp;
                    }
               }
          } catch (Exception e) {
               System.out.println(e);
               System.exit(1);
          }
          if(wordUnit==null){
               System.out.println("Exception: word: "+word+" not in lexicon");
               System.exit(1);
          }
          boolean[][] input = loadWord(word);
          double response=0;
          double probability=0;
          int cycles = 0;
          while(probability<threshold){
               interact(input);
               double totalResponse = 0.0;
               for(Unit wordUnitTemp : wordLevel){
                    totalResponse+=wordUnitTemp.getResponseStrength(oscaleW);
               }
               response = wordUnit.getResponseStrength(oscaleW);
               probability = response/totalResponse;
               cycles++;
          }
          return cycles;
     }
     public void modelWord(String word, int cycles, int num_results){
          ArrayList<ArrayList<Double>> results = new ArrayList<ArrayList<Double>>();
          boolean[][] input = loadWord(word);

          for(int i = 0; i<cycles; i++){
               interact(input);
               results.add(getActivationOutput());
               //writeOutput("testresults.txt");
          }
          output(results, num_results);

     }
     private void output(ArrayList<ArrayList<Double>> results, int num_results){
          try{
               BufferedWriter bw = new BufferedWriter(new FileWriter("testresults.txt"));
               bw.close();
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
          ArrayList<Double> finalResults = results.get(results.size()-1);
          ArrayList<Integer> indices = new ArrayList<Integer>();
          for(int i =0; i<finalResults.size(); i++){
               indices.add(i);
          }
          Collections.sort(indices, new unitComparator(finalResults));
          ArrayList<ArrayList<Double>> top_results = new ArrayList<ArrayList<Double>>();
          ArrayList<String> top_words = new ArrayList<String>();
          for(int i=0; i < num_results;i++){
               ArrayList<Double> top_result = new ArrayList<Double>();
               int index = indices.get(indices.size()-i-1);
               top_words.add(lexicon[index]);
               for(int j=0; j<results.size();j++){
                    top_result.add(results.get(j).get(index));
               }
               top_results.add(top_result);
          }
          writeOutput(top_results, "testresults.txt", top_words);

     }
     private void writeOutput(ArrayList<ArrayList<Double>> top_results, String filename, ArrayList<String> top_words){
          try{
               BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
               for(int i=0;i<top_results.size();i++){
                    bw.append(top_words.get(i));
                    for(double result : top_results.get(i)){
                         bw.append(", "+result);
                    }
                    bw.newLine();
                    bw.flush();
               }
               bw.close();
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
     }
     private ArrayList<Double> getActivationOutput(){
          ArrayList<Double> results = new ArrayList<Double>();
          for(Unit word : wordLevel){
               results.add(word.getActivation());
          }
          return results;
     }
     private ArrayList<Double> getResponseStrengthOutput(){
          ArrayList<Double> results = new ArrayList<Double>();
          for(Unit word : wordLevel){
               results.add(word.getResponseStrength(oscaleW));
          }
          return results;
     }

     private void interact(boolean[][] input){
          featureToLetter(input);
          letterToWord();
          wordToLetter();
          wordToWord();
          letterToLetter();
          update();
     }
     private void update(){
          for(int i = 0; i<WLEN; i++){
               for(Unit letter : letterLevel.get(i)){
                    updateUnitActivation(letter);
                    letter.setInput(0.0);
               }
          }
          for(Unit word : wordLevel){
               updateUnitActivation(word);
               word.setInput(0.0);
          }
     }

     private void featureToLetter(boolean[][] input){
          for(int i = 0; i<WLEN; i++){
               ArrayList<Unit> features = featureLevel.get(i);
               for(int j = 0; j<14; j++){
                    Unit feature = features.get(j);
                    ArrayList<Unit> excitedLetters = feature.getEConnections();
                    ArrayList<Unit> inhibitedLetters = feature.getIConnections();
                    if(input[i][j]){
                         for(Unit excited : excitedLetters){
                              excited.addInput(alphaFL);
                         }
                         for(Unit inhibited : inhibitedLetters){
                              inhibited.addInput(-gammaFL);
                         }
                    } else{
                         for(Unit excited : excitedLetters){
                              excited.addInput(-gammaFL);
                         }
                         for(Unit inhibited : inhibitedLetters){
                              inhibited.addInput(alphaFL);
                         }
                    }
               }
          }
     }
     private void letterToWord(){
          for(int i = 0; i < WLEN; i++){
               for(int j = 0; j < 26; j++){
                    Unit letter = letterLevel.get(i).get(j);
                    if(letter.getActivation() > 0.0){
                         ArrayList<Unit> excitedWords = letter.getEConnections();
                         for(Unit excited : excitedWords){
                              excited.addInput(letter.getActivation() * alphaLW);
                         }
                         ArrayList<Unit> inhibitedWords = letter.getIConnections();
                         for(Unit inhibited : inhibitedWords){
                              inhibited.addInput(letter.getActivation() * -gammaLW);
                         }
                    }
               }
          }


     }
     private void wordToLetter(){
          for(int i = 0; i < WLEN; i++){
               for(int j = 0; j < 26; j++){
                    Unit letter = letterLevel.get(i).get(j);
                    ArrayList<Unit> excitedWords = letter.getEConnections();
                    for(Unit excited : excitedWords){
                         if(excited.getActivation() > 0.0){
                              letter.addInput(excited.getActivation() * alphaWL);
                         }
                    }
               }
          }
     }
     private void wordToWord(){
          double sum = 0.0;
          for(Unit word : wordLevel){
               if(word.getActivation() > 0){
                    sum += word.getActivation();
               }
          }
          for(Unit word : wordLevel){
               double total = sum;
               if(word.getActivation()>0){
                    total -= word.getActivation();
               }
               word.addInput(total * -gammaWW);
          }
     }
     private void letterToLetter(){
          for(int i = 0; i < WLEN; i++){
               double sum = 0.0;
               for(Unit letter : letterLevel.get(i)){
                    if(letter.getActivation() > 0){
                         sum += letter.getActivation();
                    }
               }
               for(Unit letter : letterLevel.get(i)){
                    double total = sum;
                    if(letter.getActivation() > 0){
                         total -= letter.getActivation();
                    }
                    letter.addInput(total * -gammaLL);
               }
          }
     }
     private void updateUnitActivation(Unit a){
          double net = a.getNet();
          double act = a.getActivation();
          double delta = 0.0;
          if(net > 0.0){
               delta = (max - act) * net - (decay * act);
          }else{
               delta = (act - min) * net - (decay * act);
          }
          a.addActivation(delta);
          if(a.getActivation() > max){
               a.setActivation(max);
          }else if(a.getActivation() < min){
               a.setActivation(min);
          }
     }

     private void instantiateNetwork(){
          featureLevel = new ArrayList<ArrayList<Unit>>();
          letterLevel = new ArrayList<ArrayList<Unit>>();
          wordLevel = new ArrayList<Unit>();
          for(int i = 0; i<4; i++){
               ArrayList<Unit> position_features = new ArrayList<Unit>();
               for (int j = 0; j<14; j++){
                    Unit feature = new Unit(0);
                    position_features.add(feature);
               }
               featureLevel.add(position_features);
          }
          for(int i = 0; i<4; i++){
               ArrayList<Unit> position_letters = new ArrayList<Unit>();
               for (int j = 0; j<26; j++){
                    Unit letter = new Unit(1);
                    position_letters.add(letter);
               }
               letterLevel.add(position_letters);
          }
          try{
               lexicon = loadWords(W_FILE);
               for(String word : lexicon){
                    wordLevel.add(new Unit(2, word));
               }
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
          instantiateFeatureConnections();
          instantiateLetterConnections();
     }
     private void instantiateFeatureConnections(){
          for(int i = 0; i<4; i++){
               ArrayList<Unit> position_features = featureLevel.get(i);
               ArrayList<Unit> position_letters = letterLevel.get(i);
               for (int j = 0; j<14; j++){
                    Unit feature = position_features.get(j);
                    for(int k = 0; k<26; k++){
                         if (uc.get(k)[j]){
                              feature.addConnection(position_letters.get(k), true);
                         } else {
                              feature.addConnection(position_letters.get(k), false);
                         }
                    }
               }
          }
     }
     private void instantiateLetterConnections(){
          try{
               for(int i = 0; i<4; i++){
                    ArrayList<Unit> position_letters = letterLevel.get(i);
                    for(int j=0; j<26; j++){
                         Unit letter = position_letters.get(j);
                         for(Unit word : wordLevel){
                              //System.out.println(word);
                              if(word.getLetter(i)==(char)(j+97)){
                                   letter.addConnection(word, true);
                              } else {
                                   letter.addConnection(word, false);
                              }
                         }
                    }
               }
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
     }

     private String[] loadWords(String filename){
          //NOTE: where to split on? be consistent? currently splits on lines
          try{
               Scanner wordScan = new Scanner(new File(filename));
               int i=0;
               ArrayList<String> words = new ArrayList<String>();
               while(wordScan.hasNextLine()){
                    words.add(wordScan.nextLine());
               }
               //String[] words = wordScan.nextLine().replace(" ", "").split("\n");
               //System.out.println(words[1]);
               return words.toArray(new String[words.size()]);
          } catch (Exception e){
               System.out.println(e);
               System.exit(1);
               return null;
          }
     }
     private boolean[][] loadWord(String word){
          if (word.length()!=WLEN){
               System.out.println("Incorrect word size, invalid input");
               System.exit(1);
          }
          boolean[][] input = new boolean[WLEN][14];
          for (int i = 0; i<WLEN; i++){
               char letter = word.charAt(i);
               int index = (int)letter - 97;
               input[i]=uc.get(index);
          }
          return input;
     }
     private void loadSegs(){
          try{
               File segs = new File(L_SEG);
               Scanner s_segs = new Scanner(segs);

               while(s_segs.hasNextLine()){
                    String curLine = s_segs.nextLine();
                    String[] temp_segs = curLine.split(",");
                    boolean[] thisLetter = new boolean[temp_segs.length];
                    for(int i = 0; i<temp_segs.length; i++){
                         if(temp_segs[i].equals("0")){
                              thisLetter[i] = false;
                         }else if(temp_segs[i].equals("1")){
                              thisLetter[i] = true;
                         }else{
                              System.out.println("Problem! Have:" + temp_segs[i]);
                         }
                    }
                    uc.add(thisLetter);
               }
          }catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
     }

     private void clearNetwork(){
          for(ArrayList<Unit> letters : letterLevel){
               for(Unit letter : letters){
                    letter.setActivation(0.0);
                    letter.setAvgActivation(0.0);
               }
          }
          for(Unit word : wordLevel){
               word.setActivation(0.0);
               word.setAvgActivation(0.0);
          }
     }
}
