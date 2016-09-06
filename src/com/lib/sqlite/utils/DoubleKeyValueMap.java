/* Copyright (c) 2013. wyouflf (wyouflf@gmail.com) Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License. */

package com.lib.sqlite.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-6-19
 * Time: PM 1:18
 */
public class DoubleKeyValueMap<K1, K2, V> {

	private final HashMap<K1, HashMap<K2, V>> map;

	public DoubleKeyValueMap() {
		this.map = new HashMap<K1, HashMap<K2, V>>();
	}

	public void put(K1 key1, K2 key2, V value) {
		if (map.containsKey(key1)) {
			HashMap<K2, V> k2V_map = map.get(key1);
			if (k2V_map != null) {
				k2V_map.put(key2, value);
			} else {
				k2V_map = new HashMap<K2, V>();
				k2V_map.put(key2, value);
				map.put(key1, k2V_map);
			}
		} else {
			HashMap<K2, V> k2V_map = new HashMap<K2, V>();
			k2V_map.put(key2, value);
			map.put(key1, k2V_map);
		}
	}

	public Set<K1> getFirstKeys() {
		return map.keySet();
	}

	public HashMap<K2, V> get(K1 key1) {
		return map.get(key1);
	}

	public V get(K1 key1, K2 key2) {
		HashMap<K2, V> k2_v = map.get(key1);
		return k2_v == null ? null : k2_v.get(key2);
	}

	public Collection<V> getAllValues(K1 key1) {
		HashMap<K2, V> k2_v = map.get(key1);
		return k2_v == null ? null : k2_v.values();
	}

	public Collection<V> getAllValues() {
		Collection<V> result = null;
		Set<K1> k1Set = map.keySet();
		if (k1Set != null) {
			result = new ArrayList<V>();
			for (K1 k1 : k1Set) {
				Collection<V> values = map.get(k1).values();
				if (values != null) {
					result.addAll(values);
				}
			}
		}
		return result;
	}

	public boolean containsKey(K1 key1, K2 key2) {
		if (map.containsKey(key1)) {
			return map.get(key1).containsKey(key2);
		}
		return false;
	}

	public boolean containsKey(K1 key1) {
		return map.containsKey(key1);
	}

	public int size() {
		if (map.size() == 0)
			return 0;

		int result = 0;
		for (HashMap<K2, V> k2V_map : map.values()) {
			result += k2V_map.size();
		}
		return result;
	}

	public void remove(K1 key1) {
		map.remove(key1);
	}

	public void remove(K1 key1, K2 key2) {
		HashMap<K2, V> k2_v = map.get(key1);
		if (k2_v != null) {
			k2_v.remove(key2);
		}
	}

	public void clear() {
		if (map.size() > 0) {
			for (HashMap<K2, V> k2V_map : map.values()) {
				k2V_map.clear();
			}
			map.clear();
		}
	}
}
