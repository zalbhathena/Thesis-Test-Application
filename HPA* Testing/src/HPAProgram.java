import java.io.File;

public class HPAProgram {
	static {
		//System.out.println(new File(".").getAbsolutePath());
		System.loadLibrary("hpaprogram"); // hello.dll (Windows) or libhello.so (Unixes)
	}
	//private native void sayHello();
	  
	public static void main(String[] args) {
		MapWindowController map = new MapWindowController();		
	}
}