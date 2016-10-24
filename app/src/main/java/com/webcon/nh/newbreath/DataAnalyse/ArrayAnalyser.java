package com.webcon.nh.newbreath.DataAnalyse;

import android.content.Intent;
import android.util.Log;

import com.webcon.nh.newbreath.utils.FreeApp;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ArrayAnalyser
 * Created by Administrator on 16-1-14.
 */
public class ArrayAnalyser {

    /**
     * for identifying each breath cycle,findLocalMins() {@link com.webcon.nh.newbreath.service.HandlerService} will fail due to noises.
     * this function can filter out noises by checking the particular breath-cycle-condition(wall height, wall size, minimum interval) to be met.
     *
     * @param intArray input data array 300
     * @return min data breath
     */

    public static int[] findLocalMins_refined(int[] intArray, BreathCycleCondition breathCycleCondition) {
        int[] simpleLocalMins = findLocalMins(intArray);
        int simpleLocalMins_length = simpleLocalMins.length;
        List<Integer> refinedMinList = new ArrayList<>();  // list to hold refined indexes

        for (int ii = 0; ii < simpleLocalMins_length - 1; ii++) {
            boolean condA = (simpleLocalMins[ii] - breathCycleCondition.wallWidth) < 0;
            boolean condB = (simpleLocalMins[ii] + breathCycleCondition.wallWidth) > intArray.length - 1;

            boolean frontWallCondition;
            if (condA)
                frontWallCondition = false;
            else {
                int[] tempArray = new int[breathCycleCondition.wallWidth + 1];
                System.arraycopy(intArray, simpleLocalMins[ii] - breathCycleCondition.wallWidth, tempArray, 0, breathCycleCondition.wallWidth + 1);
                frontWallCondition = (findMax_inIntArray(tempArray) - intArray[simpleLocalMins[ii]]) > breathCycleCondition.wallHeight;
            }
            boolean endWallCondition;
            if (condB)
                endWallCondition = false;
            else {
                int[] tempArray2 = new int[breathCycleCondition.wallWidth + 1];
                System.arraycopy(intArray, simpleLocalMins[ii], tempArray2, 0, breathCycleCondition.wallWidth + 1);
                endWallCondition = (findMax_inIntArray(tempArray2) - intArray[simpleLocalMins[ii]]) > breathCycleCondition.wallHeight;
            }

            // if the breathCycleCondition height is satisfied in both directions
            if (frontWallCondition && endWallCondition)
                refinedMinList.add(simpleLocalMins[ii]);
        }

//        -------satisfy the minimum time interval-------
        if (refinedMinList.size() > 1)
            refinedMinList = refinedWithInterval2(refinedMinList, breathCycleCondition.minimumInterval);


        // convert to array
        return convert2intArray(refinedMinList);
    }

    // this should be used instead of refinedWithInterval
    private static List<Integer> refinedWithInterval2(List<Integer> refinedMinList, int minimumInterval) {
        List<Integer> refinedMinList2 = new ArrayList<>();  // refine the indexes to make sure the minimum-time-interval condition is met
        for (int tempInt = 0; tempInt < refinedMinList.size() - 1; tempInt++) {
            boolean cond1 = (refinedMinList.get(tempInt + 1) - refinedMinList.get(tempInt)) > minimumInterval;
            if (cond1) {
                refinedMinList2.add(refinedMinList.get(tempInt));
            }
        }
        return refinedMinList2;
    }

    /**
     * find simple local minimum of an array ---> new array
     *
     * @param intArray src Data Array
     * @return all min data
     */
    private static int[] findLocalMins(int[] intArray) {
        int[] differentiated = diff_array(intArray);
        return findCrossZeros_array_indexShift(differentiated);
    }

    /**
     * find the backward differential of an integer array 获取后向差分
     *
     * @param intArray:array to be differentiated, the size of the array has to be larger than 1 ，需要进行后向差分的数组，长度大于1
     * @return int[] of length-1, differentiated array  后向差分后的数组，长度为原来数组-1
     */
    private static int[] diff_array(int[] intArray) {
        int arrayLength = intArray.length;
        int[] array_temp1 = new int[arrayLength - 1]; // subtractee --减去array
        int[] array_temp2 = new int[arrayLength - 1]; // subtracter --被减array
        int[] array_diff = new int[arrayLength - 1]; //  differnce array, to be returned
        System.arraycopy(intArray, 0, array_temp1, 0, arrayLength - 1);
        System.arraycopy(intArray, 1, array_temp2, 0, arrayLength - 1);
        for (int ii = 0; ii < arrayLength - 1; ii++) {    // find the difference
            array_diff[ii] = array_temp2[ii] - array_temp1[ii];
        }
        return array_diff;
    }


