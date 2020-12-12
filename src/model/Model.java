package model;

import java.io.Serializable;

public abstract class Model<ENTRY_TYPE> implements Serializable {

    abstract public ENTRY_TYPE getKey();
    abstract public void setKey(ENTRY_TYPE key);
}
