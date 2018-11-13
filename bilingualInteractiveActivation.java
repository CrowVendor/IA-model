import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Collections;
public class bilingualInteractiveActivation extends interactiveActivation{
     private static final double alphaFL = 0.005;
     private static final double alphaLW = 0.07;
     private static final double alphaWL = 0.3;
     private static final double alphaWLa = 0.3; //NOTE: what should this be?
     private static final double gammaFL = 0.15;
     private static final double gammaLW = 0.04;
     private static final double gammaWW = 0.21;
     private static final double gammaLL = 0;
     private static final double gammaLa1W = 0;//NOTE: what should this be?
     private static final double gammaLa2W = 0;//NOTE: what should this be?

     private static final double max = 1.0;
     private static final double min = -0.2;
     private static final double decay = 0.07;
     private static final double oscaleW = 20;
     private static final double oscaleL = 10;
     private static final double resultsThreshold = 1;

     private static String[] lexicon1;
     private static String[] lexicon2;
     private static final String W_FILE1 = "combined_possibles.txt";//"testwords.txt";
     private static final String W_FILE2 = "combined_dutch_possibles.txt";//"testwords.txt";
     private static final String L_SEG = "letter_segmentation.txt";
     private static ArrayList<boolean[]> uc;
     private static ArrayList<ArrayList<Unit>> featureLevel;
     private static ArrayList<ArrayList<Unit>> letterLevel;
     private static ArrayList<Unit> wordLevel1;
     private static ArrayList<Unit> wordLevel2;
     private static ArrayList<Unit> languageLevel;
     private static final int WLEN = 4;

