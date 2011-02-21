package cl.own.usi.gateway.client.pool.policy;

public interface ObjectValidationPolicy {

	boolean validateOnBorrow();
	boolean validateOnRelease();
	
	static ObjectValidationPolicy VALIDATE_ON_BORROW = new DefaultObjectValidationPolicy(true, false);
	static ObjectValidationPolicy VALIDATE_ON_RELEASE = new DefaultObjectValidationPolicy(false, true);
	static ObjectValidationPolicy VALIDATE_NONE = new DefaultObjectValidationPolicy(false, false);
	static ObjectValidationPolicy VALIDATE_BOTH = new DefaultObjectValidationPolicy(true, true);
	
	public static class DefaultObjectValidationPolicy implements ObjectValidationPolicy {

		private final boolean validateOnBorrow;
		private final boolean validateOnRelease;
		public DefaultObjectValidationPolicy(boolean validateOnBorrow, boolean validateOnRelease) {
			this.validateOnBorrow = validateOnBorrow;
			this.validateOnRelease = validateOnRelease;
		}
		public boolean validateOnBorrow() {
			return validateOnBorrow;
		}

		public boolean validateOnRelease() {
			return validateOnRelease;
		}
	}
	
}
