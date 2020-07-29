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
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
* List the collection of timeranges that accommodate for both mandatory and optional attendees depending on the duration
* of the requested event. If no timerange is available for both types of attendees, then it will return only the timeranges
* that accommodate mandatory attendees.
*/
public final class FindMeetingQuery {

  private ModifiableTimeRange currentFilledTimeRange;
  private long requestDuration;
  private List<TimeRange> timeRangesForRequestedEvent;
  private List<TimeRange> timeRangesForEventsWithOptionalAndNoMandatoryAttendees;
  private List<TimeRange> timeRangesIncludingMandatoryAndOptionalAttendees;
  private List<Event> eventsList;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

      Set<String> mandatoryAttendees = new HashSet<String>(request.getAttendees());
      Set<String> optionalAttendees = new HashSet<String>(request.getOptionalAttendees());

      currentFilledTimeRange = new ModifiableTimeRange(0,0);
      requestDuration = request.getDuration();
      timeRangesForRequestedEvent = new ArrayList<TimeRange>();
      timeRangesForEventsWithOptionalAndNoMandatoryAttendees = new ArrayList<TimeRange>();
      timeRangesIncludingMandatoryAndOptionalAttendees = new ArrayList<TimeRange>();
      eventsList = (events instanceof List) ? (List)events : new ArrayList(events);
      
      Collections.sort(eventsList, Event.ORDER_BY_TIMERANGE_START_TIME);

