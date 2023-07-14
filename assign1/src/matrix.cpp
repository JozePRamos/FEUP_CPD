#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <fstream>
#include <algorithm>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t

 
double OnMult(int m_ar, int m_br) 
{
	
	double Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	
  pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = omp_get_wtime();

	#pragma omp parallel
	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			#pragma omp for
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = omp_get_wtime();
	sprintf(st, "Time: %3.3f seconds\n", (Time2 - Time1));
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
	return Time2-Time1;
	
}

// add code here for line x line matriz multiplication
double OnMultLine(int m_ar, int m_br)
{
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);


    Time1 = clock();

	for (i =0;i<m_ar;i++){
		for (j =0;j<m_ar;j++){
			for (k=0;k<m_ar;k++){
				phc[i*m_ar+k] += pha[i*m_ar+k]*phb[j*m_ar+k];
			}
		}
	}

    Time2 = clock();

	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
	return Time2-Time1;
    
}

// add code here for block x block matriz multiplication
double OnMultBlock(int m_ar, int m_br, int bkSize)
{
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

    Time1 = clock();

    for (int ib = 0; ib<m_ar;ib+=bkSize)
			for (int jb = 0; jb<m_ar;jb+=bkSize)
				for (int kb =0;kb<m_ar;kb+=bkSize)
					for (i =ib;i<min(ib+bkSize,m_ar);i++)
						for (j =jb;j<min(jb+bkSize,m_ar);j++)
							for (k=kb;k<min(kb+bkSize,m_ar);k++)
								phc[i*m_ar+k] += pha[i*m_ar+k]*phb[j*m_ar+k];

	Time2 = clock();

	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	return Time2-Time1;
}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
	
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
						
}

