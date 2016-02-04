package hhup.web.controller;

import hhup.service.StatisticsService;
import hhup.service.UserService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

@Controller
@RequestMapping("/rest")
public class PublicController {

	@Value("${hhup.event.begin}")
	private String begin;

	@Value("${hhup.event.end}")
	private String end;

	@Autowired
	private UserService users;

	@Autowired
	private StatisticsService statistics;

	@RequestMapping(value = "/public/stats", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> getStats() {
		return ImmutableMap.of(
				"numberCountries", users.getCountryCount() + "",
				"numberParticipants", users.getAll().size() + "",
				"eventBegin", begin,
				"eventEnd", end
		);
	}

	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getStatistics() {
		return statistics.getAllStatistics();
	}
}