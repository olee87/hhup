package hhup.web.controller;

import hhup.model.activity.Activity;
import hhup.model.exception.ActivityNotFoundException;
import hhup.model.exception.UserNotFoundException;
import hhup.service.ActivitiesService;
import hhup.web.request.UUIDRequest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ActivitiesController {
	private final Logger log = LoggerFactory.getLogger(ActivitiesController.class);

	@Value("${hhup.repository}")
	private String repo;

	@Autowired
	private ActivitiesService activities;

	@RequestMapping(value = "/rest/activities", method = RequestMethod.GET)
	@ResponseBody
	public List<Activity> getEvents() {
		return activities.getAllActivities();
	}

	@Secured("ADMIN")
	@RequestMapping(value="/rest/uploadImage", method=RequestMethod.POST)
	public void handleFileUpload(@RequestBody MultipartFile file, HttpServletResponse response) throws IOException{
		log.info("file is being uploaded");
		UUID imageId = UUID.randomUUID();
		if (file.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "the file is empty");
			return;
		}

		ImageFormat format;
		try {
			format = Sanselan.guessFormat(file.getBytes());
		} catch (ImageReadException e1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid image format");
			return;
		}

		if (!(format.equals(ImageFormat.IMAGE_FORMAT_PNG) || format.equals(ImageFormat.IMAGE_FORMAT_JPEG))) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "only PNG or JPEG permitted");
			return;
		}

		File dir = new File(repo + "uploadedImages");
		if (!dir.exists()) {
			dir.mkdir();
		}
		String imageFile = imageId.toString() + "." + format.extension.toLowerCase();
		try (BufferedOutputStream stream =
				new BufferedOutputStream(new FileOutputStream(new File(dir, imageFile)))) {
			byte[] bytes = file.getBytes();
			stream.write(bytes);
			response.setStatus(HttpServletResponse.SC_OK);
			response.addHeader("imageFile", imageFile);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	@RequestMapping(value="/rest/images/{imageFile}.{ext}", method=RequestMethod.GET)
	public void getImage(@PathVariable String imageFile, @PathVariable String ext, HttpServletResponse response) throws IOException{

		File image = new File(repo + "uploadedImages/" + imageFile + "." + ext);
		if (!image.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "no such image");
			return;
		}

		response.getOutputStream().write(FileUtils.readFileToByteArray(image));
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@RequestMapping(value = "/rest/activities/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Activity getActivity(@PathVariable UUID id, HttpServletResponse response) {
		try {
			return activities.getActivity(id);
		} catch (ActivityNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}
	}

	@Secured("USER")
	@RequestMapping(value = "/rest/activities/join", method = RequestMethod.POST)
	@ResponseBody
	public void joinActivity(@RequestBody UUIDRequest request, HttpServletResponse response) {
		try {
			activities.joinActivity(request.getId());
		} catch (ActivityNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		}
	}

	@Secured("USER")
	@RequestMapping(value = "/rest/activities/leave", method = RequestMethod.POST)
	@ResponseBody
	public void leaveActivity(@RequestBody UUIDRequest request, HttpServletResponse response) {
		try {
			activities.leaveActivity(request.getId());
		} catch (ActivityNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		}
	}

	@Secured("USER")
	@RequestMapping(value = "/rest/activities/forUser/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Set<Activity> getEventsForUser(@PathVariable UUID id) {
		return activities.getActivitiesForUser(id);
	}

	@Secured("USER")
	@RequestMapping(value = "/rest/activities/conflicts/{activityId}", method = RequestMethod.GET)
	@ResponseBody
	public Set<Activity> getConflicts(@PathVariable UUID activityId) throws ActivityNotFoundException,
			UserNotFoundException {
		return activities.getConflicts(activityId);
	}
}