void onMultLoop(){
	
	int op=1;
	
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "0. Go Back" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;

		ofstream f;

		f.open("result.csv");

		switch (op){
			case 1: 
				for (int i =600 ; i<=1800; i+=400){
					int EventSet = PAPI_NULL;
  					long long values[8];
  					int ret;
					ret = PAPI_start(EventSet);
					if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

					printf("Matrix size: %d*%d\n", i,i);
					double time;
					time = OnMult(i, i);

					char st[100];
					sprintf(st, "Time: %3.6f seconds - %d*%d", (time),i,i);
					f << st << "," << "\n";

					ret = PAPI_stop(EventSet, values);
  					if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
					f <<"L1 DCM " << values[0] << ",\n";
					f <<"L2 DCM " << values[1] << ",\n";
					f <<"L1 ICM " << values[2] << ",\n";
					f <<"L2 ICM " << values[3] << ",\n";
					f <<"L2 DCA " << values[4] << ",\n";
					f <<"L2 TCR " << values[5] << ",\n";
					f <<"L2 TCW " << values[6] << ",\n";
					f <<"TOT INS " << values[7] << ",\n";
  					printf("L1 DCM: %lld \n",values[0]);
  					printf("L2 DCM: %lld \n",values[1]);
					printf("L1 ICM: %lld \n",values[2]);
					printf("L2 ICM: %lld \n",values[3]);
					printf("L2 DCA: %lld \n",values[4]);
					printf("L2 TCR: %lld \n",values[5]);
					printf("TOT INS: %lld \n",values[7]);
					
				}		
				break;
			case 2:
				for (int i =600; i<=1800;i+=400){
					int EventSet = PAPI_NULL;
  					long long values[8];
  					int ret;
					ret = PAPI_start(EventSet);
					if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

					printf("Matrix size: %d*%d\n", i,i);
					double time;

					time = OnMultLine(i,i);

					char st[100];
					sprintf(st, "Time: %3.6f seconds - %d*%d", (double)(time) / CLOCKS_PER_SEC,i,i);
					f << st << "," << "\n";

					ret = PAPI_stop(EventSet, values);
  					if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
					f <<"L1 DCM " << values[0] << ",\n";
					f <<"L2 DCM " << values[1] << ",\n";
					f <<"L1 ICM " << values[2] << ",\n";
					f <<"L2 ICM " << values[3] << ",\n";
					f <<"L2 DCA " << values[4] << ",\n";
					f <<"L2 TCR " << values[5] << ",\n";
					f <<"L2 TCW " << values[6] << ",\n";
					f <<"TOT INS " << values[7] << ",\n";
  					printf("L1 DCM: %lld \n",values[0]);
  					printf("L2 DCM: %lld \n",values[1]);
					printf("L1 ICM: %lld \n",values[2]);
					printf("L2 ICM: %lld \n",values[3]);
					printf("L2 DCA: %lld \n",values[4]);
					printf("L2 TCR: %lld \n",values[5]);
					printf("L2 TCW: %lld \n",values[6]);
					printf("TOT INS: %lld \n",values[7]);
					
				} 
				break;
			case 3:
				for (int j = 128;j<=128;j+=128){
					f<<"Block sise: "<<j<<",\n";
					printf("Block sise: %d\n",j);
					for (int i =600; i<=1800;i+=400){
						int EventSet = PAPI_NULL;
  						long long values[8];
  						int ret;
						ret = PAPI_start(EventSet);
						if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;


						printf("Matrix size: %d*%d\n", i,i);
						double time;

						time = OnMultBlock(i,i,j);
						char st[100];
						sprintf(st, "Time: %3.6f seconds - %d*%d", (double)(time) / CLOCKS_PER_SEC,i,i);
						f << st << "," << "\n";

						ret = PAPI_stop(EventSet, values);
  						if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
						f <<"L1 DCM " << values[0] << ",\n";
						f <<"L2 DCM " << values[1] << ",\n";
						f <<"L1 ICM " << values[2] << ",\n";
						f <<"L2 ICM " << values[3] << ",\n";
						f <<"L2 DCA " << values[4] << ",\n";
						f <<"L2 TCR " << values[5] << ",\n";
						f <<"L2 TCW " << values[6] << ",\n";
						f <<"TOT INS " << values[7] << ",\n";
  						printf("L1 DCM: %lld \n",values[0]);
  						printf("L2 DCM: %lld \n",values[1]);
						printf("L1 ICM: %lld \n",values[2]);
						printf("L2 ICM: %lld \n",values[3]);
						printf("L2 DCA: %lld \n",values[4]);
						printf("L2 TCR: %lld \n",values[5]);
						printf("L2 TCW: %lld \n",values[6]);
						printf("TOT INS: %lld \n",values[7]);
						
					}  
					f<< "\n";
				}
				break;

		}


	f.close();

	}while (op != 0);
}

void specifcMult(){
	int op =1;
	int size;
	while (op !=0){
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "0. Go Back" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;

			switch (op)
			{
			case 1:

				cout << endl <<"Matrix size:";
				cin >> size;
				OnMult(size,size);
				break;

			case 2:

				cout << endl <<"Matrix size:";
				cin >> size;
				OnMultLine(size,size);
				break;

			case 3:
				int block;
				cout << endl <<"Matrix size:";
				cin >> size;
				cout << endl <<"Block size:";
				cin >> block;
				OnMultBlock(size,size,block);
				break;
			
			default:
				break;
			}
	}
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[8];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L1_ICM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_ICM" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L2_ICM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_ICM" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L2_DCA);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCA" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L2_TCR);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_TCR" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L2_TCW);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_TCW" << endl;

	ret = PAPI_add_event(EventSet,PAPI_TOT_INS);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_TCW" << endl;

	op = 1;
	while(op != 0){
		cout << endl << "1. Multiplication Loop" << endl;
		cout << "2. Specific Multiplication" << endl;
		cout << "0. Exit" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;

		switch (op)
		{
		case 1:
			onMultLoop();
			break;
		case 2:
			specifcMult();
			break;
		}	
	}
	
	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L1_ICM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_ICM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCA );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_TCR );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_TCW );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_TOT_INS );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}