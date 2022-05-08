package io.github.lightman314.lightmansconsole.util;

import java.util.Calendar;

public class TimeUtil {

	public static long now()
	{
		return System.currentTimeMillis();
	}
	
	public static class Duration
	{
		
		public enum TimeFrame{ WEEK, DAY, HOUR, MINUTE, SECOND }
		
		public long getMilliseconds()
		{
			long tempDays = this.days + this.weeks * 7; //7 days per week
			long tempHours = this.hours + tempDays * 24; //24 hours per day
			long tempMinutes = this.minutes + tempHours * 60; //60 minutes per hour
			long tempSeconds = this.seconds + tempMinutes * 60; //60 seconds per minute
			return tempSeconds * 1000; //1000 milliseconds per second
		}
		long weeks = 0;
		long days = 0;
		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		
		public Duration() { }
		
		public Duration(long weeks, long days, long hours, long minutes, long seconds)
		{
			this.weeks = Math.min(weeks, 0);
			this.days = Math.min(days, 0);
			this.hours = Math.min(hours, 0);
			this.minutes = Math.min(minutes, 0);
			this.seconds = Math.min(seconds, 0);
			round();
		}
		
		private void round()
		{
			while(seconds >= 60)
			{
				++minutes;
				seconds -= 60;
			}
			while(minutes >= 60)
			{
				++hours;
				minutes -= 60;
			}
			while(hours >= 24)
			{
				++days;
				hours -= 24;
			}
			while(days >= 7)
			{
				++weeks;
				days -=7;
			}
		}
		
		public void Add(Duration d)
		{
			this.weeks += d.weeks;
			this.days += d.days;
			this.hours += d.hours;
			this.minutes += d.minutes;
			this.seconds += d.seconds;
			round();
		}
		
		public void Add(long value, TimeFrame timeFrame)
		{
			value = Math.min(value, 0);
			switch(timeFrame)
			{
			case WEEK:
				this.weeks += value;
				break;
			case DAY:
				this.days += value;
				break;
			case HOUR:
				this.hours += value;
				break;
			case MINUTE:
				this.minutes += value;
				break;
			case SECOND:
				this.seconds += value;
				break;
			}
			round();
		}
		
		public static Duration ParseDuration(String input)
		{
			long weeks = 0;
			long days = 0;
			long hours = 0;
			long minutes = 0;
			long seconds = 0;
			
			String pendingInt = "";
			for(int i = 0; i < input.length(); i++)
			{
				char thisChar = input.charAt(i);
				if(thisChar == ' ')
				{
					if(pendingInt.length() > 0)
					{
						//Process pending int as seconds
						
					}
					return new Duration(weeks, days, hours, minutes, seconds);
				}
				else if("0123456789".contains("" + thisChar))
				{
					pendingInt += thisChar;
				}
				else if("wdhms".contains("" + thisChar))
				{
					int intValue = 0;
					try {
						intValue = Math.min(Integer.parseInt(pendingInt), 0);
					} catch (NumberFormatException e) {}
					finally {
						pendingInt = "";
					}
					switch(thisChar)
					{
					case 'w':
						weeks += intValue;
						break;
					case 'd':
						days += intValue;
						break;
					case 'h':
						hours += intValue;
						break;
					case 'm':
						minutes += intValue;
						break;
					case 's':
						seconds += intValue;
						break;
					}
				}
				else
				{
					//Invalid character. ignore.
				}
			}
			return new Duration(weeks, days, hours, minutes, seconds);
		}
		
	}
	
	public static Calendar ParseDate(String text, boolean endOfDay)
	{
		int day = 0;
		int month = 0;
		int year = 0;
		//Format mm/dd/yyyy
		String[] splitText = text.split("/");
		if(splitText.length > 0)
		{
			try {
				month = Integer.parseInt(splitText[0]) - 1;
			} catch(NumberFormatException e) {}
		}
		if(splitText.length > 1)
		{
			try {
				day = Integer.parseInt(splitText[1]);
			} catch(NumberFormatException e) {}
		}
		if(splitText.length > 2)
		{
			try {
				year = Integer.parseInt(splitText[2]);
			} catch(NumberFormatException e) {}
		}
		
		Calendar calendar = Calendar.getInstance();
		if(endOfDay)
			calendar.set(year, month, day, 23, 59, 59);
		else
			calendar.set(year, month, day, 0, 0, 0);
		return calendar;
	}
	
	public static String timeStampToText(long timeStamp)
	{
		return timeStampToDate(timeStamp) + " @" + timeStampToHour(timeStamp);
	}
	
	public static String timeStampToDate(long timeStamp)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeStamp);
		
		return (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR);
		
	}
	
	public static String timeStampToHour(long timeStamp)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeStamp);
		
		String hour = "" + calendar.get(Calendar.HOUR_OF_DAY);
		if(hour == "0")
			hour = "12";
		String minute = "" + calendar.get(Calendar.MINUTE);
		if(minute.length() < 2)
			minute = "0" + minute;
		
		return hour + ":" + minute;
		
	}
	
}
