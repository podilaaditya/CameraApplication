/**
 * 
 */
package com.aptina.miscellaneous;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.optimization.fitting.CurveFitter;
import org.apache.commons.math3.optimization.fitting.WeightedObservedPoint;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;

import com.aptina.miscellaneous.Exceptions.NotParabolaException;

import android.graphics.PointF;
import android.util.Log;

/**
 * @author stoyan
 *
 */
public class GestureCurve {

	private static final String TAG = "GestureCurve";
	private static final boolean LOG_ON = true;
	
	public static final String SMILE_GESTURE = "smile";
	public static final String FROWN_GESTURE = "frown";
	public static final String UKNOWN_GESTURE = "unkown";
	/**
	 * The raw curve obtained from the finger MotionEvent
	 */
	private CurveFitter mRawCurve;
	
	/**
	 * The curve, whose vertex or center is translated to the origin
	 */
	private CurveFitter mOriginCurve;
	
	/**
	 * The second order polynomial that we calculate fits through least-squares 
	 */
	private UnivariateFunction mOrder2Function;
	
	/**
	 * The linear function that we calculate fits
	 */
	private UnivariateFunction mOrder1Function;
	
	/**
	 * Initial coefficients guess for the 2nd order function
	 */
	private double[] mInitCoefOrder2 = { 1, -1, 1 }; // 1 - 1 x + 1 x^2 
	
	/**
	 * Initial coefficients guess for the 2nd order function
	 */
	private double[] mInitCoefOrder1 = { 1, -1 }; // 1 - 1 x 

	/**
	 * A limit to the error from a straight line. 
	 */
	private static final double STRAIGHT_LINE_ERROR = 0.02;
	/**
	 * 
	 */
	public GestureCurve() {
		mRawCurve = new CurveFitter(new LevenbergMarquardtOptimizer()); 
		mOriginCurve = new CurveFitter(new LevenbergMarquardtOptimizer()); 
	}
	
	/**
	 * Add a point to the raw curve
	 * @param x The x value
	 * @param y The y value
	 */
	public void addCurvePoint(double x, double y){
		mRawCurve.addObservedPoint(x,y);
	}
	
	public String analyze(){
//		mOriginCurve = translateToOrigin(mRawCurve);
		mOrder1Function = getFirstOrder(mRawCurve);
		mInitCoefOrder1 = getOrder1Coeff(mRawCurve);
		mOrder2Function = getSecondOrder(mRawCurve);
		mInitCoefOrder2 = getOrder2Coeff(mRawCurve);
		
		if(LOG_ON){
			logFunction(mInitCoefOrder1);
			logFunction(mInitCoefOrder2);
		}
		
		double p2 = getRelativeErrorSum(mOrder2Function, mRawCurve);
		
		double p1 = getRelativeErrorSum(mOrder1Function, mRawCurve);
		
		if(p2*3 < p1 && p1 > STRAIGHT_LINE_ERROR){
			if(mInitCoefOrder2[2] > 0){
				return FROWN_GESTURE;
			}
			return SMILE_GESTURE;
		}
		
		return UKNOWN_GESTURE;
	}
	
	/**
	 * Make sure that the gesture corresponds to a proper function
	 * I.E f(x) = c , one y per x value
	 */
	private boolean isProperGesture(CurveFitter curve){
		return false;
	}
	private PointF getParabolaVertex(UnivariateFunction funct, double[] coef){
		PointF ans = new PointF();
		if(coef.length == 3){
			double ansX = coef[1]/(2*(-1)*coef[2]);
			double ansY = funct.value(ansX);
			Log.i(TAG, "Parabola Vertex : " + ansX + ", " + ansY);
		}else{
			try {
				throw new Exceptions.NotParabolaException();
			} catch (NotParabolaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ans;
	}
	private double getRelativeErrorSum(UnivariateFunction func, CurveFitter curve){
		double sum = 0;
		WeightedObservedPoint[] ps =  curve.getObservations();
		double y0;
		for(int i = 0; i < ps.length; i++){
			y0 = func.value(ps[i].getX());
			sum += Math.abs((y0 - ps[i].getY())/y0);
		}
		Log.e(TAG, "relative error sum : " + sum/ps.length);
		return sum/ps.length;
	}
	/**
	 * Function that fits the curve using a linear, first-order
	 * continuous differential function. 
	 * @param curve The curve to be analyzed
	 * @return
	 */
	public static UnivariateFunction getFirstOrder(CurveFitter curve) {
		final double[] init = { 1, 1};//1 + x 
		// Compute optimal coefficients.
    	final double[] best = curve.fit(new PolynomialFunction.Parametric(), init);
    	
		return getFunction(best);
	}
	
	/**
	 * Function that fits the curve using a polynomial, second-order
	 * continuous differential function. 
	 * @param curve The curve to be analyzed
	 * @return
	 */
	public static UnivariateFunction getSecondOrder(CurveFitter curve) {
		final double[] init = { 12.9, -3.4, 2.1};
		// Compute optimal coefficients.
    	final double[] best = curve.fit(new PolynomialFunction.Parametric(), init);
    	
		return getFunction(best);
	}
	
	public static double[] getOrder1Coeff(CurveFitter curve){
		double[] init = { 1, 1};//1 + x 
		// Compute optimal coefficients.
    	final double[] best = curve.fit(new PolynomialFunction.Parametric(), init);
    	return best;
	}
	public static double[] getOrder2Coeff(CurveFitter curve){
		double[] init = { 12.9, -3.4, 2.1};
		// Compute optimal coefficients.
    	final double[] best = curve.fit(new PolynomialFunction.Parametric(), init);
    	return best;
	}
	
	public static UnivariateFunction getFunction(final double[] coef) {
	    return new UnivariateFunction() {
	        		public double value(double x) {
	        			double val = coef[0];//handle the constant
	        			for(int i = 1; i < coef.length; i++){
	        				val += coef[i]*Math.pow(x, i);
	        			}
	        			return val;
	        		}
	    };
	}

	/**
	 * Translates the raw curve so that its center is at the origin
	 * @param rawCurve The curve to translate to the origin
	 */
	public static CurveFitter translateToOrigin(CurveFitter rawCurve){
		double x = 0;
		double y = 0;
		CurveFitter transCurve = new CurveFitter(new LevenbergMarquardtOptimizer()); 
		WeightedObservedPoint[] rawObserv = rawCurve.getObservations();
		

		
		WeightedObservedPoint centerP = rawObserv[rawObserv.length/2];
		for(int i = 0; i < rawObserv.length; i++){
			x = rawObserv[i].getX() - centerP.getX();
			y = rawObserv[i].getY() - centerP.getY();
			transCurve.addObservedPoint(x, y);
		}

		return transCurve;
	}
	
	
	/**
	 * Log the functions
	 * @param coef The coefficients to be logged
	 * @return
	 */
	public static void logFunction(double[] coef){
		StringBuilder sb = new StringBuilder();
		sb.append("Order (" + (coef.length - 1) + ") function : " + coef[0] + " ");

		for(int i = 1; i < coef.length; i++){
			sb.append(coef[0] + "x^" + i + " ");
		}
		Log.i(TAG, sb.toString());
	}
	
	
	public CurveFitter getRawCurve(){
		return mRawCurve;
	}
	
	public CurveFitter getOriginCurve(){
		return mOriginCurve;
	}
	
	public UnivariateFunction getOrder1Function(){
		return mOrder1Function;
	}
	public UnivariateFunction getOrder2Function(){
		return mOrder2Function;
	}

}
