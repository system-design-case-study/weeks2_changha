package com.systemdesign.weeks2_changha.friend;

import java.util.Set;

public interface FriendRelationService {

	Set<String> findFriendIds(String userId);

	default boolean areFriends(String userId, String targetUserId) {
		return findFriendIds(userId).contains(targetUserId);
	}
}
