package hhup.service;

import hhup.model.couchmarket.CouchMarketPost;
import hhup.model.couchmarket.CouchMarketPost.Type;
import hhup.model.exception.PostNotFoundException;
import hhup.model.exception.UnauthorizedException;
import hhup.model.exception.UserNotFoundException;
import hhup.model.history.HistoryType;
import hhup.model.user.Authority;
import hhup.model.user.InternalUserInfo;
import hhup.repository.Repository;
import hhup.web.request.CouchMarketPostRequest;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

@Service
public class CouchMarketService {

	@Autowired
	@Qualifier("CouchMarketPostRepository")
	private Repository<CouchMarketPost> posts;

	@Autowired
	private UserService users;

	@Autowired
	private HistoryService history;

	public List<CouchMarketPost> getAll() {
		return ImmutableList.copyOf(posts);
	}

	public void create(CouchMarketPostRequest request) throws UnauthorizedException {
		int count = request.getCount() != null ? request.getCount() : 1;
		Type type = request.getType() != null ? Type.valueOf(request.getType()) : Type.OFFER;
		String text = StringUtils.defaultIfBlank(request.getText(), "");
		InternalUserInfo user;
		try {
			user = users.getCurrentUser();
		} catch (UserNotFoundException e) {
			throw new UnauthorizedException();
		}

		posts.add(new CouchMarketPost(type, text, user.getId(), count, DateTime.now(), UUID.randomUUID()));

		String message = postedCouchMarketMessage(user, type, count, text);
		history.recordEvent(user.getId(), HistoryType.POSTED_COUCH_MARKET, message);
	}

	public boolean remove(UUID id) throws UnauthorizedException, UserNotFoundException, PostNotFoundException {
		InternalUserInfo user = users.getCurrentUser();
		CouchMarketPost post = posts.findFirst(x -> x.getId().equals(id));

		if (post == null) { throw new PostNotFoundException(id); }

		if (user.getAuthorities().contains(Authority.ADMIN) || post.getUserId().equals(user.getId())) {
			boolean remove = posts.remove(post);

			String message = deletedCouchMarketMessage(user, post);
			history.recordEvent(user.getId(), HistoryType.DELETED_COUCH_MARKET, message, id);
			return remove;
		}

		throw new UnauthorizedException();
	}

	public void edit(CouchMarketPost edited) throws UserNotFoundException, PostNotFoundException, UnauthorizedException {
		InternalUserInfo user = users.getCurrentUser();
		CouchMarketPost post = posts.findFirst(x -> x.getId().equals(edited.getId()));

		if (post == null) { throw new PostNotFoundException(edited.getId()); }

		if (user.getAuthorities().contains(Authority.ADMIN) || post.getUserId().equals(user.getId())) {
			posts.replace(edited);

			String message = editedCouchMarketMessage(user, post, edited);
			history.recordEvent(user.getId(), HistoryType.EDITED_COUCH_MARKET, message, edited.getId());
			return;
		}

		throw new UnauthorizedException();
	}

	public CouchMarketPost getPost(UUID id) throws PostNotFoundException {
		CouchMarketPost post = posts.findFirst(x -> x.getId().equals(id));

		if (post == null) { throw new PostNotFoundException(id); }

		return post;
	}

	private String postedCouchMarketMessage(InternalUserInfo user, Type type, int count, String text) {
		String typeName = type == Type.OFFER ? "offer" : "search";
		return user.getUsername() + " posted a couch" + typeName + " for " + count + " spots: '" + text + "'";
	}

	private String editedCouchMarketMessage(InternalUserInfo user, CouchMarketPost post, CouchMarketPost edited) {
		String oldType = post.getType() == Type.OFFER ? "offer" : "search";
		String newType = edited.getType() == Type.OFFER ? "offer" : "search";
		String oldPost = "couch" + oldType + " for " + post.getCount() + " spots: '" + post.getText() + "'";
		String newPost = "couch" + newType + " for " + edited.getCount() + " spots: '" + edited.getText() + "'";
		return user.getUsername() + " changed the " + oldPost + " to a " + newPost;
	}

	private String deletedCouchMarketMessage(InternalUserInfo user, CouchMarketPost post) {
		String typeName = post.getType() == Type.OFFER ? "offer" : "search";
		return user.getUsername() + " deleted the couch" + typeName + " for " + post.getCount() + " spots: '" + post.getText() + "'";
	}
}