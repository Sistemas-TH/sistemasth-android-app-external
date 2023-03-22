/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.sistemasthexample.java.facedetector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import br.com.sistemasthexample.GraphicOverlay;
import br.com.sistemasthexample.GraphicOverlay.Graphic;
import br.com.sistemasthexample.sistemasth.models.Config;

import com.google.common.base.Strings;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.face.FaceLandmark.LandmarkType;

import java.util.List;

/**
 * Graphic instance for rendering face position, contour, and landmarks within the associated
 * graphic overlay view.
 */
public class FaceGraphic extends Graphic {
    private static final float FACE_POSITION_RADIUS = 8.0f;

    private static float NOSE_RADIUS = 16.0f;
    private static int NOSE_POINT_COLOR = Color.WHITE;
    private static float NOSE_SIZE = 16.0f;
    private static int BOX_COLOR = Color.RED;


    private final Paint facePositionPaint;
    private final Paint textPaint;
    private volatile Face face;
    public static boolean isNoseDetected;
    private boolean isCentralized = false;
    private FaceDetectorProcessor faceDetectorProcessor;

    FaceGraphic(GraphicOverlay overlay, Face face, FaceDetectorProcessor faceDetectorProcessor) {
        super(overlay);

        NOSE_RADIUS = Config.nosePointRadius == 0 ? NOSE_RADIUS : Config.nosePointRadius;
        NOSE_POINT_COLOR = Strings.isNullOrEmpty(Config.nosePointColor) ? Color.WHITE : Color.parseColor(Config.nosePointColor);
        BOX_COLOR = Strings.isNullOrEmpty(Config.boxColor) ? Color.RED : Color.parseColor(Config.boxColor);
        NOSE_SIZE = Config.nosePointSize == 0 ? NOSE_SIZE : Config.nosePointSize;

        this.face = face;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(35.0f);
        this.faceDetectorProcessor = faceDetectorProcessor;

    }


    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (isNoseDetected) return;
        Face faceScoped = this.face;

        Point nosePoint = drawNosePoint(canvas);
        drawFaceRect(canvas);

