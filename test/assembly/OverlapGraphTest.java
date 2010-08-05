package assembly;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import assembly.ShotgunSequenceAssembler.OverlapGraph;
import assembly.ShotgunSequenceAssembler.OverlapGraph.Edge;
import java.util.*;

public class OverlapGraphTest
{
	/**
	 * ATCATGATACTA
	 */
	private final String[] basicStrings = { "ATCAT", "CATG", "TGATACTA" };
	private List<Fragment> basicList;
	/**
	 * ACTGACCTGCATTTCA
	 */
	private final String[] biggerStrings = { "ACTGAC", "ACCTG", "CTGCA", "GCATT", "ATTT", "TTCA" };
	private List<Fragment> biggerList;

	@Before
	public void setUp()
	{
		ArrayList<Fragment> temp = new ArrayList<Fragment>();
		for (String string : basicStrings)
		{
			temp.add(new Fragment(string));
		}
		basicList = Collections.unmodifiableList(temp);
		temp = new ArrayList<Fragment>();
		for (String string : biggerStrings)
		{
			temp.add(new Fragment(string));
		}
		biggerList = Collections.unmodifiableList(temp);
	}

	/**
	 * Make sure that the constructor doesn't throw an exception or anything
	 * else nasty. Also verify basic stuff like creating the right number of
	 * vertices and edges.
	 */
	@Test
	public void testOverlapGraph()
	{
		OverlapGraph graph = new OverlapGraph(basicList);
		assertEquals(3, graph.getVertexCount());
		assertEquals(4, graph.getEdgeCount());
		graph = new OverlapGraph(biggerList);
		assertEquals(6, graph.getVertexCount());
		assertEquals(14, graph.getEdgeCount());
		System.out.println(graph.dump());
	}

	@Test
	public void testGetHeaviestEdge()
	{
		OverlapGraph graph = new OverlapGraph(basicList);
		int i;
		int lastOverlap = Integer.MAX_VALUE;
		for (i = 0; graph.hasMoreEdges(); i++)
		{
			Edge edge = graph.getHeaviestEdge();
			assertTrue("Currrent overlap was greater than previous.", edge.overlap <= lastOverlap);
			lastOverlap = edge.overlap;
		}
		assertEquals(i, 4);
	}

	@Test
	public void testGetOverlap()
	{
		OverlapGraph graph = new OverlapGraph(basicList);
		/*
		 * This may seem counter-intuitive, but vertices in the overlap graph do
		 * not have edges that point to themselves.
		 */
		assertEquals(new Integer(0), graph.getOverlap(new Fragment("ATCAT"), new Fragment("ATCAT")));
		assertEquals(new Integer(3), graph.getOverlap(new Fragment("ATCAT"), new Fragment("CATG")));
		assertEquals(new Integer(0), graph.getOverlap(new Fragment("CATG"), new Fragment("ATCAT")));
	}

	@Test(expected = NullPointerException.class)
	public void testUnexpectedString()
	{
		OverlapGraph graph = new OverlapGraph(basicList);
		graph.getOverlap(new Fragment("not present"), new Fragment("whatever"));
	}

	@Test
	public void testResetEdgeQueue()
	{
		OverlapGraph graph = new OverlapGraph(basicList);
		int i;
		int lastOverlap = Integer.MAX_VALUE;
		for (i = 0; graph.hasMoreEdges(); i++)
		{
			Edge overlapEdge = graph.getHeaviestEdge();
			assertTrue("Currrent overlap was greater than previous.",
				overlapEdge.overlap <= lastOverlap);
			lastOverlap = overlapEdge.overlap;
		}
		assertEquals(i, 4);
		graph.resetEdgeQueue();
		lastOverlap = Integer.MAX_VALUE;
		for (i = 0; graph.hasMoreEdges(); i++)
		{
			Edge overlapEdge = graph.getHeaviestEdge();
			assertTrue(overlapEdge.overlap <= lastOverlap);
			lastOverlap = overlapEdge.overlap;
		}
		assertEquals(i, 4);
	}
}
