/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package fr.dossierfacile.api.pdfgenerator.service.templates;

import com.jhlabs.image.ImageMath;
import com.jhlabs.image.TransformFilter;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * A filter which simulates a lens placed over an image.
 */
public class DFFilter extends TransformFilter {

    private float a = 0;
    private float b = 0;
    private float a2 = 0;
    private float b2 = 0;
    private float centreX = 0.5f;
    private float centreY = 0.5f;
    private float refractionIndex = 1.5f;

    private float icentreX;
    private float icentreY;
    private float width;
    private float height;
    private float s = 50f;

    public DFFilter() {
        setEdgeAction(CLAMP);
        setRadius(100.0f);
    }

    /**
     * Set the index of refaction.
     *
     * @param refractionIndex the index of refaction
     * @see #getRefractionIndex
     */
    public void setRefractionIndex(float refractionIndex) {
        this.refractionIndex = refractionIndex;
    }

    /**
     * Get the index of refaction.
     *
     * @return the index of refaction
     * @see #setRefractionIndex
     */
    public float getRefractionIndex() {
        return refractionIndex;
    }

    /**
     * Set the radius of the effect.
     *
     * @param r the radius
     * @min-value 0
     * @see #getRadius
     */
    public void setRadius(float r) {
        this.a = r;
        this.b = r;
    }

    /**
     * Get the radius of the effect.
     *
     * @return the radius
     * @see #setRadius
     */
    public float getRadius() {
        return a;
    }

    /**
     * Set the centre of the effect in the X direction as a proportion of the image size.
     *
     * @param centreX the center
     * @see #getCentreX
     */
    public void setCentreX(float centreX) {
        this.centreX = centreX;
    }

    public float getCentreX() {
        return centreX;
    }

    /**
     * Set the centre of the effect in the Y direction as a proportion of the image size.
     *
     * @param centreY the center
     * @see #getCentreY
     */
    public void setCentreY(float centreY) {
        this.centreY = centreY;
    }

    /**
     * Get the centre of the effect in the Y direction as a proportion of the image size.
     *
     * @return the center
     * @see #setCentreY
     */
    public float getCentreY() {
        return centreY;
    }

    /**
     * Set the centre of the effect as a proportion of the image size.
     *
     * @param centre the center
     * @see #getCentre
     */
    public void setCentre(Point2D centre) {
        this.centreX = (float) centre.getX();
        this.centreY = (float) centre.getY();
    }

    /**
     * Get the centre of the effect as a proportion of the image size.
     *
     * @return the center
     * @see #setCentre
     */
    public Point2D getCentre() {
        return new Point2D.Float(centreX, centreY);
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        icentreX = width * centreX;
        icentreY = height * centreY;
        this.width = width;
        this.height = height;
        if (a == 0)
            a = width / 2f;
        if (b == 0)
            b = height / 2f;
        a2 = a * a;
        b2 = b * b;
        return super.filter(src, dst);
    }

    protected void transformInverse(int x, int y, float[] out) {
        out[0] = x;

        float r = (float) Math.sin(x * 12 / width);
        out[1] = y + 10 * (float) Math.sin(y * 12 / height) * r;
    }

    public String toString() {
        return "Distort/Sphere...";
    }

}
