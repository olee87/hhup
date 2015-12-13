package hhup.service;

import hhup.model.activity.Activity;
import hhup.model.history.HistoryType;
import hhup.model.user.InternalUserInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class PDFService {

	public static final String SORTING_BY_USER_NAME = "user name";
	public static final String SORTING_BY_REAL_NAME = "real name";

	private static final int pageHeight = 800;
	private static final int padding = 40;
	private static final int maxEntriesFirstPage = 31;
	private static final int maxEntriesFollowingPages = 34;

	private static final Locale en = new Locale("en", "gb");

	@Autowired
	private UserService users;

	@Autowired
	private ActivitiesService activities;

	@Autowired
	private HistoryService history;

	public void createPdfOfAllUsers(OutputStream destination, String sorting) throws Exception {
		List<InternalUserInfo> allUsers = Lists.newArrayList(users.getAll());
		createPdf(allUsers, sorting, "HHUP - all participants", destination);

		InternalUserInfo requestingUser = users.getCurrentUser();
		String message = requestingUser.getUsername() + " downloaded a PDF of all participants of HHUP";
		history.recordEvent(requestingUser.getId(), HistoryType.DOWNLOADED_PDF, message);
	}

	public void createPdfForActivity(UUID activityId, ServletOutputStream destination, String sorting) throws Exception {
		Activity activity = activities.getActivity(activityId);

		List<InternalUserInfo> participants = Lists.newArrayList();
		for (UUID userId : activity.getParticipants()) {
			participants.add(users.getUserForId(userId));
		}

		createPdf(participants, sorting, activity.getTitle() + " - participants", destination);

		InternalUserInfo requestingUser = users.getCurrentUser();
		String message = requestingUser.getUsername() + " downloaded a PDF of the participants in the activity '"
				+ activity.getTitle() + "'";
		history.recordEvent(requestingUser.getId(), HistoryType.DOWNLOADED_PDF, message, activityId);
	}

	private void createPdf(List<InternalUserInfo> users, String sorting, String title, OutputStream destination)
			throws Exception {

		// Create a document and add a page to it
		try (PDDocument document = new PDDocument()) {
			if (sorting.equals(SORTING_BY_REAL_NAME)) {
				Collections.sort(users, (x, y) -> x.getLastName().compareTo(y.getLastName()));
				Collections.sort(users, (x, y) -> x.getFirstName().compareTo(y.getFirstName()));
			} else {
				Collections.sort(users, (x, y) -> x.getUsername().compareTo(y.getUsername()));
			}

			List<List<InternalUserInfo>> pages = splitIntoPages(users);

			int pageNumber = 1;
			for (List<InternalUserInfo> pageEntries : pages) {

				// Start a new content stream which will "hold" the to be created content
				createUserListingPage(document, title, sorting, pageNumber, pages.size(), users.size(), pageEntries);
				pageNumber++;
			}

			// Save the results and ensure that the document is properly closed:
			document.save(destination);
		}
	}

	private void createUserListingPage(PDDocument doc, String title, String sorting, int pageNumber, int totalPages,
			int totalEntries, List<InternalUserInfo> users) throws IOException {

		PDPage page = new PDPage();
		doc.addPage(page);

		try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {

			boolean firstPage = pageNumber == 1;

			// measures
			int tableTop = 730;
			if (firstPage) {
				tableTop = 670;
			}

			// --==| HEADER | ==--

			if (firstPage) {

				// insert logo
				int logoHeight = 72;
				int logoWidth = 75;
				try {
					stream.drawImage(new PDJpeg(doc, ClassLoader.getSystemResourceAsStream("images/hhup.jpeg")),
							padding, pageHeight - padding - logoHeight);
				} catch (IOException e) {
					e.printStackTrace();
				}

				stream.beginText();
				// place the title
				stream.setFont(PDType1Font.HELVETICA, 28);
				stream.moveTextPositionByAmount(logoWidth + 50, 730);
				stream.drawString(title);

				// page and date info
				stream.setFont(PDType1Font.HELVETICA, 12);
				stream.moveTextPositionByAmount(0, -22);
				stream.drawString("as of " + DateTime.now().toString("MMMM dd, HH:mm", en) + " - page " + pageNumber
						+ " / " + totalPages);

				// total and sorting info
				stream.moveTextPositionByAmount(0, -16);
				stream.drawString("total participants: " + totalEntries + " - sorted by " + sorting);

				stream.endText();
			} else {
				// not first page
				// title and page info
				stream.beginText();
				stream.setFont(PDType1Font.HELVETICA, 12);
				stream.moveTextPositionByAmount(padding, pageHeight - padding);
				stream.drawString(title + " - page " + pageNumber + " / " + totalPages);
				stream.endText();
			}

			// draw the table frame
			for (int i = 0; i <= users.size(); i++) { // one more for the headers
				float y = tableTop - 20 - (i * 20);
				stream.addRect(45, y, 150, 20); // real name or user name
				stream.addRect(195, y, 150, 20); // real name or user name
				stream.addRect(345, y, 65, 20); // nationality
				stream.addRect(410, y, 135, 20); // email
				stream.addRect(545, y, 36, 20); // paid
			}
			stream.stroke();

			// create headers
			stream.beginText();
			stream.setFont(PDType1Font.HELVETICA_BOLD, 12);

			switch (sorting) {
				case SORTING_BY_USER_NAME: {
					// user name
					stream.moveTextPositionByAmount(50, tableTop - 15);
					stream.drawString("user name");

					// real name
					stream.moveTextPositionByAmount(150, 0);
					stream.drawString("real name");

				}
					break;
				default: {
					// real name
					stream.moveTextPositionByAmount(50, tableTop - 15);
					stream.drawString("real name");

					// user name
					stream.moveTextPositionByAmount(150, 0);
					stream.drawString("user name");
				}
			}

			// nationality
			stream.moveTextPositionByAmount(150, 0);
			stream.drawString("country");

			// email
			stream.moveTextPositionByAmount(65, 0);
			stream.drawString("email");

			// paid
			stream.moveTextPositionByAmount(135, 0);
			stream.drawString("paid");

			stream.endText();
			// end of headers

			// --==| User Info |==--
			for (int i = 0; i < users.size(); i++) {
				InternalUserInfo user = users.get(i);

				stream.beginText();
				stream.setFont(PDType1Font.HELVETICA, 12);

				int y = tableTop - 35 - (20 * i);

				switch (sorting) {
					case SORTING_BY_USER_NAME: {
						// user name
						stream.moveTextPositionByAmount(50, y);
						stream.drawString(abbreviate(user.getUsername(), 21));

						// real name
						stream.moveTextPositionByAmount(150, 0);
						stream.drawString(abbreviate(StringUtils.defaultIfBlank(user.getFirstName() + " " + user.getLastName(), "-"), 21));

					}
						break;
					default: {
						// real name
						stream.moveTextPositionByAmount(50, y);
						stream.drawString(abbreviate(StringUtils.defaultIfBlank(user.getFirstName() + " " + user.getLastName(), "-"), 21));

						// user name
						stream.moveTextPositionByAmount(150, 0);
						stream.drawString(abbreviate(user.getUsername(), 21));

					}
				}

				// nationality
				stream.moveTextPositionByAmount(150, 0);
				String nationality = user.getNationality();
				if (nationality != null) {
					stream.drawString(abbreviate(new Locale("", nationality).getDisplayCountry(en), 9));
				} else {
					stream.drawString("-");
				}

				// email
				stream.moveTextPositionByAmount(65, 0);
				stream.drawString(abbreviate(user.getEmail(), 19));

				stream.endText();
				// paid
				if (user.isPaid()) {
					try {
						stream.drawImage(new PDJpeg(doc, ClassLoader.getSystemResourceAsStream("images/tick.jpg")),
								553, y - 4);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private List<List<InternalUserInfo>> splitIntoPages(List<InternalUserInfo> users) {
		List<List<InternalUserInfo>> result = Lists.newArrayList();
		if (users.size() <= maxEntriesFirstPage) {
			result.add(users);
			return result;
		}

		Iterator<InternalUserInfo> iterator = users.iterator();
		List<InternalUserInfo> page = Lists.newArrayList();
		boolean firstPage = true;
		while (iterator.hasNext()) {
			page.add(iterator.next());
			if ((firstPage && (page.size() == maxEntriesFirstPage))
					|| (!firstPage && (page.size() == maxEntriesFollowingPages))) {
				result.add(page);
				page = Lists.newArrayList();
				firstPage = false;
			}
		}

		// add the last page
		if (!page.isEmpty()) {
			result.add(page);
		}

		return result;
	}

	private String abbreviate(String in, int maxLength) {
		if (in.length() <= maxLength) { return in; }
		return in.substring(0, maxLength - 3) + "...";
	}
}