package tourGuide.helper;

public class InternalTestHelper {

	// Set this default up to 100,000 for testing
	public static int internalUserNumber = 10;
	
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}
	
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
