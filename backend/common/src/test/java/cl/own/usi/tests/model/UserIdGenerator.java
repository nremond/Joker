package cl.own.usi.tests.model;
import org.junit.Test;

import cl.own.usi.model.util.IdHelper;


public class UserIdGenerator {

	@Test
	public void test() {
			String email = "mayfield.wiseman@gmail.com";
			System.out.println(IdHelper.generateUserId(email));
	}

}
