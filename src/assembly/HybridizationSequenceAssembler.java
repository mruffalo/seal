package assembly;

import java.util.*;

/**
 * @author mruffalo
 */
public class HybridizationSequenceAssembler implements SequenceAssembler
{
	@Override
	public String assembleSequence(List<String> fragments)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	protected static class DeBruijnGraph
	{
		private Map<String, Vertex> vertices;
		
		/**
		 * @param k
		 *            Length of each fragment
		 * @param fragments
		 *            Fragment list
		 * @throws FragmentSizeException
		 *             if an entry of <code>fragments</code> is not of length <code>k</code>
		 */
		protected DeBruijnGraph(int k, List<String> fragments)
		{
			vertices = new HashMap<String, Vertex>(fragments.size() * 2);
			for (String string : fragments)
			{
				if (string.length() != k)
				{
					throw new FragmentSizeException(String.format("Length of fragment %s was not %d", string, k));
				}
				String first = string.substring(0, k - 1);
				Vertex from = getVertex(first);
				String second = string.substring(1);
				Vertex to = getVertex(second);
				if (from == null)
				{
					from = addVertex(first);
				}
				if (to == null)
				{
					to = addVertex(second);
				}
				from.addEdge(to);
			}
		}
		
		/**
		 * TODO: test this
		 * 
		 * @return
		 */
		public boolean isEulerian()
		{
			int moreOutgoingThanIncoming = 0, moreIncomingThanOutgoing = 0;
			for (Vertex v : vertices.values())
			{
				int edgeCountDifference = v.getOutgoingIncomingDifference();
				if (Math.abs(edgeCountDifference) > 1)
				{
					/*
					 * A vertex has more than a single edge count difference (e.g. two more outgoing
					 * edges than incoming).
					 */
					return false;
				}
				if (edgeCountDifference > 0)
				{
					moreOutgoingThanIncoming++;
				}
				else
				{
					moreIncomingThanOutgoing++;
				}
			}
			return moreOutgoingThanIncoming == 1 && moreIncomingThanOutgoing == 1;
		}
		
		public Vertex addVertex(String string)
		{
			Vertex v = new Vertex(string);
			vertices.put(string, v);
			return v;
		}
		
		public Vertex getVertex(String string)
		{
			return vertices.get(string);
		}
		
		public boolean eulerize()
		{
			// TODO this
			return false;
		}
		
		protected class Vertex
		{
			private final String string;
			private Map<String, Edge> edges;
			/**
			 * TODO: Figure out if a Vertex should keep incoming edges references too
			 */
			private int incomingEdges;
			
			public Vertex(String string_)
			{
				string = string_;
				edges = new HashMap<String, Edge>();
			}
			
			public Vertex(String string_, int edges_)
			{
				string = string_;
				edges = new HashMap<String, Edge>(edges_);
			}
			
			public Edge addEdge(Vertex to)
			{
				Edge e = new Edge(this, to);
				edges.put(to.string, e);
				to.incrementIncomingEdges();
				return e;
			}
			
			public Edge getEdge(String string)
			{
				return edges.get(string);
			}
			
			public void incrementIncomingEdges()
			{
				incomingEdges++;
			}
			
			/**
			 * @return out - in (edge counts).
			 */
			public int getOutgoingIncomingDifference()
			{
				return edges.size() - incomingEdges;
			}
		}
		
		protected class Edge
		{
			public Vertex from;
			public Vertex to;
			
			public Edge(Vertex from_, Vertex to_)
			{
				from = from_;
				to = to_;
			}
		}
		
		protected class Path
		{
			
		}
	}
}
