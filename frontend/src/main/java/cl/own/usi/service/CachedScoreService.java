package cl.own.usi.service;

public interface CachedScoreService {

	StringBuilder getBefore(String userId, int limit);
	
	StringBuilder getAfter(String userId, int limit);
	
	void addUser(String userId, String lastname, String firstname, String email, int score);
	
	void flush();
}
