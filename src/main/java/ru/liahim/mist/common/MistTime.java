package ru.liahim.mist.common;

import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.network.PacketTimeSync;
import ru.liahim.mist.util.TimeData;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class MistTime {

	public static final int monthCount = 12;
	private static long timeOffset;
	private static int dayInMonth;
	private static long tickInMonth;
	private static long tickInYear;

	private static int day;
	private static int month;
	private static int year;

	private static int lastTick;
	private static boolean dirty;

	public static void updateTime(WorldServer world) {
		int tick = 0;
		if (world.areAllPlayersAsleep()) {
			wakeUp();
		} else {
			tick = (int)(world.getWorldTime() % 24000);
			if (tick == 18000 && lastTick == 17999) {
				dayUp();
			}
		}
		if (dirty) {
			TimeData.get(world).setTime(day, month, year, timeOffset);
			PacketHandler.INSTANCE.sendToAll(new PacketTimeSync(day, month, year, timeOffset));
			dirty = false;
		}
		lastTick = tick;
	}

	private static void dayUp() {
		day += 1;
		if (day >= dayInMonth) {
			day = 0;
			month += 1;
			if (month >= monthCount) {
				month = 0;
				year += 1;
			}
		}
		dirty = true;
	}

	public static void wakeUp() {
		if (lastTick < 18000) dayUp();
	}

	public static int getDayOfYear() {
		return month * dayInMonth + day;
	}

	public static long getTimeOffset() {
		return timeOffset;
	}

	public static int getDay() {
		return day;
	}

	public static int getMonth() {
		return month;
	}

	public static int getYear() {
		return year;
	}
	
	public static void setTimeOffset(long offset) {
		MistTime.timeOffset = offset;
		dirty = true;
	}
	
	public static void setDay(int day) {
		MistTime.day = day;
	}

	public static void setMonth(int month) {
		MistTime.month = month;
	}

	public static void setYear(int year) {
		MistTime.year = year;
	}

	public static void setMonthLength(int days) {
		dayInMonth = days;
		tickInMonth = days * 24000;
		tickInYear = tickInMonth * monthCount;
	}

	public static int getDayInMonth() {
		return dayInMonth;
	}

	public static long getTickInMonth() {
		return tickInMonth;
	}

	public static long getTickInYear() {
		return tickInYear;
	}

	public static long getTickOfMonth(World world) {
		return day * 24000 + (world.getWorldTime() + 6000) % 24000;
	}

	public static long getTickOfYear(World world) {
		return getTickOfYear(getTickOfMonth(world));
	}

	public static long getTickOfYear(long tickOfMonth) {
		return month * tickInMonth + tickOfMonth ;
	}

	public static void setTime(int day, int month, int year, long offset) {
		day = day % dayInMonth;
		month = month % monthCount;
		if (MistTime.day != day) {
			MistTime.day = day;
			dirty = true;
		}
		if (MistTime.month != month) {
			MistTime.month = month;
			dirty = true;
		}
		if (MistTime.year != year) {
			MistTime.year = year;
			dirty = true;
		}
		if (MistTime.timeOffset != offset) {
			MistTime.timeOffset = offset;
			dirty = true;
		}
	}

	private static final String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	private static final String[] months1 = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

	public static String getDate() {
		return "Date: " + (day + 1) + " " + months1[month] + " " + (year + 1000);
	}
}