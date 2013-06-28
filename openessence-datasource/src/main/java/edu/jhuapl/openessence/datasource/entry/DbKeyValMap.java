package edu.jhuapl.openessence.datasource.entry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maps a database key to it's actual database value. The key is the dimensionid defined in the datasource definition
 * file.
 */
public class DbKeyValMap implements Map<String, Object> {

    private HashMap<String, Object> map;

    /**
     * Default constructor
     */
    public DbKeyValMap() {
        map = new HashMap<String, Object>();
    }

    /**
     * Contstructor will turn any Map<String,Object> into a DbKeyValMap
     *
     * @param map map of pk dimension id to it's value
     */
    public DbKeyValMap(Map<String, ?> map) {
        this.map = new HashMap<String, Object>(map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }


    @Override
    public Set<String> keySet() {
        return map.keySet();
    }


    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

}