     public static void main(String[] args){
          uc = new ArrayList<boolean[]>();
          loadSegs();
          instantiateNetwork();
          modelWord(args[0], 100, 10);
          //modelWord(args[1], 100, 10);
     }
     private static void modelWord(String word, int cycles, int num_results){
          ArrayList<ArrayList<Double>> results = new ArrayList<ArrayList<Double>>();
          boolean[][] input = loadWord(word);

          for(int i = 0; i<cycles; i++){
               interact(input);
               results.add(getOutput());
               //writeOutput("testresults.txt");
          }
          output(results, num_results);

     }
     private static void output(ArrayList<ArrayList<Double>> results, int num_results){
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
               if(index >= lexicon1.length){
                    top_words.add("DUTCH: "+lexicon2[index - lexicon1.length]);
               } else {
                    top_words.add("ENGLISH: "+lexicon1[index]);
               }
               for(int j=0; j<results.size();j++){
                    top_result.add(results.get(j).get(index));
               }
               top_results.add(top_result);
          }
          writeOutput(top_results, "testresults2.txt", top_words);

     }
     private static void writeOutput(ArrayList<ArrayList<Double>> top_results, String filename, ArrayList<String> top_words){
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
     private static ArrayList<Double> getOutput(){
          ArrayList<Double> results = new ArrayList<Double>();
          for(Unit word : wordLevel1){
               results.add(word.getActivation());
          }
          for(Unit word : wordLevel2){
               results.add(word.getActivation());
          }
          return results;
     }

     private static void interact(boolean[][] input){
          featureToLetter(input);
          letterToWord();
          wordToLetter();
          wordToLanguage();
          languageToWord();
          wordToWord();
          letterToLetter();
          update();
     }
     private static void update(){
          for(int i = 0; i<WLEN; i++){
               for(Unit letter : letterLevel.get(i)){
                    updateUnitActivation(letter);
                    letter.setInput(0.0);
               }
          }
          for(Unit word : wordLevel1){
               updateUnitActivation(word);
               word.setInput(0.0);
          }
          for(Unit word : wordLevel2){
               updateUnitActivation(word);
               word.setInput(0.0);
          }
          for(Unit language : languageLevel){
               updateUnitActivation(language);
               language.setInput(0.0);
          }
     }

     private static void featureToLetter(boolean[][] input){
          for(int i = 0; i<WLEN; i++){
               for(int j = 0; j<14; j++){
                    ArrayList<Unit> features = featureLevel.get(i);
                    Unit feature = features.get(j);
                    if(input[i][j]){
                         ArrayList<Unit> excitedLetters = feature.getEConnections();
                         for(Unit excited : excitedLetters){
                              excited.addInput(alphaFL);
                         }
                         ArrayList<Unit> inhibitedLetters = feature.getIConnections();
                         for(Unit inhibited : inhibitedLetters){
                              inhibited.addInput(-gammaFL);
                         }
                    }
               }
          }
     }
     private static void letterToWord(){
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
     private static void wordToLetter(){
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
     private static void wordToLanguage(){
          for(Unit word : wordLevel1){
               if(word.getActivation()>0.0){
                    languageLevel.get(0).addInput(word.getActivation()*alphaWLa);
               }
          }
          for(Unit word : wordLevel2){
               if(word.getActivation()>0.0){
                    languageLevel.get(1).addInput(word.getActivation()*alphaWLa);
               }
          }
     }
     private static void languageToWord(){
          double act1 = languageLevel.get(0).getActivation();
          double act2 = languageLevel.get(1).getActivation();
          if(act2>0.0){
               for(Unit word : wordLevel1){
                    word.addInput(act2 * -gammaLa2W);
               }
          }
          if(act1>0.0){
               for(Unit word : wordLevel2){
                    word.addInput(act1 * -gammaLa1W);
               }
          }
     }
     private static void wordToWord(){
          double sum = 0.0;
          for(Unit word : wordLevel1){
               if(word.getActivation() > 0){
                    sum += word.getActivation();
               }
          }
          for(Unit word : wordLevel2){
               if(word.getActivation() > 0){
                    sum += word.getActivation();
               }
          }
          for(Unit word : wordLevel1){
               double total = sum;
               if(word.getActivation()>0){
                    total -= word.getActivation();
               }
               word.addInput(total * -gammaWW);
          }
          for(Unit word : wordLevel2){
               double total = sum;
               if(word.getActivation()>0){
                    total -= word.getActivation();
               }
               word.addInput(total * -gammaWW);
          }
     }
     private static void letterToLetter(){
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
     private static void updateUnitActivation(Unit a){
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

     private static void instantiateNetwork(){
          featureLevel = new ArrayList<ArrayList<Unit>>();
          letterLevel = new ArrayList<ArrayList<Unit>>();
          wordLevel1 = new ArrayList<Unit>();
          wordLevel2 = new ArrayList<Unit>();
          languageLevel = new ArrayList<Unit>();
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
               lexicon1 = loadWords(W_FILE1);
               for(String word : lexicon1){
                    wordLevel1.add(new Unit(2, word));
               }
               lexicon2 = loadWords(W_FILE2);
               for(String word : lexicon2){
                    wordLevel2.add(new Unit(2, word));
               }
          } catch(Exception e){
               System.out.println(e);
               System.exit(1);
          }
          languageLevel.add(new Unit(3));
          languageLevel.add(new Unit(3));
          instantiateFeatureConnections();
          instantiateLetterConnections();
     }
     private static void instantiateFeatureConnections(){
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
     private static void instantiateLetterConnections(){
          instantiateLetterWordConnections(wordLevel1);
          instantiateLetterWordConnections(wordLevel2);
     }
     private static void instantiateLetterWordConnections(ArrayList<Unit> wordLevel){
          try{
               for(int i = 0; i<4; i++){
                    ArrayList<Unit> position_letters = letterLevel.get(i);
                    for(int j=0; j<26; j++){
                         Unit letter = position_letters.get(j);
                         for(Unit word : wordLevel){
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
     private static String[] loadWords(String filename){
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
     private static boolean[][] loadWord(String word){
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
     private static void loadSegs(){
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
     private static void clearNetwork(){
          for(ArrayList<Unit> letters : letterLevel){
               for(Unit letter : letters){
                    letter.setActivation(0.0);
               }
          }
          for(Unit word : wordLevel1){
               word.setActivation(0.0);
          }
          for(Unit word : wordLevel2){
               word.setActivation(0.0);
          }
     }
}
