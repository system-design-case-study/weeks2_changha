package com.systemdesign.weeks2_changha.friend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.friends")
public class FriendRelationProperties {

	private Map<String, List<String>> relations = new HashMap<>();

	public Map<String, List<String>> getRelations() {
		return relations;
	}

	public void setRelations(Map<String, List<String>> relations) {
		this.relations = relations;
	}
}
