# define END -12345.6
# define FIRST_EXAMPLE  Example1
//# include "DCDTsrc/se_dcdt.h"
//# include "DCDTsrc/gs_polygon.h"
//# include <stdlib.h>
# include "DCDTWrapper.h"

static SeDcdt TheDcdt;
static GsPolygon CurPath;
static GsPolygon CurChannel;

void create_dcdt (const double* input, GsArray<SeDcdtFace*>& faces, GsArray<GsPnt2>& constr_edges) {
	const double* data = input;
	GsPolygon pol;
	printf("1");
	// domain:
	int x = data[0], y = data[1];
	while ( *data!=END ) {
		pol.push().set((float)data[0],(float)data[1]); data+=2;
	}
	TheDcdt.init ( pol, 0.00001f );

	while ( *++data!=END )
	{ pol.size(0);
		while ( *data!=END )  { pol.push().set((float)data[0],(float)data[1]); data+=2; }
		TheDcdt.insert_polygon ( pol );
	}

	GsArray<GsPnt2> temp;

	TheDcdt.extract_faces(faces, x, y);

	TheDcdt.get_mesh_edges(&constr_edges, &temp);
}

void create_dcdt2 (const double* input, GsArray<GsPnt2>* edges)
 {
   const double* data = input;
   GsPolygon pol;

   // domain:
   while ( *data!=END ) { pol.push().set((float)data[0],(float)data[1]); data+=2; }
   TheDcdt.init ( pol, 0.00001f );

   while ( *++data!=END )
    { pol.size(0);
      while ( *data!=END )  { pol.push().set((float)data[0],(float)data[1]); data+=2; }
      TheDcdt.insert_polygon ( pol );
    }

   GsArray<GsPnt2> unconstr;

   TheDcdt.get_mesh_edges(edges, &unconstr);


   for(int i = 0; i < unconstr.size(); i++) {
	   edges -> push(unconstr.get(i));
   }
 }

