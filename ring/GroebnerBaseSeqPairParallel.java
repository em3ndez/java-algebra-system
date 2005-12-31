/*
 * $Id$
 */

package edu.jas.ring;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;

import edu.jas.util.Terminator;
import edu.jas.util.ThreadPool;
import edu.unima.ky.parallel.Semaphore;


/**
 * Groebner Base Parallel class.
 * Makes some effort to produce the same sequence of critical pairs 
 * as in the sequential version.
 * However already reduced pairs are not rereduced if new
 * polynomials appear.
 * Implements a shared memory parallel version of Groebner bases.
 * Slaves maintain pairlist.
 * @author Heinz Kredel
 */

public class GroebnerBaseSeqPairParallel<C extends RingElem<C>>
             extends GroebnerBaseAbstract<C>  {

    private static final Logger logger = Logger.getLogger(GroebnerBaseSeqPairParallel.class);


    /**
     * Number of threads to use.
     */
    protected final int threads;


    /**
     * Pool of threads to use.
     */
    protected final ThreadPool pool;


    /**
     * Constructor.
     */
    public GroebnerBaseSeqPairParallel() {
        this(2);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     */
    public GroebnerBaseSeqPairParallel(int threads) {
        this(threads, new ThreadPool(threads) );
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pool ThreadPool to use.
     */
    public GroebnerBaseSeqPairParallel(int threads, ThreadPool pool) {
	if ( threads < 1 ) {
           threads = 1;
        }
        this.threads = threads;
        this.pool = pool;
        red = new ReductionPar<C>();
    }


    /**
     * Cleanup and terminate ThreadPool.
     */
    public void terminate() {
	if ( pool == null ) {
           return;
        }
        pool.terminate();
    }


    /**
     * Parallel Groebner base using sequential pair order class.
     * Slaves maintain pairlist.
     * @param C coefficient type.
     * @param modv number of module variables.
     * @param F polynomial list.
     * @return GB(F) a Groebner base of F.
     */
    public List<GenPolynomial<C>> 
             GB( int modv,
                 List<GenPolynomial<C>> F ) {  
        GenPolynomial<C> p;
        List<GenPolynomial<C>> G = new ArrayList<GenPolynomial<C>>();
        CriticalPairList<C> pairlist = null; 
        int l = F.size();
        ListIterator<GenPolynomial<C>> it = F.listIterator();
        while ( it.hasNext() ) { 
            p = it.next();
            if ( p.length() > 0 ) {
               p = p.monic();
               if ( p.isONE() ) {
		  G.clear(); G.add( p );
                  return G; // since no threads activated jet
	       }
               G.add( p );
	       if ( pairlist == null ) {
                  pairlist = new CriticalPairList<C>( modv, p.ring );
               }
               // putOne not required
               pairlist.put( p );
	    } else {
               l--;
            }
	}
        if ( l <= 1 ) {
           return G; // since no threads activated jet
        }

	Terminator fin = new Terminator(threads);
	ReducerSeqPair<C> R;
	for ( int i = 0; i < threads; i++ ) {
	      R = new ReducerSeqPair<C>( fin, G, pairlist );
	      pool.addJob( R );
	}
	fin.done();
        logger.debug("#parallel list = "+G.size());
	G = minimalGB(G);
	// not in this context // pool.terminate();
        logger.info("pairlist #put = " + pairlist.putCount() 
                  + " #rem = " + pairlist.remCount()
                    //+ " #total = " + pairlist.pairCount()
                   );
	return G;
    }


    /**
     * Minimal ordered groebner basis, parallel.
     * @param C coefficient type.
     * @param Fp a Groebner base.
     * @return minimalGB(F) a minimal Groebner base of Fp.
     */
    public List<GenPolynomial<C>> 
           minimalGB(List<GenPolynomial<C>> Fp) {  
        GenPolynomial<C> a;
        ArrayList<GenPolynomial<C>> G;
        G = new ArrayList<GenPolynomial<C>>( Fp.size() );
        ListIterator<GenPolynomial<C>> it = Fp.listIterator();
        while ( it.hasNext() ) { 
            a = it.next();
            if ( a.length() != 0 ) { // always true
	       // already monic  a = a.monic();
               G.add( a );
	    }
	}
        if ( G.size() <= 1 ) {
           return G;
        }

        ExpVector e;        
        ExpVector f;        
        GenPolynomial<C> p;
        ArrayList<GenPolynomial<C>> F;
        F = new ArrayList<GenPolynomial<C>>( G.size() );
	boolean mt;
        while ( G.size() > 0 ) {
            a = G.remove(0);
	    e = a.leadingExpVector();

            it = G.listIterator();
	    mt = false;
	    while ( it.hasNext() && ! mt ) {
               p = it.next();
               f = p.leadingExpVector();
	       mt = ExpVector.EVMT( e, f );
	    }
            it = F.listIterator();
	    while ( it.hasNext() && ! mt ) {
               p = it.next();
               f = p.leadingExpVector();
	       mt = ExpVector.EVMT( e, f );
	    }
	    if ( ! mt ) {
		F.add( a ); // no thread at this point
	    } else {
		// System.out.println("dropped " + a.length());
	    }
	}
	G = F;
        if ( G.size() <= 1 ) {
           return G;
        }

	MiReducerSeqPair<C>[] mirs = new MiReducerSeqPair[ G.size() ];
        int i = 0;
        F = new ArrayList<GenPolynomial<C>>( G.size() );
        while ( G.size() > 0 ) {
            a = G.remove(0);
	    // System.out.println("doing " + a.length());
	    mirs[i] = new MiReducerSeqPair<C>( 
                                        (List<GenPolynomial<C>>)G.clone(), 
                                        (List<GenPolynomial<C>>)F.clone(), 
                                        a );
	    pool.addJob( mirs[i] );
	    i++;
            F.add( a );
	}
	G = F;
	F = new ArrayList<GenPolynomial<C>>( G.size() );
	for ( i = 0; i < mirs.length; i++ ) {
	    a = mirs[i].getNF();
            F.add( a );
	}
	return F;
    }

}


/**
 * Reducing worker threads.
 */
class ReducerSeqPair<C extends RingElem<C>> implements Runnable {
	private List<GenPolynomial<C>> G;
	private CriticalPairList<C> pairlist;
	private Terminator pool;
        private ReductionPar<C> red;
        private static Logger logger = Logger.getLogger(Reducer.class);

	ReducerSeqPair(Terminator fin, 
                List<GenPolynomial<C>> G, 
                CriticalPairList<C> L) {
	    pool = fin;
	    this.G = G;
	    pairlist = L;
            red = new ReductionPar<C>();
	} 


	public void run() {
           CriticalPair<C> pair;
           GenPolynomial<C> S;
           GenPolynomial<C> H;
	   boolean set = false;
	   int reduction = 0;
	   int sleeps = 0;
           while ( pairlist.hasNext() || pool.hasJobs() ) {
	      while ( ! pairlist.hasNext() ) {
                  pairlist.update();
                  // wait
                  pool.beIdle(); set = true;
		  try {
		      sleeps++;
                      if ( sleeps % 10 == 0 ) {
                         logger.info(" reducer is sleeping");
		      } else {
                         logger.debug("r");
		      }
		      Thread.sleep(100);
		  } catch (InterruptedException e) {
                     break;
		  }
		  if ( ! pool.hasJobs() ) {
                     break;
                  }
              }
              if ( ! pairlist.hasNext() && ! pool.hasJobs() ) {
                 break;
              }
              if ( set ) {
                 pool.notIdle();
              }
              pair = pairlist.getNext();
              if ( pair == null ) {
                 pairlist.update();
                 continue; 
              }
              if ( false && logger.isDebugEnabled() ) {
                 logger.debug("pi = " + pair.pi );
                 logger.debug("pj = " + pair.pj );
	      }
              S = red.SPolynomial( pair.pi, pair.pj );
              if ( S.isZERO() ) {
                 pairlist.record( pair, S );
                 continue;
              }
              if ( logger.isDebugEnabled() ) {
                 logger.debug("ht(S) = " + S.leadingExpVector() );
	      }
              H = red.normalform( G, S ); //mod
	      reduction++;
              if ( H.isZERO() ) {
                 pairlist.record( pair, H );
                 continue;
              }
              if ( logger.isDebugEnabled() ) {
                 logger.debug("ht(H) = " + H.leadingExpVector() );
	      }
              H = H.monic();
              // System.out.println("H   = " + H);
	      if ( H.isONE() ) { 
                 // pairlist.update( pair, H );
                 pairlist.putOne(); // not really required
		 synchronized (G) {
                     G.clear(); G.add( H );
		 }
	         pool.allIdle();
                 return;
	      }
              if ( logger.isDebugEnabled() ) {
                 logger.debug("H = " + H );
	      }
              synchronized (G) {
                     G.add( H );
              }
              pairlist.update( pair, H );
              //pairlist.record( pair, H );
              //pairlist.update();
	   }
           logger.info( "terminated, done " + reduction + " reductions");
	}
}


/**
 * Reducing worker threads for minimal GB.
 */
class MiReducerSeqPair<C extends RingElem<C>> implements Runnable {
	private List<GenPolynomial<C>> G;
	private List<GenPolynomial<C>> F;
	private GenPolynomial<C> S;
	private GenPolynomial<C> H;
        private ReductionPar<C> red;
	private Semaphore done = new Semaphore(0);
        private static Logger logger = Logger.getLogger(MiReducer.class);

	MiReducerSeqPair(List<GenPolynomial<C>> G, 
                         List<GenPolynomial<C>> F, 
                         GenPolynomial<C> p) {
	    this.G = G;
	    this.F = F;
	    S = p;
	    H = S;
            red = new ReductionPar<C>();
	} 


	/**
	 * getNF. Blocks until the normal form is computed.
	 * @return the computed normal form.
	 */
	public GenPolynomial<C> getNF() {
	    try { done.P();
	    } catch (InterruptedException e) { 
            }
	    return H;
	}

	public void run() {
	    if ( logger.isDebugEnabled() ) {
                 logger.debug("ht(S) = " + S.leadingExpVector() );
	    }
            H = red.normalform( G, H ); //mod
            H = red.normalform( F, H ); //mod
            done.V();
	    if ( logger.isDebugEnabled() ) {
                 logger.debug("ht(H) = " + H.leadingExpVector() );
	    }
	    // H = H.monic();
	}

}

