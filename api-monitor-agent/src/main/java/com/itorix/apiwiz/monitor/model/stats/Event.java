package com.itorix.apiwiz.monitor.model.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {
	public long timestamp;
	public String eventID;
	public String status;
	public int latency;
}
