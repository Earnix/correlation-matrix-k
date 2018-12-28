package com.earnix.eo.gui.correlation;

import java.awt.Color;

/**
 * Utilities, used by correlation matrix component.
 */
class Utilities
{
	/**
	 * Interpolates (mixes) given colors by applying proportional addition of color components.
	 *
	 * @param color1 the first color to mix
	 * @param color2 the second color to mix
	 * @param interpolation the proportion of first color in resulting one
	 * @return the resulting mixed color
	 */
	static Color interpolateColor(Color color1, Color color2, double interpolation)
	{
		int red = (int) (color1.getRed() * interpolation + color2.getRed() * (1 - interpolation));
		int green = (int) (color1.getGreen() * interpolation + color2.getGreen() * (1 - interpolation));
		int blue = (int) (color1.getBlue() * interpolation + color2.getBlue() * (1 - interpolation));
		return new Color(red, green, blue);
	}

	/**
	 * Returns equal or next larger {@code int} value for given {@code double}.
	 *
	 * @param value given value
	 * @return equal or next larger {@code int}
	 */
	static int ceil(double value)
	{
		return (int) Math.ceil(value);
	}

	/**
	 * Creates string for double data value, keeping 4 decimal places.
	 * In case of {@link Double#NaN}, returns "N/A".
	 *
	 * @param value value to format
	 * @param decimalPlaces number of decimal places in resulting formatted number
	 * @return formatted value as {@link String}
	 */
	static String formatCorrelationValue(double value, short decimalPlaces)
	{
		return Double.isNaN(value) ? "N/A" : String.format("%." + decimalPlaces + "f", value);
	}

	/**
	 * Abbreviates given label by removing all symbols after given length.
	 * The last 3 symbols which last will be replaced with dots for presentational purpose.
	 * <br>
	 * The same label will be returned if it's length is less or equal given one.
	 *
	 * @param label the label to abbreviate if needed
	 * @param length target maximum length
	 * @return the same label or label with length equal to the given length and 3 dots at the end.
	 */
	static String abbreviate(String label, short length)
	{
		return label.length() > length ? label.substring(0, length - 3) + "..." : label;
	}
}
