package com.disc.teslademo;

/**
  * Created by davieshin on 2/10/15.
  */

import android.util.Log;

 import com.google.android.gms.maps.model.LatLng;

 import org.ejml.simple.SimpleMatrix;

 import java.util.ArrayList;


public class TrajectoryPlotter {

     //debugger
     private static final String TAG = "myApp";

     public int size, i;
     public double a1, a2, a3, a4, a5, a6, y1, y2, y3, y4, y_squared, min_x, max_x, avg_distance, cuts, hold, temp, n, avg_x, avg_y, avg_temp, mhold;
     public ArrayList<LatLng> plots = new ArrayList<LatLng>();
     public double[] GPSData;

     public void initData(double[] BluetoothData) {
         size = BluetoothData.length;
         GPSData = new double[size];
         avg_x = 0;
         avg_y = 0;
         for (i = 0; i < size - 1; i = i + 2) {
             avg_x = avg_x + BluetoothData[i];
             avg_y = avg_y + BluetoothData[i + 1];
         }
         avg_temp = size / 2;
         avg_x = avg_x / avg_temp;
         avg_y = avg_y / avg_temp;
         for (i = 0; i < size - 1; i = i + 2) {
             //GPSData[i] = Double.parseDouble(BluetoothData[i]);
             GPSData[i] = BluetoothData[i] - avg_x;
             GPSData[i + 1] = BluetoothData[i + 1] - avg_y;
         }
         n=size/2;
         a1 = 0;
         a2 = 0;
         a3 = 0;
         a4 = 0;
         a5 = 0;
         a6 = 0;
         y1 = 0;
         y2 = 0;
         y3 = 0;
         y4 = 0;
         y_squared = 0;
         min_x = 99999;
         max_x = -99999;
         avg_distance = 0;
         temp = 0;
     }


