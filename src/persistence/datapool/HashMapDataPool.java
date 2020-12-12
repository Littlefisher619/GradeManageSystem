package persistence.datapool;

import model.Model;
import persistence.Pair;

import java.util.*;

public class HashMapDataPool<ENTRY_TYPE, ENTITY extends Model<ENTRY_TYPE>> implements DataPool<ENTRY_TYPE, ENTITY>  {
    private static final long serialVersionUID = 1111000000000000000L;

    private final HashMap<ENTRY_TYPE, ENTITY> hashMap;
    private Long version;

    public HashMapDataPool(){
        this.hashMap = new HashMap<>();
    }

    public Collection<ENTITY> retrieveAll(){
        return new ArrayList<>(hashMap.values());
    }

    public ENTITY retrieve(ENTRY_TYPE entry){
        return hashMap.get(entry);
    }

    public boolean remove(ENTITY entity){
        return remove((ENTRY_TYPE) entity.getKey());
    }

    public boolean remove(ENTRY_TYPE entry){
        ENTITY deleted = hashMap.remove(entry);
        return deleted!=null;
    }
    public boolean put(ENTITY entity){
        ENTRY_TYPE entry = (ENTRY_TYPE)entity.getKey();
        if(hashMap.containsKey(entity.getKey())) return false;
        hashMap.put(entry, entity);
        return true;
    }

    @Override
    public boolean contains(ENTITY entity) {
        return hashMap.containsKey(entity.getKey());
    }

    public Long getVersion() {
        return this.version == null ? Long.MIN_VALUE : this.version;
    }

    public Long updateVersion() {
        return (this.version = System.currentTimeMillis());
    }
}