        if (faceScoped == null) {
            return;
        }
        CheckIfNoseTouchPoint(canvas, faceScoped, nosePoint);
        // CentralizeFace(canvas);

    }

    private void CheckIfNoseTouchPoint(Canvas canvas, Face faceScoped, Point nosePoint) {

        for (FaceContour contour : faceScoped.getAllContours()) {
            if (contour.getFaceContourType() != FaceContour.NOSE_BRIDGE) continue;
            List<PointF> f = contour.getPoints();
            PointF noseTip = f.get(1);
            isNoseDetected = circlesOverlap((int) noseTip.x, (int) noseTip.y, faceDetectorProcessor.xNose, faceDetectorProcessor.yNose, (int) NOSE_RADIUS);
            if (isNoseDetected) {
                Log.d("FaceGraphic", "Nose detected!!!");
                facePositionPaint.setColor(Color.RED);


                // draw a circle on the nose


                canvas.drawCircle(
                        translateX(nosePoint.x), translateY(nosePoint.y), FACE_POSITION_RADIUS, facePositionPaint);
                break;
                //isFinishing = true; // Iniciar o envio para o challengeVerify
            }
        }
    }

    private Boolean circlesOverlap(int x1, int y1, int x2, int y2, int r) {
        // Find the distance between the centers of the circles
        int dx = x2 - x1;
        int dy = y2 - y1;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Check if the distance between the centers of the circles is less than the sum of the radii
        return distance < r * 2;
    }

    private Point drawNosePoint(Canvas canvas) {
        int prevColor = facePositionPaint.getColor();
        facePositionPaint.setColor(NOSE_POINT_COLOR);
        Point p = new Point(faceDetectorProcessor.xNose, faceDetectorProcessor.yNose);
        canvas.drawCircle(translateX(p.x), translateY(p.y), NOSE_SIZE, facePositionPaint);
        facePositionPaint.setColor(prevColor);
        return p;
    }

    private void drawFaceRect(Canvas canvas) {


        float xInit = faceDetectorProcessor.xBox;
        float xEnd = faceDetectorProcessor.xBox + faceDetectorProcessor.widthBox;
        float yInit = faceDetectorProcessor.yBox;
        float yEnd = faceDetectorProcessor.yBox + faceDetectorProcessor.heightBox;

        xInit = xInit - 20;
        xEnd = xEnd - 20;
        yInit = yInit - 20;
        yEnd = yEnd - 20;

        facePositionPaint.setColor(BOX_COLOR);
        for (int i = (int) xInit; i < xEnd; i++) {
            canvas.drawCircle(translateX(i), translateY(yInit), FACE_POSITION_RADIUS, facePositionPaint);
            canvas.drawCircle(translateX(i), translateY(yEnd), FACE_POSITION_RADIUS, facePositionPaint);
        }
        facePositionPaint.setColor(BOX_COLOR);
        for (int i = (int) yInit; i < yEnd; i++) {
            canvas.drawCircle(translateX(xInit), translateY(i), FACE_POSITION_RADIUS, facePositionPaint);
            canvas.drawCircle(translateX(xEnd), translateY(i), FACE_POSITION_RADIUS, facePositionPaint);
        }

        facePositionPaint.setColor(Color.GREEN);
    }

    private void CentralizeFace(Canvas canvas) {


        int distanceFromBorder = 180;
        float rightDistance = translateX(overlay.getImageWidth() - distanceFromBorder);
        float leftDistance = translateX(distanceFromBorder);
        FaceLandmark leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK);
        FaceLandmark rightCheek = face.getLandmark(FaceLandmark.RIGHT_CHEEK);
        facePositionPaint.setColor(Color.WHITE);
        if (leftCheek != null && rightCheek != null) {
            int leftCheekX = (int) translateX(leftCheek.getPosition().x);
            int rightCheekX = (int) translateX(rightCheek.getPosition().x);
            // Log.d("DebuggingFace", "rightDistance (black): " + rightDistance);
            // Log.d("DebuggingFace", "rightCheek.getPosition().x (black): " + leftCheekX);
            // Log.d("DebuggingFace", "LeftDistance (green): " + leftDistance);
            // Log.d("DebuggingFace", "leftCheek.getPosition().x (green): " + rightCheekX);
            isCentralized = leftCheekX > leftDistance && rightCheekX < rightDistance;
            if (isCentralized) {
                facePositionPaint.setColor(Color.GREEN);
            } else {

                canvas.drawText(
                        "Centralize e aproxime o seu rosto da câmera",
                        10,
                        translateY(overlay.getImageHeight() - 20),
                        textPaint);

            }
        }
        for (int i = 0; i < overlay.getImageHeight(); i++) {
            //facePositionPaint.setColor(Color.BLACK);
            canvas.drawCircle(rightDistance, translateY(i), FACE_POSITION_RADIUS, facePositionPaint);
            //facePositionPaint.setColor(Color.GREEN);
            canvas.drawCircle(leftDistance, translateY(i), FACE_POSITION_RADIUS, facePositionPaint);
        }
        drawFaceLandmark1(canvas, FaceLandmark.LEFT_CHEEK, Color.GREEN);
        drawFaceLandmark1(canvas, FaceLandmark.RIGHT_CHEEK, Color.BLACK);
    }

    private void drawFaceLandmark1(Canvas canvas, @LandmarkType int landmarkType, int color) {

        facePositionPaint.setColor(color);
        drawFaceLandmark(canvas, landmarkType);
        facePositionPaint.setColor(Color.WHITE);
    }

    private void drawFaceLandmark(Canvas canvas, @LandmarkType int landmarkType) {
        FaceLandmark faceLandmark = face.getLandmark(landmarkType);
        if (faceLandmark != null) {
            canvas.drawCircle(
                    translateX(faceLandmark.getPosition().x),
                    translateY(faceLandmark.getPosition().y),
                    FACE_POSITION_RADIUS,
                    facePositionPaint);

        }

    }

    public int distanceToLeft(PointF point) {
        return (int) point.x;


    }

    public int distanceToRight(PointF point) {
        return (int) (overlay.getImageWidth() - point.x);

    }


}