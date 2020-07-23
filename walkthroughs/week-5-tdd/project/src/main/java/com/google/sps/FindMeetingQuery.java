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

import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public final class FindMeetingQuery {

    /**
    * This algorithm returns a collection of timeslots available for the meeting request based on existing events.
    * It will return an empty collection if no timeslots are available.
    */

  private int currentStartTime = 0;
  private int currentDuration = 0;
  private TimeRange eventTimeRange;
  private int eventStartTime;
  private int eventDuration;
  private int eventEndTime;
  private int currentEndTime;
  private long requestDuration;
  private List<TimeRange> timeRangesForRequestedEvent;

  private Event[] eventsArray;

  //for testing
  public Event[] getEventsArray(){
    return eventsArray;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

      String[] requestAttendees = request.getAttendees().toArray(new String[]{});
      requestDuration = request.getDuration();
      timeRangesForRequestedEvent = new ArrayList<TimeRange>();
      eventsArray = events.toArray(new Event[]{});
      
      Arrays.sort(eventsArray, Event.ORDER_BY_TIMERANGE_START_TIME);

      for(Event event: eventsArray){

          eventTimeRange = event.getWhen();
          eventStartTime = eventTimeRange.start();
          eventDuration = eventTimeRange.duration();
          eventEndTime = eventTimeRange.end();
          currentEndTime = currentStartTime + currentDuration;
          
          if(eventFitsInCurrentTimeRange()){

              updateCurrentTimeRangeWithEvent();
                                        
              continue;
          }
          
          else{
              for(String attendee: event.getAttendees()){
                  if(Arrays.asList(requestAttendees).contains(attendee)){
                      if(noCurrentTimeRangeAndEventStartsAtStartOfDay(eventStartTime, 
                                                                    currentStartTime, 
                                                                    currentDuration)){
                          setCurrentTimeRangeDurationAsEventDuration();
                      }
                      else{
                          setCurrentTimeRangeAsEventTimeRangeAndAddTheFreeTimeSlotToTimeRangesForRequestedEvent();
                      }
                      break;
                  }
              }
          }
      }
      
      addRemainingTimeSlotToTimeRangesForRequestedEvent();

      return timeRangesForRequestedEvent;

  }

  public boolean eventFitsInCurrentTimeRange(){
    
      return eventStartTime >= currentStartTime && eventEndTime <= currentEndTime;
  }

  public void updateCurrentTimeRangeWithEvent(){
      currentStartTime = eventStartTime;
      currentDuration = currentEndTime - currentStartTime;
  }

  public boolean noCurrentTimeRangeAndEventStartsAtStartOfDay(int eventStartTime,
                                                            int currentStartTime, 
                                                            int currentDuration){

      return eventStartTime == currentStartTime && currentDuration == 0;

  }

  public void setCurrentTimeRangeDurationAsEventDuration(){
      currentDuration = eventDuration;
  }

  public boolean noCurrentTimeRangeAndEventStartAfterStartOfDay(int eventStartTime,
                                                                int currentStartTime,
                                                                int currentDuration){
      return eventStartTime > currentStartTime && currentDuration == 0;
  }

  public void setCurrentTimeRangeAsEventTimeRangeAndAddTheFreeTimeSlotToTimeRangesForRequestedEvent(){
    int timeRangeDuration = eventStartTime - currentEndTime;
    if(timeRangeDuration >= requestDuration){
        timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentEndTime, timeRangeDuration));
    }
    currentStartTime = eventStartTime;
    currentDuration = eventDuration;
  }

  public void addRemainingTimeSlotToTimeRangesForRequestedEvent(){
      currentEndTime = currentStartTime + currentDuration;
      int endOfDay = 24 * 60;
      int timeLeftBetweenEndOfDayAndCurrentEndTime = endOfDay - currentEndTime;
      if(currentEndTime < endOfDay && timeLeftBetweenEndOfDayAndCurrentEndTime >= requestDuration){
          timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentEndTime, timeLeftBetweenEndOfDayAndCurrentEndTime));
      }
  }
}