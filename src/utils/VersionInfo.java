package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionInfo
{
	public static final String SOFTWARE_VERSION;
	
	static
	{
		InputStream istream = VersionInfo.class.getResourceAsStream("version.properties");
		Properties p = new Properties();
		String version;
		try
		{
			p.load(istream);
			version = p.getProperty("software.version");
		}
		catch (IOException e)
		{
			version = "Unknown";
		}
		SOFTWARE_VERSION = version;
	}
}
