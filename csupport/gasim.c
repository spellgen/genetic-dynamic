/* gasim.c - Genetic Algorithm simulation call interface */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <tf.h>
#include <math.h>

int verb=1,ecsave=0,bestflag=0,besthold=0,bestonly=0;
int bestidx=0, nonoise=0;
long seed;
double omega=1;
extern long iseed;
extern int npop,navg,nsde,dtper,dtsav;
extern double tend, sigamp;
extern char *baseid;
extern int genid;

void usage(char *);
int process(char *, int, int []);
void *getsde(int,int);
void initsde();
void integrate(double t1, double t2, double sigamp,
	int dtper, int dtsav, FILE *ftf,
	int ichromo, void (*icond)(double []),
	void (*derivs)(double, double, double [], double [], double []),
	double *fitlist);
int listmax(double *l1, int n);
double gasdev(long *idum);
double ran1(long *idum);

int main(int argc, char *argv[]) {
    int i,nproc=0;
    char basename[80];
    int sel[100],nsel=0;
    initsde();
    for(i=1; i<argc; i++) {
	if(*argv[i]=='-') {
            if(strcmp(argv[i],"-v")==0) {
	        verb=1;
	    }
	    else if(strcmp(argv[i],"-id")==0) {
		printf("%s %1i\n",baseid,genid);
		exit(0);
	    }
            else if(strcmp(argv[i],"-nonoise")==0) {
	        nonoise=1;
	    }
            else if(strcmp(argv[i],"-ec")==0) {
	        ecsave=1;
	    }
	    else if(strcmp(argv[i],"-dtper")==0) {
		dtper=atoi(argv[++i]);
	    }
	    else if(strcmp(argv[i],"-dtsav")==0) {
		dtsav=atoi(argv[++i]);
	    }
	    else if(strcmp(argv[i],"-tend")==0) {
		tend=atof(argv[++i]);
	    }
	    else if(strcmp(argv[i],"-sigamp")==0) {
		sigamp=atof(argv[++i]);
	    }
	    else if(strcmp(argv[i],"-omega")==0) {
		omega=atof(argv[++i]);
	    }
	    else if(strcmp(argv[i],"-navg")==0) {
		navg=atoi(argv[++i]);
	    }
	    else if(strcmp(argv[i],"-sel")==0) {
		sel[nsel++]=atoi(argv[++i]);
	    }
	    else if(strcmp(argv[i],"-best")==0) {
		bestflag=1;
	    }
	    else if(strcmp(argv[i],"-bestonly")==0) {
		bestflag=bestonly=1;
	    }
            else {
	        fprintf(stderr,"Unrecognized option: %s\n",argv[i]);
	        exit(1);
	    }
	}
	else {
	    strcpy(basename,argv[i]);
	    if(bestflag) {
		nsel=0;
		besthold=1; /* don't output a lot of stuff right now */
		sel[0]=process(basename,nsel,sel);
		nsel=1;
		besthold=0;
	    }
	    process(basename,nsel,sel);
	    nproc++;
	}
    }
    if(nproc==0) usage(argv[0]);
    exit(0);
}

void usage(char *caller) {
printf("usage: %s [flags] basename [basename...]\n",caller);
printf("-v         verbose output [%1i]\n",verb);
printf("-id        identify simulation and step responsible for this code\n");
printf("-ec        save e-c fitness vectors [%1i]\n",ecsave);
printf("-dtper #   integration timesteps per period of the applied signal [%1i]\n",dtper);
printf("-dtsav #   save time dependent data every dtsav timestep [%1i]\n",dtsav);
printf("-tend #    lenght of simulation (in periods of the applied signal) [%g]\n",tend);
printf("-sigamp #  signal amplitude [%g]\n",sigamp);
printf("-omega #   relative frequency of the signal [%g]\n",omega);
printf("-navg #    number of e/c pairs to average for fitness estimate [%1i]\n",navg);
printf("-sel #     select particular element in the vector\n");
printf("-nonoise   turn off noise\n");
printf("-best      select the best performing system\n");
exit(0);
}

