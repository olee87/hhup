package hhup.repository;

import hhup.model.activity.Activity;
import hhup.model.couchmarket.CouchMarketPost;
import hhup.model.guestbook.GuestbookEntry;
import hhup.model.history.HistoryItem;
import hhup.model.token.AccountActivationToken;
import hhup.model.token.PasswordRecoveryToken;
import hhup.model.token.RememberMeToken;
import hhup.model.user.InternalUserInfo;
import hhup.model.user.Note;
import hhup.web.request.PaypalPaymentInfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Repositories {

	@Value("${hhup.repository}")
	private String base;

	@Bean(name = "AccountActivationTokenRepository")
	public Repository<AccountActivationToken> accountActivationTokenRepo() {
		return new Repository<>(AccountActivationToken[].class, makePath("accountActivationTokens"));
	}

	@Bean(name = "ActivityRepository")
	public Repository<Activity> activityRepo() {
		return new Repository<>(Activity[].class, makePath("activities"));
	}

	@Bean(name = "CouchMarketPostRepository")
	public Repository<CouchMarketPost> CouchMarketPostRepo() {
		return new Repository<>(CouchMarketPost[].class, makePath("couchMarket"));
	}

	@Bean(name = "GuestbookRepository")
	public Repository<GuestbookEntry> GuestbookRepo() {
		return new Repository<>(GuestbookEntry[].class, makePath("guestbook"));
	}

	@Bean(name = "HistoryRepository")
	public Repository<HistoryItem> historyRepo() {
		return new Repository<>(HistoryItem[].class, makePath("history"));
	}

	@Bean(name = "NoteRepository")
	public Repository<Note> noteRepo() {
		return new Repository<>(Note[].class, makePath("notes"));
	}

	@Bean(name = "PasswordRecoveryTokenRepository")
	public Repository<PasswordRecoveryToken> passwordRecoveryTokenRepo() {
		return new Repository<>(PasswordRecoveryToken[].class, makePath("passwordRecoveryTokens"));
	}

	@Bean(name = "PaypalRepository")
	public Repository<PaypalPaymentInfo> paypalRepo() {
		return new Repository<>(PaypalPaymentInfo[].class, makePath("paypal"));
	}

	@Bean(name = "RememberMeTokenRepository")
	public Repository<RememberMeToken> rememberMeTokenRepo() {
		return new Repository<>(RememberMeToken[].class, makePath("rememberMeTokens"));
	}

	@Bean(name = "UserRepository")
	public Repository<InternalUserInfo> userRepo() {
		return new Repository<>(InternalUserInfo[].class, makePath("users"));
	}

	private String makePath(String name) {
		return base + name + ".json";
	}
}
