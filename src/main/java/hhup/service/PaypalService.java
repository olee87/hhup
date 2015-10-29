package hhup.service;

import hhup.repository.Repository;
import hhup.web.request.PaypalPaymentInfo;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaypalService {
	private Logger log = LoggerFactory.getLogger(PaypalService.class);

	private final static String RESP_VERIFIED = "VERIFIED";

	@Value("${hhup.paypal.receiverEmail}")
	private String receiver;

	@Value("${hhup.paypal.costPerUser}")
	private double costPerUser;

	@Autowired
	@Qualifier("PaypalRepository")
	private Repository<PaypalPaymentInfo> repo;

	@Autowired
	private UserService users;

	@Autowired
	private MailService mailer;

	public void processPayment(Map<String, String> params, HttpPost httpPost) {

		PaypalPaymentInfo info;
		try {
			info = new PaypalPaymentInfo(params);
		} catch (Exception e) {
			return;
		}

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			if (log.isTraceEnabled()) {
				log.trace("paypal: sending echo. params: \n" + paramsToString(params));
			}
			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
				if (verifyRequest(info, params) && verifyResponse(response)) {
					log.trace("paypal: payment confirmed, updating user infos, saving payment info and sending confirmation mail.");
					users.setPaypalPaid(info);
					repo.add(info);
					mailer.sendPaypalConfirmedMail(info);
				}
			}
		} catch ( Exception e ) {
			log.error("paypal: error during processing payment info", e);
		}
	}

	private String paramsToString(Map<String, String> params) {
		StringBuilder builder = new StringBuilder();
		params.entrySet().stream().forEach(entry -> builder.append(entry.getKey() + ": " + entry.getValue() + "\n"));
		return builder.toString();
	}

	private boolean verifyRequest(PaypalPaymentInfo info, Map<String, String> params) {
		log.trace("paypal: verifying request validity");
		// from advice from paypal
		// (see https://developer.paypal.com/webapps/developer/docs/classic/ipn/integration-guide/IPNIntro/):
		// 1. check, if the receiver email is correct
		if (!receiver.equals(params.get("receiver_email"))) {
			log.error("paypal: received payment info where receiver eMail differs from configured one, someone"
					+ " is trying to get confirmed without actually sending money! params: " + paramsToString(params));
			return false;
		}

		// 2. check, if this info is a duplicate
		if (repo.findFirst(i -> i.isDuplicateOf(info))!= null) {
			log.error("paypal: received duplicate payment info");
			return false;
		}

		// 3. check, that payment status is "Completed"
		if (!info.getPaymentStatus().equalsIgnoreCase("completed")) {
			log.trace("paypal: received payment info with status '" + info.getPaymentStatus() + "'");
			return false;
		}

		// 4. check, that the amount is correct
		if (info.getAmount() < (info.getUserInfo().getPaidFor().size() * costPerUser)) {
			log.error("paypal: received payment info with potentially manipulated gross amount! params: "
					+ paramsToString(params));
			return false;
		}

		log.trace("paypal: request is valid, continuing processing");
		return true;
	}

	private boolean verifyResponse( HttpResponse response ) throws IllegalStateException, IOException {
		String verifyString = IOUtils.toString(response.getEntity().getContent());
		log.trace("paypal: echo response received, validating. content: '" + verifyString + "'");
		boolean verified = verifyString.equalsIgnoreCase(RESP_VERIFIED);
		return verified;
	}
}