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

// FindMeetingQuery is the container class for finding free time slots between events for attendees on the meeting request
public final class FindMeetingQuery {

  //the current TimeRange that is being used as a bench mark for when populating mandatoryTimeRange
  private TimeRange window = new TimeRange.fromStartDuration(0,0);

  //the precious optionalTimeRange for when populating mixedTimeRange
  private TimeRange previousOptionalTimeRange = new TimeRange.fromStartDuration(0,0);

  private long requestDuration;

  //AVAILABLE time slots for mandatory attendees
  private List<TimeRange> mandatoryTimeRanges = new ArrayList<>();

  //UNAVAILABLE time slots for optional attendees
  private List<TimeRange> optionalTimeRanges = new ArrayList<>();

  //AVAILABLE time slots for mandatory and optional attendees
  private List<TimeRange> mixedTimeRanges = new ArrayList<>();

  private Set<String> mandatoryAttendees;
  private Set<String> optionalAttendees;
  private List<Event> eventsList;
  private boolean eventContainsMandatoryAttendee = false;
  private boolean eventContainsOptionalAttendee = false;

  /**
  * @return the collection of timeranges that accommodate for both mandatory and optional attendees depending on the duration
  * of the requested event. If no timerange is available for both types of attendees, then it will @return only the timeranges
  * that accommodate mandatory attendees.
  *
  * @param events a collection of events that have timeranges and attendees for each corresponding event.
  * @param request meeting request containing a collection of mandatory and 
  *                optional attendees, and duration for the requested event.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

      setRequestedAttendees(request);
      setRequestedDuration(request);
      setEventsList(events);
      
      //eventsList must be sorted so timeRanges can be populated starting from the beginning to end of day
      Collections.sort(eventsList, Event.ORDER_BY_TIMERANGE_START_TIME);

      populateTimeRanges();
      
      if(mandatoryTimeRanges.isEmpty() || optionalTimeRanges.size() == 1){
          return mandatoryTimeRanges;
      }    

      populateMixedTimeRanges();

      return (mixedTimeRanges.isEmpty()) ? mandatoryTimeRanges : mixedTimeRanges;
  }

///////////////////////////////////////////////////////////////////////////////////
  //Setters
  private void setRequestedAttendees(MeetingRequest request){
      mandatoryAttendees = new HashSet<>(request.getAttendees());
      optionalAttendees = new HashSet<>(request.getOptionalAttendees());
  }

  private void setRequestedDuration(MeetingRequest request){
      requestDuration = request.getDuration();
  }

  private void setEventsList(Collection<Event> events){
      eventsList = (events instanceof List) ? (List)events : new ArrayList(events);
  }
////////////////////////////////////////////////////////////////////////////////////

  //populate mandatoryTimeRanges and optionalTimeRanges
  private void populateTimeRanges(){
      for(Event event: eventsList){

          if(eventFitsWindow(event)){
              continue;
          }
          
          else{
              Set<String> eventAttendees = event.getAttendees();
              eventContainsOptionalAttendee = false;
              eventContainsMandatoryAttendee = false;

              populateMandatoryTimeRanges(event, eventAttendees);
              populateOptionalTimeRanges(event);
          }
      }
      completeMandatoryTimeRanges();
      completeOptionalTimeRanges();
  }

  private boolean eventFitsWindow(Event event){
      
      return eventStartTime(event) >= currentFilledTimeRangeStartTime() && 
            eventEndTime(event) <= currentFilledTimeRangeEndTime();
  }

  private void populateMandatoryTimeRanges(Event event, Set<String> eventAttendees){
      for(String attendee: eventAttendees){
          if(mandatoryAttendees.contains(attendee)){
              eventContainsMandatoryAttendee = true;
              addMandatoryTimeRange(event);
              updateWindow(event);
              break;
          }

          else{
              if(!eventContainsOptionalAttendee){
                  eventContainsOptionalAttendee = (optionalAttendees.contains(attendee)) ? true : false;
              }
          }
      }
  }

  private void addMandatoryTimeRange(Event event){
      int timeRangeDuration = eventStartTime(event) - windowEndTime();
      if(timeRangeDuration >= requestDuration){
        mandatoryTimeRanges.add(TimeRange.fromStartDuration(windowEndTime(), timeRangeDuration));
      }
  }

  //updating window as current time range that is being iterated
  private void updateWindow(Event event){
    int newStartTime = eventStartTime(event);
    int newDuration = eventDuration(event);
    setWindow(newStartTime, newDuration);
  }

  /**
  * populate optionalTimeRanges only if the event does not have any mandatory attendees, because if the event has one or more
  * mandatory attendees, then the event's time range will already be added to mandatoryTimeRanges
  */
  private void populateOptionalTimeRanges(Event event){
      if(!eventContainsMandatoryAttendee && eventContainsOptionalAttendee){
          if(optionalTimeRanges.isEmpty()){
              optionalTimeRanges.add(eventTimeRange(event));   
          }
          else{
              size = optionalTimeRanges.size();
              TimeRange lastTimeRange = optionalTimeRanges.get(size - 1);    
              if(eventStartsAfterLastEvent(event, lastTimeRange)){
                  addToOptionalTimeRanges(event);
              }
              else{
                  if(eventEndsAfterLastEvent(event, lastTimeRange)){
                      replaceLastTimeRange(event, lastTimeRangeInList, size);
                  }
              }
          }
      }
  }

  private boolean eventStartsAfterLastEvent(Event event, TimeRange lastTimeRange){
      return eventTimeRange(event).start() > lastTimeRange.end();
  }

