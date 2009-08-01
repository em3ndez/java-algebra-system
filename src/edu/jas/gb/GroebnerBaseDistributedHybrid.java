/*
 * $Id$
 */

package edu.jas.gb;


import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
//import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import edu.jas.kern.ComputerThreads;
import edu.jas.kern.PreemptingException;
import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.structure.RingElem;
import edu.jas.util.ChannelFactory;
import edu.jas.util.DistHashTable;
import edu.jas.util.DistHashTableServer;
import edu.jas.util.SocketChannel;
import edu.jas.util.TaggedSocketChannel;
import edu.jas.util.Terminator;
import edu.jas.util.ThreadPool;


/**
 * Groebner Base distributed hybrid algorithm. Implements a distributed memory
 * with multi-core CPUs parallel version of Groebner bases. Using pairlist
 * class, slaves maintain pairlist, distributed slaves do reduction.
 * @param <C> coefficient type
 * @author Heinz Kredel
 */

public class GroebnerBaseDistributedHybrid<C extends RingElem<C>> extends GroebnerBaseAbstract<C> {

    public static final Logger logger = Logger.getLogger(GroebnerBaseDistributedHybrid.class);

    public static final boolean debug = true || logger.isDebugEnabled();


    /**
     * Number of threads to use.
     */
    protected final int threads;


    /**
     * Default number of threads.
     */
    protected static final int DEFAULT_THREADS = 2;


    /**
     * Number of threads per node to use.
     */
    protected final int threadsPerNode;


    /**
     * Default number of threads per compute node.
     */
    protected static final int DEFAULT_THREADS_PER_NODE = 1;


    /**
     * Pool of threads to use.
     */
    //protected final ExecutorService pool; // not for single node tests
    protected final ThreadPool pool;


    /**
     * Default server port.
     */
    protected static final int DEFAULT_PORT = 4711;


    /**
     * Server port to use.
     */
    protected final int port;


    /**
     * Message tag for pairs.
     */
    public static final Integer pairTag = new Integer(1);


    /**
     * Message tag for results.
     */
    public static final Integer resultTag = new Integer(2);



