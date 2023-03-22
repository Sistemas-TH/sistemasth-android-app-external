package br.com.sistemasthexample.sistemasth.models;



// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.SerializedName; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
THSDKFaceMatchModel[] THSDKFaceMatchResult = om.readValue(myJsonString, THSDKFaceMatchModel[].class); */

import com.google.gson.annotations.SerializedName;

public class THSDKFaceMatchModel{
    @SerializedName("BIO")
    public BIO bIO;

    public class BIO{
        @SerializedName("IDBIOMETRIA")
        public String iDBIOMETRIA;
        @SerializedName("Similaridade")
        public String similaridade;
        @SerializedName("Biometria")
        public String biometria;
        @SerializedName("JsonAWS")
        public JsonAWS jsonAWS;


        public class JsonAWS{
            @SerializedName("Message")
            public String message;
            @SerializedName("ExceptionMessage")
            public String exceptionMessage;
            @SerializedName("ExceptionType")
            public String exceptionType;
            @SerializedName("StackTrace")
            public String stackTrace;
            @SerializedName("InnerException")
            public InnerException innerException;


            public class InnerException{
                @SerializedName("Message")
                public String message;
                @SerializedName("ExceptionMessage")
                public String exceptionMessage;
                @SerializedName("ExceptionType")
                public String exceptionType;
                @SerializedName("StackTrace")
                public String stackTrace;
                @SerializedName("InnerException")
                public InnerException innerException;
            }


        }

    }


}


