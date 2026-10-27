package app;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
public final class Main {
    public static void main(String[] args){
        Path directory=Path.of(System.getProperty("workspace.data","data"));
        if(args.length==2&&args[0].equals("--data-dir"))directory=Path.of(args[1]);
        final Path dataDir=directory;
        SwingUtilities.invokeLater(()->{
            try {
                Service service=new Service(dataDir);
                Runtime.getRuntime().addShutdownHook(new Thread(()->{try{service.close();}catch(Exception ignored){}}));
                JFrame frame=new JFrame(Config.NAME+" · Project selection workspace");frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);frame.setContentPane(new WorkspaceUI(service));frame.setSize(1320,850);frame.setMinimumSize(new Dimension(1100,760));frame.setLocationRelativeTo(null);frame.setVisible(true);
            } catch(Exception ex){JOptionPane.showMessageDialog(null,ex.getMessage(),"Startup failed",JOptionPane.ERROR_MESSAGE);}
        });
    }
}
