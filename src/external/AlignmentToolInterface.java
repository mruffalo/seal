package external;

public abstract class AlignmentToolInterface
{
	public abstract void preAlignmentProcessing();
	
	public abstract void align();
	
	public abstract void postAlignmentProcessing();
	
	public abstract void readAlignment();
}
