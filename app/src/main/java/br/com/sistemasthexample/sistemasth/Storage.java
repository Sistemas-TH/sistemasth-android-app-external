package br.com.sistemasthexample.sistemasth;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Hashtable;

public final class Storage {
    public static boolean requested_PV() { return Storage.functions.get(Constants.PV) != null && Storage.functions.get(Constants.PV) == 1; }

    public static boolean requested_DC() {        return Storage.functions.get(Constants.DC) != null && Storage.functions.get(Constants.DC) == 1;    }

    public static boolean requested_FM() {       return Storage.functions.get(Constants.FM) != null && Storage.functions.get(Constants.FM) == 1; }

    public static boolean requested_OC() {        return Storage.functions.get(Constants.OC) != null && Storage.functions.get(Constants.OC) == 1;    }

    public static boolean requested_DV() {        return Storage.functions.get(Constants.DV) != null && Storage.functions.get(Constants.DV) == 1;    }


    public static Hashtable<Constants, Integer> functions = new Hashtable<Constants, Integer>();
    public static Hashtable<Constants, Object> storage = new Hashtable<Constants, Object>();
    public static String coalesceStorage(Object one, String two)
    {

        return one != null ? one.toString() : two;
    }
    public static Integer coalesceInteger(Integer one, Integer two)
    {

        return one != null ? one : two;
    }
    public static String transformObjectToString(Object obj) {
        String jsonSerialized;
        try{

            ObjectMapper mapper = new ObjectMapper();
            jsonSerialized = mapper.writeValueAsString(obj);
        }catch (Exception e){
            jsonSerialized = e.toString();
        }
        return jsonSerialized;
    }
}