    /**
     * find cross-zero indexes in an integer array, cross zero index is  defined for (either one of the) below conditions :
     * condition 0: int[ii]<0 and int[ii+1]>0
     * condition 1: int[ii]<0 and int[ii+1]==0 and int[ii+2]>0
     * condition 2: int[ii]<0 and int[ii+1]==0 and int[ii+2]==0 and int[ii+3]>0,
     *
     * @param intArray:array to be differentiated,
     * @return ArrayList of indexes
     */
    private static List<Integer> findCrossZeros(int[] intArray) {
        List<Integer> mlist = new ArrayList<>();
        int arrayLength = intArray.length;

        for (int ii = 0; ii < arrayLength - 3; ii++) {
            boolean condA = intArray[ii] < 0;
            boolean condB = intArray[ii + 1] > 0;
            boolean condC = (intArray[ii + 1] == 0) && ((intArray[ii + 2] > 0));
            boolean condD = (intArray[ii + 1] == 0) && (intArray[ii + 2] == 0) && ((intArray[ii + 3] > 0));
            if (condA && (condB || condC || condD)) mlist.add(ii);
        }
        return mlist;
    }

    /**
     * save function as  findCrossZeros  different return type: return an array instead of arrayList
     *
     * @param intArray:array to be differentiated,
     * @return an array instead of arrayList
     */

    // to shift the index (index+1) --> for finding local minimums
    private static int[] findCrossZeros_array_indexShift(int[] intArray) {
        List<Integer> mlist = findCrossZeros(intArray);
        int[] ret_array = new int[mlist.size()];
        Iterator<Integer> mlistIterator = mlist.iterator();
        int ii = 0;
        while (mlistIterator.hasNext()) {
            ret_array[ii] = mlistIterator.next() + 1;
            ii++;
        }
        return ret_array;
    }


    // find the max int in an int array
    public static int findMax_inIntArray(int[] ints) {
        int max = ints[0];
        for (int anInt : ints) {
            if (anInt > max) max = anInt;
        }
        return max;
    }

    // find the min int value in an int array
    public static int findMin_inIntArray(int[] ints) {
        int min = ints[0];
        for (int anInt : ints) {
            if (anInt < min) min = anInt;
        }
        return min;
    }

    // find the index corresponding to min int value in an int array
    private static int findMinIndex_inIntArray(int[] ints) {
        int min = ints[0];
        int minIndex = 0;
        for (int ii = 0; ii < ints.length; ii++) {
            if (ints[ii] < min) {
                min = ints[ii];
                minIndex = ii;
            }
        }
        return minIndex;
    }

    private static int sortIndexMin2AVE(int[] ints, int index) {
        int[] b = findLocalMins(ints);
        List<Integer> bb = new ArrayList<>();
        for (int a : b) {
            bb.add(ints[a]);
        }
        int[] a = convert2intArray(bb);
        Arrays.sort(a);
        return aveIntArray(ints) - a[index];
    }

    // find the total of a double array
    private static double findSum(double[] doubles, int startBin, int endBin) {
        double total = 0;
        for (int aa = startBin; aa < endBin + 1; aa++) total = total + doubles[aa];
        return total;
    }


    // convert an Integer arraylist to an int array
    private static int[] convert2intArray(List<Integer> list) {
        int[] convertedArray = new int[list.size()];
        Iterator<Integer> iterator = list.iterator();
        int tempIndex = 0;
        while (iterator.hasNext()) {
            convertedArray[tempIndex] = iterator.next();
            tempIndex++;
        }
        return convertedArray;
    }

