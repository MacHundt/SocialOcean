package scripts;

import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class JRubyTest {
	
	private ScriptingContainer ruby;

	public static void main(String[] args) {
		new JRubyTest().run();

	}
	
	public void run() {
        ruby = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
            // Assign the Java objects that you want to share
        ruby.put("main", this);
            // Execute a script (can be of any length, and taken from a file)
//        Object result = ruby.runScriptlet("main.hello_world");
        Object result = ruby.runScriptlet("main.getHelloWorld()");
            // Use the result as if it were a Java object
        System.out.println(result);
    }

    public String getHelloWorld() {
        return "Hello, worlds!";
    }

}
