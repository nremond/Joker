package cl.own.usi.gateway.client.pool.policy;

public interface ObjectValidationPolicy {

	boolean validateOnBorrow();

	boolean validateOnRelease();

	ObjectValidationPolicy VALIDATE_ON_BORROW = new DefaultObjectValidationPolicy(
			true, false);
	ObjectValidationPolicy VALIDATE_ON_RELEASE = new DefaultObjectValidationPolicy(
			false, true);
	ObjectValidationPolicy VALIDATE_NONE = new DefaultObjectValidationPolicy(
			false, false);
	ObjectValidationPolicy VALIDATE_BOTH = new DefaultObjectValidationPolicy(
			true, true);

	class DefaultObjectValidationPolicy implements ObjectValidationPolicy {

		private final boolean validateOnBorrow;
		private final boolean validateOnRelease;

		public DefaultObjectValidationPolicy(boolean validateOnBorrow,
				boolean validateOnRelease) {
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
