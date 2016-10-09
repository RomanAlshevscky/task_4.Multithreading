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

    /*  public int[][] multiplyWithManyThreads(int[][] firstMatrix, int[][] secondMatrix) {
          int[][] result = new int[0][];
          if(validate(firstMatrix, secondMatrix)){
              int length = firstMatrix.length;
              result = new int[length][length];
              for(int i = 0; i < length; i++){
                  int sum = 0;
                  int j = 0;
                  for (; j < length; j++){
                      int a = firstMatrix[i][j];
                      int b = secondMatrix[j][i];
                      sum = sum + a*b;
                  }
                  result[i][j-1] = sum;
              }
          }
          return result;
      }*/
    private class CalculateSumThread extends Thread {

        private final int[][] firstMatrix;
        private final int[][] secondMatrix;
        private final int[][] resultMatrix;
        private volatile boolean isActive = false;
        private int row;
        private int colomn;
        // index in threadPull
        private int index;

        /**
         * @param fMatrix First matrix
         * @param sMatrix Second matrix
         * @param rMatrix Result matrix
         */
        public CalculateSumThread(int[][] fMatrix, int[][] sMatrix, int[][] rMatrix) {
            firstMatrix = fMatrix;
            secondMatrix = sMatrix;
            resultMatrix = rMatrix;
        }

        public void setRowAndColumn(int row, int col){
            this.row = row;
            this.colomn = col;
        }

        public synchronized void run() {
        }

        /**
         * Paste sum into resultMatrix
         */
        private void calculateSum() {
            isActive = true;
            int sum = 0;
            for (int k = 0; k < resultMatrix.length; k++) {
                sum += firstMatrix[row][k] * secondMatrix[k][colomn];
            }
            synchronized (lockObject) {
                resultMatrix[row][colomn] = sum;
            }
            isActive = false;
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
                    if(!item.isActive){
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
                thread.calculateSum();
            }
        }
        try{
            for(CalculateSumThread t : threadPull){
                t.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
