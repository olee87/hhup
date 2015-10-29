package hhup.service;

import hhup.model.user.InternalUserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class StatisticsService {

	@Autowired
	private UserService users;

	@Autowired
	private ActivitiesService activities;

	public Map<String, Object> getAllStatistics() {
		Map<String, Object> result = Maps.newHashMap();

		result.put("userCountByCountry", getUserCountByCountry());
		result.put("paidUserCountByCountry", getPaidUserCountByCountry());
		result.put("averagePaymentDurationByCountry", getAveragePaymentDurationByCountry());
		result.put("paymentRatioByCountry", getPaymentRatioByCountry());
		result.put("signupAndPaymentTimeLine", getSingupAndPaymentTimeline());

		return result;
	}

	private Map<String, Integer> getUserCountByCountry() {
		HashMap<String, Integer> result = Maps.newHashMap();
		for (InternalUserInfo user : users.getAll()) {
			String nationality = user.getNationality();

			if (nationality == null) {
				continue;
			}

			if (result.containsKey(nationality)) {
				result.put(nationality, result.get(nationality) + 1);
			} else {
				result.put(nationality, 1);
			}
		}

		return result;
	}

	private Map<String, Integer> getPaidUserCountByCountry() {
		HashMap<String, Integer> result = Maps.newHashMap();
		for (InternalUserInfo user : users.getAll()) {
			String nationality = user.getNationality();

			if (!user.isPaid() || (nationality == null)) {
				continue;
			}

			if (result.containsKey(nationality)) {
				result.put(nationality, result.get(nationality) + 1);
			} else {
				result.put(nationality, 1);
			}
		}

		return result;
	}

	private Map<String, Double> getPaymentRatioByCountry() {
		HashMap<String, Double> result = Maps.newHashMap();
		Map<String, Integer> userCount = getUserCountByCountry();
		Map<String, Integer> paidUserCount = getPaidUserCountByCountry();

		for (Entry<String, Integer> entry : userCount.entrySet()) {
			Integer paid = paidUserCount.getOrDefault(entry.getKey(), 0);
			Double ratio = new Double(paid) / new Double(entry.getValue());
			result.put(entry.getKey(), ratio);
		}

		return result;
	}

	private Map<String, Duration> getAveragePaymentDurationByCountry() {
		HashMap<String, Duration> total = Maps.newHashMap();
		for (InternalUserInfo user : users.getAll()) {
			String nationality = user.getNationality();

			if (!user.isPaid() || (nationality == null)) {
				continue;
			}

			Duration paymentDuration = new Duration(user.getRegistrationDate(), user.getPayDate());

			if (total.containsKey(nationality)) {
				total.put(nationality, total.get(nationality).withDurationAdded(paymentDuration, 1));
			} else {
				total.put(nationality, paymentDuration);
			}
		}

		HashMap<String, Duration> result = Maps.newHashMap();
		Map<String, Integer> userCount = getPaidUserCountByCountry();
		for (Entry<String, Duration> entry : total.entrySet()) {
			result.put(entry.getKey(), entry.getValue().dividedBy(userCount.get(entry.getKey())));
		}

		return result;
	}

	private List<DataPoint> getSingupAndPaymentTimeline() {
		List<DataPoint> result = Lists.newArrayList();

		List<DateTime> signedUpDates = users.getAll().stream()
			.map(user -> user.getRegistrationDate())
			.sorted()
			.collect(Collectors.toList());

		List<DateTime> payDates = users.getAll().stream()
			.filter(user -> user.isPaid())
			.map(user -> user.getPayDate())
			.sorted()
			.collect(Collectors.toList());

		Integer signedUpCount = 0;
		Integer paidCount = 0;

		while ((signedUpCount < signedUpDates.size()) || (paidCount < payDates.size())) {
			DateTime date;
			if (signedUpCount == signedUpDates.size()) {
				date = payDates.get(paidCount);
				paidCount++;
			} else if (paidCount == payDates.size()) {
				date = signedUpDates.get(signedUpCount);
				signedUpCount++;
			} else {

				if (signedUpDates.get(signedUpCount).isBefore(payDates.get(paidCount))) {
					date = signedUpDates.get(signedUpCount);
					signedUpCount++;
				} else {
					date = payDates.get(paidCount);
					paidCount++;
				}
			}
			result.add(new DataPoint(date, signedUpCount, paidCount));
		}
		return result;
	}

	public class DataPoint {
		public DateTime date;
		public Integer signedUp;
		public Integer paid;

		public DataPoint(DateTime date, Integer signedUp, Integer paid) {
			this.date = date;
			this.signedUp = signedUp;
			this.paid = paid;
		}
	}
}
