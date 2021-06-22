package ed.od.ua.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Container {

    private static final int DEFAULT_SIZE = 64;
    private final Map<String, Integer> hashMapVariable = new ConcurrentHashMap<>(DEFAULT_SIZE);
    private final int DEFAULT_CAPACITY = 1;

    public Map<String, Integer> getHashMapVariable() {
        return hashMapVariable;
    }

    public void add(String word) {
        if (hashMapVariable.containsKey(word)) {
            hashMapVariable.put(word, hashMapVariable.get(word) + DEFAULT_CAPACITY);
        } else {
            hashMapVariable.put(word, DEFAULT_CAPACITY);
        }
    }

}
