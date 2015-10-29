package hhup.service;

import hhup.model.activity.Activity;
import hhup.model.exception.ActivityNotFoundException;
import hhup.model.exception.UserNotFoundException;
import hhup.model.history.HistoryType;
import hhup.model.user.InternalUserInfo;
import hhup.repository.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

@Service
public class ActivitiesService {

	private Logger log = LoggerFactory.getLogger(ActivitiesService.class);

	@Autowired
	@Qualifier("ActivityRepository")
	private Repository<Activity> activities;

	@Autowired
	private UserService users;

	@Autowired
	private HistoryService history;

	public List<Activity> getAllActivities() {
		return ImmutableList.copyOf(activities);
	}

	public void createActivity(Activity activity) throws UserNotFoundException {
		log.info("creating activity: {}", activity);
		activity.setId(UUID.randomUUID());
		activity.setParticipants(Sets.<UUID> newHashSet());
		activities.add(activity);

		InternalUserInfo user = users.getCurrentUser();
		String message = createActivityMessage(user, activity);
		history.recordEvent(user.getId(), HistoryType.CREATED_ACTIVITY, message, activity.getId());
	}

	public Activity getActivity(final UUID id) throws ActivityNotFoundException {
		Activity activity = activities.findFirst(x -> x.getId().equals(id));

		if (activity == null) { throw new ActivityNotFoundException(); }

		return activity;
	}

	public Set<Activity> getActivitiesForUser(final UUID id) {
		return activities.findAll(x -> x.getParticipants().contains(id));
	}

	public void removeActivity(final UUID id) throws ActivityNotFoundException, UserNotFoundException {
		Activity activity = activities.findFirst(x -> x.getId().equals(id));
		boolean removed = activities.remove(activity);

		if (removed) {
			InternalUserInfo user = users.getCurrentUser();
			String message = activityRemovedMessage(user, activity);
			history.recordEvent(user.getId(), HistoryType.DELETED_ACTIVITY, message, id);
		} else {
			throw new ActivityNotFoundException();
		}
	}

	public void editActivity(Activity activity) throws ActivityNotFoundException, UserNotFoundException {
		if (!activities.replace(activity)) { throw new ActivityNotFoundException(); }
		InternalUserInfo user = users.getCurrentUser();
		String message = editActivityMessage(user, activity);
		history.recordEvent(user.getId(), HistoryType.EDITED_ACTIVITY, message, activity.getId());
	}

	public void joinActivity(final UUID id) throws ActivityNotFoundException, UserNotFoundException {
		// TODO: check participants limit, check time collision with users other activities

		InternalUserInfo user = users.getCurrentUser();
		Activity activity = getActivity(id);
		activity.addParticipant(user.getId());
		activities.replace(activity);

		String message = joinMessage(user, activity);
		history.recordEvent(user.getId(), HistoryType.JOINED_ACTIVITY, message, activity.getId());
	}

	public void leaveActivity(UUID id) throws ActivityNotFoundException, UserNotFoundException {
		InternalUserInfo user = users.getCurrentUser();
		Activity activity = getActivity(id);
		activity.removeParticipant(user.getId());
		activities.replace(activity);

		String message = leftMessage(user, activity);
		history.recordEvent(user.getId(), HistoryType.LEFT_ACTIVITY, message, activity.getId());
	}

	public Set<Activity> getConflicts(UUID id) throws ActivityNotFoundException, UserNotFoundException {
		Activity activity = getActivity(id);

		if (!activity.isCanCollide()) {
			return Collections.emptySet();
		}

		return getActivitiesForUser(users.getCurrentUser().getId()).stream()
			.filter(userActivity -> userActivity.isCanCollide())
			.filter(userActivity -> userActivity.overlapsWith(activity))
			.filter(userActivity -> !userActivity.getId().equals(activity.getId())) // no conflict with self
			.collect(Collectors.toSet());
	}

	private String createActivityMessage(InternalUserInfo user, Activity activity) {
		return user.getUsername() + " created the activity " + activity.getTitle();
	}

	private String activityRemovedMessage(InternalUserInfo user, Activity activity) {
		return user.getUsername() + " deleted the activity " + activity.getTitle();
	}

	private String editActivityMessage(InternalUserInfo user, Activity activity) {
		return user.getUsername() + " edit the activity " + activity.getTitle();
	}

	private String joinMessage(InternalUserInfo user, Activity activity) {
		return user.getUsername() + " joined the activity " + activity.getTitle();
	}

	private String leftMessage(InternalUserInfo user, Activity activity) {
		return user.getUsername() + " left the activity " + activity.getTitle();
	}
}