      for(Event event: eventsList){
          
          if(eventFitsInCurrentFilledTimeRange(event)){

              updateCurrentFilledTimeRangeWithEvent(event);
                                        
              continue;
          }
          
          else{

              Set<String> eventAttendees = event.getAttendees();
              boolean eventContainsOptionalAttendee = false;
              boolean eventContainsMandatoryAttendee = false;

              for(String attendee: eventAttendees){
                  if(mandatoryAttendees.contains(attendee)){

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

              //make the optional timeranges to be a one-layered collection of timeranges
              if(!eventContainsMandatoryAttendee && eventContainsOptionalAttendee){
                  int listSize = timeRangesForEventsWithOptionalAndNoMandatoryAttendees.size();
                  if(listSize == 0){
                      timeRangesForEventsWithOptionalAndNoMandatoryAttendees.add(eventTimeRange(event));   
                  }
                  else{
                      TimeRange lastTimeRangeInList = timeRangesForEventsWithOptionalAndNoMandatoryAttendees.get(listSize - 1);    
                      if(currentEventTimeRangeStartIsLocatedAfterLastTimeRangeInListEnd(event, lastTimeRangeInList)){
                          addEventTimeRangeToTimeRangesForEventsWithOptionalAndNoMandatoryAttendees(event);
                      }
                      else if(eventTimeRange(event).end() > lastTimeRangeInList.end()){
                          replaceTheLastTimeRangeWithCurrentTimeRange(event, lastTimeRangeInList, listSize);
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

      if(timeRangesForEventsWithOptionalAndNoMandatoryAttendees.size() > 0){
        
        addEndOfDayToTimeRangesForEventsWithOptionalAndNoMandatoryAttendees();

        //previousOptionalAttendeesTimeRange initialized as start of day (00:00)
        ModifiableTimeRange previousOptionalTimeRange = new ModifiableTimeRange(0,0);

        /**
        * Find all the appropriate time ranges that exist at the gaps of timeRangesForEventsWithOptionalAndNoMandatoryAttendees.
        *
        * There are 4 test cases for the lookups, which are:
        * Note: 
        * Top => currentMandatoryTimeRange (All the free timeranges on timeRangesForRequestedEvent)
        * Bottom => optionalTimeRange (All the filled timeranges by optional attendees only and no mandatory attendees)
        *
        *  1.  _________       ________   _______       _____
        *      _________  ,  __________ , _________ , _________
        *
        *  2.  _________     __________   _________   _________
        *      _________  ,    ________ , _______   ,   _____
        *
        *  3.  ________        ______
        *      ______     ,  ______ 
        *
        *  4.  ______        ______
        *      ________   ,    ______
        */

        for(TimeRange optionalTimeRange: timeRangesForEventsWithOptionalAndNoMandatoryAttendees){

            //gapOptionalTimeRange is the current gap between the current optional time range and the previous optional time range
            TimeRange currentGapOptionalTimeRange = setCurrentGapOptionalTimeRange(optionalTimeRange, 
                                                                                    previousOptionalTimeRange);

            for(TimeRange currentMandatoryTimeRange: timeRangesForRequestedEvent){

                //updating previousOptionalAttendeesTimeRange
                previousOptionalTimeRange.setStart(optionalTimeRange.start());
                previousOptionalTimeRange.setDuration(optionalTimeRange.duration());

                if(thisContainsThat(currentGapOptionalTimeRange, currentMandatoryTimeRange)){
                    //The time range can automatically be added since the currentMandatoryTimeRange has the appropriate duration
                    timeRangesIncludingMandatoryAndOptionalAttendees.add(currentMandatoryTimeRange);
                }
                else if(thisContainsThat(currentMandatoryTimeRange, currentGapOptionalTimeRange)){
                    if(currentGapOptionalTimeRangeHasAppropriateDuration(currentGapOptionalTimeRange, requestDuration)){
                        timeRangesIncludingMandatoryAndOptionalAttendees.add(currentGapOptionalTimeRange);
                    }
                }
                else if(thisOnlyContainsThatStart(currentGapOptionalTimeRange, currentMandatoryTimeRange)){
                    int temporaryDuration = currentGapOptionalTimeRange.end() - currentMandatoryTimeRange.start();
                    if(temporaryDuration >= requestDuration){
                        timeRangesIncludingMandatoryAndOptionalAttendees.add(
                            TimeRange.fromStartDuration(currentMandatoryTimeRange.start(), temporaryDuration)
                        );
                    }
                }
                else if(thisOnlyContainsThatEnd(currentGapOptionalTimeRange, currentMandatoryTimeRange)){
                    int temporaryDuration = currentMandatoryTimeRange.end() - currentGapOptionalTimeRange.start();
                    if(temporaryDuration >= requestDuration){
                        timeRangesIncludingMandatoryAndOptionalAttendees.add(
                            TimeRange.fromStartDuration(currentGapOptionalTimeRange.start(), temporaryDuration)
                        );
                    }
                }
            }
        }

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

  public boolean thisContainsThatStart(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisTimeRange.containsStart(thatTimeRange.start());
  }

  public boolean thisContainsThatEnd(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisTimeRange.containsEnd(thatTimeRange.end());
  }

  public boolean thisContainsThat(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisContainsThatStart(thisTimeRange, thatTimeRange) &&
            thisContainsThatEnd(thisTimeRange, thatTimeRange);
  }

  public boolean thisOnlyContainsThatStart(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisContainsThatStart(thisTimeRange, thatTimeRange) &&
            !thisContainsThatEnd(thisTimeRange, thatTimeRange);
  }

  public boolean thisOnlyContainsThatEnd(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return !thisContainsThatStart(thisTimeRange, thatTimeRange) &&
            thisContainsThatEnd(thisTimeRange, thatTimeRange);
  }

  public boolean currentEventTimeRangeStartIsLocatedAfterLastTimeRangeInListEnd(Event event, TimeRange lastTimeRangeInList){
      return eventTimeRange(event).start() > lastTimeRangeInList.end();
  }

  public void addEventTimeRangeToTimeRangesForEventsWithOptionalAndNoMandatoryAttendees(Event event){
      timeRangesForEventsWithOptionalAndNoMandatoryAttendees.add(eventTimeRange(event));
  }

  public boolean currentEventTimeRangeEndTimeIsLocatedAfterLastTimeRangeEndTime(Event event, TimeRange lastTimeRangeInList){
      return eventTimeRange(event).end() > lastTimeRangeInList.end();
  }

  public void replaceTheLastTimeRangeWithCurrentTimeRange(Event event, TimeRange lastTimeRangeInList, int listSize){
      int newTimeRangeDuration = eventTimeRange(event).end() - lastTimeRangeInList.start();
      TimeRange newTimeRange = TimeRange.fromStartDuration(lastTimeRangeInList.start(), newTimeRangeDuration);
      timeRangesForEventsWithOptionalAndNoMandatoryAttendees.remove(listSize - 1);
      timeRangesForEventsWithOptionalAndNoMandatoryAttendees.add(newTimeRange);
  }

  public void addEndOfDayToTimeRangesForEventsWithOptionalAndNoMandatoryAttendees(){
      TimeRange endOfDayPoint = TimeRange.fromStartDuration(1440,0);
      timeRangesForEventsWithOptionalAndNoMandatoryAttendees.add(endOfDayPoint);
  }

  public TimeRange setCurrentGapOptionalTimeRange(TimeRange optionalTimeRange, 
                                                ModifiableTimeRange previousOptionalTimeRange){
      int duration = optionalTimeRange.start() - previousOptionalTimeRange.end();
      int start = previousOptionalTimeRange.end();
      return TimeRange.fromStartDuration(start, duration);
  }

  public boolean currentGapOptionalTimeRangeHasAppropriateDuration(TimeRange currentGapOptionalTimeRange,
                                                                    long requestDuration){
      return currentGapOptionalTimeRange.duration() >= requestDuration;
  }  

}