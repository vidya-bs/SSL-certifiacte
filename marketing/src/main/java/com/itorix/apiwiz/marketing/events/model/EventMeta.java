package com.itorix.apiwiz.marketing.events.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.marketing.common.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
@Data
@Slf4j
public class EventMeta implements Comparable<EventMeta>{
    private String name;
    private String eventDate;
    private String location;
    private String category;
    private String summary;
    private String description;
    private String image;
    private String bannerImage;
    private String thumbnailImage;
    private String title;
    private String eventTime;
    private String slug;
    private List<Tag>tags;
    private String videoUrl;
    private String registrationUrl;
    private List<EventSpeaker>eventSpeakers;


    @JsonIgnore
    public Date getEventDateOn() throws ParseException {
        return new SimpleDateFormat("MM/dd/yyyy").parse(eventDate);
    }

    @Override
    public int compareTo(EventMeta o) {
        try {
            if (getEventDateOn() == null || o.getEventDateOn() == null) {
                return 0;
            }
            return getEventDateOn().compareTo(o.getEventDateOn());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getStatus() throws ParseException {
        if (eventDate != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String formattedDate = formatter.format(new Date());
            Date currentDate = formatter.parse(formattedDate);
            if (currentDate.compareTo(getEventDateOn()) < 0)
                return "active";
        }
        return "expired";
    }
}
