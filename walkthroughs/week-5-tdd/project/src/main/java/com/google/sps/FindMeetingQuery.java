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

  private CurrentTimeRange currentTimeRange;
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
      currentTimeRange = new CurrentTimeRange(0,0);
      timeRangesForRequestedEvent = new ArrayList<TimeRange>();
      eventsArray = events.toArray(new Event[]{});
      
      Arrays.sort(eventsArray, Event.ORDER_BY_TIMERANGE_START_TIME);

      for(Event event: eventsArray){
          
          if(eventFitsInCurrentTimeRange(event)){

              updateCurrentTimeRangeWithEvent(event);
                                        
              continue;
          }
          
          else{
              for(String attendee: event.getAttendees()){
                  if(Arrays.asList(requestAttendees).contains(attendee)){
                      if(noCurrentTimeRangeAndEventStartsAtStartOfDay(event)){
                          setCurrentTimeRangeDurationAsEventDuration(event);
                      }
                      else{
                          setCurrentTimeRangeAsEventTimeRangeAndAddTheFreeTimeSlotToTimeRangesForRequestedEvent(event);
                      }
                      break;
                  }
              }
          }
      }
      
      addRemainingTimeSlotToTimeRangesForRequestedEvent();

      return timeRangesForRequestedEvent;

  }

  public boolean eventFitsInCurrentTimeRange(Event event){
      
      return eventStartTime(event) >= currentTimeRangeStartTime() && 
            eventEndTime(event) <= currentTimeRangeEndTime();
  }

  public void updateCurrentTimeRangeWithEvent(Event event){
      int temporaryCurrentTimeRangeEndTime = currentTimeRangeEndTime();
      setCurrentTimeRangeStartTime(eventStartTime(event));
      setCurrentTimeRangeDuration(temporaryCurrentTimeRangeEndTime - currentTimeRangeStartTime());
  }

  public boolean noCurrentTimeRangeAndEventStartsAtStartOfDay(Event event){

      return eventStartTime(event) == currentTimeRangeStartTime() && currentTimeRangeDuration() == 0;

  }

  public void setCurrentTimeRangeDurationAsEventDuration(Event event){
      setCurrentTimeRangeDuration(eventDuration(event));
  }

  public void setCurrentTimeRangeAsEventTimeRangeAndAddTheFreeTimeSlotToTimeRangesForRequestedEvent(Event event){
    int timeRangeDuration = eventStartTime(event) - currentTimeRangeEndTime();
    if(timeRangeDuration >= requestDuration){
        timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentTimeRangeEndTime(), timeRangeDuration));
    }
    setCurrentTimeRangeStartTime(eventStartTime(event));
    setCurrentTimeRangeDuration(eventDuration(event));
  }

  public void addRemainingTimeSlotToTimeRangesForRequestedEvent(){
      int endOfDay = 24 * 60;
      int timeLeftBetweenEndOfDayAndCurrentEndTime = endOfDay - currentTimeRangeEndTime();
      if(currentTimeRangeEndTime() < endOfDay && timeLeftBetweenEndOfDayAndCurrentEndTime >= requestDuration){
          timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentTimeRangeEndTime(), 
                                                                    timeLeftBetweenEndOfDayAndCurrentEndTime));
      }
  }

  public int currentTimeRangeStartTime(){
      return currentTimeRange.start();
  }

  public int currentTimeRangeDuration(){
      return currentTimeRange.duration();
  }

  public int currentTimeRangeEndTime(){
      return currentTimeRange.end();
  }

  public void setCurrentTimeRangeStartTime(int start){
      currentTimeRange.setStart(start);
  }

  public void setCurrentTimeRangeDuration(int duration){
      currentTimeRange.setDuration(duration);
  }

  public int eventStartTime(Event event){
      return event.getWhen().start();
  }

  public int eventDuration(Event event){
      return event.getWhen().duration();
  }

  public int eventEndTime(Event event){
      return event.getWhen().end();
  }
}