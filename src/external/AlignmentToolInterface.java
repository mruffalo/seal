package external;

import java.io.File;

public abstract class AlignmentToolInterface
{
	protected static class GenomeDescriptor
	{
		public File genome;
		public File reads;
		public File binaryOutput;
		public File samOutput;
	}
	
	public abstract void preAlignmentProcessing();
	
	public abstract void align();
	
	public abstract void postAlignmentProcessing();
	
	public abstract void readAlignment();
	
	protected GenomeDescriptor processHumanGenome()
	{
		return processGenome();
	}
	
	protected GenomeDescriptor processGenome()
	{
		return null;
	}
}
