import java.util.*;
import java.lang.*;
import java.io.*;

import static java.lang.Math.min;

//import com.google.common.base.Stopwatch;

public class MatrixMultiplication {

                                /* lines, cols */
    public static void onMult(int m_ar, int m_br) {

        double temp;
        int i, j, k;

        /* arrays to hold the matrices */
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_br * m_br];
        double[] phc = new double[m_ar * m_ar];

        /* initializing the matrix with 1.0 for every position */
        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_ar; j++) {
                pha[i * m_ar + j] = 1.0;
            }
        }

        /* initializing another matrix starting with 1 and increasing 1 for each row */
        for (i = 0; i < m_br; i++) {
            for (j = 0; j < m_br; j++) {
                phb[i * m_br + j] = i + 1;
            }
        }

        /* current time in nanoseconds */
        float startTime = System.nanoTime();

        /* multiplication */
        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_br; j++) {
                temp = 0;
                for (k = 0; k < m_ar; k++) {
                    temp += pha[i * m_ar + k] * phb[k * m_br + j];
                }
                phc[i * m_ar + j] = temp;
            }
        }

        /* measuring elapsed time */
        float endTime   = System.nanoTime();
        float totalTime = (endTime - startTime) / 1000000000;
        String myString = String.format("\nTime: %f seconds", totalTime);
        System.out.println(myString);
        myString += String.format(" - %dx%d", m_ar,m_br);
        try {
            /* time is written to the csv file */
            BufferedWriter writer = new BufferedWriter(new FileWriter("filename.csv",true));
            writer.write("onMult\n");
            writer.write(myString);
            writer.close();
            System.out.println("\nText appended to file.\n");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        // display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix: ");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }

    }

    // add code here for line x line matrix multiplication
    public static void onMultLine(int m_ar, int m_br) {

        int i, j, k;

        /* arrays to hold the matrices */
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_br * m_br];
        double[] phc = new double[m_ar * m_ar];

        /* initializing the matrix with 1.0 for every position */
        for(i=0; i<m_ar; i++)
            for(j=0; j<m_ar; j++)
                pha[i*m_ar + j] = 1.0;


        /* initializing another matrix starting with 1 and increasing 1 for each row */
        for(i=0; i<m_br; i++)
            for(j=0; j<m_br; j++)
                phb[i*m_br + j] = i+1;

        /* current time in nanoseconds */
        float startTime = System.nanoTime();

        /* iterating through the lines of the first matrix */
        for (i =0;i<m_ar;i++){
            /* iterating through the columns of both matrices */
            for (j =0;j<m_ar;j++){
                /* iterating through the columns of both matrices */
                for (k=0;k<m_ar;k++){
                    /* product of element at line i, column k of the 1st matrix
                    with element at line j, column k of the 2nd
                    that result is stored in line i, column k of the result matrix
                    and it's added to previous multiplications */
                    phc[i*m_ar+k] += pha[i*m_ar+k]*phb[j*m_ar+k];
                }
            }
        }
        float endTime   = System.nanoTime();
        float totalTime = (endTime - startTime) / 1000000000;
        String myString = String.format("\nTime: %f seconds", totalTime);
        System.out.println(myString);
        myString += String.format(" - %dx%d", m_ar,m_br);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("filename.csv",true));
            writer.write("onMultLine\n");
            writer.write(myString);
            writer.close();
            System.out.println("\nText appended to file.\n");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        // display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix: ");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }
    }

    // add code here for block x block matrix multiplication
    public static void onMultBlock(int m_ar, int m_br, int bkSize) {

        int i, j, k;

        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_br * m_br];
        double[] phc = new double[m_ar * m_ar];

        for(i=0; i<m_ar; i++)
            for(j=0; j<m_ar; j++)
                pha[i*m_ar + j] = 1.0;



        for(i=0; i<m_br; i++)
            for(j=0; j<m_br; j++)
                phb[i*m_br + j] = i+1;

        float startTime = System.nanoTime();

        for (int ib = 0; ib<m_ar;ib+=bkSize)
			for (int jb = 0; jb<m_ar;jb+=bkSize)
				for (int kb =0;kb<m_ar;kb+=bkSize)
					for (i =ib;i<min(ib+bkSize,m_ar);i++)
						for (j =jb;j<min(jb+bkSize,m_ar);j++)
							for (k=kb;k<min(kb+bkSize,m_ar);k++)
								phc[i*m_ar+k] += pha[i*m_ar+k]*phb[j*m_ar+k];

        float endTime   = System.nanoTime();

        float totalTime = (endTime - startTime) / 1000000000;
        String myString = String.format("\nTime: %f seconds", totalTime);
        System.out.println(myString);
        myString += String.format(" - %dx%d", m_ar,m_br);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("filename.csv",true));
            writer.write("onMultBlock\n");
            writer.write(myString);
            writer.close();
            System.out.println("\nText appended to file.\n");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        // display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix: ");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }
    }

    public static void main(String[] args) {

        /* file creation */
        try {
            File myObj = new File("filename.csv");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        int lin, col, blockSize;
        int op;

        Scanner scanner = new Scanner(System.in);
        /* while 0 not inserted */
        while (true){
            /* operation and dimensions input */
            System.out.println();
            System.out.println("1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.print("Selection?: ");
            op = scanner.nextInt();
            if (op == 0)
                break;
            System.out.print("Dimensions: lins=cols ? ");
            lin = scanner.nextInt();
            col = lin;

            /* operation */
            switch (op) {
                case 1 -> onMult(lin, col);
                case 2 -> onMultLine(lin, col);
                case 3 -> {
                    System.out.print("Block Size? ");
                    blockSize = scanner.nextInt();
                    onMultBlock(lin, col, blockSize);
                }
            }

        }

        scanner.close();

    }
}
