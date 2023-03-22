package br.com.sistemasthexample.sistemasth.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class OCRData implements Serializable {

    private static final long serialVersionUID = 8349471379971135732L;

    private OCRExtraction extraction;

    public OCRExtraction getExtraction() {
        return extraction;
    }

    public void setExtraction(OCRExtraction extraction) {
        this.extraction = extraction;
    }



    public class BuscaCPF  implements Serializable {

        @SerializedName("CAD")
        private ArrayList<CAD> CAD;

        @SerializedName("END")
        private ArrayList<END> END;

        @SerializedName("FIXFULL")
        private ArrayList<FIXFULL> FIXFULL;

        @SerializedName("MOV")
        private ArrayList<MOV> MOV;

        @SerializedName("EMAIL")
        private ArrayList<EMAIL> EMAIL;

        // Getter Methods
        public ArrayList<CAD> getCAD() {
            return CAD;
        }
        public ArrayList<END> getEND() {
            return END;
        }
        public ArrayList<FIXFULL> getFIXFULL() {
            return FIXFULL;
        }
        public ArrayList<MOV> getMOV() {
            return MOV;
        }
        public ArrayList<EMAIL> getEMAIL() {
            return EMAIL;
        }


        public void setCAD(ArrayList<CAD> CAD) {
            this.CAD = CAD;
        }
        public void setEND(ArrayList<END> END) {
            this.END = END;
        }
        public void setFIXFULL(ArrayList<FIXFULL> FIXFULL) {
            this.FIXFULL = FIXFULL;
        }
        public void setMOV(ArrayList<MOV> MOV) {
            this.MOV = MOV;
        }
        public void setEMAIL(ArrayList<EMAIL> EMAIL) {
            this.EMAIL = EMAIL;
        }

        public class CAD{
            @SerializedName("CPF")
            public String cPF;
            @SerializedName("NOME")
            public String nOME;
            @SerializedName("SEXO")
            public String sEXO;
        }

        public class EMAIL{
            @SerializedName("DCR_EMAIL")
            public String dCR_EMAIL;
        }

        public class END{
            @SerializedName("BASE")
            public String bASE;
            @SerializedName("TIPO")
            public String tIPO;
            @SerializedName("LOGRADOURO")
            public String lOGRADOURO;
            @SerializedName("NUMERO")
            public String nUMERO;
            @SerializedName("COMPLEMENTO")
            public String cOMPLEMENTO;
            @SerializedName("BAIRRO")
            public String bAIRRO;
            @SerializedName("CIDADE")
            public String cIDADE;
            @SerializedName("UF")
            public String uF;
            @SerializedName("CEP")
            public String cEP;
        }

        public class FIXFULL{
            @SerializedName("FONEFIX")
            public String fONEFIX;
        }

        public class MOV{
            @SerializedName("FONECEL")
            public String fONECEL;
        }

        public class Root{
            @SerializedName("CAD")
            public ArrayList<CAD> cAD;
            @SerializedName("END")
            public ArrayList<END> eND;
            @SerializedName("FIXFULL")
            public ArrayList<FIXFULL> fIXFULL;
            @SerializedName("MOV")
            public ArrayList<MOV> mOV;
            @SerializedName("EMAIL")
            public ArrayList<EMAIL> eMAIL;
        }
    }




}