double *x, *dxdt, *stoch, *xsave;
int process(char *basename, int nsel, int sel[]) {
    char fname[80];
    char chlabel[80];
    int i,j,k,l,nout,nsave,ichromo,imax;
    FILE *ftf;
    double *fitc,*fite,*fitt,*fitx,*fit,*liste,*listc,*listt;
    double fitmax;
    void *iconds,*derivs;
    seed=(iseed<0 ? iseed : -iseed);
    ran1(&seed);
    assert(fitc=(double *)malloc(sizeof(double)*navg));
    assert(fite=(double *)malloc(sizeof(double)*navg));
    assert(fitt=(double *)malloc(sizeof(double)*navg));
    assert(fitx=(double *)malloc(sizeof(double)*npop));
    assert(fit=(double *)malloc(sizeof(double)*npop));
    assert(liste=(double *)malloc(sizeof(double)*nsde*navg));
    assert(listc=(double *)malloc(sizeof(double)*nsde*navg));
    assert(listt=(double *)malloc(sizeof(double)*nsde*navg));
    assert(x=(double *)malloc(sizeof(double)*5*nsde));
    assert(dxdt=(double *)malloc(sizeof(double)*nsde));
    assert(stoch=(double *)malloc(sizeof(double)*nsde));
    if(dtsav) {
	nsave=tend*dtper/dtsav+1;
	assert(xsave=(double *)malloc(sizeof(double)*nsde*nsave));
    }
    sprintf(fname,"%s.out",basename);
    if(verb && !besthold) printf("opening output file: %s\n",fname);
    if(!besthold) assert(ftf=tf_wopen(fname));
    if(nsel) {
        for(i=0;i<nsel;i++) fit[i]=(double)sel[i];
	if(!besthold) tf_wv(ftf,"sel",nsel,fit);
	if(verb) {
	    printf("processing selections:");
	    for(i=0;i<nsel;i++) printf(" %1i",sel[i]);
	    printf("\n");
	}
    }
    nout=(nsel ? nsel : npop);
    for(i=0;i<nout;i++) fit[i]=0.0;
    for(i=0;i<nout;i++) {
	if(!besthold) {
	    sprintf(chlabel,"chromo=%1i",i);	
	    tf_ws(ftf,"chromoid",chlabel);
	}
	if(nsel) {
	    ichromo=sel[i];
	    iconds=getsde(sel[i],0);
	    derivs=getsde(sel[i],1);
	}
	else {
	    ichromo=i;
	    iconds=getsde(i,0);
	    derivs=getsde(i,1);
	}
	for(k=0;k<nsde;k++) listt[k]=0.0;
        for(j=0;j<navg;j++) {
	    integrate(0.0,tend,sigamp,dtper,dtsav,ftf,
		ichromo,iconds,derivs,&liste[nsde*j]);
	    integrate(0.0,tend,0.0,   dtper,dtsav,ftf,
		ichromo,iconds,derivs,&listc[nsde*j]);
	    for(k=0;k<nsde;k++) {
		l=nsde*j+k;
		listt[k]+=(liste[l]-listc[l]);
	    }
	} /* navg loop (j) */
	k=listmax(listt,navg);
        for(j=0;j<navg;j++) {
	    fite[j]=liste[k+nsde*j];
	    fitc[j]=listc[k+nsde*j];
	    fitt[j]=(fite[j]-fitc[j]);
	    fit[i]+=(fite[j]-fitc[j]);
	}
	fitx[i]=(double)k;
	if(ecsave && !besthold) {
	    tf_wn(ftf,"fitx",fitx[i]);
	    tf_wv(ftf,"fite",navg,fite);
	    tf_wv(ftf,"fitc",navg,fitc);
	    tf_wv(ftf,"fitt",navg,fitt);
	}
    } /* nout loop (i) */
    
    fitmax=0.0;
    for(i=0;i<nout;i++) {
	fit[i]/=(double)navg;
	if(fit[i]>fitmax) {
	    fitmax=fit[i];
	    imax=i;
	}
    }
    bestidx=(int)(fitx[imax]+0.5);
    if(verb && besthold) {
	printf("%s: best chromosome=%1i\n",basename,(nsel ? sel[imax] : imax));
    }
    if(!besthold) {
	tf_wv(ftf,"fitness",nout,fit);
    	tf_close(ftf);
    }
    if(dtsav) free(xsave);
    free(stoch); free(dxdt); free(x);
    free(liste); free(listc); free(listt); free(fit);
    free(fitx); free(fite); free(fitc); free(fitt);
    return imax;
}

