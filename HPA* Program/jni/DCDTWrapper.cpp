# define END 12345.6
# define FIRST_EXAMPLE  Example1
//# include "DCDTsrc/se_dcdt.h"
//# include "DCDTsrc/gs_polygon.h"
//# include <stdlib.h>
# include "DCDTWrapper.h"

static double Example1[] =
       { -10, -10, 10, -10, 10, 10, -10, 10, END,
           1, 1, 7, 3, 3, 8, END,
         END };

static const double* CurExample = FIRST_EXAMPLE;
static SeDcdt *TheDcdt;
static GsPolygon CurPath;
static GsPolygon CurChannel;
static float CurX1=0, CurY1=0, CurX2=0, CurY2=0;
static int   CurSelection=0; // -2,-1: moving point, >0: moving polygon


void create_dcdt ()
 {
   const double* data = CurExample;
   GsPolygon pol;

   // domain:
   while ( *data!=END ) { pol.push().set((float)data[0],(float)data[1]); data+=2; }
   TheDcdt->init ( pol, 0.00001f );

   while ( *++data!=END )
    { pol.size(0);
      while ( *data!=END )  { pol.push().set((float)data[0],(float)data[1]); data+=2; }
      TheDcdt->insert_polygon ( pol );
    }
 }
