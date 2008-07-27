#
# jython examples for jas.
# $Id$
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example F1
# integral function coefficients

r = Ring( "IntFunc(a, b) (y,x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { a } x^4 y + x y^2 + { b } x ),
 ( x^3 + 2 x y ),
 ( { b } x^2 + x^2 y )
) 
""";

startLog();

f = r.paramideal( ps );
print "ParamIdeal: " + str(f);
print;

gs = f.CGBsystem();
print "CGBsystem: " + str(gs);
print;

#sys.exit();

bg = gs.isCGBsystem();
if bg:
    print "isCGBsystem: true";
else:
    print "isCGBsystem: false";
print;

#sys.exit();

gs = f.CGB();
print "CGB: " + str(gs);
print;

bg = gs.isCGB();
if bg:
    print "isCGB: true";
else:
    print "isCGB: false";
print;

sys.exit();

f = r.ideal( ps );
print "Ideal: " + str(f);
print;

from edu.jas.application import PolyUtilApp;
from edu.jas.poly import PolynomialList;
from edu.jas.application import ComprehensiveGroebnerBaseSeq;  

startLog();

cofac = r.ring.coFac.coFac;
#print "cofac:", cofac;
#print;
cgb = ComprehensiveGroebnerBaseSeq( cofac );
#print "cgb:", cgb;
#print;

cl = cgb.GB( f.list );
#print "cl:", cl;
#print;
c = r.ideal( list=cl );
print "c:", c;
print;

bg = cgb.isGB( cl );
if bg:
    print "isCGB: true";
else:
    print "isCGB: false";
print;

terminate();
#------------------------------------------
sys.exit();


from edu.jas.application import PolyUtilApp;
from edu.jas.poly import PolynomialList;

pl = PolyUtilApp.productEmptyDecomposition( f.list );
print;
print "product decomposition:", pl;
print;

sl = PolyUtilApp.productSlice( pl );
#print;
#print "product slice:", sl;
#print;

ssl = PolyUtilApp.productSliceToString( sl );
print;
print "product slice:", ssl;
print;

#sys.exit();

startLog();

from edu.jas.ring import RCGroebnerBasePseudoSeq;  
from edu.jas.application import ComprehensiveGroebnerBaseSeq;  

pr = Ring( ring=pl.ring );

pf = pr.ideal( list=pl.list );
print;
print "Ideal of product decomposition: \n" + str(pf);
print;

cofac = pl.ring.coFac;
#rgbp = RCGroebnerBasePseudoSeq( cofac );
cgb = ComprehensiveGroebnerBaseSeq( cofac );

#sys.exit();

#bg = rgbp.isGB(pl.list);
bg = cgb.isGB(pl.list);
print "isGB:", bg;
print;

#rg = rgbp.GB(pl.list);
rg = cgb.GB(pl.list);

pg = pr.ideal( list=rg );
print "Ideal, GB: " + str(pg);
print;

rgl = PolynomialList(pl.ring,rg);

sl = PolyUtilApp.productSlice( rgl );
#print;
#print "product slice:", sl;
#print;

ssl = PolyUtilApp.productSliceToString( sl );
print;
print "product slice:", ssl;
print;


#bg = rgbp.isGB(rg);
bg = cgb.isGB(rg);
print "isGB:", bg;
print;

cpl = PolyUtilApp.productSlice( rgl, 0 );
cplist = cpl.list;
bg = cgb.isCGB(0,cplist);
print "isCGB:", bg;
print;

cpl = PolyUtilApp.productSlicesUnion( rgl );
cplist = cpl.list;
bg = cgb.isCGB(0,cplist);
print "isCGB:", bg;
print;

terminate();
#sys.exit();
