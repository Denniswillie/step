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
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {

    /**
    * This algorithm returns a collection of timeslots available for the meeting request based on existing events.
    * It will return an empty collection if no timeslots are available.
    */

  private ModifiableTimeRange currentFilledTimeRange;
  private long requestDuration;
  private List<TimeRange> timeRangesForRequestedEvent;
  private List<TimeRange> timeRangesForEventsWithOptionalAndNoMandatoryAttendees;
  private List<TimeRange> timeRangesIncludingMandatoryAndOptionalAttendees;

  private Event[] eventsArray;

  //for testing
  public Event[] getEventsArray(){
    return eventsArray;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

      Set<String> mandatoryAttendees = new HashSet<String>(request.getAttendees());

      requestDuration = request.getDuration();
      currentFilledTimeRange = new CurrentTimeRange(0,0);
      timeRangesForRequestedEvent = new ArrayList<TimeRange>();
      timeRangesForEventsWithOptionalAndNoMandatoryAttendees = new ArrayList<TimeRange>();
      timeRangesIncludingMandatoryAndOptionalAttendees = new ArrayList<TimeRange>();
      eventsArray = events.toArray(new Event[]{});
      
      Arrays.sort(eventsArray, Event.ORDER_BY_TIMERANGE_START_TIME);

      for(Event event: eventsArray){
          
          if(eventFitsInCurrentFilledTimeRange(event)){

              updateCurrentFilledTimeRangeWithEvent(event);
                                        
              continue;
          }
          
          else{

              Set<String> eventAttendees = event.getAttendees();
              boolean eventContainsOptionalAttendee = false;
              boolean eventContainsMandatoryAttendee = false;

              for(String attendee: eventAttendees){
                  if(requestAttendees.contains(attendee)){

                      eventContainsMandatoryAttendee = true;

                      if(noCurrentFilledTimeRangeAndEventStartsAtStartOfDay(event)){
                          setCurrentFilledTimeRangeDurationAsEventDuration(event);
                      }
                      else{
                          setCurrentFilledTimeRangeAsEventTimeRangeAndAddTheFreeTimeSlotToTimeRangesForRequestedEvent(event);
                      }
                      break;
                  }
                  else if(!eventContainsOptionalAttendee){
                      if(optionalAttendees.contains(attendee)){
                          eventContainsOptionalAttendee = true;
                      }
                  }
              }

              //make the optional timerange a one layer time range
              if(!eventContainsMandatoryAttendee && eventContainsOptionalAttendee){
                  int listSize = timeRangesForEventsWithOptionalAndNoMandatoryAttendees.size();
                  if(listSize == 0){
                      timeRangesForEventsWithOptionalAndNoMandatoryAttendees.add(eventTimeRange(event));   
                  }
                  else{
                      TimeRange lastTimeRangeInList = timeRangesForEventsWithOptionalAndNoMandatoryAttendees.get(listSize - 1);    
                      if(eventTimeRange(event).start() > lastTimeRangeInList.end()){
                          timeRangesForEventsWithOptionalAndNoMandatoryAttendees.add(eventTimeRange(event));
                      }
                      else if(eventTimeRange(event).end() > lastTimeRangeInList.end()){
                          int newTimeRangeDuration = eventTimeRange(event).end() - lastTimeRangeInList.start();
                          TimeRange newTimeRange = new TimeRange.fromStartDuration(lastTimeRangeInList.start(), newTimeRangeDuration);
                          timeRangesForEventsWithOptionalAndNoMandatoryAttendees.remove(listSize - 1);
                          timeRangesForEventsWithOptionalAndNoMandatoryAttendees.add(newTimeRange);
                      }
                  }
              }
          }
      }
      
      addRemainingTimeSlotToTimeRangesForRequestedEvent();

      int timeRangesForRequestedEventSize = timeRangesForRequestedEvent.size();
      if(timeRangesForRequestedEventSize == 0){
          return timeRangesForRequestedEvent;
      }

      //find closest optional attendees time range from (x = 0)
      if(timeRangesForEventsWithOptionalAndNoMandatoryAttendees.size() > 0){
          
        ModifiableTimeRange currentTimeRange = new ModifiableTimeRange(0,0);
        

      }

      return (timeRangesIncludingMandatoryAndOptionalAttendees.size() == 0) ? 
                timeRangesForRequestedEvent : timeRangesIncludingMandatoryAndOptionalAttendees;
  }

  public boolean eventFitsInCurrentFilledTimeRange(Event event){
      
      return eventStartTime(event) >= currentFilledTimeRangeStartTime() && 
            eventEndTime(event) <= currentFilledTimeRangeEndTime();
  }

  public void updateCurrentFilledTimeRangeWithEvent(Event event){
      int temporaryCurrentTimeRangeEndTime = currentFilledTimeRangeEndTime();
      setCurrentFilledTimeRangeStartTime(eventStartTime(event));
      setCurrentFilledTimeRangeDuration(temporaryCurrentTimeRangeEndTime - currentFilledTimeRangeStartTime());
  }

  public boolean noCurrentFilledTimeRangeAndEventStartsAtStartOfDay(Event event){

      return eventStartTime(event) == currentFilledTimeRangeStartTime() && currentFilledTimeRangeDuration() == 0;

  }

  public void setCurrentFilledTimeRangeDurationAsEventDuration(Event event){
      setCurrentFilledTimeRangeDuration(eventDuration(event));
  }

  public void setCurrentFilledTimeRangeAsEventTimeRangeAndAddTheFreeTimeSlotToTimeRangesForRequestedEvent(Event event){
    int timeRangeDuration = eventStartTime(event) - currentFilledTimeRangeEndTime();
    if(timeRangeDuration >= requestDuration){
        timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentFilledTimeRangeEndTime(), timeRangeDuration));
    }
    setCurrentFilledTimeRangeStartTime(eventStartTime(event));
    setCurrentFilledTimeRangeDuration(eventDuration(event));
  }

  public void addRemainingTimeSlotToTimeRangesForRequestedEvent(){
      int endOfDay = 24 * 60;
      int timeLeftBetweenEndOfDayAndCurrentEndTime = endOfDay - currentFilledTimeRangeEndTime();
      if(currentFilledTimeRangeEndTime() < endOfDay && timeLeftBetweenEndOfDayAndCurrentEndTime >= requestDuration){
          timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentFilledTimeRangeEndTime(), 
                                                                    timeLeftBetweenEndOfDayAndCurrentEndTime));
      }
  }

  public int currentFilledTimeRangeStartTime(){
      return currentFilledTimeRange.start();
  }

  public int currentFilledTimeRangeDuration(){
      return currentFilledTimeRange.duration();
  }

  public int currentFilledTimeRangeEndTime(){
      return currentFilledTimeRange.end();
  }

  public void setCurrentFilledTimeRangeStartTime(int start){
      currentFilledTimeRange.setStart(start);
  }

  public void setCurrentFilledTimeRangeDuration(int duration){
      currentFilledTimeRange.setDuration(duration);
  }

  public TimeRange eventTimeRange(Event event){
      return event.getWhen();
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