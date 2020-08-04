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

import com.google.common.collect.Sets; 
import com.google.common.collect.Sets.SetView;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;

/** FindMeetingQuery is the container class for the public method query. */
public final class FindMeetingQuery {

    /**
    * Finds the timeslots that are available for both mandatory and optional attendees. 
    * If there's no timeslots to accommodate both type of attendees, then it will find the 
    * timeslots only for the mandatory attendees. Each timeslot will have a duration
    * equal or longer than the requested duration. 
    *
    * @param events a collection of events that have timeranges and attendees for each 
    * corresponding event.
    * @param request meeting request containing a collection of mandatory and 
    *                optional attendees, and duration for the requested event.
    */
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        List<Event> eventsList = new ArrayList(events);

        // EventsList must be sorted so the three TimeRanges can be populated 
        // starting from the beginning to end of day.
        Collections.sort(eventsList, Event.ORDER_BY_TIMERANGE_START_TIME);

        // AVAILABLE time slots for mandatory attendees
        List<TimeRange> mandatoryTimeRanges = getMandatoryTimeRanges(request, eventsList);
      
        // UNAVAILABLE time slots for optional attendees
        List<TimeRange> optionalTimeRanges = getOptionalTimeRanges(request, eventsList);

        if (mandatoryTimeRanges.isEmpty() || optionalTimeRanges.isEmpty()) {
            return mandatoryTimeRanges;
        }            

        // AVAILABLE time slots that accommodate all mandatory and optional attendees
        List<TimeRange> mixedTimeRanges = getMixedTimeRanges(mandatoryTimeRanges, 
                                                            optionalTimeRanges, request);

