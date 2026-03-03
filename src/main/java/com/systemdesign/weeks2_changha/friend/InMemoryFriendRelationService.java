package com.systemdesign.weeks2_changha.friend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InMemoryFriendRelationService implements FriendRelationService {

	private final Map<String, Set<String>> normalizedRelations;

	public InMemoryFriendRelationService(FriendRelationProperties properties) {
		Map<String, Set<String>> mutable = new HashMap<>();
		properties.getRelations().forEach((userId, friendIds) -> {
			if (!StringUtils.hasText(userId) || friendIds == null) {
				return;
			}
			friendIds.stream()
				.filter(StringUtils::hasText)
				.filter(friendId -> !userId.equals(friendId))
				.forEach(friendId -> {
					mutable.computeIfAbsent(userId, ignored -> new java.util.HashSet<>()).add(friendId);
					mutable.computeIfAbsent(friendId, ignored -> new java.util.HashSet<>()).add(userId);
				});
		});
		this.normalizedRelations = mutable.entrySet().stream()
			.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
	}

	@Override
	public Set<String> findFriendIds(String userId) {
		if (!StringUtils.hasText(userId)) {
			return Set.of();
		}
		return normalizedRelations.getOrDefault(userId, Collections.emptySet());
	}
}
