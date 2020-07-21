// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.*;

public final class QuickSort{

    public static int partition(Event[] eventsArray, int low, int pivot){
        int smallerIndex = low - 1;
        for(int index = low; index < pivot; index++){
            if(eventsArray[index].getWhen().start() < eventsArray[pivot].getWhen().start()){
                smallerIndex++;
                Event temporaryEvent = eventsArray[index];
                eventsArray[index] = eventsArray[smallerIndex];
                eventsArray[smallerIndex] = temporaryEvent;
            }
        }
        smallerIndex++;
        Event temporaryEvent = eventsArray[pivot];
        eventsArray[pivot] = eventsArray[smallerIndex];
        eventsArray[smallerIndex] = temporaryEvent;

        return smallerIndex;
    }

    public static void sort(Event[] eventsArray, int low, int pivot){
        if(low < pivot){
            int pivotIndex = partition(eventsArray, low, pivot);
            
            sort(eventsArray, low, pivotIndex - 1);
            sort(eventsArray, pivotIndex + 1, pivot);
        }

    }

}