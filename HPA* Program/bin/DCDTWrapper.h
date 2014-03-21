# include "DCDTsrc/se_dcdt.h"
# include "DCDTsrc/gs_polygon.h"
# include <stdlib.h>
void create_dcdt (const double* input, GsArray<SeDcdtFace*>& faces, GsArray<GsPnt2>& constr_edges);
void create_dcdt2 (const double* input, GsArray<GsPnt2>* edges);
