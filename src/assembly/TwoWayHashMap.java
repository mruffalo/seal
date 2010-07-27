package assembly;

import java.util.*;

public class TwoWayHashMap<K, V> implements TwoWayMap<K, V>
{
	private Map<K, V> forward;
	private Map<V, K> backward;

	public TwoWayHashMap()
	{
		forward = new HashMap<K, V>();
		backward = new HashMap<V, K>();
	}

	public TwoWayHashMap(int size)
	{
		forward = new HashMap<K, V>(size);
		backward = new HashMap<V, K>(size);
	}

	@Override
	public void clear()
	{
		forward.clear();
		backward.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return forward.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return backward.containsKey(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return forward.entrySet();
	}

	@Override
	public V get(Object key)
	{
		return forward.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return forward.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return forward.keySet();
	}

	@Override
	public V put(K key, V value)
	{
		backward.put(value, key);
		return forward.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		forward.putAll(m);
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
		{
			backward.put(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public V remove(Object key)
	{
		backward.remove(forward.get(key));
		return forward.remove(key);
	}

	@Override
	public int size()
	{
		return forward.size();
	}

	@Override
	public Collection<V> values()
	{
		return forward.values();
	}

	@Override
	public K getKey(V value)
	{
		return backward.get(value);
	}

}
