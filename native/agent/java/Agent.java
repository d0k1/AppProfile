
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

    public static Unsafe getUnsafe() {
      try {
	  Field f = Unsafe.class.getDeclaredField("theUnsafe");
	  f.setAccessible(true);
	  return (Unsafe)f.get(null);
      } catch (Exception e) { /* ... */ }
    }

    
    public static void pause(){
    }
    
    public static void resume(){
    }
}