    /**
     * Constructor.
     */
    public GroebnerBaseDistributedHybrid() {
        this(DEFAULT_THREADS, DEFAULT_PORT);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     */
    public GroebnerBaseDistributedHybrid(int threads) {
        this(threads, new ThreadPool(threads), DEFAULT_PORT);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param port server port to use.
     */
    public GroebnerBaseDistributedHybrid(int threads, int port) {
        this(threads, new ThreadPool(threads), port);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param threadsPerNode threads per node to use.
     * @param port server port to use.
     */
    public GroebnerBaseDistributedHybrid(int threads, int threadsPerNode, int port) {
        this(threads, threadsPerNode, new ThreadPool(threads), port);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param pool ThreadPool to use.
     * @param port server port to use.
     */
    public GroebnerBaseDistributedHybrid(int threads, ThreadPool pool, int port) {
        this(threads,DEFAULT_THREADS_PER_NODE,pool,port);
    }


    /**
     * Constructor.
     * @param threads number of threads to use.
     * @param threadsPerNode threads per node to use.
     * @param pool ThreadPool to use.
     * @param port server port to use.
     */
    public GroebnerBaseDistributedHybrid(int threads, int threadsPerNode, ThreadPool pool, int port) {
        if (threads < 1) {
            threads = 1;
        }
        this.threads = threads;
        this.threadsPerNode = threadsPerNode;
        this.pool = pool;
        this.port = port;
        red = new ReductionPar<C>();
        //logger.info("generated pool: " + pool);
    }


    /**
     * Cleanup and terminate.
     */
    public void terminate() {
        if (pool == null) {
            return;
        }
        pool.terminate(); 
    }


    /**
     * Distributed Groebner base. Slaves maintain pairlist.
     * @param modv number of module variables.
     * @param F polynomial list.
     * @return GB(F) a Groebner base of F or null, if a IOException occurs.
     */
    public List<GenPolynomial<C>> GB(int modv, List<GenPolynomial<C>> F) {
        final int DL_PORT = port + 100;
        ChannelFactory cf = new ChannelFactory(port);
        DistHashTableServer<Integer> dls = new DistHashTableServer<Integer>(DL_PORT);
        dls.init();
        logger.debug("dist-list server running");

        GenPolynomial<C> p;
        List<GenPolynomial<C>> G = new ArrayList<GenPolynomial<C>>();
        OrderedPairlist<C> pairlist = null;
        boolean oneInGB = false;
        int l = F.size();
        int unused;
        ListIterator<GenPolynomial<C>> it = F.listIterator();
        while (it.hasNext()) {
            p = it.next();
            if (p.length() > 0) {
                p = p.monic();
                if (p.isONE()) {
                    oneInGB = true;
                    G.clear();
                    G.add(p);
                    //return G; must signal termination to others
                }
                if (!oneInGB) {
                    G.add(p);
                }
                if (pairlist == null) {
                    pairlist = new OrderedPairlist<C>(modv, p.ring);
                }
                // theList not updated here
                if (p.isONE()) {
                    unused = pairlist.putOne(p);
                } else {
                    unused = pairlist.put(p);
                }
            } else {
                l--;
            }
        }
        if (l <= 1) {
            //return G; must signal termination to others
        }
        logger.info("pairlist " + pairlist);

        logger.debug("looking for clients");
        //long t = System.currentTimeMillis();
        // now in DL, uses resend for late clients
        //while ( dls.size() < threads ) { sleep(); }

        DistHashTable<Integer, GenPolynomial<C>> theList = new DistHashTable<Integer, GenPolynomial<C>>(
                "localhost", DL_PORT);
        ArrayList<GenPolynomial<C>> al = pairlist.getList();
        for (int i = 0; i < al.size(); i++) {
            // no wait required
            GenPolynomial<C> nn = theList.put(new Integer(i), al.get(i));
            if (nn != null) {
                logger.info("double polynomials " + i + ", nn = " + nn + ", al(i) = " + al.get(i));
            }
        }

        Terminator fin = new Terminator(threads);
        HybridReducerServer<C> R;
        logger.info("using pool[" + threads + "] = " + pool);
        for (int i = 0; i < threads; i++) {
            R = new HybridReducerServer<C>(fin, cf, theList, G, pairlist);
            pool.addJob(R);
            //logger.info("server submitted " + R);
        }
        logger.debug("main loop waiting");
        fin.waitDone();
        int ps = theList.size();
        logger.debug("#distributed list = " + ps);
        // make sure all polynomials arrived
        // G = (ArrayList)theList.values();
        G = pairlist.getList();
        if (ps != G.size()) {
            logger.error("#distributed list = " + theList.size() + " #pairlist list = " + G.size());
        }
        long time = System.currentTimeMillis();
        List<GenPolynomial<C>> Gp;
        Gp = minimalGB(G); // not jet distributed but threaded
        time = System.currentTimeMillis() - time;
        logger.info("parallel gbmi = " + time);
        /*
        time = System.currentTimeMillis();
        G = GroebnerBase.<C>GBmi(G); // sequential
        time = System.currentTimeMillis() - time;
        logger.info("sequential gbmi = " + time);
        */
        G = Gp;
        logger.debug("cf.terminate()");
        cf.terminate();
        // no more required // pool.terminate();
        logger.info("theList.terminate()");
        theList.terminate();
        logger.info("dls.terminate()");
        dls.terminate();
        logger.info("pairlist #put = " + pairlist.putCount() + " #rem = " + pairlist.remCount()
        //+ " #total = " + pairlist.pairCount()
                );
        return G;
    }


    /**
     * GB distributed client.
     * @param host the server runs on.
     * @throws IOException
     */
    public void clientPart(String host) throws IOException {

        ChannelFactory cf = new ChannelFactory(port + 10); // != port for localhost
        SocketChannel channel = cf.getChannel(host, port);
        TaggedSocketChannel pairChannel = new TaggedSocketChannel(channel);

        if (debug) {
            logger.info("clientPart pairChannel   = " + pairChannel);
        }

        final int DL_PORT = port + 100;
        DistHashTable<Integer, GenPolynomial<C>> theList = new DistHashTable<Integer, GenPolynomial<C>>(host, DL_PORT);

        //HybridReducerClient<C> R = new HybridReducerClient<C>(threadsPerNode, pairChannel, theList);
        //R.run();
        //if ( false ) {

        ThreadPool pool = new ThreadPool(threadsPerNode);
        //Future[] fu = new Future[ threadsPerNode ]; 
        for ( int i = 0; i < threadsPerNode; i++ ) {
            HybridReducerClient<C> Rr = new HybridReducerClient<C>(threadsPerNode, pairChannel, theList);
            pool.addJob(Rr);
        }
        if (debug) {
            logger.info("clients submitted");
        }
        pool.terminate();

        pairChannel.close();
        channel.close();

        theList.terminate();
        cf.terminate();
        return;
    }


    /**
     * Minimal ordered groebner basis.
     * @param Fp a Groebner base.
     * @return a reduced Groebner base of Fp.
     */
    @Override
    public List<GenPolynomial<C>> minimalGB(List<GenPolynomial<C>> Fp) {
        GenPolynomial<C> a;
        ArrayList<GenPolynomial<C>> G;
        G = new ArrayList<GenPolynomial<C>>(Fp.size());
        ListIterator<GenPolynomial<C>> it = Fp.listIterator();
        while (it.hasNext()) {
            a = it.next();
            if (a.length() != 0) { // always true
                // already monic  a = a.monic();
                G.add(a);
            }
        }
        if (G.size() <= 1) {
            return G;
        }

        ExpVector e;
        ExpVector f;
        GenPolynomial<C> p;
        ArrayList<GenPolynomial<C>> F;
        F = new ArrayList<GenPolynomial<C>>(G.size());
        boolean mt;

        while (G.size() > 0) {
            a = G.remove(0);
            e = a.leadingExpVector();

            it = G.listIterator();
            mt = false;
            while (it.hasNext() && !mt) {
                p = it.next();
                f = p.leadingExpVector();
                mt = e.multipleOf(f);
            }
            it = F.listIterator();
            while (it.hasNext() && !mt) {
                p = it.next();
                f = p.leadingExpVector();
                mt = e.multipleOf(f);
            }
            if (!mt) {
                F.add(a);
            } else {
                // System.out.println("dropped " + a.length());
            }
        }
        G = F;
        if (G.size() <= 1) {
            return G;
        }

        MiReducerServer<C>[] mirs = (MiReducerServer<C>[]) new MiReducerServer[G.size()];
        int i = 0;
        F = new ArrayList<GenPolynomial<C>>(G.size());
        while (G.size() > 0) {
            a = G.remove(0);
            // System.out.println("doing " + a.length());
            mirs[i] = new MiReducerServer<C>((List<GenPolynomial<C>>) G.clone(), (List<GenPolynomial<C>>) F
                    .clone(), a);
            pool.addJob(mirs[i]);
            i++;
            F.add(a);
        }
        G = F;
        F = new ArrayList<GenPolynomial<C>>(G.size());
        for (i = 0; i < mirs.length; i++) {
            a = mirs[i].getNF();
            F.add(a);
        }
        return F;
    }

}


/**
 * Distributed server reducing worker threads.
 * @param <C> coefficient type
 */

class HybridReducerServer<C extends RingElem<C>> implements Runnable {

    public static final Logger logger = Logger.getLogger(HybridReducerServer.class);

    public static final boolean debug = true || logger.isDebugEnabled();


    private final Terminator finner;


    private final ChannelFactory cf;


    //private SocketChannel pairChannel;


    private TaggedSocketChannel pairChannel;


    private final DistHashTable<Integer, GenPolynomial<C>> theList;


    //private List<GenPolynomial<C>> G;
    private final OrderedPairlist<C> pairlist;


    /**
     * Message tag for pairs.
     */
    public final Integer pairTag = GroebnerBaseDistributedHybrid.pairTag;


    /**
     * Message tag for results.
     */
    public final Integer resultTag = GroebnerBaseDistributedHybrid.resultTag;



    HybridReducerServer(Terminator fin, ChannelFactory cf, DistHashTable<Integer, GenPolynomial<C>> dl,
            List<GenPolynomial<C>> G, OrderedPairlist<C> L) {
        finner = fin;
        this.cf = cf;
        theList = dl;
        //this.G = G;
        pairlist = L;
        //logger.info("reducer server created " + this);
    }


    public void run() {
        logger.info("reducer server running " + this);
        try {
            SocketChannel channel = cf.getChannel();
            pairChannel = new TaggedSocketChannel(channel);
        } catch (InterruptedException e) {
            logger.debug("get pair channel interrupted");
            e.printStackTrace();
            return;
        }
        if (debug) {
            logger.info("pairChannel   = " + pairChannel);
            //logger.info("taggedChannel = " + taggedChannel);
        }
        Pair<C> pair;
        //GenPolynomial<C> pi;
        //GenPolynomial<C> pj;
        //GenPolynomial<C> S;
        GenPolynomial<C> H = null;
        boolean set = false;
        boolean goon = true;
        int polIndex = -1;
        int red = 0;
        int sleeps = 0;

        // while more requests
        while (goon) {
            // receive request
            logger.debug("receive request");
            Object req = null;
            try {
                req = pairChannel.receive(pairTag);
            } catch (InterruptedException e) {
                goon = false;
                e.printStackTrace();
            } catch (IOException e) {
                goon = false;
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                goon = false;
                e.printStackTrace();
            } 
            logger.info("received request, req = " + req);
            if (req == null) {
                goon = false;
                break;
            }
            if (!(req instanceof GBTransportMessReq)) {
                goon = false;
                break;
            }

            // find pair
            logger.info("find pair");
            while (!pairlist.hasNext()) { // wait
                if (!set) {
                    finner.beIdle();
                    set = true;
                }
                if (!finner.hasJobs() && !pairlist.hasNext()) {
                    goon = false;
                    break;
                }
                try {
                    sleeps++;
                    if (sleeps % 10 == 0) {
                        logger.info(" reducer is sleeping");
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    goon = false;
                    break;
                }
            }
            if (!pairlist.hasNext() && !finner.hasJobs()) {
                goon = false;
                break; //continue; //break?
            }
            if (set) {
                set = false;
                finner.notIdle();
            }

            pair = pairlist.removeNext();
            /*
             * send pair to client, receive H
             */
            logger.info("send pair = " + pair);
            GBTransportMess msg = null;
            if (pair != null) {
                msg = new GBTransportMessPairIndex(pair);
            } else {
                msg = new GBTransportMess(); //End();
                // goon ?= false;
            }
            try {
                pairChannel.send(pairTag,msg);
            } catch (IOException e) {
                e.printStackTrace();
                goon = false;
                break;
            }
            logger.debug("#distributed list = " + theList.size());
            Object rh = null;
            try {
                rh = pairChannel.receive(resultTag);
            } catch (InterruptedException e) {
                goon = false;
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                goon = false;
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                goon = false;
                break;
            }
            logger.info("received H polynomial " + rh);
            if (rh == null) {
                if (pair != null) {
                    pair.setZero();
                }
            } else if (rh instanceof GBTransportMessPoly) {
                // update pair list
                red++;
                H = ((GBTransportMessPoly<C>) rh).pol;
                if (logger.isDebugEnabled()) {
                    logger.debug("H = " + H);
                }
                if (H == null) {
                    if (pair != null) {
                        pair.setZero();
                    }
                } else {
                    if (H.isZERO()) {
                        pair.setZero();
                    } else {
                        if (H.isONE()) {
                            // finner.allIdle();
                            polIndex = pairlist.putOne(H);
                            GenPolynomial<C> nn = theList.put(new Integer(polIndex), H);
                            if (nn != null) {
                                logger.info("double polynomials nn = " + nn + ", H = " + H);
                            }
                            goon = false;
                            break;
                        } else {
                            polIndex = pairlist.put(H);
                            // use putWait ? but still not all distributed
                            GenPolynomial<C> nn = theList.put(new Integer(polIndex), H);
                            if (nn != null) {
                                logger.info("double polynomials nn = " + nn + ", H = " + H);
                            }
                        }
                    }
                }
            }
        }
        logger.info("terminated, done " + red + " reductions");

        /*
         * send end mark to client
         */
        logger.debug("send end");
        try {
            pairChannel.send(pairTag,new GBTransportMessEnd());
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        finner.beIdle();
        pairChannel.close();
    }
}


/**
 * Distributed clients reducing worker threads.
 */

class HybridReducerClient<C extends RingElem<C>> implements Runnable {

    private static final Logger logger = Logger.getLogger(HybridReducerClient.class);

    public static final boolean debug = true || logger.isDebugEnabled();



    private final TaggedSocketChannel pairChannel;


    private final DistHashTable<Integer, GenPolynomial<C>> theList;


    private final ReductionPar<C> red;


    private final int threadsPerNode;


    /**
     * Message tag for pairs.
     */
    public final Integer pairTag = GroebnerBaseDistributedHybrid.pairTag;


    /**
     * Message tag for results.
     */
    public final Integer resultTag = GroebnerBaseDistributedHybrid.resultTag;



    HybridReducerClient(int tpn, TaggedSocketChannel tc, DistHashTable<Integer, GenPolynomial<C>> dl) {
        this.threadsPerNode = tpn;
        pairChannel = tc;
        theList = dl;
        red = new ReductionPar<C>();
    }


    public void run() {
        if (debug) {
           logger.info("pairChannel   = " + pairChannel + " reducer client running");
     }
        Pair<C> pair = null;
        GenPolynomial<C> pi;
        GenPolynomial<C> pj;
        GenPolynomial<C> S;
        GenPolynomial<C> H = null;
        //boolean set = false;
        boolean goon = true;
        int reduction = 0;
        //int sleeps = 0;
        Integer pix;
        Integer pjx;

        while (goon) {
            /* protocol:
             * request pair, process pair, send result
             */
            // pair = (Pair) pairlist.removeNext();
            Object req = new GBTransportMessReq();
            logger.info("send request = " + req);
            try {
                pairChannel.send(pairTag,req);
            } catch (IOException e) {
                goon = false;
                e.printStackTrace();
                break;
            }
            logger.debug("receive pair, goon = " + goon);
            Object pp = null;
            try {
                pp = pairChannel.receive(pairTag);
            } catch (InterruptedException e) {
                goon = false;
                e.printStackTrace();
            } catch (IOException e) {
                goon = false;
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                break;
            } catch (ClassNotFoundException e) {
                goon = false;
                e.printStackTrace();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("received pair = " + pp);
            }
            H = null;
            if (pp == null) { // should not happen
                continue;
            }
            if (pp instanceof GBTransportMessEnd) {
                goon = false;
                continue;
            }
            if (pp instanceof GBTransportMessPair || pp instanceof GBTransportMessPairIndex) {
                pi = pj = null;
                if (pp instanceof GBTransportMessPair) {
                    pair = ((GBTransportMessPair<C>) pp).pair;
                    if (pair != null) {
                        pi = pair.pi;
                        pj = pair.pj;
                        //logger.debug("pair: pix = " + pair.i 
                        //               + ", pjx = " + pair.j);
                    }
                }
                if (pp instanceof GBTransportMessPairIndex) {
                    pix = ((GBTransportMessPairIndex) pp).i;
                    pjx = ((GBTransportMessPairIndex) pp).j;
                    pi = (GenPolynomial<C>) theList.getWait(pix);
                    pj = (GenPolynomial<C>) theList.getWait(pjx);
                    //logger.info("pix = " + pix + ", pjx = " +pjx);
                }

                if (pi != null && pj != null) {
                    S = red.SPolynomial(pi, pj);
                    //System.out.println("S   = " + S);
                    if (S.isZERO()) {
                        // pair.setZero(); does not work in dist
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ht(S) = " + S.leadingExpVector());
                        }
                        H = red.normalform(theList, S);
                        reduction++;
                        if (H.isZERO()) {
                            // pair.setZero(); does not work in dist
                        } else {
                            H = H.monic();
                            if (logger.isInfoEnabled()) {
                                logger.info("ht(H) = " + H.leadingExpVector());
                            }
                        }
                    }
                }
            }

            // send H or must send null
            if (logger.isDebugEnabled()) {
                logger.debug("#distributed list = " + theList.size());
                logger.debug("send H polynomial = " + H);
            }
            try {
                pairChannel.send(resultTag,new GBTransportMessPoly<C>(H));
            } catch (IOException e) {
                goon = false;
                e.printStackTrace();
            }
        }
        logger.info("terminated, done " + reduction + " reductions");
    }
}
