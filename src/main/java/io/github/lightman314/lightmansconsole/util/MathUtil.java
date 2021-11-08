package io.github.lightman314.lightmansconsole.util;

public class MathUtil {

	public static long clamp(long value, long minVal, long maxVal)
	{
		if(minVal > maxVal)
		{
			long temp = minVal;
			minVal = maxVal;
			maxVal = temp;
		}
		if(value < minVal)
			return minVal;
		else if(value > maxVal)
			return maxVal;
		return value;
	}
	
	public static int clamp(int value, int minVal, int maxVal)
	{
		if(minVal > maxVal)
		{
			int temp = minVal;
			minVal = maxVal;
			maxVal = temp;
		}
		if(value < minVal)
			return minVal;
		else if(value > maxVal)
			return maxVal;
		return value;
	}
	
}
