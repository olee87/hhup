package hhup.service;

import hhup.model.activity.Activity;
import hhup.model.history.HistoryType;
import hhup.model.user.InternalUserInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
public class SpreadsheetService {

	@Autowired
	private UserService users;

	@Autowired
	private ActivitiesService activities;

	@Autowired
	private HistoryService history;

	private static final Locale en = new Locale("en", "gb");

	public void createSpreadsheetOfAllUsers(OutputStream destination) throws Exception {
		List<InternalUserInfo> allUsers = Lists.newArrayList(users.getAll());
		createSpreadsheet(allUsers, "HHUP - all participants", true, destination);

		InternalUserInfo requestingUser = users.getCurrentUser();
		String message = requestingUser.getUsername() + " downloaded a spreadsheet of all the participants of HHUP";
		history.recordEvent(requestingUser.getId(), HistoryType.DOWNLOADED_XLS, message);
	}

	public void createSpreadsheetForActivity(UUID activityId, ServletOutputStream destination) throws Exception {
		Activity activity = activities.getActivity(activityId);

		List<InternalUserInfo> participants = Lists.newArrayList();
		for (UUID userId : activity.getParticipants()) {
			participants.add(users.getUserForId(userId));
		}

		createSpreadsheet(participants, activity.getTitle() + " - participants", false, destination);

		InternalUserInfo requestingUser = users.getCurrentUser();
		String message = requestingUser.getUsername() + " downloaded a spreadsheet of the participants in the activity '"
				+ activity.getTitle() + "'";
		history.recordEvent(requestingUser.getId(), HistoryType.DOWNLOADED_XLS, message, activityId);
	}

	private void createSpreadsheet(List<InternalUserInfo> users, String titleString, boolean listActivities, OutputStream destination)
			throws IOException {

		try (Workbook wb = new HSSFWorkbook()) {
			Map<String, CellStyle> styles = createStyles(wb);
			Sheet sheet = createSheet(titleString, wb);
			createHeader(titleString, sheet, styles, listActivities);

			int[] maxwidths = new int[] { 8, 9, 5, 10, 5, 5, 12, 12, 30};

			for (int i = 0; i < users.size(); i++) {
				createUserRow(sheet, maxwidths, i, users, styles, listActivities);
			}

			// set column widths, the width is measured in units of 1/256th of a character width
			for (int i = 0; i < maxwidths.length; i++) {
				sheet.setColumnWidth(i, 256 * (maxwidths[i] + 3));
			}
			sheet.setZoom(3, 4);

			// Write to the destination stream
			wb.write(destination);
			destination.close();
		}
	}

	private Sheet createSheet(String titleString, Workbook wb) {
		Sheet sheet = wb.createSheet(titleString);

		// turn off gridlines
		sheet.setDisplayGridlines(false);
		sheet.setPrintGridlines(false);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);
		PrintSetup printSetup = sheet.getPrintSetup();

