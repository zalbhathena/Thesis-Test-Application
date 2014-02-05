public class HPAProgram {
/*	static {
		//System.out.println(new File(".").getAbsolutePath());
		System.loadLibrary("libhpaprogram"); // hello.dll (Windows) or libhello.so (Unixes)
	}
	//private native void sayHello();
	*/  
    public native void sayHello();
	public static void main(String[] args) {
		System.loadLibrary("HPAProgram");
		HPAProgram s = new HPAProgram();
		s.sayHello();
		//MapWindowController map = new MapWindowController();		
	}
}