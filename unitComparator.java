import java.util.ArrayList;
import java.util.Comparator;
public class unitComparator implements Comparator<Integer>{
     private final ArrayList<Double> activations;
     public unitComparator(ArrayList<Double> activations){
          this.activations = activations;
     }
     @Override
     public int compare(Integer index1, Integer index2) {
        return activations.get(index1).compareTo(activations.get(index2));
    }
}
