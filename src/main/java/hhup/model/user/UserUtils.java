package hhup.model.user;

import java.util.Set;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

public class UserUtils {

	private UserUtils() { /* no instances */}

	public static Set<PublicUserInfo> toPublicUserInfos(Set<InternalUserInfo> input) {
		return Sets.newHashSet(Collections2.transform(input, user -> UserBuilder.from(user).asPublicUserInfo()));
	}
}