package facerecognition.javafaces;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;


public class Matrix2D extends DenseDoubleMatrix2D {
	public Matrix2D(double[][] data){
		super(data);
	}
	public Matrix2D(DoubleMatrix2D dmat){
		super(dmat.toArray());
	}
	public Matrix2D(int rows,int cols){
		super(rows,cols);
	}
	public Matrix2D(double[] data,int rows){
		super(rows,(rows != 0 ? data.length/rows : 0));
		int columns=(rows != 0 ? data.length/rows : 0);
		if (rows*columns != data.length) {
	         throw new IllegalArgumentException("Array length must be a multiple of "+rows);
	    }
		double[][]vals = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
		    for (int j = 0; j < columns; j++) {
		      vals[i][j] = data[i+j*rows];
		    }
		}
		super.assign(vals);		
	}
	public Matrix2D getSubMatrix(int rows){
		Matrix2D mat=new Matrix2D( viewPart(0,0,rows,super.columns()).copy());
		return mat;
	}
	public static void fitToUnitLength(double[] data) {
		double arrayMax = max(data);
		for (int i = 0; i < data.length; i++) {
			data[i] /= arrayMax;
		}		
	}
	public void adjustToZeroMean(){
		double[] averageValues=getAverageOfEachColumn();
		subtractFromEachRow(averageValues);
	}
	public double[] getAverageOfEachColumn(){
		int cols=this.columns();
		double[][] data=this.toArray();
		double t=0.0;
		double[] avgValues = new double[cols];
		for(int i=0;i<cols;i++){
			t=0.0;
			for(int j=0;j<rows;j++){
				t +=data[j][i];
			}
			avgValues[i]=t/rows;
		}
		return avgValues;
	}
	
	public void replaceRowsWithArray(double[] data){
		if(this.columns!=data.length)throw new RuntimeException("matrix columns not matching number of input array elements");
		
		for(int row=0;row<this.rows;row++ ){
			for(int col=0;col<this.columns;col++){
				this.set(row,col, data[col]);
			}
		}
		
	}
	public void normalise(){
		double[][] temp=this.toArray();
		double[] mvals=new double[temp.length];
		for(int i=0;i<temp.length;i++){
			mvals[i]=max(temp[i]);
		}	
		for(int i=0;i<temp.length;i++){
			for(int j=0;j<temp[0].length;j++){
				temp[i][j]/=mvals[i];
			}
		}
		this.assign(temp);
	}
	private static double max(double[] arr){
		double m=Double.MIN_VALUE;
		for(int i=0;i<arr.length;i++){
			m=Math.max(m,arr[i]);			
		}		
		return m;
	}
	public void subtract(Matrix2D mat){
		//cern.jet.math.Functions F = cern.jet.math.Functions.functions;
		this.assign(mat,cern.jet.math.Functions.minus);
	}
	public void add(Matrix2D mat){
		//cern.jet.math.Functions F = cern.jet.math.Functions.functions;
		this.assign(mat,cern.jet.math.Functions.plus);
	}
	public void subtractFromEachRow(double[] oneDArray){
		double[][] denseArray=this.toArray();
		for(int i=0;i<denseArray.length;i++){
			for(int j=0;j<denseArray[0].length;j++){
				denseArray[i][j]-=oneDArray[j];
			}
		}
		this.assign(denseArray);
	}
	public Matrix2D multiply(Matrix2D mat){
		return new Matrix2D(this.zMult(mat, null));		
	}	
	public void multiplyElementWise(Matrix2D mat){
		//cern.jet.math.Functions F = cern.jet.math.Functions.functions;
		this.assign(mat, cern.jet.math.Functions.mult);
	}
	public Matrix2D transpose(){
		return new Matrix2D(this.viewDice());
		
	}
	public double[] flatten(){
	    int nr= this.rows;
	    int nc= this.columns;
	    double[] res= new double[nr*nc];
	    int i= 0;

	    for(int r= 0; r< nr; r++){
	      for(int c= 0; c< nc; c++){
	        res[i]= get(r, c);
	        i++;
	      }
	    }
	    return res;
	  }
	
	public static double norm(double[] oneDArray){
		double val=0.0;
		for(int i=0;i<oneDArray.length;i++){
			val+=(oneDArray[i]*oneDArray[i]);
		}
		return val;
	}
	
	public static void subtract(double[] inputFace, double[] avgFace) {
		for (int i = 0; i < inputFace.length; i++) {
			inputFace[i] -= avgFace[i];
		}
	}
	
	public EigenvalueDecomposition getEigenvalueDecomposition(){
		return new EigenvalueDecomposition(this);
	}
	
	/***
	 * Instance of Matrix2D class cannot be put as element in HashList or as a key in HashMap
	 * In the colt library,cern.colt.matrix.DoubleMatrix2D defines equals and uses Object.hashCode()
	 * This is not a good idea since Object.hashCode() returns JVM assigned integer.
	 */
	
	public int hashCode(){
		throw new UnsupportedOperationException("cannot use this class instance as hash key");
	}
	
	
	
}
