package br.com.sistemasthexample.java;

import com.google.mlkit.vision.face.FaceLandmark;

public class FaceLandmarkExtensions {
    public static int getX(FaceLandmark faceLandmark) {
        return (int) faceLandmark.getPosition().x;
    }

    public static int getY(FaceLandmark faceLandmark) {
        return (int) faceLandmark.getPosition().y;
    }
} 