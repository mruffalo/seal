package assembly;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import assembly.HybridizationSequenceAssembler.DeBruijnGraph;

public class DeBruijnGraphTest
{
	private final int k = 4;
	private final String basicString = "ATCATGATACTA";
	private List<String> basicList;
	
	@Before
	public void setUp() throws Exception
	{
		LinkedList<String> temp = new LinkedList<String>();
		final int k = 4;
		for (int i = 0; i <= basicString.length() - k; i++)
		{
			temp.add(basicString.substring(i, i + k));
		}
		basicList = Collections.unmodifiableList(temp);
	}
	
	@Test
	@SuppressWarnings("unused")
	public void testDeBruijnGraph()
	{
		DeBruijnGraph graph = new DeBruijnGraph(k, basicList);
	}
	
	@Test
	public void testIsEulerian()
	{
		DeBruijnGraph graph = new DeBruijnGraph(k, basicList);
		System.out.printf("DeBruijnGraph is Eulerian: %b%n", graph.isEulerian());
	}
	
	@Test
	public void testAddVertex()
	{
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetVertex()
	{
		fail("Not yet implemented");
	}
	
	@Test
	public void testEulerize()
	{
		fail("Not yet implemented");
	}
	
}
