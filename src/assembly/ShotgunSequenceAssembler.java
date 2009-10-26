package assembly;

import java.util.*;

/**
 * Most or all data structures and operations in this class utilize HashMaps and HashSets instead of
 * Lists (see Vertex.adj). This incurs some (maybe significant) memory overhead, but in my opinion,
 * the constant access time is worth the tradeoff.
 * 
 * @author mruffalo
 */
public class ShotgunSequenceAssembler implements SequenceAssembler
{
	/**
	 * Assembles the given fragments into a String using greedy Hamiltonian path creation.
	 */
	@Override
	public String assembleSequence(List<Fragment> fragments)
	{
		OverlapGraph graph = new OverlapGraph(fragments);
		OverlapGraph.Path path = graph.createPath();
		while (graph.hasMoreEdges())
		{
			OverlapGraph.Edge e = graph.getHeaviestEdge();
			path.addEdge(e);
		}
		return path.assembleString();
	}
	
	protected static class OverlapGraph
	{
		/**
		 * The OverlapGraph stores the vertices as a (Hash)Map from Strings to Vertexes. This allows
		 * constant-time retrieval of the overlap directly from the graph data structure. Each
		 * Vertex also maps Strings to Edges for the same reason.
		 */
		private Map<Fragment, Vertex> vertices;
		private Queue<Edge> queue;
		
		/**
		 * Ω(n^2).
		 * 
		 * @param fragments
		 *            Note: This overlap calculation <b>is case-sensitive</b>. It is assumed that
		 *            all Strings in this list are case-normalized (either all lowercase or all
		 *            uppercase).
		 */
		public OverlapGraph(List<Fragment> fragments)
		{
			vertices = new HashMap<Fragment, Vertex>(fragments.size());
			queue = new PriorityQueue<Edge>();
			for (Fragment fragment : fragments)
			{
				addVertex(fragment);
			}
			for (Fragment first : fragments)
			{
				Vertex from = getVertex(first);
				for (Fragment second : fragments)
				{
					// Filter out substrings
					if (!first.string.contains(second.string))
					{
						Vertex to = getVertex(second);
						/*
						 * TODO: Maybe use a more efficient way of doing this, like suffix trees.
						 * Doesn't seem to be a requirement for this assignment.
						 */
						for (int i = Math.min(first.string.length(), second.string.length()); i >= 0; i--)
						{
							String firstSuffix = first.string.substring(first.string.length() - i);
							String secondPrefix = second.string.substring(0, i);
							if (secondPrefix.equals(firstSuffix))
							{
								if (i > 0)
								{
									Edge e = from.addEdge(to, i);
									queue.add(e);
								}
								break;
							}
						}
					}
				}
			}
		}
		
		public Vertex addVertex(Fragment fragment)
		{
			return vertices.put(fragment, new Vertex(fragment));
		}
		
		public Vertex getVertex(Fragment fragment)
		{
			return vertices.get(fragment);
		}
		
		/**
		 * @return The OverlapEdge with the highest overlap value.
		 */
		public Edge getHeaviestEdge()
		{
			return queue.poll();
		}
		
		/**
		 * O(V + E)
		 */
		public void resetEdgeQueue()
		{
			queue = new PriorityQueue<Edge>();
			for (Vertex v : vertices.values())
			{
				for (Edge e : v.adj.values())
				{
					if (e.overlap > 0)
					{
						queue.add(e);
					}
				}
			}
		}
		
		public boolean hasMoreEdges()
		{
			return !queue.isEmpty();
		}
		
		public int getVertexCount()
		{
			return vertices.size();
		}
		
		public Path createPath()
		{
			return new Path();
		}
		
		public Collection<Vertex> getVertices()
		{
			return Collections.unmodifiableCollection(vertices.values());
		}
		
		public int getEdgeCount()
		{
			int count = 0;
			for (Vertex v : vertices.values())
			{
				count += v.adj.size();
			}
			return count;
		}
		
		/**
		 * @param from
		 * @param to
		 * @return The length of the longest String that is a suffix of <code>from</code> and a
		 *         prefix of <code>to</code>.
		 * @throws NullPointerException
		 *             if <code>from</code> was not in the fragment list that was used to
		 *             instantiate this Overlaps
		 */
		public Integer getOverlap(Fragment from, Fragment to)
		{
			Edge e = getVertex(from).getEdge(to);
			if (e == null)
			{
				return 0;
			}
			else
			{
				return e.overlap;
			}
		}
		
