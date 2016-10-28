package by.epam.training;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 06.10.2016.
 */
public class Matrix {

    private int[][] firstMatrix;
    private int[][] secondMatrix;
    private int[][] resultMatrix;
    private int currentColumn;
    private final Object lockObject = new Object();
    private List<CalculateSumThread> threadPull;
    // Case 0: maxThreadCount not limited;
    private int maxThreadCount = 0;

    public Matrix(int[][] firstM, int[][] secondM, int maxThreadCount) {
        if (!validate(firstM, secondM) || maxThreadCount < 0)
            throw new IllegalArgumentException();
        firstMatrix = firstM;
        secondMatrix = secondM;
        resultMatrix = new int[firstM.length][firstM.length];
        this.maxThreadCount = maxThreadCount;
        if (maxThreadCount > 0){
            threadPull = new ArrayList<>(maxThreadCount);
        }
        else
            threadPull = new ArrayList<>();
    }

    public void setFirstMatrix(int[][] matrix) {
        firstMatrix = matrix;
    }

    public void setSecondMatrix(int[][] matrix) {
        secondMatrix = matrix;
    }

    public int[][] getResultMatrix() {
        ;
        return resultMatrix;
    }

    private boolean checkSquare(int[][] matrix) {
        int width = matrix.length;
        for (int i = 0; i < width; i++) {
            int l = matrix[i].length;
            if (l != width)
                return false;
        }
        return true;
    }

    private boolean validate(int[][] firstMatrix, int[][] secondMatrix) {
        if (firstMatrix != null && secondMatrix != null) {
            if (checkSquare(firstMatrix) && checkSquare(secondMatrix)) {
                if (firstMatrix[0].length == secondMatrix.length) {
                    return true;
                }
            }
        }
        return false;
    }

    private class CalculateSumThread extends Thread {

        private final int[][] firstMatrix;
        private final int[][] secondMatrix;
        private final int[][] resultMatrix;
        private int row;
        private int colomn;
        private volatile boolean stop;
        private Object synchronizedObject = new Object();
        //True, после выставления значений для вычислений
        private volatile boolean isReady = false;

        public void setStop(boolean value) {
            stop = value;
        }

        public boolean isStoped() {
            return stop;
        }

        /**
         * @param fMatrix First matrix
         * @param sMatrix Second matrix
         * @param rMatrix Result matrix
         */
        public CalculateSumThread(int[][] fMatrix, int[][] sMatrix, int[][] rMatrix) {
            firstMatrix = fMatrix;
            secondMatrix = sMatrix;
            resultMatrix = rMatrix;
            stop = false;
        }

        public void setRowAndColumn(int row, int col) {
            synchronized (synchronizedObject) {
                this.row = row;
                this.colomn = col;
                isReady = true;
            }
        }

        public synchronized void run() {
            //Пока не stop != true и вычисления не закончены
            while(!stop || isReady) {
                synchronized (synchronizedObject) {
                    if (isReady) {
                        calculateSum();
                        isReady = false;
                    }
                }
            }
        }

        /**
         * Paste sum into resultMatrix
         */
        private void calculateSum() {
            int sum = 0;
            for (int k = 0; k < resultMatrix.length; k++) {
                sum += firstMatrix[row][k] * secondMatrix[k][colomn];
            }
            synchronized (lockObject) {
                resultMatrix[row][colomn] = sum;
            }
        }
    }

    public CalculateSumThread getFreeSumThread() {
        CalculateSumThread t = null;
        if (threadPull.size() < maxThreadCount || maxThreadCount ==0) {
            t = new CalculateSumThread(firstMatrix, secondMatrix, resultMatrix);
            threadPull.add(t);
            t.start();
        }
        else {
            while (t == null){

                for (CalculateSumThread item: threadPull) {
                    if(!item.isReady){
                        t = item;
                        break;
                    }
                }
            }
        }
        return t;
    }

    public void multiply() {
        final int length = firstMatrix.length;
        for (currentColumn = 0; currentColumn < length; currentColumn++) {
            for (int currentRow = 0; currentRow < length; currentRow++) {
                CalculateSumThread thread = getFreeSumThread();
                thread.setRowAndColumn(currentRow, currentColumn);
            }
        }
        try{
            for(CalculateSumThread t : threadPull){
                t.setStop(true);
                t.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