int listmax(double *list, int n) {
    int i,imax=0;
    double lmax=-1.0;
    for(i=0;i<n;i++) {
	if(list[i]>lmax) {
	    lmax=list[i];
	    imax=i;
	}
    }
    return imax;
}

#define SQR(x) ((x)*(x))
#define XMAX (1.0e6)
void integrate(double t1, double t2, double sigamp,
	int dtpar, int dtsav, FILE *ftf,
	int ichromo, void (*icond)(double []),
	void (*derivs)(double, double, double [], double [], double []),
	double *fitlist) {
    double t,h,sint,cost,noise,tint,signal,harmpow;
    int i,k,k2,isave=0,nsave;
    char str[80];
    icond(x);
    for(i=nsde;i<5*nsde;i++) x[i]=0.0;
    t=t1*2.0*M_PI;
    h=2.0*M_PI/omega/dtper;
    k2=t2*dtper;
    if(dtsav) nsave=tend*dtper/dtsav+1;
    for(k=0; k<k2; t+=h, k++) {
	sint=sin(omega*t);
	cost=cos(omega*t);
	signal=sigamp*sint;
	derivs(t,signal,x,dxdt,stoch);
	for(i=0;i<nsde;i++) {
	    if(dtsav && k%dtsav==0) xsave[isave+i*nsave]=x[i];
	    x[i+nsde]+=x[i]*x[i]*h;		/* Calc total power */
	    x[i+2*nsde]+=2.0*x[i]*sint*h;	/* Fourier-sin component */
	    x[i+3*nsde]+=2.0*x[i]*cost*h;	/* Fourier-cos component */
	    x[i+4*nsde]+=x[i]*h;		/* Calc DC component */
	    if(nonoise) {
		noise=0.0;
	    }
	    else {
	        noise = (stoch[i]!=0.0 ? stoch[i]*gasdev(&seed) : 0.0);
	    }
	    x[i]+=dxdt[i]*h+noise*h;
	    if(fabs(x[i])>XMAX) x[i]=(x[i]>0 ? XMAX : -XMAX);
	}
	if(dtsav && k%dtsav==0) isave++;
    }
    tint=(t2-t1)*2.0*M_PI/omega;
    for(i=0;i<nsde;i++) {
	x[i+nsde]/=tint;
	x[i+2*nsde]/=tint;
	x[i+3*nsde]/=tint;
	x[i+4*nsde]/=tint;
    }
    for(i=0;i<nsde;i++) {
	harmpow=2.0*(x[i+nsde]-SQR(x[i+4*nsde]));
	if(harmpow==0.0) {
	    fitlist[i]=0.0;
	}
	else {
	    fitlist[i]=(SQR(x[i+2*nsde])+SQR(x[i+3*nsde]))/harmpow;
	    if(fitlist[i]>1.0) {
		fprintf(stderr,"integrate: warning fitness>1.0: %g\n",
			fitlist[i]);
		fitlist[i]=1.0;
	    }
	}
        if(dtsav && !besthold) {
	    sprintf(str,"chromo%1i_x%1i%c",ichromo,i,
			(signal==0.0 ? 'c' : 'e'));
	    if(bestonly) {
		if(i==bestidx) tf_wv(ftf,str,isave,&xsave[i*nsave]);
	    }
	    else {
		tf_wv(ftf,str,isave,&xsave[i*nsave]);
	    }
	}
    }
}
