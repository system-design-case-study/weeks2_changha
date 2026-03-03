package com.systemdesign.weeks2_changha.friend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryFriendRelationServiceTest {

	@Test
	void normalizesRelationAsBidirectionalAndSkipsInvalidValues() {
		FriendRelationProperties properties = new FriendRelationProperties();
		properties.setRelations(Map.of(
			"user-a", List.of("user-b", "user-c", "user-a", ""),
			"user-d", List.of("user-e")
		));

		InMemoryFriendRelationService service = new InMemoryFriendRelationService(properties);

		assertThat(service.findFriendIds("user-a")).containsExactlyInAnyOrder("user-b", "user-c");
		assertThat(service.findFriendIds("user-b")).containsExactly("user-a");
		assertThat(service.findFriendIds("user-c")).containsExactly("user-a");
		assertThat(service.findFriendIds("user-d")).containsExactly("user-e");
		assertThat(service.findFriendIds("user-e")).containsExactly("user-d");
		assertThat(service.findFriendIds("unknown")).isEmpty();
	}
}
