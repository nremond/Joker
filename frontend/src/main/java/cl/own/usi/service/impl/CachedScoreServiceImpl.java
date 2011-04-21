package cl.own.usi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cl.own.usi.cache.SortedCachedUser;
import cl.own.usi.service.CachedScoreService;

@Component
public class CachedScoreServiceImpl implements CachedScoreService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CachedScoreServiceImpl.class);

	private final Map<String, SortedCachedUser> usersMap = new ConcurrentHashMap<String, SortedCachedUser>();
	private final ConcurrentSkipListSet<SortedCachedUser> rankedUsers = new ConcurrentSkipListSet<SortedCachedUser>();

	private final static StringBuilder EMPTY_SB = new StringBuilder();

	@Override
	public StringBuilder getBefore(final String userId, final int limit) {

		SortedCachedUser user = usersMap.get(userId);

		if (user != null) {
			List<SortedCachedUser> users = new ArrayList<SortedCachedUser>(
					limit);

			NavigableSet<SortedCachedUser> usersBefore = rankedUsers.headSet(
					user, false);
			int i = 0;
			for (SortedCachedUser userBefore : usersBefore) {
				if (userBefore.getScore() > user.getScore()) {
					if (i < limit) {
						users.add(userBefore);
					} else {
						break;
					}
				}
			}
			return appendUsersScores(users);
		} else {
			return EMPTY_SB;
		}
	}

	@Override
	public StringBuilder getAfter(final String userId, final int limit) {
		SortedCachedUser user = usersMap.get(userId);

		if (user != null) {
			List<SortedCachedUser> users = new ArrayList<SortedCachedUser>(
					limit);

			NavigableSet<SortedCachedUser> usersAfter= rankedUsers.tailSet(
					user, false);
			int i = 0;
			for (SortedCachedUser userAfter : usersAfter) {
				if (userAfter.getScore() > user.getScore()) {
					if (i < limit) {
						users.add(userAfter);
					} else {
						break;
					}
				}
			}
			return appendUsersScores(users);
		} else {
			return EMPTY_SB;
		}
	}

	@Override
	public void addUser(final String userId, final String lastname,
			final String firstname, final String email, final int score) {
		SortedCachedUser user = new SortedCachedUser(lastname, firstname,
				email, score);
		usersMap.put(userId, user);
		rankedUsers.add(user);
	}

	@Override
	public void flush() {
		usersMap.clear();
		rankedUsers.clear();
	}

	public static StringBuilder appendUsersScores(List<SortedCachedUser> users) {
		StringBuilder sb = new StringBuilder();
		StringBuilder scoresMails = new StringBuilder("\"mail\":[");
		StringBuilder scoresScores = new StringBuilder("\"scores\":[");
		StringBuilder scoresFirstName = new StringBuilder("\"firstname\":[");
		StringBuilder scoresLastname = new StringBuilder("\"lastname\":[");
		boolean first = true;
		for (SortedCachedUser user : users) {
			if (!first) {
				scoresMails.append(",");
			}
			scoresMails.append("\"").append(user.getFullEmail()).append("\"");
			if (!first) {
				scoresScores.append(",");
			}
			scoresScores.append("\"").append(user.getScore()).append("\"");
			if (!first) {
				scoresFirstName.append(",");
			}
			scoresFirstName.append("\"").append(user.getFirstname())
					.append("\"");
			if (!first) {
				scoresLastname.append(",");
			}
			scoresLastname.append("\"").append(user.getLastname()).append("\"");
			first = false;
		}
		sb.append(scoresMails).append("],");
		sb.append(scoresScores).append("],");
		sb.append(scoresFirstName).append("],");
		sb.append(scoresLastname).append("]");
		return sb;
	}

}
