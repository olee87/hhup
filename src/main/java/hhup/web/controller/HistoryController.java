package hhup.web.controller;

import hhup.model.history.HistoryItem;
import hhup.service.HistoryService;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HistoryController {

	@Autowired
	private HistoryService history;

	@RequestMapping(value = "/rest/history", method = RequestMethod.GET)
	@ResponseBody
	public List<HistoryItem> getHistoryFor(
			@RequestParam(value = "userId", required = false) UUID actorOrSubjectId,
			@RequestParam(value = "maxItems", required = false) Integer maxItems,
//			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "actorOnly", required = false) Boolean actorOnly) {

		return history.getHistoryFor(actorOrSubjectId, maxItems != null ? maxItems : -1, BooleanUtils.isTrue(actorOnly));
	}
}