		/**
		 * TODO: Make this not cause <code>OutOfMemoryError</code>s when graphs are huge. This
		 * return value must be printed to a terminal to be useful, so maybe consider just dumping
		 * stuff to <code>System.out</code> instead of returning a String.
		 * 
		 * @return
		 */
		public String dump()
		{
			StringBuilder sb = new StringBuilder();
			for (Vertex v : vertices.values())
			{
				sb.append(String.format("%s%n", v.toString()));
				for (Edge e : v.adj.values())
				{
					sb.append(String.format("\tEdge to %s, overlap %d%n", e.to.fragment.string, e.overlap));
				}
			}
			return sb.toString();
		}
		
		protected class Vertex
		{
			public final Fragment fragment;
			/**
			 * This is the adjacency list of this Vertex, stored as a (Hash)Map from Strings to
			 * Edges. This allows constant-time lookup of an Edge from a String, as opposed to
			 * linear-time lookup if this were a List&lt;Vertex&gt;.
			 */
			protected final Map<Fragment, Edge> adj;
			
			public Vertex(Fragment fragment_)
			{
				adj = new HashMap<Fragment, Edge>();
				fragment = fragment_;
			}
			
			public Edge addEdge(Vertex to, int overlap)
			{
				Edge e = new Edge(this, overlap, to);
				adj.put(to.fragment, e);
				return e;
			}
			
			public Edge getEdge(Fragment fragment)
			{
				return adj.get(fragment);
			}
			
			@Override
			public String toString()
			{
				return String.format("Vertex \"%s\": %d edge(s)", fragment.string, adj.size());
			}
		}
		
		protected class Edge implements Comparable<Edge>
		{
			public final Vertex from;
			public final int overlap;
			public final Vertex to;
			
			public Edge(Vertex from_, int weight_, Vertex to_)
			{
				from = from_;
				overlap = weight_;
				to = to_;
			}
			
			/**
			 * Note: {@link java.util.PriorityQueue} returns the <b>minimum</b> priority. Since we
			 * want the edge with the greatest weight, this comparison is inverted.
			 */
			@Override
			public int compareTo(Edge o)
			{
				return new Integer(o.overlap).compareTo(overlap);
			}
			
			@Override
			public String toString()
			{
				return String.format("%s %d %s", from.fragment.string, overlap, to.fragment.string);
			}
		}
		
		/**
		 * This class wraps a sequence of edges, which describes a path through the graph.
		 */
		protected class Path
		{
			/**
			 * The insertion order of edges is not important, otherwise I would use a
			 * {@link java.util.LinkedHashSet}. The crucial order is the path through the graph,
			 * which is captured elsewhere.
			 * 
			 * @see #vertexEdgeMap
			 */
			protected Set<Edge> edges = new HashSet<Edge>(vertices.size());
			protected Set<Vertex> visitedSourceVertices = new HashSet<Vertex>(vertices.size());
			protected Set<Vertex> visitedTargetVertices = new HashSet<Vertex>(vertices.size());
			/**
			 * Stores the parent of each disconnected segment of this path. Used for cycle
			 * detection.
			 */
			protected TwoWayMap<Vertex, Vertex> parentMap = new TwoWayHashMap<Vertex, Vertex>(vertices.size());
			/**
			 * This, along with the disconnected path parents Vertex set, stores the path through
			 * the graph. Each Vertex maps to the correct Edge, so you can always tell where to go
			 * next from where you are.
			 * 
			 * @see #assembleString()
			 */
			protected Map<Vertex, Edge> vertexEdgeMap = new HashMap<Vertex, Edge>(vertices.size());
			/**
			 * If this path is connected, this Set contains only one Vertex. Otherwise, the
			 * assembled String is obtained by concatenating the paths starting at each Vertex here.
			 */
			private Set<Vertex> disconnectedPathParents = new HashSet<Vertex>(vertices.size());
			
			/**
			 * Creates a String from the path (sequence of edges) contained in this object. This
			 * method is <b>not</b> responsible for creating an optimal or correct path -- it only
			 * puts Edges together into a String. Path creation is contained in
			 * {@link ShotgunSequenceAssembler#assembleSequence}.
			 * 
			 * @return A String specified by the Vertexes and Edges in this Path.
			 */
			public String assembleString()
			{
				StringBuilder sb = new StringBuilder();
				int position = 0;
				for (Vertex v : disconnectedPathParents)
				{
					Edge e = vertexEdgeMap.get(v);
					if (e != null)
					{
						e.from.fragment.setPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE, position);
						sb.append(e.from.fragment.string);
						// TODO test this
						position += e.from.fragment.string.length() - e.overlap;
						e.to.fragment.setPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE, position);
						sb.append(e.to.fragment.string.substring(e.overlap));
						e = vertexEdgeMap.get(e.to);
					}
					while (e != null)
					{
						// TODO test this
						position += e.from.fragment.string.length() - e.overlap;
						sb.append(e.to.fragment.string.substring(e.overlap));
						e = vertexEdgeMap.get(e.to);
					}
				}
				return sb.toString();
			}
			
