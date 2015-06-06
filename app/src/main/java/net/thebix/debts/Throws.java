package net.thebix.debts;

public final class Throws {
	
	public static final <T> void ifNull(T value, String message) throws Exception
	{
		if(value == null)
			throw new Exception(message);
	}
	
	public static final <T> void ifIntNullOrZeroOrLess(Integer value, String message) throws Exception
	{
		if(value == null || value <= 0)
			throw new Exception(message);
	}
	
	public static final <T> void ifLongNullOrZeroOrLess(Long value, String message) throws Exception
	{
		if(value == null || value <= 0)
			throw new Exception(message);
	}
	
	public static final void Exception(String message)  throws Exception
	{
		throw new Exception(message);
	}
}
