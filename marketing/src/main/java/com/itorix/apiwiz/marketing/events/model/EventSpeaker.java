package com.itorix.apiwiz.marketing.events.model;

import lombok.Data;

import java.util.List;

@Data
public class EventSpeaker {
    private String name;
    private String summary;
    private String designation;
    private String image;
    private List<SpeakerProfileLink>profileLinks;
}
