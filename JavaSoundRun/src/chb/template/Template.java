package chb.template;

/**
 * @author Hongbao Chen
 *
 */
public interface Template {
	
	/**
	 * Get parameters for emotion processing.
	 * @return double array containing 3 elements.
	 */
	public double[] GetEmotionX();
	
	/**
	 * Get parameters for narrow processing.
	 * @return double array containing 2 elements
	 */
	public double[] GetNarrowX();
	
	/**
	 * Get parameters for fusion processing.
	 * @return double number.
	 */
	public double GetFusionX();
	
	/**
	 * Get parameters for trim processing.
	 * @return double array containing two parameters.
	 */
	public double[] GetTrimBeforeX();
	public double[] GetTrimAfterX();
	
	/**
	 * Get parameters for interval processing.
	 * @return double number.
	 */
	public double GetIntervalX();
	
	/**
	 * Set parameters for emotion processing.
	 * @param x double array containing 3 elements.
	 */
	public void SetEmotionX(double[] x);
	
	/**
	 * Set parameters for narrow processing.
	 * @param x double array containing 2 elements.
	 */
	public void SetNarrowX(double[] x);
	
	/**
	 * Set parameters for fusion processing.
	 * @param x double number.
	 */
	public void SetFusionX(double x);
	
	/**
	 * Set parameters for trim processing.
	 * @param x double array containing 2 elements.
	 */
	public void SetTrimBeforeX(double[] x);
	public void SetTrimAfterX(double[] x);
	
	/**
	 * Set parameters for interval processing.
	 * @param x double number.
	 */
	public void SetIntervalX(double x);

}