			public int getPathVertexCount()
			{
				HashSet<Vertex> union = new HashSet<Vertex>(visitedSourceVertices.size() + visitedTargetVertices.size());
				union.addAll(visitedSourceVertices);
				union.addAll(visitedTargetVertices);
				return union.size();
			}
			
			/**
			 * (protected) Accessor only used for unit testing
			 * 
			 * @return The first Vertex that makes up this Path.
			 */
			protected Set<Vertex> getPathParents()
			{
				return Collections.unmodifiableSet(disconnectedPathParents);
			}
			
			public int getPathEdgeCount()
			{
				return edges.size();
			}
			
			/**
			 * Adds an edge to the path, if it's valid.
			 * 
			 * @param edge
			 * @return Whether the edge was added to the path.
			 */
			public boolean addEdge(Edge edge)
			{
				boolean valid = isValidEdge(edge);
				if (valid)
				{
					boolean edgeAdded = edges.add(edge);
					boolean sourceAdded = visitedSourceVertices.add(edge.from);
					boolean targetAdded = visitedTargetVertices.add(edge.to);
					Vertex parent = parentMap.get(edge.from);
					if (parent == null)
					{
						parent = edge.from;
					}
					Vertex toChild = parentMap.getKey(edge.to);
					if (toChild == null)
					{
						toChild = edge.to;
					}
					/*
					 * A TwoWayMap does not allow duplicate values for the same reason that a Map
					 * does not allow duplicate keys: the keys of the forward map are the values of
					 * the reverse map and vice versa. This operation plays fast and loose with this
					 * requirement, but the tests pass.
					 */
					parentMap.put(toChild, parent);
					disconnectedPathParents.add(parent);
					disconnectedPathParents.remove(edge.to);
					vertexEdgeMap.put(edge.from, edge);
					return edgeAdded && sourceAdded && targetAdded;
				}
				else
				{
					return false;
				}
			}
			
			/**
			 * @param edge
			 * @return Whether this Edge is valid:
			 *         <ul>
			 *         <li>it must not already exist in this Path,</li>
			 *         <li>its source and target Vertexes must not already be used for those
			 *         purposes, and</li>
			 *         <li>it must not create a cycle.</li>
			 *         </ul>
			 */
			private boolean isValidEdge(Edge edge)
			{
				if (edges.size() == 0)
				{
					return true;
				}
				else
				{
					boolean alreadyExists = edges.contains(edge);
					boolean sourceUsed = visitedSourceVertices.contains(edge.from);
					boolean targetUsed = visitedTargetVertices.contains(edge.to);
					/*
					 * This check is straightforward: this Edge creates a cycle if [pseudocode] to
					 * == parent(from). Debugging this and implementing vertex parent tracking were
					 * not straightforward, however. There are other cases that would cause cycles,
					 * but those are caught by the above two checks (sourceUnused && targetUnused).
					 */
					boolean createsCycle = edge.to.equals(parentMap.get(edge.from));
					return !alreadyExists && !sourceUsed && !targetUsed && !createsCycle;
				}
			}
			
			/**
			 * @return Whether this path is Hamiltonian. It must
			 *         <ul>
			 *         <li>be connected,</li>
			 *         <li>touch every vertex,</li>
			 *         <li>and have one less edge than the parent Graph has Vertexes.</li>
			 *         </ul>
			 * @see #isConnected()
			 */
			public boolean isHamiltonian()
			{
				int pathVertices = getPathVertexCount();
				int pathEdges = getPathEdgeCount();
				int graphVertices = getVertexCount();
				return pathVertices == graphVertices && (pathEdges == graphVertices - 1) && isConnected();
			}
			
			/**
			 * @return Whether this Path represents a continuous list of Edges.
			 */
			protected boolean isConnected()
			{
				int sourceCount = visitedSourceVertices.size();
				int targetCount = visitedTargetVertices.size();
				int graphVertices = getVertexCount();
				return (sourceCount == graphVertices - 1) && (targetCount == graphVertices - 1);
			}
		}
	}
}