  //add the current event's {@code TimeRange} to optionalTimeRanges
  private void addToOptionalTimeRanges(Event event){
      optionalTimeRanges.add(eventTimeRange(event));
  }

  private boolean eventEndsAfterLastEvent(Event event, TimeRange lastTimeRange){
      return eventTimeRange(event).end() > lastTimeRange.end();
  }

  //replacing the last {@code TimeRange} with the current event's {@code TimeRange}
  private void replaceLastTimeRange(Event event, TimeRange lastTimeRange, int size){
      int newTimeRangeDuration = eventTimeRange(event).end() - lastTimeRange.start();
      TimeRange newTimeRange = TimeRange.fromStartDuration(lastTimeRange.start(), newTimeRangeDuration);
      optionalTimeRanges.remove(size - 1);
      optionalTimeRanges.add(newTimeRange);
  }

  //add the remaining time slot until end of day to mandatoryTimeRanges
  private void completeMandatoryTimeRanges(){
      int endOfDay = 24 * 60;

      //time left between current window end time and end of day
      int timeLeft = endOfDay - windowEndTime();

      if(timeLeft >= requestDuration){
          mandatoryTimeRanges.add(TimeRange.fromStartDuration(windowEndTime(), timeLeft));
      }
  }

  //add end of day as a point to optionalTimeRanges
  private void completeOptionalTimeRanges(){
      TimeRange endOfDayPoint = TimeRange.fromStartDuration(1440,0);
      optionalTimeRanges.add(endOfDayPoint);
  }

  private int windowStartTime(){
      return window.start();
  }

  private int windowDuration(){
      return window.duration();
  }

  private int windowEndTime(){
      return window.end();
  }

  private void setWindow(int start, int duration){
      window = TimeRange.fromStartDuration(start, duration);
  }

  private TimeRange eventTimeRange(Event event){
      return event.getWhen();
  }

  private int eventStartTime(Event event){
      return event.getWhen().start();
  }

  private int eventDuration(Event event){
      return event.getWhen().duration();
  }

  private int eventEndTime(Event event){
      return event.getWhen().end();
  }

  private void populateMixedTimeRanges(){

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
      
    for(TimeRange optionalTimeRange: optionalTimeRanges){
    
        //current gap between the current optional time range and the previous optional time range
        TimeRange currentGap = setCurrentGap(optionalTimeRange, previousOptionalTimeRange);

        for(TimeRange mandatoryTimeRange: mandatoryTimeRanges){
    
            //updating previousOptionalAttendeesTimeRange
            int newStartTime = optionalTimeRange.start();
            int newDuration = optionalTimeRange.duration();
            previousOptionalTimeRange = TimeRange.fromStartDuration(newStartTime, newDuration);
    
            if(thisContainsThat(currentGapOptionalTimeRange, mandatoryTimeRange)){
                //The time range can automatically be added since the currentMandatoryTimeRange has the appropriate duration
                mixedTimeRanges.add(mandatoryTimeRange);
            }
            else if(thisContainsThat(mandatoryTimeRange, currentGapOptionalTimeRange)){
                if(currentGapOptionalTimeRangeHasAppropriateDuration(currentGapOptionalTimeRange, requestDuration)){
                    mixedTimeRanges.add(currentGapOptionalTimeRange);
                }
            }
            else if(thisOnlyContainsThatStart(currentGapOptionalTimeRange, mandatoryTimeRange)){
                int temporaryDuration = currentGapOptionalTimeRange.end() - mandatoryTimeRange.start();
                if(temporaryDuration >= requestDuration){
                    mixedTimeRanges.add(
                        TimeRange.fromStartDuration(mandatoryTimeRange.start(), temporaryDuration)
                    );
                }
            }
            else if(thisOnlyContainsThatEnd(currentGapOptionalTimeRange, mandatoryTimeRange)){
                int temporaryDuration = mandatoryTimeRange.end() - currentGapOptionalTimeRange.start();
                if(temporaryDuration >= requestDuration){
                    mixedTimeRanges.add(
                        TimeRange.fromStartDuration(currentGapOptionalTimeRange.start(), temporaryDuration)
                    );
                }
            }
        }
    }
  }

  private boolean thisContainsThatStart(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisTimeRange.containsStart(thatTimeRange.start());
  }

  private boolean thisContainsThatEnd(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisTimeRange.containsEnd(thatTimeRange.end());
  }

  private boolean thisContainsThat(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisContainsThatStart(thisTimeRange, thatTimeRange) &&
            thisContainsThatEnd(thisTimeRange, thatTimeRange);
  }

  private boolean thisOnlyContainsThatStart(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return thisContainsThatStart(thisTimeRange, thatTimeRange) &&
            !thisContainsThatEnd(thisTimeRange, thatTimeRange);
  }

  private boolean thisOnlyContainsThatEnd(TimeRange thisTimeRange, TimeRange thatTimeRange){
      return !thisContainsThatStart(thisTimeRange, thatTimeRange) &&
            thisContainsThatEnd(thisTimeRange, thatTimeRange);
  }

  private TimeRange setCurrentGapOptionalTimeRange(TimeRange optionalTimeRange, 
                                                TimeRange previousOptionalTimeRange){
      int duration = optionalTimeRange.start() - previousOptionalTimeRange.end();
      int start = previousOptionalTimeRange.end();
      return TimeRange.fromStartDuration(start, duration);
  }

  private boolean currentGapOptionalTimeRangeHasAppropriateDuration(TimeRange currentGapOptionalTimeRange,
                                                                    long requestDuration){
      return currentGapOptionalTimeRange.duration() >= requestDuration;
  }  

}