package hhup.web.controller;

import hhup.model.couchmarket.CouchMarketPost;
import hhup.model.exception.PostNotFoundException;
import hhup.model.exception.UnauthorizedException;
import hhup.model.exception.UserNotFoundException;
import hhup.service.CouchMarketService;
import hhup.web.request.CouchMarketPostRequest;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CouchMarketController {

	@Autowired
	private CouchMarketService posts;

	@RequestMapping(value = "/rest/couchmarket", method = RequestMethod.GET)
	@ResponseBody
	public List<CouchMarketPost> getPosts() {
		return posts.getAll();
	}

	@RequestMapping(value = "/rest/couchmarket/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CouchMarketPost getPost(@PathVariable UUID id, HttpServletResponse response) {
		try {
			return posts.getPost(id);
		} catch (PostNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}
	}

	@RequestMapping(value = "/couchmarket", method = RequestMethod.POST)
	@ResponseBody
	public void createPost(@RequestBody CouchMarketPostRequest request, HttpServletResponse response) {
		try {
			posts.create(request);
		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		}
	}

	@RequestMapping(value = "/couchmarket/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void removePost(@PathVariable UUID id, HttpServletResponse response) {
		try {
			posts.remove(id);
		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		} catch (UserNotFoundException | PostNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@RequestMapping(value = "/couchmarket/edit", method = RequestMethod.POST)
	@ResponseBody
	public void editPost(@RequestBody CouchMarketPost editedPost, HttpServletResponse response) {
		try {
			posts.edit(editedPost);
		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		} catch (UserNotFoundException | PostNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}
}