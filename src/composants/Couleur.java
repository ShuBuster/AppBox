package composants;

import android.graphics.Color;

public class Couleur {

	/**
	 * Assombrit une couleur
	 * 
	 * @param color
	 *            la couleur a assombrir
	 * @return la couleur assombrie
	 */
	public static int darken(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.85f;
		color = Color.HSVToColor(hsv);
		return color;
	}

	/**
	 * Eclaircit une couleur
	 * 
	 * @param color
	 *            la couleur a eclaircir
	 * @return la couleur eclaircie
	 */
	public static int lighten(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 1.2f;
		hsv[1] *= 0.85f;
		color = Color.HSVToColor(hsv);
		return color;
	}

}
