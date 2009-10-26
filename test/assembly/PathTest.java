package assembly;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import assembly.ShotgunSequenceAssembler.OverlapGraph;
import assembly.ShotgunSequenceAssembler.OverlapGraph.*;

public class PathTest
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
	
	/**
	 * AABBCC + XXYYZZ
	 */
	private final String[] disconnectedStrings = { "AABB", "BBCC", "XXYY", "YYZZ" };
	private List<Fragment> disconnectedList;
	
	@Before
	public void setUp()
	{
		List<Fragment> temp = new LinkedList<Fragment>();
		for (String string : basicStrings)
		{
			temp.add(new Fragment(string));
		}
		basicList = Collections.unmodifiableList(temp);
		temp = new LinkedList<Fragment>();
		for (String string : biggerStrings)
		{
			temp.add(new Fragment(string));
		}
		biggerList = Collections.unmodifiableList(temp);
		temp = new LinkedList<Fragment>();
		for (String string : disconnectedStrings)
		{
			temp.add(new Fragment(string));
		}
		disconnectedList = Collections.unmodifiableList(temp);
	}
	
	@Test
	public void testAssembleSequence()
	{
		System.out.println();
		System.out.println("testAssembleSequence");
		System.out.println();
		OverlapGraph graph = new OverlapGraph(basicList);
		Path path = graph.createPath();
		int i = 0;
		while (graph.hasMoreEdges())
		{
			Edge e = graph.getHeaviestEdge();
			System.out.printf("Adding edge \"%s\" to path%n", e);
			boolean added = path.addEdge(e);
			System.out.printf("\tResult: %b%n", added);
			assertEquals("More than two edges were added to the path.", i < 2, added);
			i++;
			assertEquals("Path was prematurely Hamiltonian", i > 1, path.isHamiltonian());
		}
		assertEquals(graph.getVertexCount(), path.getPathVertexCount());
		assertEquals(2, path.getPathEdgeCount());
		assertTrue("Path was not Hamiltonian after adding all available edges", path.isHamiltonian());
		assertEquals("ATCATGATACTA", path.assembleString());
	}
	
	@Test
	public void testAssembleBiggerSequence()
	{
		System.out.println();
		System.out.println("testAssembleBiggerSequence");
		System.out.println();
		OverlapGraph graph = new OverlapGraph(biggerList);
		Path path = graph.new Path();
		int i = 0;
		while (graph.hasMoreEdges())
		{
			Edge e = graph.getHeaviestEdge();
			System.out.printf("Adding edge \"%s\" to path%n", e);
			boolean added = path.addEdge(e);
			System.out.printf("\tResult: %b%n", added);
			i++;
		}
		assertEquals(graph.getVertexCount(), path.getPathVertexCount());
		Set<Vertex> parents = path.getPathParents();
		assertEquals(1, parents.size());
		assertEquals(graph.getVertex(new Fragment("ACTGAC")), parents.iterator().next());
		assertTrue("Path was not Hamiltonian", path.isHamiltonian());
		assertEquals(5, path.getPathEdgeCount());
		assertEquals("ACTGACCTGCATTTCA", path.assembleString());
	}
	
	@Test
	public void testAssembleDisconnectedSequence()
	{
		System.out.println();
		System.out.println("testAssembleDisconnectedSequence");
		System.out.println();
		OverlapGraph graph = new OverlapGraph(disconnectedList);
		Path path = graph.new Path();
		int i = 0;
		while (graph.hasMoreEdges())
		{
			Edge e = graph.getHeaviestEdge();
			System.out.printf("Adding edge \"%s\" to path%n", e);
			boolean added = path.addEdge(e);
			System.out.printf("\tResult: %b%n", added);
			i++;
		}
		assertEquals(graph.getVertexCount(), path.getPathVertexCount());
		Set<Vertex> parents = path.getPathParents();
		assertEquals(2, parents.size());
		assertTrue(parents.contains(graph.getVertex(new Fragment("AABB"))));
		assertTrue(parents.contains(graph.getVertex(new Fragment("XXYY"))));
		assertFalse(path.isHamiltonian());
		assertEquals(2, path.getPathEdgeCount());
		System.out.printf("Assembled string: %s%n", path.assembleString());
	}
	
	@Test
	public void testGetEdgeVertexCounts()
	{
		OverlapGraph graph = new OverlapGraph(basicList);
		Path path = graph.createPath();
		int i = 0;
		while (graph.hasMoreEdges())
		{
			Edge e = graph.getHeaviestEdge();
			if (path.addEdge(e))
			{
				i++;
			}
			assertEquals(i + 1, path.getPathVertexCount());
			assertEquals(i, path.getPathEdgeCount());
		}
	}
	
	@Test
	public void testAssembleEmptySequence()
	{
		List<Fragment> emptyList = new LinkedList<Fragment>();
		OverlapGraph emptyGraph = new OverlapGraph(emptyList);
		Path path = emptyGraph.createPath();
		assertEquals("", path.assembleString());
	}
}
