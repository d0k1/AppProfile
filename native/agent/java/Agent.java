public class Agent {

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
}
