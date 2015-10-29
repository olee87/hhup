package hhup.web.request;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PaypalPaymentInfo {

	Logger log = LoggerFactory.getLogger(PaypalPaymentInfo.class);

	public static class PaypalPaymentUserInfo {

		private UUID payingUser;
		private List<UUID> paidFor;

		public List<UUID> getPaidFor() {
			return paidFor;
		}

		public UUID getPayingUser() {
			return payingUser;
		}
	}

	private String paymentId;
	private String paymentStatus;
	private String payerEmail;
	private String payerName;
	private DateTime date;
	private int quantity;
	private double amount;
	private double fee;

	private PaypalPaymentUserInfo userInfo;

	public PaypalPaymentInfo(Map<String, String> requestParams) throws Exception {
		String userIdsString = requestParams.get("custom");
		if (StringUtils.isNotBlank(userIdsString)) {
			try {
				userInfo = new ObjectMapper().readValue(userIdsString, PaypalPaymentInfo.PaypalPaymentUserInfo.class);
			} catch (Exception e) {
				log.error("PaypalPaymentUserInfo creation failed! String content: '" + userIdsString + "' exception: ", e);
			}
		} else {
			log.error("PaypalPaymentUserInfo creation failed beacause the custom string is blank!");
			throw new Exception("paypal: no user payment info");
		}

		payerName = requestParams.get("first_name") + " " + requestParams.get("last_name");

		if (StringUtils.isBlank(payerName)) {
			payerName = "N/A";
		}

		paymentId = requestParams.get("txn_id");
		if ( StringUtils.isBlank(paymentId) ) {
			log.error("PaypalPaymentUserInfo creation failed beacause the transaction id is missing!");
			throw new Exception("paypal: no payment id");
		}

		paymentStatus = requestParams.get("payment_status");
		if ( StringUtils.isBlank(paymentStatus) ) {
			log.error("PaypalPaymentUserInfo creation failed beacause the payment status is missing!");
			throw new Exception("paypal: no payment status");
		}

		payerEmail= StringUtils.defaultIfEmpty(requestParams.get("payer_email"), "N/A");
		quantity = Integer.valueOf(StringUtils.defaultIfEmpty(requestParams.get("quantity"), "-1"));
		amount = Double.valueOf(StringUtils.defaultIfEmpty(requestParams.get("mc_gross"), "-1"));
		fee = Double.valueOf(StringUtils.defaultIfEmpty(requestParams.get("mc_fee"), "-1"));
		date = DateTime.parse(requestParams.get("payment_date"),
				DateTimeFormat.forPattern("HH:mm:ss MMM d, yyyy zzz").withLocale(Locale.ENGLISH));
	}

	public PaypalPaymentInfo() { /* for jackson */ }

	public boolean isDuplicateOf(PaypalPaymentInfo other) {
		return paymentId.equals(other.getPaymentId()) && paymentStatus.equals(other.getPaymentStatus());
	}

	public String getPaymentId() {
		return paymentId;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public double getAmount() {
		return amount;
	}

	public String getPayerEmail() {
		return payerEmail;
	}

	public String getPayerName() {
		return payerName;
	}

	public int getQuantity() {
		return quantity;
	}

	public PaypalPaymentUserInfo getUserInfo() {
		return userInfo;
	}

	public DateTime getDate() {
		return date;
	}

	public double getFee() {
		return fee;
	}
}