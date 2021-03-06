package hhup.model.history;

public enum HistoryType {
	SIGNED_UP,
	ACTIVATED,
	DEACTIVATED,
	MARKED_PAID,
	UNMARKED_PAID,
	PAYPAL_PAID,
	MARKED_CHECKED_IN,
	UNMARKED_CHECKED_IN,
	CHANGED_PASSWORD,
	JOINED_ACTIVITY,
	LEFT_ACTIVITY,
	WROTE_GUESTBOOK_ENTRY,
	DELETED_GUESTBOOK_ENTRY,
	POSTED_COUCH_MARKET,
	EDITED_COUCH_MARKET,
	DELETED_COUCH_MARKET,
	LOGGED_IN,
	LOGGED_OUT,
	EDITED_PROFILE,
	REQUESTED_PASSWORD_RECOVERY,
	CONFIRMED_PASSWORD_RECOVERY,

	// ADMIN
	CREATED_ACTIVITY,
	EDITED_ACTIVITY,
	DELETED_ACTIVITY,
	DOWNLOADED_PDF,
	DOWNLOADED_XLS,
	CREATED_NOTE,
	EDITED_NOTE,
	DELETED_NOTE,
	SENT_MASS_MAIL,
	CHANGED_ALERT_TEXT,
	MADE_ADMIN,
	UNMADE_ADMIN,
	SENT_TEST_MAIL,
	RE_READ_IN
}