package com.systemdesign.weeks2_changha.socketio;

public class SocketIoLocationEvent {

	private String userId;
	private Double lat;
	private Double lon;
	private Long sequence;
	private Long clientTs;
	private Long serverTs;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	public Long getClientTs() {
		return clientTs;
	}

	public void setClientTs(Long clientTs) {
		this.clientTs = clientTs;
	}

	public Long getServerTs() {
		return serverTs;
	}

	public void setServerTs(Long serverTs) {
		this.serverTs = serverTs;
	}
}
