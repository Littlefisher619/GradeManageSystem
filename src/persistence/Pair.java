package persistence;

import java.util.Map;
import java.util.Objects;

public class Pair<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value){
        this.key=key;
        this.value=value;
    }
    public K getKey(){
        return this.key;
    }
    public V getValue(){
        return this.value;
    }

    public V setValue(V value){
        return this.value = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Pair<" + key +
                ", " + value +
                '>';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return key.equals(pair.key) && Objects.equals(value, pair.value);
    }
}