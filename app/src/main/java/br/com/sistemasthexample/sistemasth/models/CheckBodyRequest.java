package br.com.sistemasthexample.sistemasth.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class CheckBodyRequest implements Serializable {
    private String sIdBiometria;
    private Boolean sValidarCPF;
    private String sFrente;
    private String sVerso;
    private String sSelfie;
    @SerializedName("IdBiometria")
    public String getsIdBiometria() {
        return sIdBiometria;
    }

    public void setsIdBiometria(String sIdBiometria) {
        this.sIdBiometria = sIdBiometria;
    }
    @SerializedName("tsValidarCPF")
    public Boolean getsValidarCPF() {
        return sValidarCPF;
    }

    public void setsValidarCPF(Boolean sValidarCPF) {
        this.sValidarCPF = sValidarCPF;
    }
    @SerializedName("sFrente")
    public String getsFrente() {
        return sFrente;
    }

    public void setsFrente(String sFrente) {
        this.sFrente = sFrente;
    }
    @SerializedName("sVerso")
    public String getsVerso() {
        return sVerso;
    }

    public void setsVerso(String sVerso) {
        this.sVerso = sVerso;
    }
    @SerializedName("sSelfie")
    public String getsSelfie() {
        return sSelfie;
    }

    public void setsSelfie(String sSelfie) {
        this.sSelfie = sSelfie;
    }


}
