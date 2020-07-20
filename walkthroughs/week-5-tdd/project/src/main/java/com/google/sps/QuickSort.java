import java.util.*;

public class QuickSort{

    public int partition(Event[] eventsArray, int low, int high){
        int smallerIndex = low - 1;
        for(int index = low, index < pivot, index++){
            if(eventsArray[index].getWhen().start() < eventsArray[pivot].getWhen().start()){
                smallerIndex++;
                Event temporaryEvent = eventsArray[index];
                eventsArray[index] = eventsArray[smallerIndex]
                eventsArray[smallerIndex] = temporaryElement;
            }
        }
        smallerIndex++;
        Event temporaryEvent = eventsArray[pivot];
        eventsArray[pivot] = eventsArray[smallerIndex];
        eventsArray[smallerIndex] = temporaryEvent;

        return smallerIndex;
    }

    public void sort(Event[] eventsArray, int low, int pivot){
        if(low < pivot){
            int pivotIndex = partition(eventsArray, low, pivot);
            
            sort(eventsArray, low, pivotIndex - 1);
            sort(eventsArray, pivotIndex + 1, pivot);
        }

    }

}