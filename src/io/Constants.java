package io;

import java.util.regex.Pattern;

public class Constants
{
	public static final String FIELD_SEPARATOR = ",";
	public static final Pattern READ_POSITION_HEADER = Pattern.compile("[>@]\\d+:READ_POS=(\\d+(,\\d+)?)");
	public static final String FASTQ_QUALITY_CHARACTERS = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	public static final int SANGER_OFFSET = 0;
	public static final int SOLEXA_OFFSET = 26;
	public static final int ILLUMINA_1_3_OFFSET = 33;
	public static final int ILLUMINA_1_5_OFFSET = 36;
}
