package persistence;

import exceptions.DBError;
import exceptions.SyncException;
import model.Model;
import persistence.datapool.DataPool;
import persistence.datapool.HashMapDataPool;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class Database<ENTRY_TYPE, ENTITY extends Model<ENTRY_TYPE>> {
    /* This Database is NOT ThreadSafety*/
    private DataPool<ENTRY_TYPE, ENTITY> dataPool;

    private final Class<ENTITY> modelType;
    private final String dbFileName;
    private final String modelName;

    public Collection<ENTITY> all(){return dataPool.retrieveAll();}

    private String capitalizeField(String field){
        char[] buf = field.toCharArray();
        if (buf[0] >= 'a' && buf[0] <= 'z') {
            buf[0] = (char) (buf[0] - 32);
        }
        return new String(buf);
    }

    public Database(Class<ENTITY> entityClass, DataPool<ENTRY_TYPE, ENTITY> dataPool){
        this.modelType = entityClass;
        this.modelName = modelType.getSimpleName();
        this.dbFileName = this.modelName + ".db";
        
        
        try {
            initDataPool(dataPool);
        } catch (IOException e) {
            throw new SyncException("Failed to sync DB " + dbFileName + " due to IOException");
        }

    }



    private Object retrieveField(String field, ENTITY entity){

        field = capitalizeField(field);

        try {
            Method method = modelType.getMethod("get" + field);
            return method.invoke(entity);
        } catch (InvocationTargetException e) {
            throw new DBError("Unknown error when call Getter of '" + field + "'  in model " + modelName);
        } catch (NoSuchMethodException e) {
            throw new DBError("Getter of " + field + " doesn't exist in model " + modelName);
        } catch (IllegalAccessException e){
            throw new DBError("Cannot access to Getter of '" + field + "' in model " + modelName);
        }

    }

    private Map<String, Object> retrieveField(String[] fields, ENTITY entity){

        Map<String, Object> ret = new TreeMap<>();

        for(String field: fields){
            ret.put(field, retrieveField(field, entity));
        }

        return ret;
    }

    private <FIELDTYPE> void setField(String field, FIELDTYPE value, ENTITY entity){

        field = capitalizeField(field);

        try {
            Method method = modelType.getMethod("set" + field, value.getClass());
            method.invoke(entity, value);
        } catch (InvocationTargetException e) {
            throw new DBError("Unknown error when call Setter of " + field + "  in model " + modelName);
        } catch (NoSuchMethodException e) {
            throw new DBError("Setter of " + field + " doesn't exist in model " + modelName);
        } catch (IllegalAccessException e){
            throw new DBError("Cannot access to setter of" + field + " in model " + modelName);
        }

    }

    private void setField(HashMap<String, Object> map, ENTITY entity){

        for(Map.Entry<String, Object> entry: map.entrySet()){
            setField(entry.getKey(), entry.getValue(), entity);
        }
    }


    public <T> ENTITY retrieveOne(String field, T value) throws ClassCastException{
        if(field.equals("key")) return (ENTITY) dataPool.retrieve((ENTRY_TYPE) value);

        Collection<ENTITY> dataCollection = dataPool.retrieveAll();
        ENTITY result = null;
        for(ENTITY entity:dataCollection){
            if(exist(field, value,entity)){
                result = entity;
                break;
            }
        }
        return  result;
    }

    public <T> boolean exist(String field, T value, ENTITY entity){
        if(entity == null) return false;

        return retrieveField(field, entity).equals(value);
    }


    public <T extends Comparable<? super T>> boolean exist(String field, T valueFrom, T valueTo, ENTITY entity){
        if(entity == null) return false;

        T invokeResult = (T) retrieveField(field, entity);
        return (invokeResult.compareTo(valueFrom) >= 0 && invokeResult.compareTo(valueTo) <= 0);
    }

    public boolean exist(Map<String, Object> conditions, ENTITY entity){

        if(entity == null) return false;

        String []fields = conditions.keySet().toArray(new String[0]);

        Map<String, Object> values = retrieveField(fields, entity);
        boolean compareFlag = true;
        for(Map.Entry<String, Object> entry: conditions.entrySet()){

            String key = (String) entry.getKey();
            Object value = values.get(key);

            if(value instanceof Pair){
                Pair<Object, Object> pair = (Pair<Object, Object>) value;
                Object target = values.get(key);
                if(target instanceof Comparator){
                    Comparable<Object> comparable = (Comparable<Object>) target;
                    if(comparable.compareTo(pair.getKey()) < 0 || comparable.compareTo(pair.getValue()) > 0){
                        compareFlag = false;
                        break;
                    }
                }

            }

            if(values.get(key) != null && !values.get(key).equals(conditions.get(key))){
                compareFlag = false;
                break;
            }
            if(values.get(key) == null && values.get(key) != null){
                compareFlag = false;
                break;
            }
        }
        return compareFlag;
    }


    public Collection<ENTITY> filter(Map<String, Object> conditions, Collection<ENTITY> entities, int limit){

        Collection<ENTITY> res = new ArrayList<>();
        int count = 0;
        for(ENTITY entity: entities){

            if(exist(conditions, entity)){
                res.add(entity);
                if(++count >= limit) break;
            }
        }
        return res;
    }



    public <T> Collection<ENTITY> filter(String field, T value, Collection<ENTITY> entities, int limit){
        int count = 0;
        ArrayList<ENTITY> results = new ArrayList<>();
        for(ENTITY entity:entities){
            if(exist(field, value, entity)){
                results.add(entity);
                if(++count >= limit) break;
            }
        }
        return results;
    }

    public <T> Collection<ENTITY> where(String field, T value, int limit){

        if(field.equals("key") && limit >= 1)
            return new ArrayList<>(Collections.singletonList(dataPool.retrieve((ENTRY_TYPE) value)));

        return filter(field, value, dataPool.retrieveAll(), limit);

    }

    public ArrayList<Map<String, Object>> select(String[] fields, Collection<ENTITY> collection){
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for(ENTITY entity: collection){
            result.add(retrieveField(fields, entity));
        }
        Collection<ENTITY> dataCollection = dataPool.retrieveAll();
        return result;

    }

    public Object select(String field, ENTITY entity){
        return retrieveField(field, entity);
    }

    public Map<String, Object> select(String[] fields, ENTITY entity){
        return retrieveField(fields, entity);
    }

    public Collection<ENTITY> where(Map<String, Object> conditions, int limit){
        Collection<ENTITY> dataCollection = dataPool.retrieveAll();
        return filter(conditions, dataCollection, limit);

    }

    public Collection<ENTITY> where(Map<String, Object> condition){
        return where(condition, Integer.MAX_VALUE);
    }

    public <T extends Comparable<? super T>> Collection<ENTITY> where(String field, T valueFrom, T valueTo, int limit){

        int count = 0;
        Collection<ENTITY> dataCollection = dataPool.retrieveAll();
        ArrayList<ENTITY> results = new ArrayList<>();
        for(ENTITY entity:dataCollection){
            if(exist(field, valueFrom, valueTo, entity)){
                results.add(entity);
                if(++count >= limit) break;
            }
        }
        return results;

    }

    public <T extends Comparable<? super T>> Collection<ENTITY> where(String field, T valueFrom, T valueTo){

        return where(field, valueFrom, valueTo, Integer.MAX_VALUE);

    }
    public <T> Collection<ENTITY> where(String field, T value){
        return where(field, value, Integer.MAX_VALUE);
    }

    public <ENTRY2, ENTITY2 extends Model<ENTRY2>> ArrayList<Pair<ENTITY, ENTITY2>> join(Map<String, String> fieldsMap, Database<ENTRY2, ENTITY2> db2){
        if(db2==null) return null;
        return join(fieldsMap, db2, db2.all());
    }
    public <ENTRY2, ENTITY2 extends Model<ENTRY2>> ArrayList<Pair<ENTITY, ENTITY2>> join(Map<String, String> fieldsMap, Database<ENTRY2, ENTITY2> db2, Collection<ENTITY2> collectionToJoin){

        ArrayList<Pair<ENTITY, ENTITY2>> res = new ArrayList<>();
        String[] fields1 = fieldsMap.keySet().toArray(new String[0]);
        Collection<ENTITY> res1 = dataPool.retrieveAll();
        Collection<ENTITY2> res2;

        for(ENTITY entity1: res1){
            Map<String, Object> fieldsToJoinLeft = retrieveField(fields1, entity1);
            Map<String, Object> fieldsToJoinRight = new HashMap<>();
            for(Map.Entry<String, Object> entry: fieldsToJoinLeft.entrySet()){
                fieldsToJoinRight.put(fieldsMap.get(entry.getKey()), entry.getValue());
            }
            res2 = db2.filter(fieldsToJoinRight, collectionToJoin, Integer.MAX_VALUE);
            for(ENTITY2 entity2: res2){
                res.add(new Pair<ENTITY, ENTITY2>(entity1, entity2));
            }
        }
        return res;
    }

    public int delete(Map<String, Object> map, int limit){

        int count = 0;

        for (ENTITY entity : dataPool.retrieveAll()) {
            if(exist(map, entity)){
                dataPool.remove(entity);
                if(++count >= limit) break;
            }
        }
        if(count>0) dataPool.updateVersion();
        return count;
    }

    public int delete(Map<String, Object> map){
        return delete(map, Integer.MAX_VALUE);
    }

    public <T> int delete(String field, T value, int limit){
        dataPool.updateVersion();
        if(field.equals("key") && limit>=1){
            dataPool.remove((ENTRY_TYPE) value);
            return 1;
        }

        int count = 0;

        for (ENTITY entity : dataPool.retrieveAll()) {
            if (exist(field, value, entity)) {
                dataPool.remove(entity);
                if (++count >= limit) break;
            }
        }
        return count;
    }


    public <T> int delete(String field, T value){
        return delete(field, value, Integer.MAX_VALUE);
    }


    public boolean create(ENTITY entity){

        if(dataPool.put(entity)){
            dataPool.updateVersion();
            return true;
        }else{
            return false;
        }
    }

    public <T> int update(String field, T value, Collection<ENTITY> entitiesToUpdate) {
        int count = 0;
        if (field.equals("key")) {

            for (ENTITY entity : entitiesToUpdate) {
                dataPool.remove(entity);
                entity.setKey((ENTRY_TYPE) value);
                dataPool.put(entity);
                count ++;
            }
        }else {

            for (ENTITY entity : entitiesToUpdate) {
                setField(field, value, entity);
                count ++;
            }
        }
        if(count > 0) dataPool.updateVersion();

        return count;
    }

    public int update(Map<String, Object> map, ENTITY entity) {

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            setField(entry.getKey(), entry.getValue(), entity);
        }
        dataPool.updateVersion();
        return 1;
    }

    public int update(Map<String, Object> map, Collection<ENTITY> entitiesToUpdate) {
        int count = 0;

        for (ENTITY entity : entitiesToUpdate) {
            count += update(map, entity);
        }

        if(count > 0) dataPool.updateVersion();

        return count;
    }

    public <T> int update(String field, T value, ENTITY entity) {
        if(field.equals("key")) {
            dataPool.remove(entity);
            entity.setKey((ENTRY_TYPE) value);
            dataPool.put(entity);
            setField(field, value, entity);
        }else {
            setField(field, value, entity);
        }
        dataPool.updateVersion();
        return 1;
    }

    public int update(ENTITY entity) {
        if(dataPool.contains(entity)){
            dataPool.updateVersion();
            return 1;
        }else return 0;

    }

    public <ENTRY2, ENTITY2 extends Model<ENTRY2>> ENTITY2 getNested(String field, ENTITY entity, Database<ENTRY2, ENTITY2> db2){
        Object obj = this.retrieveField(field, entity);
        return db2.retrieveOne("key", obj);
    }

    private void initDataPool(DataPool<ENTRY_TYPE, ENTITY> dataPoolInstance) throws IOException {
        File file = new File(dbFileName);
        boolean fileCreated = false;

        if(!file.exists())
        {
            try {
                if(file.createNewFile()){
                    try {
                        this.dataPool = dataPoolInstance;
                        writeDataPoolToDisk();
                        System.out.printf("[Debug] DB %s Newly Generated.\n", modelName);
                    }catch (NotSerializableException e){
                        throw new SyncException("Model " + modelName + " cannot be serialized");
                    }
                }else throw new SyncException("Create DB file failed dut to Unknown error");
            } catch (IOException e) {
                throw new SyncException("Cannot create DB file due to IOException");
            }
        }else{
            loadDataPool();
            System.out.printf("[Debug] DB %s loaded.\n", modelName);
        }
    }
    public boolean sync() {

        if(dataPool == null)
            throw new UnsupportedOperationException("Cannot call sync() before init the DataPool");

        File file = new File(dbFileName);
        boolean fileCreated = false;
        try {
            if(!file.exists())
            {
                try {
                    fileCreated = file.createNewFile();
                } catch (IOException e) {
                    throw new SyncException("Cannot create DB file");
                }
            }

            if(fileCreated){
                try {
                    writeDataPoolToDisk();
                    System.out.printf("[Debug] DB %s Regenerated.\n", modelName);
                }catch (NotSerializableException e){
                    throw new SyncException("Model " + modelName + " cannot be serialized");
                }
            }else{

                if(dataPool.getVersion() >= new File(dbFileName).lastModified()){
                    writeDataPoolToDisk();
                }else{
                    loadDataPool();
                }
                System.out.printf("[Debug] DB %s synced.\n", modelName);
            }

            return true;

        } catch (IOException e) {
            throw new SyncException("IO Exception(" + e.getMessage() + ") occurred when Syncing DB " + modelName);
        } catch (ClassCastException e){
            throw new SyncException("Data file is broken in DB " + modelName);
        }

    }

    private void loadDataPool() throws IOException{
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new FileInputStream(dbFileName));
            Object objectToCast = ois.readObject();
            ois.close();
            this.dataPool = (DataPool<ENTRY_TYPE, ENTITY>) objectToCast;
        }catch (NotSerializableException e){
            throw new SyncException("Cannot deserialize " + modelName + " from DB file");
        }catch (ClassNotFoundException e){
            throw new SyncException("Class of " + modelName + " was not found while deserializing");
        }
    }

    private void writeDataPoolToDisk() throws IOException {
        ObjectOutputStream oos;
        oos = new ObjectOutputStream(new FileOutputStream(dbFileName));
        oos.writeObject(dataPool);
        oos.close();
    }
}
