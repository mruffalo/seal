package io;

import java.util.regex.Pattern;

public class Constants
{
	public static final String FIELD_SEPARATOR = ",";
	public static final Pattern READ_POSITION_HEADER = Pattern.compile("[>@]\\d+:READ_POS=(\\d+(,\\d+)?)");
}