    /**
     * find the flux for every breath cycle
     *
     * @param breathSignal    : breath signal wave form , normally int array of length 300--> 1minute data
     * @param cycleStartIndex : starting index of each breath cycle ,
     * @return int[] BreathFlux
     */
    public static int[] findBreathFlux(int[] breathSignal, int[] cycleStartIndex) {
        if (cycleStartIndex.length < 2) {
            return new int[]{0};
        } else {
            int return_length = cycleStartIndex.length - 1;
            int[] flux_ret = new int[return_length];
            for (int aa = 0; aa < return_length; aa++) {
                // find the peak value within a breath cycle
                int maxStartCopyIndex = cycleStartIndex[aa];
                // find the peak within 8sec(corresponding to 40) or simply use: int endCopyIndex=cycleStartIndex[aa+1]
                int maxEndCopyIndex = (((cycleStartIndex[aa + 1] - maxStartCopyIndex) > 40) ? (maxStartCopyIndex + 40) : cycleStartIndex[aa + 1]);
                // int endCopyIndex=cycleStartIndex[aa+1];
                int[] temparrayMax = getSubArray(breathSignal, maxStartCopyIndex, maxEndCopyIndex);
                int peakValue = findMax_inIntArray(temparrayMax);

                // find the minimum value within a breath cycle---> minimum around (+- 1.6sec ) the cycleStartIndex
                int minStartCopyIndex = ((cycleStartIndex[aa] - 8 > 0) ? (cycleStartIndex[aa] - 8) : 0);
                int minEndCopyIndex = cycleStartIndex[aa] + 8;  // will array go out of bounds??
                int[] temparrayMin = getSubArray(breathSignal, minStartCopyIndex, minEndCopyIndex);
                int troughValue = findMin_inIntArray(temparrayMin);

//----------------// check if minimums differ from the cycleStartIndexes ??:---------------------
//                int troughIndex=findMinIndex_inIntArray(temparrayMin)+minStartCopyIndex;
//                if ((troughIndex-cycleStartIndex[aa])!=0) Log.i("AAA","troughIndex:"+troughIndex+"---cycle start"+cycleStartIndex[aa]);
//----------------//----------------------------------------------------------------------------
                // find the difference --> flux
                flux_ret[aa] = peakValue - troughValue;
            }
//            dumpArray(flux_ret);  // print the breath flux values
            return flux_ret;
        }
    }


    //-------subArray function1
    // return a sub-array specified by starting and ending indexes....
    private static int[] getSubArray(int[] originalArray, int startIndex, int endEndex) {
        int arrayCopyLength = endEndex - startIndex + 1;
        int[] temparray = new int[arrayCopyLength];
        System.arraycopy(originalArray, startIndex, temparray, 0, arrayCopyLength);
        return temparray;
    }


    // cast an int array to a double array
    private static double[] intArray2doubleArray(int[] ints) {
        double[] retDouble = new double[ints.length];
        for (int aa = 0; aa < ints.length; aa++) retDouble[aa] = (double) ints[aa];
        return retDouble;
    }


    /**
     * returns the percentage of particular frequency range(excluding DC component in the first place)
     * --> calculated by FFT
     *
     * @param ints         : the signal under study, its size should be powers of 2
     * @param startFreqBin : start frequency(bin) in frequency domain,should be larger than 1(since DC component is excluded)
     * @param endFreqBin   : end frequency(bin) in frequency domain
     * @return double findFreqComponent
     */
    private static double findFreqComponent(int[] ints, int startFreqBin, int endFreqBin) {
        double[] input = intArray2doubleArray(ints);
        DftNormalization norm1 = DftNormalization.STANDARD;
        FastFourierTransformer transformer = new FastFourierTransformer(norm1);

        double[] tempConversion = new double[input.length];

        // perform FFT
        Complex[] complx = transformer.transform(input, TransformType.FORWARD);  // forward transform, as apposed to inverse transform
        for (int i = 0; i < complx.length; i++) {  // convert to absolute values of a complex number
            double rr = (complx[i].getReal());
            double ri = (complx[i].getImaginary());
            tempConversion[i] = Math.sqrt((rr * rr) + (ri * ri));
        }

        // **** note: the DC component is excluded
        // **** note: the second half of the frequency domain is excluded
        return findSum(tempConversion, startFreqBin, endFreqBin) / findSum(tempConversion, 1, 128);
    }


    // computes the breath component from a detector  --> sampling frequency 5Hz, length > 256  (normally 300, 1minute data)
    // 8  --> 5Hz/256*8*60sec ~=9.3 呼吸/min
    // 23 --> 5Hz/256*23*60sec ~=26 呼吸/min
    private static double findBreathComponent(int[] ints) {
        int[] int256fft = new int[256];
        System.arraycopy(ints, 0, int256fft, 0, 256);
        return findFreqComponent(int256fft, 8, 23);
    }

    // computes the noise component(1-1.5Hz) from a detector  --> sampling frequency 5Hz, length > 256  (normally 300, 1minute data)
    // 50 --> 5Hz/256*50 ~= 1Hz
    // 75 --> 5Hz/256*75 ~= 1.5Hz
    private static double findNoiseComponent(int[] ints) {
        int[] int256fft = new int[256];
        System.arraycopy(ints, 0, int256fft, 0, 256);
        return findFreqComponent(int256fft, 50, 75);
    }

    public static boolean isDeepSleep(int[] ints, double threshold) {
        FreeApp.getInstance().setPercentageB(findBreathComponent(ints));
        FreeApp.getInstance().setPercentageN(findNoiseComponent(ints));
        return (findBreathComponent(ints) / findNoiseComponent(ints)) > threshold;
    }

