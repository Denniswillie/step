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

public final class FindMeetingQuery {

    /*The algorithm works by sorting the events based on the start time (ascending). After 
    that it loops through the eventsArray and see if the timerange for the particular
    event that it's looping is eligible to be processed. If it's eligible, then the
    it will look through the collection of attendees, if it manages to find an attendee
    that is also in the requestedAttendees array, then we will work out the currentStartTime
    and currentDuration to find out if there's an empty time slot to be inserted to the
    timeRangesForRequestedEvent list.
    */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

      String[] requestAttendees = request.getAttendees().toArray(new String[]{});
      long requestDuration = request.getDuration();
      List<TimeRange> timeRangesForRequestedEvent = new ArrayList<TimeRange>();
      Event[] eventsArray = events.toArray(new Event[]{});

      QuickSort.sort(eventsArray, 0, eventsArray.length - 1);

      int currentStartTime = 0;
      int currentDuration = 0;

      for(Event event: eventsArray){

          TimeRange eventTimeRange = event.getWhen();
          int eventStartTime = eventTimeRange.start();
          int eventDuration = eventTimeRange.duration();
          int eventEndTime = eventStartTime + eventDuration;
          int currentEndTime = currentStartTime + currentDuration;
          
          if(eventStartTime >= currentStartTime && eventEndTime <= currentEndTime){
              currentStartTime = eventStartTime;
              currentDuration = currentEndTime - currentStartTime;
              continue;
          }
          
          else{
              for(String attendee: event.getAttendees()){
                  if(Arrays.asList(requestAttendees).contains(attendee)){
                      if(eventStartTime == currentStartTime && currentDuration == 0){
                          currentDuration = eventDuration;
                      }
                      else if(eventStartTime > currentStartTime && currentDuration == 0){
                          int timeRangeDuration = eventStartTime - currentStartTime;
                          if(timeRangeDuration >= requestDuration){
                              timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentStartTime, timeRangeDuration));
                          }
                          currentStartTime = eventStartTime;
                          currentDuration = eventDuration;
                      }
                      else if(eventEndTime > currentEndTime && currentDuration != 0){
                          int timeRangeDuration = eventStartTime - currentEndTime;
                          if(eventStartTime > currentEndTime && timeRangeDuration >= requestDuration){
                              timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentEndTime, timeRangeDuration));
                          }
                          currentStartTime = eventStartTime;
                          currentDuration = eventDuration;
                      }
                      break;
                  }
              }
          }
      }
      
      int currentEndTime = currentStartTime + currentDuration;
      int endOfDay = 24 * 60;
      int timeLeftBetweenEndOfDayAndCurrentEndTime = endOfDay - currentEndTime;
      if(currentEndTime < endOfDay && timeLeftBetweenEndOfDayAndCurrentEndTime >= requestDuration){
          timeRangesForRequestedEvent.add(TimeRange.fromStartDuration(currentEndTime, timeLeftBetweenEndOfDayAndCurrentEndTime));
      }

      return timeRangesForRequestedEvent;

  }
}