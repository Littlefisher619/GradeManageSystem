package persistence.datapool;

import model.Model;
import persistence.Pair;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayListDataPool<ENTRY_TYPE, ENTITY extends Model<ENTRY_TYPE>> implements DataPool<ENTRY_TYPE, ENTITY>  {
    private static final long serialVersionUID = 1112000000000000001L;

    private ArrayList<ENTRY_TYPE> entries;
    private ArrayList<ENTITY> entities;
    private Long version;


    @Override
    public Collection<ENTITY> retrieveAll() {
        return null;
    }

    @Override
    public ENTITY retrieve(ENTRY_TYPE entry) {
        return null;
    }

    @Override
    public boolean remove(ENTITY entity) {
        return false;
    }

    @Override
    public boolean remove(ENTRY_TYPE entity) {
        return false;
    }

    @Override
    public boolean put(ENTITY entity) {
        return false;
    }

    @Override
    public boolean contains(ENTITY entity) {
        return false;
    }

    @Override
    public Long getVersion() {
        return null;
    }

    @Override
    public Long updateVersion() {
        return null;
    }
}
