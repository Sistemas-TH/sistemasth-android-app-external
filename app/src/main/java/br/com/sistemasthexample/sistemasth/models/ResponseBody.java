package br.com.sistemasthexample.sistemasth.models;

import java.io.Serializable;

public class ResponseBody<Type> implements Serializable {

    private static final long serialVersionUID = -2651101556354636163L;

    private String id;
    private String version;
    private Type data;

    public Type getData() {
        return data;
    }

    public void setData(Type data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
