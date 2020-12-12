package persistence.datapool;

import model.Model;

import java.io.Serializable;
import java.util.Collection;

public interface DataPool<ENTRY_TYPE, ENTITY extends Model> extends Serializable {
    Collection<ENTITY> retrieveAll();
    ENTITY retrieve(ENTRY_TYPE entry);
    boolean remove(ENTITY entity);
    boolean remove(ENTRY_TYPE entity);
    boolean put(ENTITY entity);
    boolean contains(ENTITY entity);
    Long getVersion();
    Long updateVersion();
}
