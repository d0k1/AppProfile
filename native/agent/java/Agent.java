import java.util.concurrent.atomic.AtomicLong;

public class Agent {

    private static AtomicLong start = null;
    private static AtomicLong stop = null;

    private static int ready = 0;
    
    private static native void native_entry(int cnum, int mnum);
    public static void agent_entry(int cnum, int mnum)
    {
        if ( ready != 0 ) {
            native_entry(cnum, mnum);
        }
    }

    
    private static native void native_exit(int cnum, int mnum);
    public static void agent_exit(int cnum, int mnum)
    {
        if ( ready != 0 ) {
            native_exit(cnum, mnum);
        }
    }
    
    //private static native void native_newobj(Object o);
    public static void agent_newobj(Object o)
    {
        if ( ready != 0 ) {
            //_newobj(o);
        }
    }
    
    //private static native void native_newarr(Object a);
    public static void agent_newarr(Object a)
    {
        if ( ready != 0 ) {
            //_newarr(a);
        }
    }

    private static native void native_pause();
    public static void agent_pause(){
      native_pause();
    }
    
    private static native void native_resume();
    public static void agent_resume(){
      native_resume();
    }

    private static native void native_reset();
    public static void agent_reset(){
	native_reset();
    }
    
    private static native String native_csv();
    public static String agent_csv(){
      return native_csv();
    }

    public static void setStartMark(long val) {
        start = new AtomicLong(val);
    }

    public static long getStartMark(){
        if(start==null) {
            return 0;
        }

        return start.get();
    }

    public static void setStopMark(long val) {
        stop = new AtomicLong(val);
    }

    public static long getStopMark(){
        if(stop==null) {
            return 0;
        }

        return stop.get();
    }
}
