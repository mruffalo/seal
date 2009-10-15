package assembly;

import java.util.Map;

public interface TwoWayMap<K, V> extends Map<K, V>
{
	public K getKey(V value);
}
