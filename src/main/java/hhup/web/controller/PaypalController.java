package hhup.web.controller;

import hhup.config.PaypalConfig;
import hhup.service.PaypalService;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller
@RequestMapping("/rest")
public class PaypalController {
	private Logger log = LoggerFactory.getLogger(PaypalController.class);

	private final static String CONTENT_TYPE = "Content-Type";
	private final static String MIME_APP_URLENC = "application/x-www-form-urlencoded";

	private final static String PAY_PAL_DEBUG = "https://www.sandbox.paypal.com/cgi-bin/webscr";
	private final static String PAY_PAL_PROD = "https://www.paypal.com/cgi-bin/webscr";

	private final static String PARAM_NAME_CMD = "cmd";
	private final static String PARAM_VAL_CMD = "_notify-validate";

	@Autowired
	private PaypalService paypal;

	@Autowired
	private PaypalConfig config;

	@RequestMapping(value="/rest/paypalConfig", method=RequestMethod.GET )
	@ResponseBody
	public PaypalConfig getConfig() {
		return config;
	}

	@RequestMapping(value="/paypal", method=RequestMethod.POST )
	public void processIPN(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.trace("paypal: payment info received");

		Map<String, String> params = Maps.newHashMap();

		HttpPost httpPost = new HttpPost(config.isSandbox() ? PAY_PAL_DEBUG : PAY_PAL_PROD );
		httpPost.setHeader(CONTENT_TYPE, MIME_APP_URLENC);

		//Use name/value pair for building the encoded response string
		List<NameValuePair> nameValuePairs = Lists.newArrayList();

		//Append the required command
		nameValuePairs.add(new BasicNameValuePair(PARAM_NAME_CMD, PARAM_VAL_CMD));

		//Process the parameters
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String param = names.nextElement();
			String value = request.getParameter(param);

			nameValuePairs.add(new BasicNameValuePair(param, value));
			params.put(param, value);
		}

		try {
			httpPost.setEntity( new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			log.error("error encoding name-value-pairs for the mirror request!", e);
		}


		Executors.newSingleThreadExecutor().submit(() -> paypal.processPayment(params, httpPost));
		response.setStatus(HttpServletResponse.SC_OK);
	}
}