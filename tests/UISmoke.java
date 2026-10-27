package app;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
public final class UISmoke {
    static JFrame frame;static WorkspaceUI ui;static Service service;static Path out;
    static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container)layout((Container)child);}
    static void capture(String name)throws Exception{
        frame.validate();layout(ui);Toolkit.getDefaultToolkit().sync();
        BufferedImage image=new BufferedImage(ui.getWidth(),ui.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();ui.printAll(g);g.dispose();if(out!=null){Files.createDirectories(out);ImageIO.write(image,"png",out.resolve(name+".png").toFile());}
    }
    public static void main(String[] args)throws Exception{
        out=args.length>0?Path.of(args[0]):null;Path tmp=Files.createTempDirectory("ui-smoke-");service=new Service(tmp);
        SwingUtilities.invokeAndWait(()->{try{
            frame=new JFrame();ui=new WorkspaceUI(service);frame.setContentPane(ui);frame.setSize(1320,850);frame.setVisible(true);capture("01-login");
            service.login("teacher","DemoPass123!");ui.render();capture("02-overview");ui.navigate("Projects");capture("03-projects");if(ui.projectTable.getRowCount()!=6)throw new AssertionError("Missing projects");ui.search.setText("finance");if(ui.projectTable.getRowCount()!=1)throw new AssertionError("Search filter");ui.navigate("Students");capture("04-students");ui.navigate("Activity");ui.navigate("Settings");ui.navigate("Overview");ui.toggleTheme();capture("05-light-mode");ui.toggleTheme();service.login("student","DemoPass123!");service.select(2);ui.navigate("My selection");capture("06-student-selection");for(String page:java.util.List.of("Overview","Projects","Activity","Settings"))ui.navigate(page);frame.setSize(1100,760);ui.navigate("Projects");frame.validate();if(ui.projectTable.getWidth()<300)throw new AssertionError("Minimum-size layout");service.logout();ui.loginScreen();frame.dispose();
        }catch(Exception ex){throw new RuntimeException(ex);}});
        service.close();try(var paths=Files.walk(tmp)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}System.out.println("PASS: all pages, both themes, search, role views, minimum window, logout");System.exit(0);
    }
}