		// the following three statements are required only for HSSF
		sheet.setAutobreaks(true);
		printSetup.setFitHeight((short) 1);
		printSetup.setFitWidth((short) 1);
		return sheet;
	}

	private void createUserRow(Sheet sheet, int[] maxwidths, int i, List<InternalUserInfo> userList,
			Map<String, CellStyle> styles, boolean listActivities) {

		InternalUserInfo user = userList.get(i);
		Row row = sheet.createRow(i + 2);
		row.setHeightInPoints(14.00f);

		// username
		Cell username = row.createCell(0);
		username.setCellValue(user.getUsername());
		username.setCellStyle(styles.get("cellNormal"));
		maxwidths[0] = Math.max(user.getUsername().length(), maxwidths[0]);

		// real name
		Cell realName = row.createCell(1);
		String realNameString = StringUtils.defaultIfBlank(user.getRealName(), "n/a");
		realName.setCellValue(realNameString);
		realName.setCellStyle(styles.get("cellNormal"));
		maxwidths[1] = Math.max(realNameString.length(), maxwidths[1]);

		// email
		Cell email = row.createCell(2);
		email.setCellValue(user.getEmail());
		email.setCellStyle(styles.get("cellNormal"));
		maxwidths[2] = Math.max(user.getEmail().length(), maxwidths[2]);

		// nationality
		Cell nationality = row.createCell(3);
		String displayCountry = user.getNationality() != null ? new Locale("", user.getNationality())
				.getDisplayCountry(en) : "n/a";
		nationality.setCellValue(displayCountry);
		nationality.setCellStyle(styles.get("cellNormal"));
		maxwidths[3] = Math.max(displayCountry.length(), maxwidths[3]);

		// phone
		Cell phone = row.createCell(4);
		String phoneString = StringUtils.defaultIfBlank(user.getPhone(), "n/a");
		phone.setCellValue(phoneString);
		phone.setCellStyle(styles.get("cellNormal"));
		maxwidths[4] = Math.max(phoneString.length(), maxwidths[4]);

		// paid
		Cell paid = row.createCell(5);
		paid.setCellValue(user.isPaid() ? "yes" : "no");
		paid.setCellStyle(styles.get("cellNormal"));

		// date paid
		Cell datePaid = row.createCell(6);
		if (user.isPaid()) {
			datePaid.setCellValue(user.getPayDate().toCalendar(en));
			datePaid.setCellStyle(styles.get("cellNormalDate"));
		} else {
			datePaid.setCellValue("n/a");
			datePaid.setCellStyle(styles.get("cellNormal"));
		}

		// date registered
		Cell dateRegistered = row.createCell(7);
		dateRegistered.setCellValue(user.getRegistrationDate().toCalendar(en));
		dateRegistered.setCellStyle(styles.get("cellNormalDate"));

		// activities
		if (listActivities) {
			String activitiesString = activities.getActivitiesForUser(user.getId()).stream()
					.filter(activity -> activity.isCanCollide())
					.map(activity -> activity.getTitle())
					.collect(Collectors.joining(", "));

			Cell activitiesCell = row.createCell(8);
			activitiesCell.setCellValue(activitiesString);
			activitiesCell.setCellStyle(styles.get("cellNormal"));
		}
	}

	private void createHeader(String titleString, Sheet sheet, Map<String, CellStyle> styles, boolean listActivities) {

		// the title row
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(30.00f);

		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue(titleString + " as of " + DateTime.now().toString("MMMM dd, HH:mm", en));
		titleCell.setCellStyle(styles.get("title"));

		// the header row
		Row headerRow = sheet.createRow(1);
		headerRow.setHeightInPoints(14.00f);

		Cell usernameHeader = headerRow.createCell(0);
		usernameHeader.setCellValue("username");
		usernameHeader.setCellStyle(styles.get("header"));

		Cell realNameHeader = headerRow.createCell(1);
		realNameHeader.setCellValue("real name");
		realNameHeader.setCellStyle(styles.get("header"));

		Cell emailHeader = headerRow.createCell(2);
		emailHeader.setCellValue("email");
		emailHeader.setCellStyle(styles.get("header"));

		Cell nationalityHeader = headerRow.createCell(3);
		nationalityHeader.setCellValue("nationality");
		nationalityHeader.setCellStyle(styles.get("header"));

		Cell phoneHeader = headerRow.createCell(4);
		phoneHeader.setCellValue("phone");
		phoneHeader.setCellStyle(styles.get("header"));

		Cell paidHeader = headerRow.createCell(5);
		paidHeader.setCellValue("paid?");
		paidHeader.setCellStyle(styles.get("header"));

		Cell datePaidHeader = headerRow.createCell(6);
		datePaidHeader.setCellValue("date paid");
		datePaidHeader.setCellStyle(styles.get("header"));

		Cell dateRegisteredHeader = headerRow.createCell(7);
		dateRegisteredHeader.setCellValue("date registered");
		dateRegisteredHeader.setCellStyle(styles.get("header"));

		if (listActivities) {
			Cell activitiesHeader = headerRow.createCell(8);
			activitiesHeader.setCellValue("saturday activities");
			activitiesHeader.setCellStyle(styles.get("header"));
		}

		// freeze the first two rows
		sheet.createFreezePane(0, 2);
	}

	private Map<String, CellStyle> createStyles(Workbook wb) {

		Map<String, CellStyle> styles = Maps.newHashMap();

		DataFormat df = wb.createDataFormat();

		CellStyle title;
		Font titleFont = wb.createFont();
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleFont.setFontHeightInPoints((short) 15);
		title = wb.createCellStyle();
		title.setAlignment(CellStyle.ALIGN_LEFT);
		title.setFont(titleFont);
		styles.put("title", title);

		CellStyle header;
		Font headerFont = wb.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short) 11);
		header = createBorderedStyle(wb);
		header.setAlignment(CellStyle.ALIGN_CENTER);
		header.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
		header.setFillPattern(CellStyle.SOLID_FOREGROUND);
		header.setFont(headerFont);
		styles.put("header", header);

		CellStyle cellNormal;
		Font normalFont = wb.createFont();
		normalFont.setFontHeightInPoints((short) 11);
		cellNormal = createBorderedStyle(wb);
		cellNormal.setAlignment(CellStyle.ALIGN_LEFT);
		cellNormal.setWrapText(true);
		cellNormal.setFont(normalFont);
		styles.put("cellNormal", cellNormal);

		CellStyle cellNormalDate;
		cellNormalDate = createBorderedStyle(wb);
		cellNormalDate.setAlignment(CellStyle.ALIGN_RIGHT);
		cellNormalDate.setWrapText(true);
		cellNormalDate.setDataFormat(df.getFormat("d-mmm, HH:MM"));
		cellNormalDate.setFont(normalFont);
		styles.put("cellNormalDate", cellNormalDate);

		return styles;
	}

	private CellStyle createBorderedStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return style;
	}
}
