package hhup.web.controller;

import hhup.model.exception.ActivityNotFoundException;
import hhup.service.ActivitiesService;
import hhup.service.PDFService;
import hhup.service.SpreadsheetService;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/rest")
public class ExportController {

	@Autowired
	private PDFService pdf;

	@Autowired
	private SpreadsheetService spreadsheet;

	@Autowired
	private ActivitiesService activities;

	@RequestMapping(value = "/pdf/users", method = RequestMethod.GET)
	public void downloadPdfOfAllUsers(HttpServletResponse response) {
		try {
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"hhup_all_participants.pdf\"");
			pdf.createPdfOfAllUsers(response.getOutputStream(), PDFService.SORTING_BY_REAL_NAME);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@RequestMapping(value = "/pdf/activity/{activityId}", method = RequestMethod.GET, produces = "application/x-unknown")
	public void downloadPdfForActivity(HttpServletResponse response, @PathVariable UUID activityId) {
		try {
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"hhup_participants_"
					+ getTitle(activityId) + ".pdf\"");
			pdf.createPdfForActivity(activityId, response.getOutputStream(), PDFService.SORTING_BY_REAL_NAME);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@RequestMapping(value = "/spreadsheet/users", method = RequestMethod.GET)
	public void downloadSpreadsheetOfAllUsers(HttpServletResponse response) {
		try {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=\"hhup_all_participants.xls\"");
			spreadsheet.createSpreadsheetOfAllUsers(response.getOutputStream());
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@RequestMapping(value = "/spreadsheet/activity/{activityId}", method = RequestMethod.GET)
	public void downloadSpreadsheetForActivity(HttpServletResponse response, @PathVariable UUID activityId) {
		try {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=\"hhup_participants_"
					+ getTitle(activityId) + ".xls\"");
			spreadsheet.createSpreadsheetForActivity(activityId, response.getOutputStream());
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	private String getTitle(UUID activityId) throws ActivityNotFoundException {
		return StringUtils.replace(StringUtils.lowerCase(activities.getActivity(activityId).getTitle()), " ", "_");
	}
}