     public ArrayList<LatLng> mapTrajectory() {

         for(i=0; i<size-1; i=i+2) {
             a1=a1+GPSData[i];
             a2=a2+(Math.pow(GPSData[i],2));
             a3=a3+(Math.pow(GPSData[i],3));
             a4=a4+(Math.pow(GPSData[i],4));
             a5=a5+(Math.pow(GPSData[i],5));
             a6=a6+(Math.pow(GPSData[i],6));
             y1=y1+GPSData[i+1];
             y2=y2+GPSData[i]*GPSData[i+1];
             y3=y3+Math.pow(GPSData[i],2)*GPSData[i+1];
             y4=y4+Math.pow(GPSData[i],3)*GPSData[i+1];
             y_squared=y_squared+Math.pow(GPSData[i+1],2);
             if (GPSData[i] < min_x)
                 min_x=GPSData[i];
             if (GPSData[i] > max_x)
                 max_x=GPSData[i];
         }
         /*
         Log.d(TAG, String.valueOf(n));
         Log.d(TAG, String.valueOf(a1));
         Log.d(TAG, String.valueOf(a2));
         Log.d(TAG, String.valueOf(a3));
         Log.d(TAG, String.valueOf(a4));
         Log.d(TAG, String.valueOf(a5));
         Log.d(TAG, String.valueOf(a6));
         Log.d(TAG, String.valueOf(y1));
         Log.d(TAG, String.valueOf(y2));
         Log.d(TAG, String.valueOf(y3));
         Log.d(TAG, String.valueOf(y4));
         Log.d(TAG, String.valueOf(min_x));
         Log.d(TAG, String.valueOf(max_x));
         */

         /*
         float nf=(float)n;
         float a1f=(float)a1;
         float a2f=(float)a2;
         float a3f=(float)a3;
         float a4f=(float)a4;
         float a5f=(float)a5;
         float a6f=(float)a6;
         float y1f=(float)y1;
         float y2f=(float)y2;
         float y3f=(float)y3;
         float y4f=(float)y4;


         float[] A_cubic={nf,a1f,a2f,a3f,a1f,a2f,a3f,a4f,a2f,a3f,a4f,a5f,a3f,a4f,a5f,a6f};
         float[] Y_cubic={y1f,y2f,y3f,y4f};
         float[] Q_cubic = new float[4];
         float[] mInverseA_cubic = new float[16];
         Matrix.invertM(mInverseA_cubic,0,A_cubic,0);
         Matrix.multiplyMV(Q_cubic,0,mInverseA_cubic,0,Y_cubic,0);
         double c1=(double)Q_cubic[0];
         double c2=(double)Q_cubic[1];
         double c3=(double)Q_cubic[2];
         double c4=(double)Q_cubic[3];

         float[] testing={1f,6f,-5f,4f,76f,2f,-1f,86f,64f,2f,-45f,-100f,34f,-5f,.02f,.76f};
         float[] testing1 = new float[16];
         Matrix.invertM(testing1,0,testing,0);


         Log.d(TAG,"~~~~~~~~~~~~~~INVERSE~~~~~~~~~~~~~~~");
         Log.d(TAG, Float.toString(mInverseA_cubic[0]));
         Log.d(TAG, Float.toString(mInverseA_cubic[1]));
         Log.d(TAG, Float.toString(mInverseA_cubic[2]));
         Log.d(TAG, Float.toString(mInverseA_cubic[3]));
         Log.d(TAG, Float.toString(mInverseA_cubic[4]));
         Log.d(TAG, Float.toString(mInverseA_cubic[5]));
         Log.d(TAG, Float.toString(mInverseA_cubic[6]));
         Log.d(TAG, Float.toString(mInverseA_cubic[7]));
         Log.d(TAG, Float.toString(mInverseA_cubic[8]));
         Log.d(TAG, Float.toString(mInverseA_cubic[9]));
         Log.d(TAG, Float.toString(mInverseA_cubic[10]));
         Log.d(TAG, Float.toString(mInverseA_cubic[11]));
         Log.d(TAG, Float.toString(mInverseA_cubic[12]));
         Log.d(TAG, Float.toString(mInverseA_cubic[13]));
         Log.d(TAG, Float.toString(mInverseA_cubic[14]));
         Log.d(TAG, Float.toString(mInverseA_cubic[15]));
         */
         double nn = (double) n;
         //linear
         SimpleMatrix a_linear = new SimpleMatrix(2,2,true,nn,a1,a1,a2);
         SimpleMatrix b_linear = new SimpleMatrix(2,1,true,y1,y2);
         SimpleMatrix c_linear = a_linear.solve(b_linear);


         //cubic
         SimpleMatrix a = new SimpleMatrix(4,4,true,nn,a1,a2,a3,a1,a2,a3,a4,a2,a3,a4,a5,a3,a4,a5,a6);
         SimpleMatrix b = new SimpleMatrix(4,1,true,y1,y2,y3,y4);
         SimpleMatrix c = a.solve(b);

         //r^2
         double R_square=((n*y2)-(a1*y1))/(Math.sqrt((nn * a2) - Math.pow(a1, 2))*Math.sqrt((n * y_squared) - Math.pow(y1, 2)));

         Log.d(TAG, "%%%%%%%%%%~~~~~RSQUARED~~~~~~%%%%%%%%%%");
         Log.d(TAG, String.valueOf(R_square));

         Log.d(TAG, "%%%%%%%%%%~~~~~AVALUE~~~~~~%%%%%%%%%%");
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[0]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[1]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[2]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[3]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[4]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[5]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[6]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[7]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[8]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[9]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[10]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[11]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[12]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[13]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[14]));
         Log.d(TAG, String.valueOf(a.getMatrix().getData()[15]));
         Log.d(TAG, "%%%%%%%%%%~~~~~YVALUE~~~~~~%%%%%%%%%%");
         Log.d(TAG, String.valueOf(b.getMatrix().getData()[0]));
         Log.d(TAG, String.valueOf(b.getMatrix().getData()[1]));
         Log.d(TAG, String.valueOf(b.getMatrix().getData()[2]));
         Log.d(TAG, String.valueOf(b.getMatrix().getData()[3]));
         Log.d(TAG, "%%%%%%%%%%~~~~~INVERSEVALUE~~~~~~%%%%%%%%%%");
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[0]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[1]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[2]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[3]));
         /*
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[4]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[5]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[6]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[7]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[8]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[9]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[10]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[11]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[12]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[13]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[14]));
         Log.d(TAG, String.valueOf(c.getMatrix().getData()[15]));
         */

         /*
         Log.d(TAG, Float.toString(a1f));
         Log.d(TAG, Float.toString(a2f));
         Log.d(TAG, Float.toString(a3f));
         Log.d(TAG, Float.toString(a4f));
         Log.d(TAG, Float.toString(a5f));
         Log.d(TAG, Float.toString(a6f));
         Log.d(TAG, Float.toString(y1f));
         Log.d(TAG, Float.toString(y2f));
         Log.d(TAG, Float.toString(y3f));
         Log.d(TAG, Float.toString(y4f));
         */

         /*
         // first order
         DoubleMatrix A_linear = new DoubleMatrix(2,2, n, a1, a1, a2);
         DoubleMatrix Y_linear = new DoubleMatrix(1,2, y1, y2);
         // third order
         DoubleMatrix A_cubic = new DoubleMatrix(4,4, n, a1, a2, a3, a1, a2, a3, a4, a2, a3, a4, a5, a3, a4, a5, a6);
         DoubleMatrix Y_cubic = new DoubleMatrix(1,4, y1,y2,y3,y4);

         //DoubleMatrix Q_linear = Solve.solve(A_linear, Y_linear);
         //DoubleMatrix Q_cubic= Solve.solve(A_cubic, Y_cubic);

         double l1=Q_linear.get(1,1);
         double l2=Q_linear.get(2, 1);
         double c1=Q_cubic.get(1,1);
         double c2=Q_cubic.get(2,1);
         double c3=Q_cubic.get(3,1);
         double c4=Q_cubic.get(4,1);
         */

         //plots.add(new LatLng(GPSData[0],GPSData[1]));
         //plots.add(new LatLng(GPSData[size-2],GPSData[size-1]));

         cuts=max_x-min_x;
         cuts=cuts/30;
         hold=min_x;
         // cubic if R < 0.99, else linear
 //        if (R_square < 0.39) {
         plots.add(new LatLng(hold + avg_x, avg_y + (c.getMatrix().getData()[0] + c.getMatrix().getData()[1] * hold + c.getMatrix().getData()[2] * Math.pow(hold, 2) + c.getMatrix().getData()[3] * Math.pow(hold, 3))));

         for (i = 1; i < 31; i++) {
             hold = hold + cuts;
             plots.add(new LatLng(hold + avg_x, avg_y + (c.getMatrix().getData()[0] + c.getMatrix().getData()[1] * hold + c.getMatrix().getData()[2] * Math.pow(hold, 2) + c.getMatrix().getData()[3] * Math.pow(hold, 3))));
         }
 //        } else {
 //            plots.add(new LatLng(hold, c_linear.getMatrix().getData()[0] + c_linear.getMatrix().getData()[1] * hold));
 //
 //            for (i = 1; i < 31; i++) {
 //                hold = hold + cuts;
 //                plots.add(new LatLng(hold, c_linear.getMatrix().getData()[0] + c_linear.getMatrix().getData()[1] * hold));
 //            }
 //        }


         return plots;
     }


     public double[] filter(double[] unfilteredData) {
         int size1=unfilteredData.length;
         avg_distance=0;
         int track=0;
         int count = 0;

         // Threshold
         double K = 2.5;

         for (i=0; i<size1-3; i=i+2) {
             avg_distance=avg_distance+Math.sqrt(Math.pow(unfilteredData[i+2]-unfilteredData[i],2)+Math.pow(unfilteredData[i+3]-unfilteredData[i+1],2));
         }

         avg_distance=avg_distance/(double)size1;

         for (i=0; i<size1-3; i=i+2) {
             temp = Math.sqrt(Math.pow(unfilteredData[i + 2] - unfilteredData[i], 2) + Math.pow(unfilteredData[i + 3] - unfilteredData[i + 1], 2));
             if (temp > avg_distance * K) {
                 for (int z = i; z < size1 - 5; z = z + 2) {
                     unfilteredData[z + 2] = unfilteredData[z + 4];
                     unfilteredData[z + 3] = unfilteredData[z + 5];
                 }
                 track = track + 1;
                 count = count + 1;
                 K = K * count;
             } else {
                 K = 2.5;
                 count = 1;
             }
         }
         double[] filteredData = new double[size1-track*2];
         for (i=0;i<size1-track*2;i++){
             filteredData[i]=unfilteredData[i];
         }

         return filteredData;
     }

 }