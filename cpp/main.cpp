#include <iostream>
#include <stdlib.h>
#include <stdio.h>


enum modes{
        debug,
        verbose,
        normal
};


modes mode=normal;
using namespace std;
int main(int argc,char* argv[])
{
        if(argc <2)
        {
                mode = normal;
                printf("Staring Normal\n");
        }
        for(int argumentcnt=1;argumentcnt<argc;argumentcnt++)
        {
                switch(atoi(argv[argumentcnt]))
                {
                        case 10:
                                mode=debug;
                                printf("Starting Debug\n");
                        break;
                        case 20:
                                mode=verbose;
                                printf("Starting Verbose\n");
                        break;
                        default:
                                printf("Starting Normal\n");
                                mode=normal;
                        break;
                }
        }





        return 0;
}