    public static boolean isDeepSleep2(int[] ints, int threshold) {
        if (!isDeepSleep(ints, 1.8))
            return false;
        else if (ints[290] == 0)
            return false;
        else if (sortIndexMin2AVE(ints, 8) < sortIndexMin2AVE(ints, 7) / threshold)
            return false;
        else if (sortIndexMin2AVE(ints, 7) < sortIndexMin2AVE(ints, 6) / threshold)
            return false;
        else if (sortIndexMin2AVE(ints, 6) < sortIndexMin2AVE(ints, 5) / threshold)
            return false;
        else if (sortIndexMin2AVE(ints, 5) < sortIndexMin2AVE(ints, 4) / threshold)
            return false;
        else if (sortIndexMin2AVE(ints, 4) < sortIndexMin2AVE(ints, 3) / threshold)
            return false;
        else if (sortIndexMin2AVE(ints, 3) < sortIndexMin2AVE(ints, 2) / threshold)
            return false;
        else if (sortIndexMin2AVE(ints, 2) > 5000)
            return false;
        else
            return true;
    }

    public static boolean isNone(int[] ints, double cond) {
        return findBreathComponent(ints) < cond;
    }

    /**
     * converts a ascII byte to a integer 48-->0, 49-->1, ... 57-->9
     * 13-->(CR carriage return:-1), 10-->(LF,new line: -2)
     * <p>
     * other values -999
     *
     * @param asc array src
     * @return int ret
     */
    private static int byteAsc2int(byte asc) {
        int ret = -999;
        if ((asc <= 57) && (asc >= 48)) {
            ret = asc - (byte) 48;
        } else if (asc == 13) {
            ret = -1;
        } else if (asc == 10) {
            ret = -2;
        }
        return ret;
    }


    /**
     * convert an ArrayList of type T to an array of type T
     *
     * @param list intList
     * @return int Array
     */
    private static int[] list2Array(List<Integer> list) {
        int listSize = list.size();
        int[] array = new int[listSize];
        for (int ii = 0; ii < listSize; ii++) {
            array[ii] = list.get(ii);
        }
        return array;
    }


    // compute the average of an int array
    public static int aveIntArray(int[] array) {
        int length = array.length;
        if (length == 0)
            return 0;
        else {
            int total = 0;
            for (int anArray : array) {
                total = total + anArray;
            }
            return total / length;
        }
    }


    // get the average of 100 integer entries(~ 710 bytes) from the serial
    public static int decodeSerial(byte[] array) throws SerialMalformatException {
        byte b1 = (byte) 13;
        byte b2 = (byte) 10;
        List<Integer> intList = new ArrayList<>();
        int len = array.length - 8;
        int i1, i2, i3, i4, i5;
        for (int ii = 0; ii < len; ii++) {
            if ((array[ii] == b1) && (array[ii + 1] == b2) && (array[ii + 7] == b1) && (array[ii + 8] == b2)) {
                i1 = byteAsc2int(array[ii + 2]);
                i2 = byteAsc2int(array[ii + 3]);
                i3 = byteAsc2int(array[ii + 4]);
                i4 = byteAsc2int(array[ii + 5]);
                i5 = byteAsc2int(array[ii + 6]);

                if ((i1 > -1) && (i2 > -1) && (i3 > -1) && (i4 > -1) && (i5 > -1)) {
                    int temp = i1 * 10000 + i2 * 1000 + i3 * 100 + i4 * 10 + i5;
                    intList.add(temp);
                }
            }
        }

        int[] temp = list2Array(intList);
        if (temp.length == 0) {
            return 0;
        }
        return aveIntArray(temp);
    }

    public static double[] fft(int[] ints) {
        int[] int256fft = new int[256];
        System.arraycopy(ints, 0, int256fft, 0, 256);
        double[] input = intArray2doubleArray(int256fft);
        DftNormalization norm1 = DftNormalization.STANDARD;
        FastFourierTransformer transformer = new FastFourierTransformer(norm1);

        double[] tempConversion = new double[input.length];

        // perform FFT
        Complex[] complx = transformer.transform(input, TransformType.FORWARD);  //
        for (int i = 0; i < complx.length; i++) {  // convert to absolute values of a complex number
            double rr = (complx[i].getReal());
            double ri = (complx[i].getImaginary());
            tempConversion[i] = Math.sqrt((rr * rr) + (ri * ri));
            if (FreeApp.getInstance().isDebug())
                Log.w("FFT", String.valueOf(tempConversion[i]));
        }

        return tempConversion;
    }


}


