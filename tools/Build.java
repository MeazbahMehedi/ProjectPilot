import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** Offline build, executable-JAR packaging and test runner. Run: java tools/Build.java [--test|--screenshots] */
class Build {
    static void compile(Path source,Path target,String classpath)throws IOException{
        Files.createDirectories(target);
        var compiler=ToolProvider.getSystemJavaCompiler();
        if(compiler==null)throw new IOException("A full JDK 17+ is required to rebuild. Running the supplied JAR only needs Java 17+.");
        List<String> options=new ArrayList<>(List.of("--release","17","-encoding","UTF-8","-cp",classpath,"-d",target.toString()));
        try(var files=Files.walk(source)){files.filter(p->p.toString().endsWith(".java")).sorted().forEach(p->options.add(p.toString()));}
        if(compiler.run(null,null,null,options.toArray(String[]::new))!=0)throw new IOException("Compilation failed.");
    }
    static void jar(Path classes,Path library,Path destination)throws IOException{
        Files.createDirectories(destination.getParent());Manifest mf=new Manifest();mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS,"app.Main");Set<String> names=new HashSet<>();
        try(JarOutputStream out=new JarOutputStream(Files.newOutputStream(destination),mf)){
            names.add("META-INF/MANIFEST.MF");
            try(var paths=Files.walk(classes)){for(Path file:paths.filter(Files::isRegularFile).sorted().toList()){String name=classes.relativize(file).toString().replace(File.separatorChar,'/');names.add(name);out.putNextEntry(new JarEntry(name));Files.copy(file,out);out.closeEntry();}}
            try(JarFile dependency=new JarFile(library.toFile())){var entries=dependency.entries();while(entries.hasMoreElements()){var e=entries.nextElement();String n=e.getName();if(e.isDirectory()||n.equals("META-INF/MANIFEST.MF")||n.endsWith("module-info.class")||n.endsWith(".SF")||n.endsWith(".RSA")||!names.add(n))continue;out.putNextEntry(new JarEntry(n));try(InputStream in=dependency.getInputStream(e)){in.transferTo(out);}out.closeEntry();}}
        }
    }
    public static void main(String[] args)throws Exception{
        Properties config=new Properties();try(Reader r=Files.newBufferedReader(Path.of("branding.properties"))){config.load(r);}
        Path library=Path.of("lib/gson-2.11.0.jar"),classes=Path.of("build/classes"),tests=Path.of("build/test-classes");
        if(Files.exists(classes))try(var paths=Files.walk(classes)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}
        compile(Path.of("src"),classes,library.toString());Files.copy(Path.of("resources/seed.json"),classes.resolve("seed.json"),StandardCopyOption.REPLACE_EXISTING);
        Path destination=Path.of("dist",config.getProperty("artifact")+".jar");jar(classes,library,destination);System.out.println("Built "+destination);
        if(args.length>0){
            String cp=classes+File.pathSeparator+library+File.pathSeparator+tests;compile(Path.of("tests"),tests,cp);
            List<String> command=new ArrayList<>(List.of(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-cp",cp,args[0].equals("--screenshots")?"app.UISmoke":"app.ServiceTest"));
            if(args[0].equals("--screenshots"))command.add("docs/screenshots");int code=new ProcessBuilder(command).inheritIO().start().waitFor();if(code!=0)System.exit(code);
        }
    }
}
