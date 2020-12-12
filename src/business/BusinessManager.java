package business;

import exceptions.ConstraintException;
import model.Course;
import model.Grade;
import model.Model;
import persistence.Database;
import persistence.Pair;

import java.util.*;

public class BusinessManager<ENTRY_TYPE, ENTITY extends Model<ENTRY_TYPE>> {

    protected Database<ENTRY_TYPE, ENTITY> database;
    public Database<ENTRY_TYPE, ENTITY> getDatabase() {
        return database;
    }
    public Collection<ENTITY> getAll(){
        return database.all();
    }

    public <ENTRY2_TYPE, ENTITY2 extends Model<ENTRY2_TYPE>> ArrayList<Pair<ENTITY, ENTITY2>> join(HashMap<String, String> fields, BusinessManager<ENTRY2_TYPE, ENTITY2> manager){
        return this.getDatabase().join(fields, manager.getDatabase());
    }

    public <ENTRY2_TYPE, ENTITY2 extends Model<ENTRY2_TYPE>> ArrayList<Pair<ENTITY, ENTITY2>> join(HashMap<String, String> fields, BusinessManager<ENTRY2_TYPE, ENTITY2> manager, Collection<ENTITY2> toJoin){
        return this.getDatabase().join(fields, manager.getDatabase(), toJoin);
    }

    public <ENTRY2, ENTITY2 extends Model<ENTRY2>> ENTITY2 getNested(String field, ENTITY entity, BusinessManager<ENTRY2, ENTITY2> manager){
        return getDatabase().getNested(field, entity, manager.getDatabase());
    }

    public <ENTRY2, ENTITY2 extends Model<ENTRY2>> ENTITY2 getNested(String field, ENTITY entity, Database<ENTRY2, ENTITY2> db2){
        return getDatabase().getNested(field, entity, db2);
    }

    public <ENTRY2, ENTITY2 extends Model<ENTRY2>> ENTITY2 getNested(String field, ENTITY entity, HashMap<String, BusinessManager> managerMap){

        if(field.contains(".")){
            String[] buf = field.split("\\.+", 2);
            String nestField = buf[0], nextField = buf[1];

            BusinessManager nestManager = managerMap.get(nestField);
            Model nestEntity = this.getNested(nestField, entity, nestManager.getDatabase());

            return (ENTITY2) nestManager.getNested(nextField, nestEntity, managerMap);
        } return (ENTITY2) this.getNested(field, entity, managerMap.get(field).getDatabase());
    }

    public boolean create(ENTITY entity){
        if(entity == null) return false;
        if(this.getDatabase().create(entity)){
            this.sync();
            return true;
        }
        return false;
    }
    public boolean update(ENTITY entity){
        if(entity == null) return false;
        if(this.getDatabase().update(entity) > 0){
            this.sync();
            return true;
        }
        return false;
    }

    public boolean sync(){
        return database.sync();
    }

    public ENTITY queryByKey(ENTRY_TYPE key){
        return database.retrieveOne("key", key);
    }

    public boolean deleteByKey(ENTRY_TYPE key){
        boolean ret;
        if(ret = (database.delete("key", key) > 0))
            this.sync();
        return ret;
    }

    public boolean update(Map<String, Object> map, ENTITY target){
        boolean ret;
        if((ret = database.update(map, Collections.singletonList(target)) > 0))
            this.sync();
        return ret;
    }

}