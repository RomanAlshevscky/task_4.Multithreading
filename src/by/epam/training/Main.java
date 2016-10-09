package by.epam.training;


public class Main {

    public static int[][] f = {
            { 1, 3, 2 },
            { 4, 4, 1 },
            { 2, 3, 1 }
    };

    public static int[][] s = {
            { 2, 1, 1 },
            { 3, 2, 1 },
            { 1, 1, 2 }
    };

    public static void main(String[] args) {

        int[][] result = multiplyWithNoLimitThreads();
        printMatrix(result);

        result = multiplyWithTwoThreads();
        printMatrix(result);
    }

    public static void printMatrix(int[][] m){
        for (int i = 0; i <m.length; i++){
            for (int j = 0; j<m[0].length; j++){
                System.out.print(m[i][j] + " ");
            }
            System.out.print("\n\r");
        }
        System.out.print("\n\r");
    }

    public static int[][] multiplyWithNoLimitThreads(){
        Matrix m = new Matrix(f, s, 0);
        m.multiply();
        return m.getResultMatrix();
    }

    public static int[][] multiplyWithTwoThreads(){
        Matrix m = new Matrix(f, s, 2);
        m.multiply();
        return m.getResultMatrix();
    }
}