        return (mixedTimeRanges.isEmpty()) ? mandatoryTimeRanges : mixedTimeRanges;
    }

    /**
    * Look through the {@code eventsList} to see if there's any intersections between the event's 
    * attendees and mandatory attendees. This method uses {@code window} to mark the longest filled
    * {@code TimeRange} after the last free {@code TimeRange}. At the start of the iteration, 
    * {@code window} represents the start of day. When the window is renewed, then the gap between
    * the previous and the new window will be added to {@code mandatoryTimeRanges} if fits the 
    * {@code request} duration. 
    */
    private List<TimeRange> getMandatoryTimeRanges(MeetingRequest request, List<Event> eventsList) {
        List<TimeRange> mandatoryTimeRanges = new ArrayList<>();
        Set<String> mandatoryAttendees = new HashSet<>(request.getAttendees());
        TimeRange window = TimeRange.fromStartDuration(0,0);
        for (Event event : eventsList) {
            Set<String> eventAttendees = event.getAttendees();
            SetView<String> intersection = Sets.intersection(mandatoryAttendees, eventAttendees);
            if (!intersection.isEmpty()) {
                if (event.getWhen().start() <= window.end()) {
                    int newStartTime = Math.min(event.getWhen().start(), window.start());
                    int newEndTime = Math.max(event.getWhen().end(), window.end());
                    window = TimeRange.fromStartEnd(newStartTime, newEndTime, false);
                }
                else {
                    if (event.getWhen().start() - window.end() >= request.getDuration()) {
                        mandatoryTimeRanges
                            .add(TimeRange.fromStartEnd(window.end(), event.getWhen().start(), false));
                    }
                    window = TimeRange
                        .fromStartEnd(event.getWhen().start(), event.getWhen().end(), false);
                }
            }
        }
        if (TimeRange.END_OF_DAY + 1 - window.end() >= request.getDuration()) {
            mandatoryTimeRanges.add(TimeRange.fromStartEnd(window.end(), TimeRange.END_OF_DAY, true));
        }
        return Collections.unmodifiableList(mandatoryTimeRanges);
    }

    /**
    * Find the events that have {@code optionalAttendees}. Add the {@code TimeRange} 
    * only if the {@code event} starts after the last {@code TimeRange}. Otherwise, we
    * modify the last {@code TimeRange} to fit the current {@code event}.
    */
    private List<TimeRange> getOptionalTimeRanges(MeetingRequest request, List<Event> eventsList) {
        List<TimeRange> optionalTimeRanges = new LinkedList<>();
        Set<String> optionalAttendees = new HashSet<>(request.getOptionalAttendees());
        for (Event event : eventsList) {
            int lastElement = optionalTimeRanges.size() - 1;
            Set<String> eventAttendees = event.getAttendees();
            SetView<String> intersection = Sets.intersection(eventAttendees, optionalAttendees);
            if (!intersection.isEmpty()) {
                if (optionalTimeRanges.isEmpty()) {
                    optionalTimeRanges.add(event.getWhen());
                }
                else if (event.getWhen().start() <= optionalTimeRanges.get(lastElement).end()) {
                    int newStartTime = Math
                        .min(event.getWhen().start(), optionalTimeRanges.get(lastElement).start());
                    int newEndTime = Math
                        .max(event.getWhen().end(), optionalTimeRanges.get(lastElement).end());
                    optionalTimeRanges
                        .set(lastElement, TimeRange.fromStartEnd(newStartTime, newEndTime, false));
                }
                else {
                    optionalTimeRanges.add(event.getWhen());
                }
            }
        }
        return optionalTimeRanges;
    }

    /**
    * This method uses {@code currentGap}, which is the empty timeslot between the current 
    * {@code optionalTimeRange} and the {@code previousOptionalTimeRange}. If there's no 
    * {@code optionalTimeRange} that reaches the end of day, then the end of day will be
    * added to the {@code optionalTimeRanges} as a point in time. Note that each gap is 
    * not a free time slot for all attendees, only for {@code optionalAttendees}, so we 
    * still have to take into account the free timeslots in {@code mandatoryTimeRanges}.
    * If a free time slot for {@code optionalAttendees} is not free for {@code mandatoryAttendees},
    * then the free time slot won't be added. If a free time slot for {@code optionalAttendees} 
    * exists and also accommodate a {@code mandatoryTimeRange}, then it will be added to 
    * {@code mixedTimeRanges}.
    */
    private List<TimeRange> getMixedTimeRanges(List<TimeRange> mandatoryTimeRanges, 
                                                List<TimeRange> optionalTimeRanges, 
                                                MeetingRequest request) {
        if (optionalTimeRanges
                .get(optionalTimeRanges.size() - 1).end() < TimeRange.END_OF_DAY + 1) {
            optionalTimeRanges.add(TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 0));
        }
        List<TimeRange> mixedTimeRanges = new ArrayList<>();
        TimeRange previousOptionalTimeRange = TimeRange.fromStartDuration(0, 0);
        for (TimeRange optionalTimeRange : optionalTimeRanges) {
            int gapStart = previousOptionalTimeRange.end();
            int gapEnd = optionalTimeRange.start();
            TimeRange currentGap = TimeRange.fromStartEnd(gapStart, gapEnd, false);
            previousOptionalTimeRange = optionalTimeRange;
            for (TimeRange mandatoryTimeRange : mandatoryTimeRanges) {
                if (currentGap.contains(mandatoryTimeRange)) {
                    mixedTimeRanges.add(mandatoryTimeRange);
                }
                else if(mandatoryTimeRange.contains(currentGap) 
                        && currentGap.duration() >= request.getDuration()) {
                    mixedTimeRanges.add(currentGap);
                }
                else if (currentGap.contains(mandatoryTimeRange.start()) 
                        && currentGap.end() - mandatoryTimeRange.start() >= request.getDuration()) {
                    mixedTimeRanges.add(TimeRange
                        .fromStartEnd(mandatoryTimeRange.start(), currentGap.end(), false));
                }
                else if (currentGap.contains(mandatoryTimeRange.end()) 
                        && mandatoryTimeRange.end() - currentGap.start() >= request.getDuration()) {
                    mixedTimeRanges.add(TimeRange
                        .fromStartEnd(currentGap.start(), mandatoryTimeRange.end(), false));
                }
            }
        }
        return Collections.unmodifiableList(mixedTimeRanges);
    }
}